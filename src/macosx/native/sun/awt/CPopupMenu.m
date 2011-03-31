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

#include <jni.h>
#import <AppKit/NSGraphics.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "AWTWindow.h"
#import "CPopupMenu.h"
#import "ThreadUtilities.h"
#import "LWCToolkit.h"

@implementation CPopupMenu

- (id) initWithPeer:(jobject)peer {
    self = [super initWithPeer:peer];
    if (self == nil) {
        // TODO: not implemented
    }
    return self;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"CMenuItem[ %@ ]", fMenuItem];
}

@end // implementationCPopupMenu : CMenu

#ifndef _Included_apple_awt_CPopupMenu
#define _Included_apple_awt_CPopupMenu
#ifdef __cplusplus
extern "C" {
#endif

  /*
   * Class:     sun_lwawt_macosx_CPopupMenu
   * Method:    nativeCreatePopupMenu
   * Signature: (JII)J
   */
JNIEXPORT jlong JNICALL Java_sun_lwawt_macosx_CPopupMenu_nativeCreatePopupMenu
(JNIEnv *env, jobject peer, jlong awtWindowPtr, jint x, jint y){
    AR_POOL(pool);

    AWTWindow *awtWindow = OBJC(awtWindowPtr);
    CPopupMenu *aCPopupMenu = nil;

    jobject cPeerObjGlobal = (*env)->NewGlobalRef(env, peer);

    // We use an array here only to be able to get a return value
    NSMutableArray *args = [[NSMutableArray alloc]
                               initWithObjects:[NSValue valueWithBytes:&cPeerObjGlobal objCType:@encode(jobject)], nil];

    [ThreadUtilities performOnMainThread:@selector(_create_OnAppKitThread:)
                                              onObject:[CPopupMenu alloc] withObject:args waitUntilDone:YES awtMode:NO];

    aCPopupMenu = (CMenuItem *)[args objectAtIndex: 0];

    if (aCPopupMenu == nil) {
        return 0L;
    }

    [[awtWindow contentView] setContextMenu:[aCPopupMenu menu]];
    [pool drain];
    return ptr_to_jlong(aCPopupMenu);
}


#ifdef __cplusplus
}
#endif
#endif
