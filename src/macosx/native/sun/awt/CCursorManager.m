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

#include <CCursorManager.h> 
#include <JavaNativeFoundation/JavaNativeFoundation.h>
#include <dlfcn.h>
#include <jni.h>
#include <jni_util.h>

#include "AppKit/NSEvent.h"
#include "AppKit/NSScreen.h"
#include "AppKit/NSImage.h"


@implementation CCursorManager

+ (void) _setCursor: (NSCursor *) cursor {
    // Non-blocking cursor update, otherwise we deadlock with EDT(hold AWTLock)
    // and AppKit thread (try AWTLock). The Wait cursor could be removed in more
    // polite manner but even this should be OK.
    [CCursorManager setWaitCursor:FALSE];
    [cursor performSelectorOnMainThread: @selector(set) withObject: nil  waitUntilDone: NO];
}

+ (void) setWaitCursor: (bool) enable {
    [CCursorManager performSelectorOnMainThread: @selector(setWaitCursor_OnAppKitThread:) withObject:[NSNumber numberWithBool: enable] waitUntilDone:NO];
}

+ (void) setWaitCursor_OnAppKitThread: (NSNumber *) enable {
    // Enable false is just noop
    if( [enable boolValue] ) {
        [[CCursorManager getCustomWaitCursor] set];
    }
}

@end

JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CCursorManager_nativeGetCursorPosition
(JNIEnv *env, jclass class)
{
  NSPoint absP = [NSEvent mouseLocation];
//TODO: create function to convert coordinates.
  NSRect screenRect = [[NSScreen mainScreen] frame];
  absP.y = screenRect.size.height - absP.y;
  
  jobject ret = JNU_NewObjectByName(env, "java/awt/Point", "(II)V", (jint)absP.x , (jint)absP.y);
  return ret;
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setWaitCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager setWaitCursor:TRUE];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setHandCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setHandCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor openHandCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setCrosshairCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setCrosshairCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor crosshairCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setEResizeCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setEResizeCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor resizeRightCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setMoveCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setMoveCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor closedHandCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setNResizeCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setNResizeCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor resizeUpCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setSResizeCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setSResizeCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor resizeDownCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setTextCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setTextCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor IBeamCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setArrowCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setArrowCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor arrowCursor]];
	JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CCursorManager
 * Method:    setWResizeCursor
 * Signature: ()V;
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_setWResizeCursor
(JNIEnv *env, jclass class)
{
	JNF_COCOA_ENTER(env);
	[CCursorManager _setCursor: [NSCursor resizeLeftCursor]];
	JNF_COCOA_EXIT(env);
}
