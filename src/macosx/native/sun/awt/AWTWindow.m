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

#import <jni.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "AWTWindowDelegate.h"
#import "AWTWindow.h"
#import "AWTView.h"
#import "CMenu.h"
#import "CMenuBar.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"


@implementation AWTWindow

-(id)initWithContentRect:(NSRect)contentRect 
               styleMask:(NSUInteger)windowStyle
                 backing:(NSBackingStoreType)bufferingType 
                   defer:(BOOL)deferCreation
          platformWindow:(jobject) cPlatformWindow
             contentView:(NSView *)contentView;
{
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    
    self = [super initWithContentRect:contentRect
                            styleMask:windowStyle
                              backing:bufferingType
                                defer:deferCreation];
    
    if (self == nil) {
        // TODO: not implemented
        return nil;
    }
    
    // Init the internal NSView object
    m_view = contentView;
    
    // Set the link to Java CPlatformWindow object to perform callbacks.
    m_cPlatformWindow = cPlatformWindow;
    AWTWindowDelegate *delegate = [[AWTWindowDelegate alloc] initWithAWTWindow:self];
    [self setDelegate:delegate];
    
    //AWTView is the right component to take responsibility for MouseEvents and KeyEvents.
    [self setContentView:m_view];
    [self setInitialFirstResponder:m_view];
    [self setReleasedWhenClosed:NO];
    [self setHasShadow:YES];
    
    [self setPreservesContentDuringLiveResize:YES];
    
    return self;
}

- (void) setResizable: (BOOL)resizable {
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    [self performSelectorOnMainThread: @selector(_setResizable_OnAppKitThread:)
                           withObject:[NSNumber numberWithBool: resizable] waitUntilDone:YES];
}

- (void) setMinSizeImpl: (NSSize) minSize {
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    _minSize = minSize;
    if (_minSize.width < 1)  _minSize.width = 1;
    if (_minSize.height < 1) _minSize.height = 1;
    
    //TODO: setMinSize if only isResizable==true
    [self performSelectorOnMainThread: @selector(_setMinSize_OnAppKitThread:)
                           withObject: [NSValue valueWithSize: _minSize] waitUntilDone: YES];
}

- (void) setAlwaysOnTop: (BOOL)isAlwaysOnTop {
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    [self performSelectorOnMainThread: @selector(_setAlwaysOnTop_OnAppKitThread:)
                           withObject: [NSNumber numberWithBool: isAlwaysOnTop] waitUntilDone:YES];
}

/*
 * Helper methods which must be invoked on the main thread.
 */
- (void) dealloc {
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    (*env)->DeleteGlobalRef(env, m_cPlatformWindow);
    m_cPlatformWindow = NULL;
    
    [super dealloc];
}


/******************
 * Accessor methods
 ******************/

- (jobject) cPlatformWindow{
    return m_cPlatformWindow;
}

/*****************************************************
 * Methods that make all Cocoa calls on AppKit thread.
 ******************************************************/

- (void) _createAWTWindow_OnAppKitThread: (NSMutableArray *)argValue {
AWT_ASSERT_APPKIT_THREAD;
    
    NSUInteger style = [[argValue objectAtIndex: 0] intValue];
    jobject cPlatformWindow = (jobject)[[argValue objectAtIndex: 1] pointerValue];
    NSView * contentView = OBJC([[argValue objectAtIndex: 2] longValue]);
    
    jint x = [[argValue objectAtIndex: 3] intValue];
    jint y = [[argValue objectAtIndex: 4] intValue];
    jint w = [[argValue objectAtIndex: 5] intValue];
    jint h = [[argValue objectAtIndex: 6] intValue];
    
    [argValue removeAllObjects];
    
    NSRect frameRect = NSMakeRect(x, y, w, h);
    
    NSRect contentRect = [NSWindow contentRectForFrameRect: frameRect styleMask: style];
    
    if (contentRect.size.width <= 0.0f) {
        contentRect.size.width = 1.0f;
    }
    if (contentRect.size.height <= 0.0f) {
        contentRect.size.height = 1.0f;
    }
    
    AWTWindow *aWindow = [self initWithContentRect:contentRect
                                         styleMask:style
                                           backing:NSBackingStoreBuffered
                                             defer:NO
                                    platformWindow:cPlatformWindow
                                       contentView:contentView];
    
    [argValue addObject: aWindow];
}

