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

#import "sun_java2d_opengl_CGLSurfaceData.h"

#import "jni.h"
#import "jni_util.h"
#import "OGLRenderQueue.h"
#import "CGLGraphicsConfig.h"
#import "CGLSurfaceData.h"

// for some reason, the pbuffers fail sometimes when less or equal to 32
// in any of their dimensions
// see <rdar://problem/4566762>
#define GL_MIN_TEXTURE_SIZE 64

/**
 * The methods in this file implement the native windowing system specific
 * layer (CGL) for the OpenGL-based Java 2D pipeline.
 */

#pragma mark -
#pragma mark "--- Mac OS X specific methods for GL pipeline ---"

// TODO: hack that's called from OGLRenderQueue to test out unlockFocus behavior
#if 0
void
OGLSD_UnlockFocus(OGLContext *oglc, OGLSDOps *dstOps)
{
    CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;
    CGLSDOps *cglsdo = (CGLSDOps *)dstOps->privOps;
    fprintf(stderr, "about to unlock focus: %p %p\n",
            cglsdo->peerData, ctxinfo->context);

    NSOpenGLView *nsView = cglsdo->peerData;
    if (nsView != NULL) {
        [nsView unlockFocus];
    }
}
#endif

/**
 * Makes the given context current to its associated "scratch" surface.  If
 * the operation is successful, this method will return JNI_TRUE; otherwise,
 * returns JNI_FALSE.
 */
static jboolean
CGLSD_MakeCurrentToScratch(JNIEnv *env, OGLContext *oglc)
{
    J2dTraceLn(J2D_TRACE_INFO, "CGLSD_MakeCurrentToScratch");
	
    if (oglc == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR,
                      "CGLSD_MakeCurrentToScratch: context is null");
        return JNI_FALSE;
    }
	
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
    CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;

#if USE_NSVIEW_FOR_SCRATCH
    [ctxinfo->context makeCurrentContext];
    [ctxinfo->context setView: ctxinfo->scratchSurface];
#else
    [ctxinfo->context clearDrawable];
    [ctxinfo->context makeCurrentContext];
    [ctxinfo->context setPixelBuffer: ctxinfo->scratchSurface
            cubeMapFace: 0
            mipMapLevel: 0
            currentVirtualScreen: [ctxinfo->context currentVirtualScreen]];
#endif
    [pool drain];
    
    return JNI_TRUE;
}

/**
 * This function disposes of any native windowing system resources associated
 * with this surface.  For instance, if the given OGLSDOps is of type
 * OGLSD_PBUFFER, this method implementation will destroy the actual pbuffer
 * surface.
 */
void
OGLSD_DestroyOGLSurface(JNIEnv *env, OGLSDOps *oglsdo)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_DestroyOGLSurface");
	
    CGLSDOps *cglsdo = (CGLSDOps *)oglsdo->privOps;
    if (oglsdo->drawableType == OGLSD_PBUFFER) {
        if (oglsdo->textureID != 0) {
            j2d_glDeleteTextures(1, &oglsdo->textureID);
            oglsdo->textureID = 0;
        }
        if (cglsdo->pbuffer != NULL) {
            [cglsdo->pbuffer release];
            cglsdo->pbuffer = NULL;
        }
    } else if (oglsdo->drawableType == OGLSD_WINDOW) {
        // detach the NSView from the NSOpenGLContext
        CGLGraphicsConfigInfo *cglInfo = cglsdo->configInfo;
        OGLContext *oglc = cglInfo->context;
        CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;
        [ctxinfo->context clearDrawable];
    }
	
    oglsdo->drawableType = OGLSD_UNDEFINED;
}

/**
 * Returns a pointer (as a jlong) to the native CGLGraphicsConfigInfo
 * associated with the given OGLSDOps.  This method can be called from
 * shared code to retrieve the native GraphicsConfig data in a platform-
 * independent manner.
 */
