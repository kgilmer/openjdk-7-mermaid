/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

#import <stdlib.h>
#import <string.h>
#import <ApplicationServices/ApplicationServices.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "sun_java2d_opengl_CGLGraphicsConfig.h"

#import "jni.h"
#import "jni_util.h"
#import "CGLGraphicsConfig.h"
#import "CGLSurfaceData.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"

#pragma mark -
#pragma mark "--- Mac OS X specific methods for GL pipeline ---"

/**
 * Disposes all memory and resources associated with the given
 * CGLGraphicsConfigInfo (including its native OGLContext data).
 */
void
OGLGC_DestroyOGLGraphicsConfig(jlong pConfigInfo)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLGC_DestroyOGLGraphicsConfig");
    
    CGLGraphicsConfigInfo *cglinfo =
        (CGLGraphicsConfigInfo *)jlong_to_ptr(pConfigInfo);
    if (cglinfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "OGLGC_DestroyOGLGraphicsConfig: info is null");
        return;
    }
    
    OGLContext *oglc = (OGLContext*)cglinfo->context;
    if (oglc != NULL) {
        OGLContext_DestroyContextResources(oglc);
        
        CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;
        if (ctxinfo != NULL) {
            [NSOpenGLContext clearCurrentContext];
            [ctxinfo->context clearDrawable];
            [ctxinfo->context release];
            if (ctxinfo->scratchSurface != 0) {
                [ctxinfo->scratchSurface release];
            }
            free(ctxinfo);
        }
    }
    
    free(cglinfo);
}

#pragma mark -
#pragma mark "--- CGLGraphicsConfig methods ---"

#ifdef REMOTELAYER
mach_port_t JRSRemotePort;
int remoteSocketFD = -1;

static void *JRSRemoteThreadFn(void *data) {
    NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];

    // Negotiate a unix domain socket to communicate the
    // out of band data: to read the mach port server name, and
    // subsequently write out the layer ID.
    static char* sock_path = "/tmp/JRSRemoteDemoSocket";
    struct sockaddr_un address;
    int  socket_fd, nbytes;
    int BUFLEN = 256;
    char buffer[BUFLEN];
    
    remoteSocketFD = socket(PF_LOCAL, SOCK_STREAM, 0);
    if (remoteSocketFD < 0) {
        NSLog(@"socket() failed");
        return NULL;
    }
    memset(&address, 0, sizeof(struct sockaddr_un));
    address.sun_family = AF_UNIX;
    memcpy(address.sun_path, sock_path, strlen(sock_path)+1);
    int tries=0, status=-1;
    while (status !=0 && tries<600) {
        status = connect(remoteSocketFD, (struct sockaddr *) &address,
                         sizeof(struct sockaddr_un));
        if (status != 0) {
            tries++;
            NSLog(@"connection attempt %d failed.", tries);
            usleep(5000000);
        }
    }
    if (status != 0) {
        NSLog(@"failed to connect");
        return NULL;
    }
    nbytes = read(remoteSocketFD, buffer, BUFLEN);
    NSString* serverString = [[NSString alloc] initWithUTF8String:buffer];
    CFRetain(serverString);
    NSLog(@"Read server name %@", serverString);
    JRSRemotePort = [JRSRenderServer recieveRenderServer:serverString];
    NSLog(@"Read server port %d", JRSRemotePort);

    [pool drain];
    return NULL;
}

void sendLayerID(int layerID) {
    if (JRSRemotePort == 0 || remoteSocketFD < 0) {
        NSLog(@"No connection to send ID");
        return;
    }
    int BUFLEN = 256;
    char buffer[BUFLEN];
    snprintf(buffer, BUFLEN, "%d", layerID);
    write(remoteSocketFD, buffer, BUFLEN);
}
#endif  /* REMOTELAYER */

/**
 * This is a globally shared context used when creating textures.  When any
 * new contexts are created, they specify this context as the "share list"
 * context, which means any texture objects created when this shared context
 * is current will be available to any other context in any other thread.
 */