- (void) _getWindowContentRect_OnAppKitThread: (NSMutableArray *)value {
AWT_ASSERT_APPKIT_THREAD;
    
    AWTWindow * aWindow = (AWTWindow *)[value objectAtIndex: 0];
    NSRect frame = [aWindow frame];
    
    NSRect contentRect =
    [NSWindow contentRectForFrameRect:frame styleMask:[aWindow styleMask]];
    
    [value removeAllObjects];
    [value addObject: [NSValue valueWithRect:contentRect]];
    [value addObject: [NSValue valueWithRect:frame]];
}

- (void) _setBounds_OnAppKitThread:(NSArray *) args { //NSRect: bounds and display: YES
AWT_ASSERT_APPKIT_THREAD;
    
    NSRect rect = [(NSValue *)[args objectAtIndex: 0] rectValue];
    BOOL show = [(NSNumber *)[args objectAtIndex: 1] boolValue] ;
    [self setFrame: rect display: show];
    
    //only start tracking events if pointer is above the toplevel
    //TODO: should post an Entered event if YES.
    NSPoint mLocation = [NSEvent mouseLocation];
    
    [self setAcceptsMouseMovedEvents: NSPointInRect(mLocation, rect)];
}

- (void) _setResizable_OnAppKitThread: (NSNumber *)mayResize {
AWT_ASSERT_APPKIT_THREAD;
    
    BOOL resizable = [mayResize boolValue];
    
    if (resizable) {
        NSSize defaultMaxSize = NSMakeSize(FLT_MAX, FLT_MAX);
        [self setMaxSize: defaultMaxSize];
        [self setMinSize: _minSize];
    } else {
        NSRect currentFrame = [self frame];
        [self setMaxSize: currentFrame.size];
        [self setMinSize: currentFrame.size];
    }
    
    [self setShowsResizeIndicator: resizable];
    
    NSButton *zoomButton = [self standardWindowButton:NSWindowZoomButton];
    [zoomButton setEnabled:resizable];
}

- (void) _setMinSize_OnAppKitThread: (NSValue *) minSize {
AWT_ASSERT_APPKIT_THREAD;
    
    [self setMinSize: [minSize sizeValue]];
}


- (void) _setMenuBar_OnAppKitThread: (CMenuBar *)menuBar {
AWT_ASSERT_APPKIT_THREAD;
    
    [menuBar retain];
    
    if ([self isKeyWindow]) {
        [_menuBar deactivate];
    }
    
    [_menuBar release];
    _menuBar = menuBar;
    
    // if ([self isKeyWindow]) {
    [CMenuBar activate:_menuBar modallyDisabled:NO];
    // }
}

- (void) _setAlwaysOnTop_OnAppKitThread: (NSNumber *)isAlwaysOnTop {
AWT_ASSERT_APPKIT_THREAD;
	
    BOOL alwaysOnTop = [isAlwaysOnTop boolValue];
	
    if (alwaysOnTop) {
        // Note that always-on-top windows are below modal dialogs
        // Use (NSModalPanelWindowLevel + 1) level to change this order
        [self setLevel: NSFloatingWindowLevel];
    } else {
        [self setLevel: NSNormalWindowLevel];
    }
}

- (void) setMenuBar: (CMenuBar *)menuBar {
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    [ThreadUtilities performOnMainThread:@selector(_setMenuBar_OnAppKitThread:) onObject:self withObject:menuBar waitUntilDone:YES awtMode:YES];
}

- (BOOL) isUnderMouse {
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    return [m_view hitTest: [self mouseLocationOutsideOfEventStream]] != nil;
}

/***************************************************
 * Methods to override NSWindow's default behaviour
 ***************************************************/

- (BOOL)canBecomeKeyWindow {
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    if (env == NULL) {
        return YES;
    }
    return (BOOL) (*env)->CallBooleanMethod(env, m_cPlatformWindow,
                                            javaIDs.CPlatformWindow.canBecomeKeyWindow);
}

@end //AWTWindow

/*
 * JNI methods bodies
 */

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetMenuBar
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetMenuBar
(JNIEnv *env, jobject obj, jlong awtWindowPtr,  jlong mbPtr)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    CMenuBar *menuBar = OBJC(mbPtr);
    [awtWindow setMenuBar: menuBar];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeCreateWindow
 * Signature: (JZIIII)J
 */