jlong
OGLSD_GetNativeConfigInfo(OGLSDOps *oglsdo)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_GetNativeConfigInfo");
	
    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_GetNativeConfigInfo: ops are null");
        return 0L;
    }
	
    CGLSDOps *cglsdo = (CGLSDOps *)oglsdo->privOps;
    if (cglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_GetNativeConfigInfo: cgl ops are null");
        return 0L;
    }
	
    return ptr_to_jlong(cglsdo->configInfo);
}

/**
 * Makes the given GraphicsConfig's context current to its associated
 * "scratch" surface.  If there is a problem making the context current,
 * this method will return NULL; otherwise, returns a pointer to the
 * OGLContext that is associated with the given GraphicsConfig.
 */
OGLContext *
OGLSD_SetScratchSurface(JNIEnv *env, jlong pConfigInfo)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_SetScratchContext");
	
    CGLGraphicsConfigInfo *cglInfo = (CGLGraphicsConfigInfo *)jlong_to_ptr(pConfigInfo);
    if (cglInfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_SetScratchContext: cgl config info is null");
        return NULL;
    }

    OGLContext *oglc = cglInfo->context;
    CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;

    // avoid changing the context's target view whenever possible, since
    // calling setView causes flickering; as long as our context is current
    // to some view, it's not necessary to switch to the scratch surface
    if ([ctxinfo->context view] == nil) {
        // it seems to be necessary to explicitly flush between context changes
        OGLContext *currentContext = OGLRenderQueue_GetCurrentContext();
        if (currentContext != NULL) {
            j2d_glFlush();
        }

        if (!CGLSD_MakeCurrentToScratch(env, oglc)) {
            return NULL;
        }
    } else if ([NSOpenGLContext currentContext] == nil) {
        [ctxinfo->context makeCurrentContext];
    }
	
    if (OGLC_IS_CAP_PRESENT(oglc, CAPS_EXT_FBOBJECT)) {
        // the GL_EXT_framebuffer_object extension is present, so this call
        // will ensure that we are bound to the scratch surface (and not
        // some other framebuffer object)
        j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    return oglc;
}

/**
 * Makes a context current to the given source and destination
 * surfaces.  If there is a problem making the context current, this method
 * will return NULL; otherwise, returns a pointer to the OGLContext that is
 * associated with the destination surface.
 */
OGLContext *
OGLSD_MakeOGLContextCurrent(JNIEnv *env, OGLSDOps *srcOps, OGLSDOps *dstOps)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_MakeOGLContextCurrent");
	
    CGLSDOps *dstCGLOps = (CGLSDOps *)dstOps->privOps;
	
    J2dTraceLn4(J2D_TRACE_VERBOSE, "  src: %d %p dst: %d %p", srcOps->drawableType, srcOps, dstOps->drawableType, dstOps);

    OGLContext *oglc = dstCGLOps->configInfo->context;
    if (oglc == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_MakeOGLContextCurrent: context is null");
        return NULL;
    }

    CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;

    // it seems to be necessary to explicitly flush between context changes
    OGLContext *currentContext = OGLRenderQueue_GetCurrentContext();
    if (currentContext != NULL) {
        j2d_glFlush();
    }

    if (dstOps->drawableType == OGLSD_FBOBJECT) {
        // first make sure we have a current context (if the context isn't
        // already current to some drawable, we will make it current to
        // its scratch surface)
        if (oglc != currentContext) {
            if (!CGLSD_MakeCurrentToScratch(env, oglc)) {
                return NULL;
            }
        }

        // now bind to the fbobject associated with the destination surface;
        // this means that all rendering will go into the fbobject destination
        // (note that we unbind the currently bound texture first; this is
        // recommended procedure when binding an fbobject)
        j2d_glBindTexture(GL_TEXTURE_2D, 0);
        j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, dstOps->fbobjectID);
		
        return oglc;
    }

    // set the current surface
    if (dstOps->drawableType == OGLSD_PBUFFER) {
        // REMIND: pbuffers are not fully tested yet...
        [ctxinfo->context clearDrawable];
        [ctxinfo->context makeCurrentContext];
        [ctxinfo->context setPixelBuffer: dstCGLOps->pbuffer
                cubeMapFace: 0
                mipMapLevel: 0
                currentVirtualScreen: [ctxinfo->context currentVirtualScreen]];
    } else {
        NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];

        CGLSDOps *cglsdo = (CGLSDOps *)dstOps->privOps;
        NSView *nsView = (NSView *)cglsdo->peerData;

        if ([ctxinfo->context view] != nsView) {
            [ctxinfo->context makeCurrentContext];
            [ctxinfo->context setView: nsView];
        }

        [pool drain];
    }
	
    if (OGLC_IS_CAP_PRESENT(oglc, CAPS_EXT_FBOBJECT)) {
        // the GL_EXT_framebuffer_object extension is present, so we
        // must bind to the default (windowing system provided)
        // framebuffer
        j2d_glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }
	
    if ((srcOps != dstOps) && (srcOps->drawableType == OGLSD_PBUFFER)) {
        // bind pbuffer to the render texture object (since we are preparing
        // to copy from the pbuffer)
        CGLSDOps *srcCGLOps = (CGLSDOps *)srcOps->privOps;
        j2d_glBindTexture(GL_TEXTURE_2D, srcOps->textureID);
        [ctxinfo->context
                setTextureImageToPixelBuffer: srcCGLOps->pbuffer
                colorBuffer: GL_FRONT];
    }
	
    return oglc;
}

