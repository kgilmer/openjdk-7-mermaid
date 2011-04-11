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

#ifndef _AWTWINDOW_H
#define _AWTWINDOW_H

#import <jni.h>
#import <AppKit/NSWindow.h>

#import "CMenuBar.h"
#import "LWCToolkit.h"

@class AWTView, AWTWindowDelegate;

@interface AWTWindow : NSWindow {
@private
    NSView *m_view;
    CMenuBar *_menuBar;
    jobject m_cPlatformWindow;
    NSSize _minSize;
}

@property (nonatomic, retain) NSView *m_view;
@property (nonatomic, retain) CMenuBar *_menuBar;
@property (nonatomic) jobject m_cPlatformWindow;
@property (nonatomic) NSSize _minSize;

- (id)initWithContentRect:(NSRect)contentRect
                styleMask:(NSUInteger)windowStyle
                  backing:(NSBackingStoreType)bufferingType
                    defer:(BOOL)deferCreation
           platformWindow:(jobject) cPlatformWindow
              contentView:(NSView *)contentView;

- (jobject) cPlatformWindow;

@end

#endif _AWTWINDOW_H