JNIEXPORT jlong JNICALL Java_sun_lwawt_macosx_CPlatformWindow_nativeCreateWindow
(JNIEnv *env, jobject obj, jlong contentViewPtr, jboolean withBorder,
 jint originX, jint originY, jint width, jint height)
{
    AWTWindow *aWindow = nil;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    NSUInteger style;
    if (withBorder) {
        style = NSTitledWindowMask | NSClosableWindowMask | NSMiniaturizableWindowMask | NSResizableWindowMask;
    } else {
        style = NSBorderlessWindowMask;
    }
    
    jobject cPlatformWindow = (*env)->NewGlobalRef(env, obj);
    
    NSMutableArray * retArray = [NSMutableArray arrayWithCapacity:7];
    [retArray addObject: [NSNumber numberWithInt: style]];
    [retArray addObject: [NSValue valueWithBytes:&cPlatformWindow objCType:@encode(jobject)]];
    [retArray addObject: [NSNumber numberWithLong: contentViewPtr]];
    
    [retArray addObject: [NSNumber numberWithInt: originX]];
    [retArray addObject: [NSNumber numberWithInt: originY]];
    [retArray addObject: [NSNumber numberWithInt: width]];
    [retArray addObject: [NSNumber numberWithInt: height]];
    
    [ThreadUtilities performOnMainThread: @selector(_createAWTWindow_OnAppKitThread:) onObject:[AWTWindow alloc] withObject:retArray waitUntilDone: YES awtMode: NO];
    aWindow = (AWTWindow *)[retArray objectAtIndex: 0];
    
JNF_COCOA_EXIT(env);
    
    return OBJCLONG(aWindow);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetTitle
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
JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeGetInsets
(JNIEnv *env, jobject obj, jlong awtWindowPtr)
{
    jobject ret = NULL;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    NSMutableArray * retArray = [NSMutableArray arrayWithCapacity:2];
    [retArray addObject: awtWindow];
    [awtWindow performSelectorOnMainThread: @selector(_getWindowContentRect_OnAppKitThread:) withObject: retArray waitUntilDone: YES];
    NSRect contentRect = [(NSValue *)[retArray objectAtIndex: 0] rectValue];
    NSRect frame = [(NSValue *)[retArray objectAtIndex: 1] rectValue];
    
    CGFloat scaleFactor = 1.0f;
    CGFloat l = (contentRect.origin.x - frame.origin.x) / scaleFactor;
    
    jint top = (jint)(frame.size.height / scaleFactor - contentRect.size.height);
    jint left = (jint)l;
    jint bottom = (jint)((contentRect.origin.y - frame.origin.y) / scaleFactor);
    jint right = (jint)(frame.size.width / scaleFactor - (contentRect.size.width + l));
    ret = JNU_NewObjectByName(env, "java/awt/Insets", "(IIII)V", top, left, bottom, right);
    
//    UNLOCK([aWindow getNativeLock]);                                                                                                                                
    
JNF_COCOA_EXIT(env);
    return ret;
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetBounds
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetBounds
(JNIEnv *env, jobject obj, jlong awtWindowPtr, jint originX, jint originY, jint width, jint height)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    NSRect rect;
    rect.origin.x = originX;
    rect.origin.y = originY;
    rect.size.width = width;
    rect.size.height = height;
    
    //TODO: not sure we need displayIfNeeded message in our view
    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    NSArray * args = [NSArray arrayWithObjects:[NSValue valueWithRect:rect], [NSNumber numberWithBool: YES], nil];
    [awtWindow performSelectorOnMainThread: @selector(_setBounds_OnAppKitThread:) withObject: args waitUntilDone: YES];
    
    // ensure we repaint the whole window after the resize operation
    // (this will also re-enable screen updates, which were disabled above)
    //TODO: send PaintEvent
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeScreenOn
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeScreenOn
(JNIEnv *env, jobject self, jlong objcPtr)
{
    jlong ret = 0L;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(objcPtr);
    NSDictionary *props = [[aWindow screen] deviceDescription];
    ret = [[props objectForKey:@"NSScreenNumber"] intValue];
    
JNF_COCOA_EXIT(env);
    
    return ret;
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeToBack
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

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeToFront
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

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetResizable
(JNIEnv *env, jobject obj, jlong windowPtr, jboolean resizable)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [aWindow setResizable: resizable];
    
JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetMinSize
(JNIEnv *env, jobject obj, jlong windowPtr, jint w, jint h)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTWindow *aWindow = OBJC(windowPtr);
    [aWindow setMinSizeImpl: NSMakeSize(w, h)];
    
JNF_COCOA_EXIT(env);
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetAlwaysOnTop
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
 * Signature: (II)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetTitleIconImage
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
        
        underMouse = [aWindow isUnderMouse];
    }];
    
JNF_COCOA_EXIT(env);
    
    return underMouse;
}