/**
 * This function initializes a native window surface and caches the window
 * bounds in the given OGLSDOps.  Returns JNI_TRUE if the operation was
 * successful; JNI_FALSE otherwise.
 */
jboolean
OGLSD_InitOGLWindow(JNIEnv *env, OGLSDOps *oglsdo)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_InitOGLWindow");
	
    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_InitOGLWindow: ops are null");
        return JNI_FALSE;
    }
    
    CGLSDOps *cglsdo = (CGLSDOps *)oglsdo->privOps;
    if (cglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_InitOGLWindow: cgl ops are null");
        return JNI_FALSE;
    }
	
    AWTView *v = cglsdo->peerData;
    if (v == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "OGLSD_InitOGLWindow: view is invalid");
        return JNI_FALSE;
    }

    NSRect surfaceBounds = [v bounds];
    
    oglsdo->drawableType = OGLSD_WINDOW;
    oglsdo->isOpaque = JNI_TRUE;
    oglsdo->width = surfaceBounds.size.width;
    oglsdo->height = surfaceBounds.size.height;

    J2dTraceLn2(J2D_TRACE_VERBOSE, "  created window: w=%d h=%d", oglsdo->width, oglsdo->height);
	
    return JNI_TRUE;
}

void
OGLSD_SwapBuffers(JNIEnv *env, jlong pPeerData)
{
    J2dTraceLn(J2D_TRACE_INFO, "OGLSD_SwapBuffers");
    [[NSOpenGLContext currentContext] flushBuffer];
}

#pragma mark -
#pragma mark "--- CGLSurfaceData methods ---"

extern LockFunc        OGLSD_Lock;
extern GetRasInfoFunc  OGLSD_GetRasInfo;
extern UnlockFunc      OGLSD_Unlock;
extern DisposeFunc     OGLSD_Dispose;

