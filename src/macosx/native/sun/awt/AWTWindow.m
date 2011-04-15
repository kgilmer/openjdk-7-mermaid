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

#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "AWTWindowDelegate.h"
#import "AWTWindow.h"
#import "AWTView.h"
#import "CMenu.h"
#import "CMenuBar.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"


@implementation AWTWindow

@synthesize m_view;
@synthesize _menuBar;
@synthesize m_cPlatformWindow;
@synthesize _minSize;

- (id) initWithContentRect:(NSRect)contentRect 
               styleMask:(NSUInteger)windowStyle
                 backing:(NSBackingStoreType)bufferingType 
                   defer:(BOOL)deferCreation
          platformWindow:(jobject) cPlatformWindow
             contentView:(NSView *)contentView;
{
AWT_ASSERT_APPKIT_THREAD;
    
    self = [super initWithContentRect:contentRect
                            styleMask:windowStyle
                              backing:bufferingType
                                defer:deferCreation];
    
    if (self == nil) return nil; // no hope
    
    // Init the internal NSView object
    self.m_view = contentView;
    
    // Set the link to Java CPlatformWindow object to perform callbacks.
    self.m_cPlatformWindow = cPlatformWindow;
    AWTWindowDelegate *delegate = [[AWTWindowDelegate alloc] initWithAWTWindow:self];
    [self setDelegate:delegate];
    
    //AWTView is the right component to take responsibility for MouseEvents and KeyEvents.
    [self setContentView:self.m_view];
    [self setInitialFirstResponder:self.m_view];
    [self setReleasedWhenClosed:NO];
    [self setHasShadow:YES];
    
    [self setPreservesContentDuringLiveResize:YES];
    
    return self;
}

- (void) dealloc {
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    (*env)->DeleteGlobalRef(env, self.m_cPlatformWindow);
    self.m_cPlatformWindow = NULL;
    
    [super dealloc];
}

// NSWindow overrides

- (BOOL) canBecomeKeyWindow {
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    // TODO: add JNIEnv assert
    if (env == NULL) return YES;
    
    static JNF_CLASS_CACHE(jc_CPlatformWindow, "sun/lwawt/macosx/CPlatformWindow");
    static JNF_MEMBER_CACHE(jm_canBecomeKeyWindow, jc_CPlatformWindow, "canBecomeKeyWindow", "()Z");
    return JNFCallBooleanMethod(env, self.m_cPlatformWindow, jm_canBecomeKeyWindow);
}

@end // AWTWindow


/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetMenuBar
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetMenuBar
(JNIEnv *env, jobject obj, jlong awtWindowPtr, jlong mbPtr)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(awtWindowPtr);
    CMenuBar *menuBar = OBJC(mbPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        [menuBar retain];
        
        if ([aWindow isKeyWindow]) {
            [aWindow._menuBar deactivate];
        }
        
        [aWindow._menuBar release];
        aWindow._menuBar = menuBar;
        
        // if ([self isKeyWindow]) {
        [CMenuBar activate:aWindow._menuBar modallyDisabled:NO];
        // }
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeCreateWindow
 * Signature: (JZIIII)J
 */
JNIEXPORT jlong JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeCreateWindow
(JNIEnv *env, jobject obj, jlong contentViewPtr, jboolean withBorder,
 jint x, jint y, jint w, jint h)
{
    __block AWTWindow *aWindow = nil;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    NSUInteger style = NSBorderlessWindowMask;
    if (withBorder) {
        style = NSTitledWindowMask | NSClosableWindowMask | NSMiniaturizableWindowMask | NSResizableWindowMask;
    }
    
    jobject cPlatformWindow = (*env)->NewGlobalRef(env, obj);
    NSView *contentView = OBJC(contentViewPtr);
    NSRect frameRect = NSMakeRect(x, y, w, h);
    
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        NSRect contentRect = [NSWindow contentRectForFrameRect:frameRect styleMask:style];
        if (contentRect.size.width <= 0.0f) {
            contentRect.size.width = 1.0f;
        }
        if (contentRect.size.height <= 0.0f) {
            contentRect.size.height = 1.0f;
        }
        
        aWindow = [[AWTWindow alloc] initWithContentRect:contentRect
                                               styleMask:style
                                                 backing:NSBackingStoreBuffered
                                                   defer:NO
                                          platformWindow:cPlatformWindow
                                             contentView:contentView];
        
        CFRetain(aWindow);
        [aWindow release]; // GC
    }];
    
JNF_COCOA_EXIT(env);
    
    return ptr_to_jlong(aWindow);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetTitle
(JNIEnv *env, jobject obj, jlong awtWindowPtr, jstring jtitle)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(awtWindowPtr);
    [aWindow performSelectorOnMainThread:@selector(setTitle:)
                              withObject:JNFJavaToNSString(env, jtitle)
                           waitUntilDone:NO];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeGetInsets
 * Signature: (J)Ljava/awt/Insets;
 */