NSOpenGLContext *sharedContext = NULL;
NSOpenGLPixelFormat *sharedPixelFormat = NULL;

/**
 * Attempts to initialize CGL and the core OpenGL library.
 */
JNIEXPORT jboolean JNICALL
Java_sun_java2d_opengl_CGLGraphicsConfig_initCGL
    (JNIEnv *env, jclass cglgc)
{
    J2dRlsTraceLn(J2D_TRACE_INFO, "CGLGraphicsConfig_initCGL");
    
    if (!OGLFuncs_OpenLibrary()) {
        return JNI_FALSE;
    }
    
    if (!OGLFuncs_InitPlatformFuncs() ||
        !OGLFuncs_InitBaseFuncs() ||
        !OGLFuncs_InitExtFuncs())
    {
        OGLFuncs_CloseLibrary();
        return JNI_FALSE;
    }
#ifdef REMOTELAYER
    pthread_t jrsRemoteThread;
    pthread_create(&jrsRemoteThread, NULL, JRSRemoteThreadFn, NULL);
#endif
    return JNI_TRUE;
}


/**
 * Determines whether the CGL pipeline can be used for a given GraphicsConfig
 * provided its screen number and visual ID.  If the minimum requirements are
 * met, the native CGLGraphicsConfigInfo structure is initialized for this
 * GraphicsConfig with the necessary information (pixel format, etc.)
 * and a pointer to this structure is returned as a jlong.  If
 * initialization fails at any point, zero is returned, indicating that CGL
 * cannot be used for this GraphicsConfig (we should fallback on an existing
 * 2D pipeline).
 */
JNIEXPORT jlong JNICALL
Java_sun_java2d_opengl_CGLGraphicsConfig_getCGLConfigInfo
    (JNIEnv *env, jclass cglgc,
     jint screennum, jint pixfmt, jint swapInterval)
{
  jlong ret = 0L;
  JNF_COCOA_ENTER(env);    
  NSMutableArray * retArray = [NSMutableArray arrayWithCapacity:3];
  [retArray addObject: [NSNumber numberWithInt: (int)screennum]];
  [retArray addObject: [NSNumber numberWithInt: (int)pixfmt]];
  [retArray addObject: [NSNumber numberWithInt: (int)swapInterval]];
  [GraphicsConfigUtil performSelectorOnMainThread: @selector(_getCGLConfigInfo:) withObject: retArray waitUntilDone: YES];
  NSNumber * num = (NSNumber *)[retArray objectAtIndex: 0];
  ret = (jlong)[num longValue];
  JNF_COCOA_EXIT(env);
  return ret;
}