JNIEXPORT void JNICALL
Java_sun_java2d_opengl_CGLSurfaceData_initOps
    (JNIEnv *env, jobject cglsd,
     jlong pConfigInfo, jlong pPeerData, jint xoff, jint yoff)
{
    J2dTraceLn(J2D_TRACE_INFO, "CGLSurfaceData_initOps");
    J2dTraceLn1(J2D_TRACE_INFO, "  pPeerData=%p", jlong_to_ptr(pPeerData));
    J2dTraceLn2(J2D_TRACE_INFO, "  xoff=%d, yoff=%d", (int)xoff, (int)yoff);
	
    OGLSDOps *oglsdo = (OGLSDOps *)
        SurfaceData_InitOps(env, cglsd, sizeof(OGLSDOps));
    CGLSDOps *cglsdo = (CGLSDOps *)malloc(sizeof(CGLSDOps));
    if (cglsdo == NULL) {
        JNU_ThrowOutOfMemoryError(env, "creating native cgl ops");
        return;
    }
	
    oglsdo->privOps = cglsdo;
	
    oglsdo->sdOps.Lock               = OGLSD_Lock;
    oglsdo->sdOps.GetRasInfo         = OGLSD_GetRasInfo;
    oglsdo->sdOps.Unlock             = OGLSD_Unlock;
    oglsdo->sdOps.Dispose            = OGLSD_Dispose;
	
    oglsdo->drawableType = OGLSD_UNDEFINED;
    oglsdo->activeBuffer = GL_FRONT;
    oglsdo->needsInit = JNI_TRUE;
    oglsdo->xOffset = xoff;
    oglsdo->yOffset = yoff;
	
    cglsdo->peerData = (AWTView *)jlong_to_ptr(pPeerData);
    cglsdo->configInfo = (CGLGraphicsConfigInfo *)jlong_to_ptr(pConfigInfo);

    if (cglsdo->configInfo == NULL) {
        free(cglsdo);
        JNU_ThrowNullPointerException(env, "Config info is null in initOps");
    }
}

JNIEXPORT void JNICALL
Java_sun_java2d_opengl_CGLSurfaceData_clearWindow
(JNIEnv *env, jobject cglsd)
{
    J2dTraceLn(J2D_TRACE_INFO, "CGLSurfaceData_clearWindow");
    
    OGLSDOps *oglsdo = (OGLSDOps*) SurfaceData_GetOps(env, cglsd);
    CGLSDOps *cglsdo = (CGLSDOps*) oglsdo->privOps;

    cglsdo->peerData = NULL;
}