JNIEXPORT jobject JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeGetInsets
(JNIEnv *env, jobject obj, jlong awtWindowPtr)
{
    jobject ret = NULL;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(awtWindowPtr);
    __block NSRect contentRect = NSZeroRect;
    __block NSRect frame = NSZeroRect;
    
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        frame = [aWindow frame];
        contentRect = [NSWindow contentRectForFrameRect:frame styleMask:[aWindow styleMask]];
    }];
    
    jint top = (jint)(frame.size.height - contentRect.size.height);
    jint left = (jint)(contentRect.origin.x - frame.origin.x);
    jint bottom = (jint)(contentRect.origin.y - frame.origin.y);
    jint right = (jint)(frame.size.width - (contentRect.size.width + left));
    
    static JNF_CLASS_CACHE(jc_Insets, "java/awt/Insets");
    static JNF_CTOR_CACHE(jc_Insets_ctor, jc_Insets, "(IIII)V");
    ret = JNFNewObject(env, jc_Insets_ctor, top, left, bottom, right);                                                                                                                             
    
JNF_COCOA_EXIT(env);
    return ret;
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetBounds
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetBounds
(JNIEnv *env, jobject obj, jlong awtWindowPtr, jint originX, jint originY, jint width, jint height)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    NSRect rect = { { originX, originY }, { width, height } };
    
    //TODO: not sure we need displayIfNeeded message in our view
    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        [awtWindow setFrame:rect display:YES];
        
        //only start tracking events if pointer is above the toplevel
        //TODO: should post an Entered event if YES.
        NSPoint mLocation = [NSEvent mouseLocation];
        [awtWindow setAcceptsMouseMovedEvents:NSPointInRect(mLocation, rect)];
        
        // ensure we repaint the whole window after the resize operation
        // (this will also re-enable screen updates, which were disabled above)
        //TODO: send PaintEvent
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeScreenOn_AppKitThread
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeScreenOn_1AppKitThread
(JNIEnv *env, jobject self, jlong objcPtr)
{
    jint ret = 0;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(objcPtr);
    NSDictionary *props = [[aWindow screen] deviceDescription];
    ret = [[props objectForKey:@"NSScreenNumber"] intValue];
    
JNF_COCOA_EXIT(env);
    
    return ret;
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeToBack
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeToBack
(JNIEnv *env, jobject obj, jlong objcPtr)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(objcPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        [aWindow orderBack:nil];
    }];
	
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeToFront
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeToFront
(JNIEnv *env, jobject obj, jlong objcPtr)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
	
    AWTWindow *aWindow = OBJC(objcPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        if (![aWindow isKeyWindow]) {
            [aWindow makeKeyAndOrderFront:aWindow];
        } else {
            [aWindow orderFront:aWindow];
        }
    }];
	
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetResizable
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetResizable
(JNIEnv *env, jobject obj, jlong windowPtr, jboolean resizable)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        if (resizable) {
            NSSize defaultMaxSize = NSMakeSize(FLT_MAX, FLT_MAX);
            [aWindow setMaxSize:defaultMaxSize];
            [aWindow setMinSize:aWindow._minSize];
        } else {
            NSRect currentFrame = [aWindow frame];
            [aWindow setMaxSize:currentFrame.size];
            [aWindow setMinSize:currentFrame.size];
        }
        
        NSButton *zoomButton = [aWindow standardWindowButton:NSWindowZoomButton];
        [zoomButton setEnabled:resizable];
        
        [aWindow setShowsResizeIndicator:resizable];
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetMinSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetMinSize
(JNIEnv *env, jobject obj, jlong windowPtr, jint w, jint h)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        NSSize size = NSMakeSize(w, h);
        if (size.width < 1) size.width = 1;
        if (size.height < 1) size.height = 1;
        aWindow._minSize = size;
        [aWindow setMinSize:size];
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetAlwaysOnTop
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetAlwaysOnTop
(JNIEnv *env, jobject obj, jlong windowPtr, jboolean isAlwaysOnTop)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        // Note that always-on-top windows are below modal dialogs
        // Use (NSModalPanelWindowLevel + 1) level to change this order
        NSInteger newLevel = isAlwaysOnTop ? NSFloatingWindowLevel : NSNormalWindowLevel;
        [aWindow setLevel:newLevel];
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetTitleIconImage
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeSetTitleIconImage
(JNIEnv *env, jobject obj, jlong windowPtr, jlong nsImagePtr)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *window = OBJC(windowPtr);
    NSImage *image = OBJC(nsImagePtr);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^() {
        AWT_ASSERT_APPKIT_THREAD;
        
        [window setRepresentedURL:[NSURL fileURLWithPath:@""]];
        [image setSize:NSMakeSize(16, 16)];
        [[window standardWindowButton:NSWindowDocumentIconButton] setImage:image];
    }];
    
JNF_COCOA_EXIT(env);
}

JNIEXPORT jboolean JNICALL
Java_sun_lwawt_macosx_CMouseInfoPeer_nativeIsWindowUnderMouse
(JNIEnv *env, jclass clazz, jlong windowPtr)
{
    __block jboolean underMouse = JNI_FALSE;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^() {
        AWT_ASSERT_APPKIT_THREAD;
        
        NSPoint pt = [aWindow mouseLocationOutsideOfEventStream];
        underMouse = [aWindow.m_view hitTest:pt] != nil;
    }];
    
JNF_COCOA_EXIT(env);
    
    return underMouse;
}
