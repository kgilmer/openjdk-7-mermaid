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

#import "sun_awt_SunHints.h"
#import "CGFontSupport.h"

CGFS_FontRenderingMode
CGFS_GetFontRenderingModeForHints(const jint fmHint, const jint aaHint)
{
    if (aaHint == sun_awt_SunHints_INTVAL_TEXT_ANTIALIAS_OFF) {
        return 0;
    }

    // TODO: not implemented
    return 0;
}

CGFS_FontRenderingMode
CGFS_AlignModeForMeasurement(CGFS_FontRenderingMode mode)
{
    // TODO: not implemented
    return 0;
}

CGFS_FontRenderingMode
CGFS_AlignModeForFractionalMeasurement(const CGFS_FontRenderingMode mode)
{
    // TODO: not implemented
    return 0;
}

bool
CGFS_FontIsUsingFractionalMetrics(CGFS_FontRenderingMode mode)
{
    // TODO: not implemented
    return 0;
}

bool
CGFS_FontModeIsAntiAliased(CGFS_FontRenderingMode mode)
{
    // TODO: not implemented
    return 0;
}

void
CGFS_SetFontRenderingModeOnContext(CGContextRef context,
                                   CGFS_FontRenderingMode mode)
{
    // TODO: not implemented
}


bool
CGFS_GetAdvancesForGlyphs(const CGFontRef font, const CGFloat size,
                          const CGAffineTransform *tx,
                          const CGFS_FontRenderingMode mode,
                          const CGGlyph glyphs[], size_t count,
                          CGSize advances[])
{
    // TODO: not implemented
    // TODO(cpc): style and tx are ignored here...
    CTFontRef ctFontRef =
        CTFontCreateWithGraphicsFont(font, size, NULL, NULL);
    CTFontGetAdvancesForGlyphs(ctFontRef, kCTFontDefaultOrientation,
                               glyphs, advances, count);
    CFRelease(ctFontRef);
    return TRUE;
}

// WORKAROUND: passing size should not be necessary once we move to JRS
bool
CGFS_GetBBoxesForGlyphs(const CGFontRef font, const CGFloat size,
                        const CGAffineTransform *tx,
                        const CGFS_FontRenderingMode mode,
                        const CGGlyph glyphs[], size_t count, CGRect bboxes[])
{
    // TODO: implemented
    // TODO(cpc): style and tx are ignored here...
    CTFontRef ctFontRef =
        CTFontCreateWithGraphicsFont(font, size, NULL, NULL);
    CTFontGetBoundingRectsForGlyphs(ctFontRef, kCTFontDefaultOrientation,
                                    glyphs, bboxes, count);
    CFRelease(ctFontRef);
    return TRUE;
}
