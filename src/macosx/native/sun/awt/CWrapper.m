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

#import "AWTWindow.h"
#import "LWCToolkit.h"
#import <jni.h>

#import "CWrapper.h"
#import "ThreadUtilities.h"

@implementation CWrapper

- (void) _NSWindow_addChildWindow:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    NSWindow * child = OBJC([[args objectAtIndex: 1] longValue]);
    jint order = [[args objectAtIndex: 2] intValue];

    [obj addChildWindow: child ordered: order];
}


- (void) _NSWindow_orderWindow:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    jint order = [[args objectAtIndex: 1] intValue];
    NSWindow * relativeToWindow = OBJC([[args objectAtIndex: 2] longValue]);

    [obj orderWindow: order relativeTo: [relativeToWindow windowNumber]];
}

- (void) _NSWindow_setFrame:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj setFrame: [(NSValue *)[args objectAtIndex: 1] rectValue] display: [[args objectAtIndex: 2] boolValue]];
}

- (void) _NSWindow_makeKeyAndOrderFront:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj makeKeyAndOrderFront: nil];
}

- (void) _NSWindow_makeMainWindow:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj makeMainWindow];
}

- (void) _NSWindow_isKeyWindow:(NSMutableArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    [args addObject: [NSNumber numberWithBool: [obj isKeyWindow]]];
}

- (void) _NSWindow_orderFrontRegardless:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj orderFrontRegardless];
}

- (void) _NSWindow_setAlphaValue:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj setAlphaValue: [[args objectAtIndex: 1] floatValue]];
}

- (void) _NSWindow_screen:(NSMutableArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    NSScreen * screen = [obj screen];
    
    [args addObject: [NSNumber numberWithLong: OBJCLONG(screen)]];
}

- (void) _NSWindow_miniaturize:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    [obj miniaturize: nil];
}

- (void) _NSWindow_deminiaturize:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    [obj deminiaturize: nil];
}

- (void) _NSWindow_zoom:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    [obj zoom: nil];
}

- (void) _NSWindow_makeFirstResponder:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSWindow * obj = OBJC([[args objectAtIndex: 0] longValue]);
    NSResponder * responder = OBJC([[args objectAtIndex: 1] longValue]);
    
    [obj makeFirstResponder: responder];
}

- (void) _NSView_addSubview:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSView * view = OBJC([[args objectAtIndex: 0] longValue]);
    NSView * subview = OBJC([[args objectAtIndex: 1] longValue]);
    
    [view addSubview: subview];
}

- (void) _NSView_setFrame:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSView * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    [obj setFrame: [(NSValue *)[args objectAtIndex: 1] rectValue]];
}

- (void) _NSView_frame:(NSMutableArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSView * view = OBJC([[args objectAtIndex: 0] longValue]);
    
    NSRect frame = [view frame];
    
    [args addObject: [NSValue valueWithRect: frame]];
}

- (void) _NSView_enterFullScreenMode:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;

    NSView * obj = OBJC([[args objectAtIndex: 0] longValue]);
    NSScreen * screen = [[obj window] screen];

    NSDictionary * opts = [NSDictionary dictionaryWithObjectsAndKeys:
                           [NSNumber numberWithBool: NO], NSFullScreenModeAllScreens, nil];
    
    [obj enterFullScreenMode: screen withOptions: opts];
}

- (void) _NSView_exitFullScreenMode:(NSArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSView * obj = OBJC([[args objectAtIndex: 0] longValue]);

    [obj exitFullScreenModeWithOptions: nil];
}

- (void) _NSView_window:(NSMutableArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSView * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    NSWindow * window = [obj window];
    
    [args addObject: [NSNumber numberWithLong: OBJCLONG(window)]];
}

- (void) _NSScreen_frame:(NSMutableArray *) args {
    AWT_ASSERT_APPKIT_THREAD;
    
    NSScreen * obj = OBJC([[args objectAtIndex: 0] longValue]);
    
    NSRect frame = [obj frame];
    
    [args addObject: [NSValue valueWithRect: frame]];
}

@end //CWrapper

