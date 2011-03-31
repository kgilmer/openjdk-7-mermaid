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

#import <ApplicationServices/ApplicationServices.h>
#import <JavaVM/jni.h>

// TODO(cpc): using jint until we provide a common struct in JRS.framework
#if 0
typedef CGFontRenderingStyle _CGFS_FontRenderingMode;
#else
typedef jint _CGFS_FontRenderingMode;
#endif
typedef _CGFS_FontRenderingMode CGFS_FontRenderingMode;


// provides the desired text rendering mode for these hints
inline CGFS_FontRenderingMode
CGFS_GetFontRenderingModeForHints(const jint fmHint, const jint aaHint);

// provides an altered mode which is more appropriate for measuring,
// but not striking glyphs
inline CGFS_FontRenderingMode
CGFS_AlignModeForMeasurement(const CGFS_FontRenderingMode mode);

// provides an altered mode which adds fractional components for measuring,
// but not for striking
inline CGFS_FontRenderingMode
CGFS_AlignModeForFractionalMeasurement(const CGFS_FontRenderingMode mode);

// checks this mode represents character strike that would return a
// non-integer size
inline bool
CGFS_FontIsUsingFractionalMetrics(const CGFS_FontRenderingMode mode);

// checks if this the mode supports antialiasing
inline bool
CGFS_FontModeIsAntiAliased(const CGFS_FontRenderingMode mode);

// set this text rendering mode onto this context
inline void
CGFS_SetFontRenderingModeOnContext(const CGContextRef context,
                                   const CGFS_FontRenderingMode mode);


// pass-though to get an array of advances for some glyphs, given this mode
// WORKAROUND: passing size should not be necessary once we move to JRS
inline bool
CGFS_GetAdvancesForGlyphs(const CGFontRef font, const CGFloat size,
                          const CGAffineTransform *tx,
                          const CGFS_FontRenderingMode mode,
                          const CGGlyph glyphs[], size_t count,
                          CGSize advances[]);

// pass-though to get an array of bounding boxes for some glyphs,
// given this mode
// WORKAROUND: passing size should not be necessary once we move to JRS
inline bool
CGFS_GetBBoxesForGlyphs(const CGFontRef font, const CGFloat size,
                        const CGAffineTransform *tx,
                        const CGFS_FontRenderingMode mode,
                        const CGGlyph glyphs[], size_t count, CGRect bboxes[]);
