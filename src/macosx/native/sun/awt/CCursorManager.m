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

#include "sun_lwawt_macosx_CCursorManager.h"

#include <Cocoa/Cocoa.h>
#include <JavaNativeFoundation/JavaNativeFoundation.h>

#include "GeomUtilities.h"
#include "ThreadUtilities.h"

#include "java_awt_Cursor.h"


static SEL lookupCursorSelectorForType(jint type) {
    switch (type) {
        case java_awt_Cursor_DEFAULT_CURSOR:        return @selector(arrowCursor);
        case java_awt_Cursor_CROSSHAIR_CURSOR:      return @selector(crosshairCursor);
        case java_awt_Cursor_TEXT_CURSOR:           return @selector(IBeamCursor);
        case java_awt_Cursor_WAIT_CURSOR:           return @selector(javaBusyButClickableCursor);
        case java_awt_Cursor_SW_RESIZE_CURSOR:      return @selector(javaResizeSWCursor);
        case java_awt_Cursor_SE_RESIZE_CURSOR:      return @selector(javaResizeSECursor);
        case java_awt_Cursor_NW_RESIZE_CURSOR:      return @selector(javaResizeNWCursor);
        case java_awt_Cursor_NE_RESIZE_CURSOR:      return @selector(javaResizeNECursor);
        case java_awt_Cursor_N_RESIZE_CURSOR:       return @selector(resizeUpDownCursor);
        case java_awt_Cursor_S_RESIZE_CURSOR:       return @selector(resizeUpDownCursor);
        case java_awt_Cursor_W_RESIZE_CURSOR:       return @selector(resizeLeftRightCursor);
        case java_awt_Cursor_E_RESIZE_CURSOR:       return @selector(resizeLeftRightCursor);
        case java_awt_Cursor_HAND_CURSOR:           return @selector(pointingHandCursor);
        case java_awt_Cursor_MOVE_CURSOR:           return @selector(javaMoveCursor);
    }
    
    return nil;
}

static SEL getBuiltInCursorSelectorForType(JNIEnv *env, jint type) {
    SEL sel = lookupCursorSelectorForType(type);
    if (sel == nil) {
        [JNFException raise:env as:kIllegalArgumentException reason:"unimplemented built-in cursor type"];
    }
    
    if (![[NSCursor class] respondsToSelector:sel]) {
        [JNFException raise:env as:kNoSuchMethodException reason:"missing NSCursor selector"];
    }
    
    return sel;
}

JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CCursorManager_nativeSetBuiltInCursor
(JNIEnv *env, jclass class, jlong windowPtr, jint type)
{
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    SEL cursorSelector = getBuiltInCursorSelectorForType(env, type);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        [[[NSCursor class] performSelector:cursorSelector] set];
    }];

JNF_COCOA_EXIT(env);
}


JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CCursorManager_nativeGetCursorPosition
(JNIEnv *env, jclass class)
{
    jobject jpt = NULL;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    __block NSPoint pt = NSZeroPoint;
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        pt = ConvertNSScreenPoint(env, [NSEvent mouseLocation]);
    }];
    jpt = NSToJavaPoint(env, pt);

JNF_COCOA_EXIT(env);
    
    return jpt;
}
