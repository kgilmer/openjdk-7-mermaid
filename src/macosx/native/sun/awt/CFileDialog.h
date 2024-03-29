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

@interface CFileDialog : NSObject <NSOpenSavePanelDelegate> {
    // Should we query back to Java for a file filter?
    jboolean fHasFileFilter;

    // sun.awt.CFileDialog
    jobject fFileDialog;
    
    // Return value from dialog
    NSInteger fPanelResult;

    // Dialog's title
    NSString *fTitle;
    
    // Starting directory and file
    NSString *fDirectory;
    NSString *fFile;

    // File dialog's mode
    jint fMode;

    // Should we navigate into apps?
    BOOL fNavigateApps;
    
    // panel's filename
    NSString *fReturnedFilename;
}

// Allocator
- (id) initWithFilter:(jboolean)inHasFilter
           fileDialog:(jobject)inDialog
                title:(NSString *)inTitle
            directory:(NSString *)inPath
                 file:(NSString *)inFile
                 mode:(jint)inMode
       shouldNavigate:(BOOL)inNavigateApps
              withEnv:(JNIEnv*)env;

// Invoked from the main thread
- (void) safeSaveOrLoad;

// Get dialog return value
- (BOOL) userClickedOK;

// Filename user chose
- (NSString *) filename;

@end
