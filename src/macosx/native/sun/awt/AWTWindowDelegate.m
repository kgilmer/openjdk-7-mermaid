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

#import <Cocoa/Cocoa.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "AWTWindowDelegate.h"
#import "AWTWindow.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"


static JNF_CLASS_CACHE(jc_CPlatformWindow, "sun/lwawt/macosx/CPlatformWindow");


@implementation AWTWindowDelegate

- (id) initWithAWTWindow: (AWTWindow *) window {
AWT_ASSERT_APPKIT_THREAD;
    
    if (self = [super init]) {
        m_awtWindow = window;
    }
    return self;
}

- (void) dealloc {
AWT_ASSERT_APPKIT_THREAD;
    
    [super dealloc];
}

// This prevents the toplevel to open a menu on the mouse click over the title
- (BOOL)window:(NSWindow *)sender shouldPopUpDocumentPathMenu:(NSMenu *)titleMenu{
AWT_ASSERT_APPKIT_THREAD;
    
    return NO;
}

- (void) _deliverMoveResizeEvent {
AWT_ASSERT_APPKIT_THREAD;
    
    NSRect screenRect = [[NSScreen mainScreen] frame];
    NSRect frame = [m_awtWindow frame];
    frame.origin.y = screenRect.size.height - frame.size.height - frame.origin.y;
    
    // deliver the event if this is a user-initiated live resize or as a side-effect
    // of a Java initiated resize, because AppKit can override the bounds and force
    // the bounds of the window to avoid the Dock or remain on screen.
    
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    static JNF_MEMBER_CACHE(jm_deliverMoveResizeEvent, jc_CPlatformWindow, "deliverMoveResizeEvent", "(IIII)V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_deliverMoveResizeEvent,
                      (jint)frame.origin.x,
                      (jint)frame.origin.y,
                      (jint)frame.size.width,
                      (jint)frame.size.height);
}

- (void)windowDidMove:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [self _deliverMoveResizeEvent];
}

- (void)windowDidResize:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [self _deliverMoveResizeEvent];
}

- (void)windowWillClose:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    static JNF_MEMBER_CACHE(jm_deliverWindowClosingEvent, jc_CPlatformWindow, "deliverWindowClosingEvent", "()V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_deliverWindowClosingEvent);
}

- (void)windowDidExpose:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [AWTToolkit eventCountPlusPlus];
    // TODO: don't see this callback invoked anytime so we track
    // window exposing in _setVisible:(BOOL)
}

- (BOOL)windowShouldZoom:(NSWindow *)window toFrame:(NSRect)proposedFrame {
AWT_ASSERT_APPKIT_THREAD;
    
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    static JNF_MEMBER_CACHE(jm_deliverZoom, jc_CPlatformWindow, "deliverZoom", "(Z)V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_deliverZoom, ![window isZoomed]);
    
    return YES;
}

- (void) _deliverIconify:(BOOL)iconify {
AWT_ASSERT_APPKIT_THREAD;
    
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    static JNF_MEMBER_CACHE(jm_deliverIconify, jc_CPlatformWindow, "deliverIconify", "(Z)V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_deliverIconify, iconify);
}

- (void)windowDidMiniaturize:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [self _deliverIconify:JNI_TRUE];
}

- (void)windowDidDeminiaturize:(NSNotification *)notification {
AWT_ASSERT_APPKIT_THREAD;
    
    [self _deliverIconify:JNI_FALSE];
}

- (void) _deliverWindowFocusEvent:(BOOL)focused {
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    static JNF_MEMBER_CACHE(jm_deliverWindowFocusEvent, jc_CPlatformWindow, "deliverWindowFocusEvent", "(Z)V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_deliverWindowFocusEvent, (jboolean)focused);
}

- (void) windowDidBecomeKey: (NSNotification *) notification {
AWT_ASSERT_APPKIT_THREAD;
    [AWTToolkit eventCountPlusPlus];
    //  [CMenuBar activate:_menuBar modallyDisabled:showMenuBarDisabled];
    [self _deliverWindowFocusEvent:YES];
}

- (void) windowDidResignKey: (NSNotification *) notification {
    // TODO: check why sometimes at start is invoked *not* on AppKit main thread.
AWT_ASSERT_APPKIT_THREAD;
    [AWTToolkit eventCountPlusPlus];
    //TODO: deactivate menubar
    [self _deliverWindowFocusEvent:NO];
}

- (void) windowDidBecomeMain: (NSNotification *) notification {
AWT_ASSERT_APPKIT_THREAD;
    [AWTToolkit eventCountPlusPlus];
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    static JNF_MEMBER_CACHE(jm_windowDidBecomeMain, jc_CPlatformWindow, "windowDidBecomeMain", "()V");
    JNFCallVoidMethod(env, m_awtWindow.m_cPlatformWindow, jm_windowDidBecomeMain);
}

- (BOOL)windowShouldClose:(id)sender {
AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    static JNF_MEMBER_CACHE(jm_windowShouldClose, jc_CPlatformWindow, "windowShouldClose", "()Z");
    return (BOOL) JNFCallBooleanMethod(env, m_awtWindow.m_cPlatformWindow, jm_windowShouldClose);
}

/*
 - (void) deliverResizePaintEvent: (JNIEnv *) env {
 if (env != NULL) {
 JNU_CallMethodByName(env, NULL, _jpeerObj,
 "deliverResizePaintEvent", "()V");
 }
 }
 */
@end // AWTWindowDelegate