static CWrapper * cwrapper = NULL;

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSObject
 * Method:    release
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSObject_release
(JNIEnv *env, jclass cls, jlong objectPtr)
{
    AR_POOL(pool);
    
    NSObject *object = OBJC(objectPtr);
    
    [object performSelectorOnMainThread: @selector(release)
                             withObject: nil waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_init
(JNIEnv *env, jclass cls)
{
    cwrapper = [[CWrapper alloc] init];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    makeKeyAndOrderFront
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_makeKeyAndOrderFront
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_makeKeyAndOrderFront:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    makeMainWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_makeMainWindow
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_makeMainWindow:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    isKeyWindow
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_isKeyWindow
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    NSMutableArray * args = [NSMutableArray arrayWithObjects: 
                             [NSNumber numberWithLong: windowPtr],
                             nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_isKeyWindow:)
                               withObject: args waitUntilDone:YES];
    
    BOOL isKeyWindow = [[args objectAtIndex: 1] boolValue];

    [pool drain];
    
    return (jboolean)isKeyWindow;
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    orderFront
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_orderFront
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);

    AWTWindow *window = OBJC(windowPtr);

    [window performSelectorOnMainThread: @selector(orderFront:)
                             withObject: window waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    orderOut
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_orderOut
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    AWTWindow *window = OBJC(windowPtr);
    
    [window performSelectorOnMainThread: @selector(orderOut:)
                             withObject: window waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    orderFrontRegardless
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_orderFrontRegardless
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_orderFrontRegardless:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    orderWindow
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_orderWindow
(JNIEnv *env, jclass cls, jlong windowPtr, jint ordered, jlong relativeToPtr)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                       [NSNumber numberWithInt: ordered],
                      [NSNumber numberWithLong: relativeToPtr],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_orderWindow:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}


/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    addChildWindow
 * Signature: (JJI)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_addChildWindow
(JNIEnv *env, jclass cls, jlong parentPtr, jlong childPtr, jint ordered)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: parentPtr],
                      [NSNumber numberWithLong: childPtr],
                       [NSNumber numberWithInt: ordered],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_addChildWindow:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    removeChildWindow
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_removeChildWindow
(JNIEnv *env, jclass cls, jlong parentPtr, jlong childPtr)
{
    AR_POOL(pool);

    AWTWindow *parent = OBJC(parentPtr);
    AWTWindow *child = OBJC(childPtr);

    [parent performSelectorOnMainThread: @selector(removeChildWindow:)
                             withObject: child waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    setFrame
 * Signature: (JIIIIZ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_setFrame
(JNIEnv *env, jclass cls, jlong windowPtr, jint x, jint y, jint w, jint h, jboolean display)
{
    AR_POOL(pool);

    AWTWindow *window = OBJC(windowPtr);

    NSRect rect;
    rect.origin.x = x;
    rect.origin.y = y;
    rect.size.width = w;
    rect.size.height = h;

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                        [NSValue valueWithRect:rect],
                      [NSNumber numberWithBool: display],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_setFrame:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    setAlphaValue
 * Signature: (JF)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_setAlphaValue
(JNIEnv *env, jclass cls, jlong windowPtr, jfloat alpha)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      [NSNumber numberWithFloat: alpha],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_setAlphaValue:)
                             withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    screen
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_screen
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    NSMutableArray * args = [NSMutableArray arrayWithObjects: 
                             [NSNumber numberWithLong: windowPtr],
                             nil];    
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_screen:)
                               withObject: args waitUntilDone:YES];
    
    jlong screenPtr = [[args objectAtIndex: 1] longValue];
    
    [pool drain];
    
    return screenPtr;
}

/*
 * Method:    miniaturize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_miniaturize
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_miniaturize:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    deminiaturize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_deminiaturize
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_deminiaturize:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    zoom
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_zoom
(JNIEnv *env, jclass cls, jlong windowPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_zoom:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSWindow
 * Method:    makeFirstResponder
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSWindow_makeFirstResponder
(JNIEnv *env, jclass cls, jlong windowPtr, jlong responderPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: windowPtr],
                      [NSNumber numberWithLong: responderPtr],
                      nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSWindow_makeFirstResponder:)
                               withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    addSubview
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_addSubview
(JNIEnv *env, jclass cls, jlong viewPtr, jlong subviewPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects:
                      [NSNumber numberWithLong: viewPtr],
                      [NSNumber numberWithLong: subviewPtr],
                      nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSView_addSubview:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    removeFromSuperview
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_removeFromSuperview
(JNIEnv *env, jclass cls, jlong viewPtr)
{
    AR_POOL(pool);

    NSView *view = OBJC(viewPtr);

    [view performSelectorOnMainThread: @selector(removeFromSuperview)
                             withObject: nil waitUntilDone:YES];
  
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    setFrame
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_setFrame
(JNIEnv *env, jclass cls, jlong viewPtr, jint x, jint y, jint w, jint h)
{
    AR_POOL(pool);
    
    NSRect rect;
    rect.origin.x = x;
    rect.origin.y = y;
    rect.size.width = w;
    rect.size.height = h;
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: viewPtr],
                      [NSValue valueWithRect:rect],
                      nil];    
    
    [cwrapper performSelectorOnMainThread: @selector(_NSView_setFrame:)
                               withObject: args waitUntilDone:YES];

    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    frame
 * Signature: (J)Ljava/awt/Rectangle;
 */
JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_frame
(JNIEnv *env, jclass cls, jlong viewPtr)
{
    AR_POOL(pool);
    
    NSMutableArray * args = [NSMutableArray arrayWithObjects:
                             [NSNumber numberWithLong: viewPtr],
                             nil];

    [cwrapper performSelectorOnMainThread: @selector(_NSView_frame:)
                               withObject: args waitUntilDone:YES];

    NSRect frame = [(NSValue *)[args objectAtIndex: 1] rectValue];

    jobject jRect = JNU_NewObjectByName(env, "java/awt/Rectangle", "(IIII)V",
                                        (jint)frame.origin.x, (jint)frame.origin.y,
                                        (jint)frame.size.width, (jint)frame.size.height);

    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }    
    
    [pool drain];    

    return jRect;
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    enterFullScreenMode
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_enterFullScreenMode
(JNIEnv *env, jclass cls, jlong viewPtr)
{
    AR_POOL(pool);

    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: viewPtr],
                      nil];    

    [cwrapper performSelectorOnMainThread: @selector(_NSView_enterFullScreenMode:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    exitFullScreenMode
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_exitFullScreenMode
(JNIEnv *env, jclass cls, jlong viewPtr)
{
    AR_POOL(pool);
    
    NSArray * args = [NSArray arrayWithObjects: 
                      [NSNumber numberWithLong: viewPtr],
                      nil];    
    
    [cwrapper performSelectorOnMainThread: @selector(_NSView_exitFullScreenMode:)
                               withObject: args waitUntilDone:YES];
    
    [pool drain];
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSView
 * Method:    window
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSView_window
(JNIEnv *env, jclass cls, jlong viewPtr)
{
    AR_POOL(pool);
    
    NSMutableArray * args = [NSMutableArray arrayWithObjects: 
                             [NSNumber numberWithLong: viewPtr],
                             nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSView_window:)
                               withObject: args waitUntilDone:YES];

    jlong windowPtr = [[args objectAtIndex: 1] longValue];

    [pool drain];

    return windowPtr;
}

/*
 * Class:     sun_lwawt_macosx_CWrapper$NSScreen
 * Method:    frame
 * Signature: (J)Ljava/awt/Rectangle;
 */
JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CWrapper_00024NSScreen_frame
(JNIEnv *env, jclass cls, jlong screenPtr)
{
    AR_POOL(pool);

    NSMutableArray * args = [NSMutableArray arrayWithObjects:
                             [NSNumber numberWithLong: screenPtr],
                             nil];
    
    [cwrapper performSelectorOnMainThread: @selector(_NSScreen_frame:)
                               withObject: args waitUntilDone:YES];

    NSRect frame = [(NSValue *)[args objectAtIndex: 1] rectValue];

    jobject jRect = JNU_NewObjectByName(env, "java/awt/Rectangle", "(IIII)V",
                                        (jint)frame.origin.x, (jint)frame.origin.y,
                                        (jint)frame.size.width, (jint)frame.size.height);
    
    if ((*env)->ExceptionOccurred(env)) {
        (*env)->ExceptionDescribe(env);
        (*env)->ExceptionClear(env);
    }    

    [pool drain];    
    
    return jRect;
}
