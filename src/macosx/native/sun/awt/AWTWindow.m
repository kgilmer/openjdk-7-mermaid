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

#import "AWTWindowDelegate.h"
#import "AWTWindow.h"
#import "AWTView.h"
#import "CMenu.h"
#import "CMenuBar.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"

@implementation AWTWindow

- (id) getLock{
  return &_lock;
}

-(id)initWithContentRect:(NSRect)contentRect 
               styleMask:(NSUInteger)windowStyle
                 backing:(NSBackingStoreType)bufferingType 
                   defer:(BOOL)deferCreation
          platformWindow:(jobject) cPlatformWindow
             contentView:(NSView *)contentView;
{
    AWT_ASSERT_APPKIT_THREAD;
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];

    self = [super initWithContentRect: contentRect
		  styleMask: windowStyle
		  backing: bufferingType
		  defer: deferCreation];

    if (self == nil) {
        // TODO: not implemented
        return nil;
    }

    // Init the internal NSView object
    m_view = contentView;
    
    // Set the link to Java CPlatformWindow object to perform callbacks.
    m_cPlatformWindow = cPlatformWindow;
    AWTWindowDelegate * delegate = [[AWTWindowDelegate alloc] initWithAWTWindow: self];
    [self setDelegate: delegate];

    //AWTView is the right component to take responsibility for MouseEvents and KeyEvents.
    [self setContentView: m_view];
    [self setInitialFirstResponder:m_view];
    [self setReleasedWhenClosed: NO];
    [self setHasShadow: YES];

    [self setPreservesContentDuringLiveResize:YES];

    return self;
}

- (void) toFront {
    AWT_ASSERT_NOT_APPKIT_THREAD;
    [ThreadUtilities performOnMainThread: @selector(_toFront_OnAppKitThread)
                     onObject:self withObject:nil waitUntilDone: YES awtMode: YES];
}

- (void) toBack {
    AWT_ASSERT_NOT_APPKIT_THREAD;
    [ThreadUtilities performOnMainThread: @selector(orderBack:)
                     onObject:self withObject:nil waitUntilDone: YES awtMode: YES];
}

/**
 * Set the application's icon.
 */
