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
    pthread_mutex_t _lock;
    NSView * m_view;
    CMenuBar * _menuBar;
    AWTWindowDelegate * m_delegate;
    jobject m_cPlatformWindow;
    NSSize _minSize;
}

- (id)initWithContentRect:(NSRect)contentRect
                styleMask:(NSUInteger)windowStyle
                  backing:(NSBackingStoreType)bufferingType
                    defer:(BOOL)deferCreation
           platformWindow:(jobject) cPlatformWindow
              contentView:(NSView *)contentView;

- (jobject) cPlatformWindow;
- (void) setMinSizeImpl: (NSSize) minSize;
- (void) setResizable: (BOOL)resizable;
- (void) setAlwaysOnTop: (BOOL)isAlwaysOnTop;

/*****************************************************
 * Methods that make all Cocoa calls on AppKit thread.
 ******************************************************/

//NSRect:bounds, BOOL:display
- (void) _setBounds_OnAppKitThread:(NSArray *)args;

//Takes UInteger style, jobject cPlatformWindow, x, y, width, height
- (void) _createAWTWindow_OnAppKitThread: (NSMutableArray *)argValue;

- (void) _setResizable_OnAppKitThread: (NSNumber *)mayResize;

- (void) _setMinSize_OnAppKitThread: (NSValue *) minSize;

- (void) setMenuBar: (CMenuBar *)menuBar;

- (void) _setMenuBar_OnAppKitThread: (CMenuBar *)menuBar;

- (void) _setAlwaysOnTop_OnAppKitThread: (NSNumber *)isAlwaysOnTop;

@end
#endif _AWTWINDOW_H