JNIEXPORT jboolean JNICALL
Java_sun_java2d_opengl_CGLSurfaceData_initPbuffer
    (JNIEnv *env, jobject cglsd,
     jlong pData, jlong pConfigInfo, jboolean isOpaque,
     jint width, jint height)
{
    J2dTraceLn3(J2D_TRACE_INFO, "CGLSurfaceData_initPbuffer: w=%d h=%d opq=%d", width, height, isOpaque);
		
    OGLSDOps *oglsdo = (OGLSDOps *)jlong_to_ptr(pData);
    if (oglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: ops are null");
        return JNI_FALSE;
    }
	
    CGLSDOps *cglsdo = (CGLSDOps *)oglsdo->privOps;
    if (cglsdo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: cgl ops are null");
        return JNI_FALSE;
    }
    
    CGLGraphicsConfigInfo *cglInfo = (CGLGraphicsConfigInfo *)
        jlong_to_ptr(pConfigInfo);
    if (cglInfo == NULL) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: cgl config info is null");
        return JNI_FALSE;
    }
	
    // find the maximum allowable texture dimensions (this value ultimately
    // determines our maximum pbuffer size)
    int pbMax = 0;
    j2d_glGetIntegerv(GL_MAX_TEXTURE_SIZE, &pbMax);
	
    int pbWidth = 0;
    int pbHeight = 0;
    if (OGLC_IS_CAP_PRESENT(cglInfo->context, CAPS_TEXNONPOW2)) {
        // use non-power-of-two dimensions directly
        pbWidth = (width <= pbMax) ? width : 0;
        pbHeight = (height <= pbMax) ? height : 0;
    } else {
        // find the appropriate power-of-two dimensions
        pbWidth = OGLSD_NextPowerOfTwo(width, pbMax);
        pbHeight = OGLSD_NextPowerOfTwo(height, pbMax);
    }
    // for some reason, the pbuffers fail sometimes when less or equal to 32
    // in any of their dimensions
    // see <rdar://problem/4566762>
    if (pbWidth  < GL_MIN_TEXTURE_SIZE) pbWidth  = GL_MIN_TEXTURE_SIZE;
    if (pbHeight < GL_MIN_TEXTURE_SIZE) pbHeight = GL_MIN_TEXTURE_SIZE;
	
    J2dTraceLn3(J2D_TRACE_VERBOSE, "  desired pbuffer dimensions: w=%d h=%d max=%d", pbWidth, pbHeight, pbMax);

    // if either dimension is 0, we cannot allocate a pbuffer/texture with the
    // requested dimensions
    if (pbWidth == 0 || pbHeight == 0) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: dimensions too large");
        return JNI_FALSE;
    }
	
    int format = isOpaque ? GL_RGB : GL_RGBA;

    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];

    cglsdo->pbuffer =
        [[NSOpenGLPixelBuffer alloc]
            initWithTextureTarget: GL_TEXTURE_2D
            textureInternalFormat: format
            textureMaxMipMapLevel: 0
            pixelsWide: pbWidth
            pixelsHigh: pbHeight];
    if (cglsdo->pbuffer == nil) {
        J2dRlsTraceLn(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: could not create pbuffer");
        [pool drain];
        return JNI_FALSE;
    }

    [pool drain];

    // make sure the actual dimensions match those that we requested
    GLsizei actualWidth  = [cglsdo->pbuffer pixelsWide];
    GLsizei actualHeight = [cglsdo->pbuffer pixelsHigh];
    if (actualWidth != pbWidth || actualHeight != pbHeight) {
        J2dRlsTraceLn2(J2D_TRACE_ERROR, "CGLSurfaceData_initPbuffer: actual (w=%d h=%d) != requested", actualWidth, actualHeight);
        [cglsdo->pbuffer release];
        return JNI_FALSE;
    }
	
    GLuint texID = 0;
    j2d_glGenTextures(1, &texID);
    j2d_glBindTexture(GL_TEXTURE_2D, texID);
	
    oglsdo->drawableType = OGLSD_PBUFFER;
    oglsdo->isOpaque = isOpaque;
    oglsdo->width = width;
    oglsdo->height = height;
    oglsdo->textureID = texID;
    oglsdo->textureWidth = pbWidth;
    oglsdo->textureHeight = pbHeight;
    oglsdo->activeBuffer = GL_FRONT;
    oglsdo->needsInit = JNI_TRUE;
	
    OGLSD_INIT_TEXTURE_FILTER(oglsdo, GL_NEAREST);

    return JNI_TRUE;
}

#pragma mark -
#pragma mark "--- CGLSurfaceData methods - Mac OS X specific ---"

// Must be called on the QFT...
JNIEXPORT void JNICALL
Java_sun_java2d_opengl_CGLSurfaceData_resize
    (JNIEnv *env, jobject jsurfacedata,
     jint xoff, jint yoff, jint width, jint height)
{
    J2dTraceLn2(J2D_TRACE_INFO, "CGLSurfaceData_resize: w=%d h=%d", width, height);

    OGLSDOps *oglsdo = (OGLSDOps*)SurfaceData_GetOps(env, jsurfacedata);
    oglsdo->needsInit = JNI_TRUE;
    oglsdo->xOffset = xoff;
    oglsdo->yOffset = yoff;
    oglsdo->width = width;
    oglsdo->height = height;

    if (oglsdo->drawableType == OGLSD_WINDOW) {
        OGLContext_SetSurfaces(env, ptr_to_jlong(oglsdo), ptr_to_jlong(oglsdo));

        // we have to explicitly tell the NSOpenGLContext that its target
        // drawable has changed size
        CGLSDOps *cglsdo = (CGLSDOps *)oglsdo->privOps;
        OGLContext *oglc = cglsdo->configInfo->context;
        CGLCtxInfo *ctxinfo = (CGLCtxInfo *)oglc->ctxInfo;
		
        NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];
        [ctxinfo->context update];
        [pool drain];
    }
}