- (void) setIconImage: (NSImage *) nsImage {
  AWT_ASSERT_NOT_APPKIT_THREAD;

  [self setRepresentedURL:[NSURL fileURLWithPath:@"My Window Title"]];
  [nsImage setSize:NSMakeSize(16, 16)];
  [[self standardWindowButton:NSWindowDocumentIconButton] setImage:nsImage];
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

    pthread_mutex_destroy(&_lock);

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
    
    AWTWindow *aWindow =
        [self initWithContentRect: contentRect
              styleMask: style
              backing: NSBackingStoreBuffered
              defer: NO
              platformWindow: cPlatformWindow
              contentView: contentView];

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

- (void) _toFront_OnAppKitThread {
    AWT_ASSERT_APPKIT_THREAD;

    if (![self isKeyWindow]) {
        [self makeKeyAndOrderFront: self];
    } else {
        [self orderFront:self];
    }
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


- (void) isWindowUnderMouse_OnAppKitThread: (NSMutableArray*)args {
  AWT_ASSERT_APPKIT_THREAD;

  BOOL ret = ([m_view hitTest: [self mouseLocationOutsideOfEventStream]] != nil);
  [args addObject: [NSNumber numberWithBool: ret]];
}


- (void) setMenuBar: (CMenuBar *)menuBar {
    AWT_ASSERT_NOT_APPKIT_THREAD;
    [ThreadUtilities performOnMainThread:@selector(_setMenuBar_OnAppKitThread:) onObject:self withObject:menuBar waitUntilDone:YES awtMode:YES];
}

- (BOOL) isUnderMouse {
  AWT_ASSERT_NOT_APPKIT_THREAD;

  NSMutableArray * retArray = [NSMutableArray arrayWithCapacity:1];
  [ThreadUtilities performOnMainThread:@selector(isWindowUnderMouse_OnAppKitThread:) onObject:self withObject:retArray waitUntilDone:YES awtMode:YES];

  return [(NSNumber*) [retArray objectAtIndex: 0] boolValue];
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
    AR_POOL(pool);

    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    CMenuBar *menuBar = OBJC(mbPtr);
    [awtWindow setMenuBar: menuBar];

    [pool drain];
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
    AR_POOL(pool);

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
    AWTWindow *aWindow = (AWTWindow *)[retArray objectAtIndex: 0];

    if (aWindow == nil) {
        return 0L;
    }
    [pool drain];

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
    AR_POOL(pool);

    AWTWindow *aWindow = OBJC(awtWindowPtr);

    const jchar *chars;
    NSString *title;

    /* Convert String to NSString */
    chars = (*env)->GetStringChars(env, jtitle, NULL);
    title = [NSString stringWithCharacters:(UniChar *)chars length:(*env)->GetStringLength(env, jtitle)];
    (*env)->ReleaseStringChars(env, jtitle, chars);

    [aWindow performSelectorOnMainThread: @selector(setTitle:) withObject: title waitUntilDone: YES];

    [pool drain];
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
  AR_POOL(pool);

  jobject ret;

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

  [pool drain];
  return ret;
}

/*
 * Class:     sun_lwawt_macosx_CPlatformWindow
 * Method:    nativeSetBounds
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetBounds
(JNIEnv *env, jobject obj, jlong awtWindowPtr,
 jint originX, jint originY, jint width, jint height)
{
  AR_POOL(pool);

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
  [pool drain];
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
  AR_POOL(pool);

  //TODO: what if windowptr is wrong or the internal error in cocoa happen?..
  jlong ret = 0L;
  NSScreen * currentScreen;
  if (objcPtr != 0L) {
    AWTWindow *aWindow = OBJC(objcPtr);
    currentScreen = [aWindow screen];
    NSDictionary * props = [currentScreen deviceDescription];
    ret = [[props objectForKey:@"NSScreenNumber"] intValue];
  }
  [pool drain];
  return ret;
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeToBack
(JNIEnv *env, jobject obj, jlong objcPtr)
{
    AR_POOL(pool);
    AWTWindow *aWindow = OBJC(objcPtr);
    [aWindow toBack];
	
    [pool drain];
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeToFront
(JNIEnv *env, jobject obj, jlong objcPtr)
{
    AR_POOL(pool);
	
    AWTWindow *aWindow = OBJC(objcPtr);
    [aWindow toFront];
	
    [pool drain];
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetResizable
(JNIEnv *env, jobject obj, jlong windowPtr, jboolean resizable)
{
  AR_POOL(pool);

  AWTWindow *aWindow = OBJC(windowPtr);
  [aWindow setResizable: resizable];

  [pool drain];
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetMinSize
(JNIEnv *env, jobject obj, jlong windowPtr, jint w, jint h)
{
  AR_POOL(pool);

  AWTWindow *aWindow = OBJC(windowPtr);
  [aWindow setMinSizeImpl: NSMakeSize(w, h)];

  [pool drain];
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CPlatformWindow_nativeSetAlwaysOnTop
(JNIEnv *env, jobject obj, jlong windowPtr, jboolean isAlwaysOnTop)
{
  AR_POOL(pool);

  AWTWindow *aWindow = OBJC(windowPtr);
  [aWindow setAlwaysOnTop:(BOOL)isAlwaysOnTop];

  [pool drain];
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

  AR_POOL(pool);
  AWTWindow *window = OBJC(windowPtr);
  [window setIconImage: jlong_to_ptr(nsImagePtr)];

  [pool drain];
}

JNIEXPORT jboolean JNICALL
Java_sun_lwawt_macosx_CMouseInfoPeer_nativeIsWindowUnderMouse
(JNIEnv *env, jclass clazz, jlong windowPtr)
{
  AR_POOL(pool);

  jboolean ret;
  AWTWindow *aWindow = OBJC(windowPtr);
  ret = [aWindow isUnderMouse];

  [pool drain];
  return ret;
}