@implementation GraphicsConfigUtil
+ (void) _getCGLConfigInfo: (NSMutableArray *)argValue {
    AWT_ASSERT_APPKIT_THREAD;

    jint screennum = (jint)[(NSNumber *)[argValue objectAtIndex: 0] intValue];
    jint pixfmt = (jint)[(NSNumber *)[argValue objectAtIndex: 1] intValue];
    jint swapInterval = (jint)[(NSNumber *)[argValue objectAtIndex: 2] intValue];
    JNIEnv *env = [ThreadUtilities getJNIEnvUncached];
    [argValue removeAllObjects];    

    J2dRlsTraceLn(J2D_TRACE_INFO, "CGLGraphicsConfig_getCGLConfigInfo");

    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    
    CGOpenGLDisplayMask glMask = (CGOpenGLDisplayMask)pixfmt;
    if (sharedContext == NULL) {
        if (glMask == 0) {
            CGDirectDisplayID id =
                FindCGDirectDisplayIDForScreenIndex(screennum);
            glMask = CGDisplayIDToOpenGLDisplayMask(id);
        }

        NSOpenGLPixelFormatAttribute attrs[] = {
            NSOpenGLPFAClosestPolicy,
            NSOpenGLPFANoRecovery,
            NSOpenGLPFAAccelerated,
            NSOpenGLPFAFullScreen,
            NSOpenGLPFAWindow,
            NSOpenGLPFAPixelBuffer,
            NSOpenGLPFADoubleBuffer,
            NSOpenGLPFAColorSize, 32,
            NSOpenGLPFAAlphaSize, 8,
            NSOpenGLPFADepthSize, 16,
            NSOpenGLPFAScreenMask, glMask,
            0
        };

        sharedPixelFormat =
            [[NSOpenGLPixelFormat alloc] initWithAttributes:attrs];
        if (sharedPixelFormat == nil) {
            J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: shared NSOpenGLPixelFormat is NULL");
            [argValue addObject: [NSNumber numberWithLong: 0L]];
            return;
        }
        
        sharedContext = 
            [[NSOpenGLContext alloc]
                initWithFormat:sharedPixelFormat
                shareContext: NULL];
        if (sharedContext == nil) {
            J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: shared NSOpenGLContext is NULL");
            [argValue addObject: [NSNumber numberWithLong: 0L]];
            return;
        }
    }

#if USE_NSVIEW_FOR_SCRATCH
    NSRect contentRect = NSMakeRect(0, 0, 64, 64);
    NSWindow *window =
        [[NSWindow alloc]
            initWithContentRect: contentRect
            styleMask: NSBorderlessWindowMask
            backing: NSBackingStoreBuffered
            defer: false];
    if (window == nil) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: NSWindow is NULL");
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }

    NSView *scratchSurface =
        [[NSView alloc]
            initWithFrame: contentRect];
    if (scratchSurface == nil) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: NSView is NULL");
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }
    [window setContentView: scratchSurface];
#else
    NSOpenGLPixelBuffer *scratchSurface =
        [[NSOpenGLPixelBuffer alloc]
            initWithTextureTarget:GL_TEXTURE_2D
            textureInternalFormat:GL_RGB
            textureMaxMipMapLevel:0
            pixelsWide:64
            pixelsHigh:64];
#endif
    
    NSOpenGLContext *context =
        [[NSOpenGLContext alloc]
            initWithFormat: sharedPixelFormat
            shareContext: sharedContext];
    if (context == nil) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: NSOpenGLContext is NULL");
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }

    GLint contextVirtualScreen = [context currentVirtualScreen];
#if USE_NSVIEW_FOR_SCRATCH
    [context setView: scratchSurface];
#else
    [context
        setPixelBuffer: scratchSurface
        cubeMapFace:0
        mipMapLevel:0
        currentVirtualScreen: contextVirtualScreen];
#endif
    [context makeCurrentContext];

    // get version and extension strings
    const unsigned char *versionstr = j2d_glGetString(GL_VERSION);
    if (!OGLContext_IsVersionSupported(versionstr)) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: OpenGL 1.2 is required");
        [NSOpenGLContext clearCurrentContext];
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }
    J2dRlsTraceLn1(J2D_TRACE_INFO, "CGLGraphicsConfig_getCGLConfigInfo: OpenGL version=%s", versionstr);

    jint caps = CAPS_EMPTY;
    OGLContext_GetExtensionInfo(env, &caps);

    GLint value = 0;
    [sharedPixelFormat
        getValues: &value
        forAttribute: NSOpenGLPFADoubleBuffer
        forVirtualScreen: contextVirtualScreen];
    if (value != 0) {
        caps |= CAPS_DOUBLEBUFFERED;
    }
    [sharedPixelFormat
        getValues: &value
        forAttribute: NSOpenGLPFAAlphaSize
        forVirtualScreen: contextVirtualScreen];
    if (value != 0) {
        caps |= CAPS_STORED_ALPHA;
    }

    J2dRlsTraceLn2(J2D_TRACE_INFO,
                   "CGLGraphicsConfig_getCGLConfigInfo: db=%d alpha=%d",
                   (caps & CAPS_DOUBLEBUFFERED) != 0,
                   (caps & CAPS_STORED_ALPHA) != 0);

    // remove before shipping (?)
