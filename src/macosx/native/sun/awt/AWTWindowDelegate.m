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

#import <AppKit/NSScreen.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "AWTWindowDelegate.h"
#import "AWTWindow.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"

@implementation AWTWindowDelegate

- (id) initWithAWTWindow: (AWTWindow *) window
{
  if (self = [super init]) {
    m_awtWindow = window;
  }
  return self;
}

- (void) dealloc {
  [super dealloc];
}

//This prevents the toplevel to open a menu on the mouse click over the title.
- (BOOL)window:(NSWindow *)sender shouldPopUpDocumentPathMenu:(NSMenu *)titleMenu{
  return NO;
}

- (void)windowDidMove:(NSNotification *)notification {
  [self deliverMoveResizeEvent];
}

- (void)windowDidResize:(NSNotification *)notification {
  [self deliverMoveResizeEvent];
}

- (void)windowWillClose:(NSNotification *)notification {
  AWT_ASSERT_APPKIT_THREAD;
  [AWTToolkit eventCountPlusPlus];
  JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
                         "deliverWindowClosingEvent", "()V");
    if ((*env)->ExceptionOccurred(env)) {
       (*env)->ExceptionDescribe(env);
       (*env)->ExceptionClear(env);
    }
  }
}

- (void)windowDidExpose:(NSNotification *)notification {
  [AWTToolkit eventCountPlusPlus];
  // TODO: don't see this callback invoked anytime so we track
  // window exposing in _setVisible:(BOOL)
}

- (BOOL)windowShouldZoom:(NSWindow *)window toFrame:(NSRect)proposedFrame {
  [self deliverZoom: ![window isZoomed]];
  return YES;
}

- (void)windowDidMiniaturize:(NSNotification *)notification {
  [self deliverIconify: JNI_TRUE];
}

- (void)windowDidDeminiaturize:(NSNotification *)notification {
  [self deliverIconify: JNI_FALSE];
}

/*******************************
 * Callbacks into Java methods
 *******************************/

- (void) windowDidBecomeKey: (NSNotification *) notification {
  AWT_ASSERT_APPKIT_THREAD;
  [AWTToolkit eventCountPlusPlus];
  BOOL showMenuBarDisabled = FALSE;
  //  [CMenuBar activate:_menuBar modallyDisabled:showMenuBarDisabled];
  JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];

  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
			 "deliverWindowFocusEvent", "(Z)V", JNI_TRUE);
  }
}

- (void) windowDidResignKey: (NSNotification *) notification {
  // TODO: check why sometimes at start is invoked *not* on AppKit main thread.
  AWT_ASSERT_APPKIT_THREAD;
  [AWTToolkit eventCountPlusPlus];
  JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
  //TODO: deactivate menubar

  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
			 "deliverWindowFocusEvent", "(Z)V", JNI_FALSE);
  }
}

- (void) windowDidBecomeMain: (NSNotification *) notification {
  AWT_ASSERT_APPKIT_THREAD;
  [AWTToolkit eventCountPlusPlus];

  JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];

  if (env != NULL) {
      (*env)->CallVoidMethod(env, [m_awtWindow cPlatformWindow],
              javaIDs.CPlatformWindow.windowDidBecomeMain);
  }
}

- (BOOL)windowShouldClose:(id)sender {
  AWT_ASSERT_APPKIT_THREAD;

  JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];

  return (BOOL) (*env)->CallBooleanMethod(env, [m_awtWindow cPlatformWindow],
              javaIDs.CPlatformWindow.windowShouldClose);
}

/*******************************
 * Callbacks into Java methods
 *******************************/

- (void) deliverIconify: (BOOL) iconify {
  [AWTToolkit eventCountPlusPlus];
  JNIEnv *env = [ThreadUtilities getJNIEnv];
  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
			 "deliverIconify", "(Z)V",
			 iconify);
  }
}

- (void) deliverZoom: (BOOL) zoom {
  [AWTToolkit eventCountPlusPlus];
  JNIEnv *env = [ThreadUtilities getJNIEnv];
  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
			 "deliverZoom", "(Z)V",
			 zoom);
  }
}

- (void) deliverMoveResizeEvent {
    AWT_ASSERT_APPKIT_THREAD;
    NSRect screenRect = [[NSScreen mainScreen] frame];
    NSRect frame = [m_awtWindow frame];
    frame.origin.y = screenRect.size.height - frame.size.height - frame.origin.y;

    //TODO: add check if we are resizing it in program or by the native system.
    // only deliver the event if this is a user-initiated live resize
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    if (env != NULL) {
        JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformWindow],
            "deliverMoveResizeEvent", "(IIII)V",
                (jint)frame.origin.x,
                (jint)frame.origin.y,
                (jint)frame.size.width,
                (jint)frame.size.height);
        if ((*env)->ExceptionOccurred(env)) {
            (*env)->ExceptionDescribe(env);
            (*env)->ExceptionClear(env);
        }
    }
}

/*
- (void) deliverResizePaintEvent: (JNIEnv *) env {
  if (env != NULL) {
    JNU_CallMethodByName(env, NULL, _jpeerObj,
			 "deliverResizePaintEvent", "()V");
  }
}
*/
@end //AWTWindowDelegate
