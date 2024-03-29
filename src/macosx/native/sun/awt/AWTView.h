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

#import "CDragSource.h"
#import "CDropTarget.h"

@interface AWTView : NSView<NSTextInputClient> {
@private
    jobject m_cPlatformView;
    NSMenu * popupMenu;

    // Handler for the tracking rect needed for Enter/Exit events management.
    NSTrackingRectTag rolloverTrackingRectTag;

    // TODO: NSMenu *contextualMenu;

    // dnd support (see AppKit/NSDragging.h, NSDraggingSource/Destination):
    CDragSource *_dragSource;
    CDropTarget *_dropTarget;
    
    // Input method data
    jobject fInputMethodLOCKABLE;
    BOOL fKeyEventsNeeded;
    BOOL fProcessingKeystroke;
    
    BOOL fEnablePressAndHold;
    BOOL fInPressAndHold;
    BOOL fPAHNeedsToSelect;

    id cglLayer; // is a sublayer of view.layer
}

@property (nonatomic, retain) id cglLayer;

- (id) initWithRect:(NSRect) rect platformView:(jobject)cPlatformView windowLayer:(CALayer*)windowLayer;
- (void) deliverJavaMouseEvent: (NSEvent *) event;
- (void) resetTrackingRect;
- (void) deliverJavaKeyEventHelper: (NSEvent *) event;
- (void) setContextMenu:(NSMenu *)aMenu;
- (jobject) awtComponent:(JNIEnv *)env;

- (void) setDragSource:(CDragSource *)source;
- (void) setDropTarget:(CDropTarget *)target;


// Input method-related events
- (void)setInputMethod:(jobject)inputMethod;
- (void)abandonInput;

@end