#if 1
    [sharedPixelFormat
        getValues: &value
        forAttribute: NSOpenGLPFAAccelerated
        forVirtualScreen: contextVirtualScreen];
    if (value == 0) {
        [sharedPixelFormat
            getValues: &value
            forAttribute: NSOpenGLPFARendererID
            forVirtualScreen: contextVirtualScreen];
        fprintf(stderr, "WARNING: GL pipe is running in software mode (Renderer ID=0x%x)\n", (int)value);
    }
#endif

    // 0: the buffers are swapped with no regard to the vertical refresh rate
    // 1: the buffers are swapped only during the vertical retrace
    GLint params = swapInterval;
    [context setValues: &params forParameter: NSOpenGLCPSwapInterval];
    
    CGLCtxInfo *ctxinfo = (CGLCtxInfo *)malloc(sizeof(CGLCtxInfo));
    if (ctxinfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGC_InitOGLContext: could not allocate memory for ctxinfo");
        [NSOpenGLContext clearCurrentContext];
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }
    memset(ctxinfo, 0, sizeof(CGLCtxInfo));
    ctxinfo->context = context;
    ctxinfo->scratchSurface = scratchSurface;
    
    OGLContext *oglc = (OGLContext *)malloc(sizeof(OGLContext));
    if (oglc == 0L) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGC_InitOGLContext: could not allocate memory for oglc");
        [NSOpenGLContext clearCurrentContext];
        free(ctxinfo);
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }
    memset(oglc, 0, sizeof(OGLContext));
    oglc->ctxInfo = ctxinfo;
    oglc->caps = caps;
    
    // create the CGLGraphicsConfigInfo record for this config
    CGLGraphicsConfigInfo *cglinfo = (CGLGraphicsConfigInfo *)malloc(sizeof(CGLGraphicsConfigInfo));
    if (cglinfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLGraphicsConfig_getCGLConfigInfo: could not allocate memory for cglinfo");
        [NSOpenGLContext clearCurrentContext];
        free(oglc);
        free(ctxinfo);
        [argValue addObject: [NSNumber numberWithLong: 0L]];
        return;
    }
    memset(cglinfo, 0, sizeof(CGLGraphicsConfigInfo));
    cglinfo->screen = screennum;
    cglinfo->pixfmt = sharedPixelFormat;
    cglinfo->context = oglc;

    [NSOpenGLContext clearCurrentContext];
    [argValue addObject: [NSNumber numberWithLong:ptr_to_jlong(cglinfo)]];
    [pool drain];
}
@end //GraphicsConfigUtil


JNIEXPORT jint JNICALL
Java_sun_java2d_opengl_CGLGraphicsConfig_getDefaultPixFmt
    (JNIEnv *env, jclass cglgc, jint screennum)
{
    J2dTraceLn(J2D_TRACE_INFO, "CGLGraphicsConfig_getDefaultPixFmt");

    CGDirectDisplayID id = FindCGDirectDisplayIDForScreenIndex(screennum);
    return (jint)CGDisplayIDToOpenGLDisplayMask(id);
}

JNIEXPORT jint JNICALL
Java_sun_java2d_opengl_CGLGraphicsConfig_getOGLCapabilities
    (JNIEnv *env, jclass cglgc, jlong configInfo)
{
    J2dTraceLn(J2D_TRACE_INFO, "CGLGraphicsConfig_getOGLCapabilities");
    
    CGLGraphicsConfigInfo *cglinfo =
        (CGLGraphicsConfigInfo *)jlong_to_ptr(configInfo);
    if ((cglinfo == NULL) || (cglinfo->context == NULL)) {
        return CAPS_EMPTY;
    } else {
        return cglinfo->context->caps;
    }
}
