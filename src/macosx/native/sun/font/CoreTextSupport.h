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

#import <AppKit/NSFont.h>
#import <JavaVM/jni.h>

#include "CGFontSupport.h"

/*	
 * Transform Unicode characters into glyphs.
 *
 * Fills the "glyphsAsInts" array with the glyph codes for the current
 * font, or the negative unicode value if we know the character can
 * be hot-substituted.
 *
 * This is the heart of "Universal Font Substitution" in Java.
 */
void
CTS_GetGlyphsAsIntsForCharacters(const NSFont *font,
                                 const UniChar unicodes[],
                                 CGGlyph glyphs[],
                                 jint glyphsAsInts[], const size_t count);

/*
 * Translates a Java glyph code int (might be a negative unicode value)
 * into a CGGlyph/CGFontRef pair.  Returns the substituted font, and
 * places the appropriate glyph into "glyph".
 */
CGFontRef
CTS_GetFontAndGlyphForJavaGlyphCode(const NSFont *font,
                                    const jint glyphCode,
                                    const CGFontRef cgFont,
                                    CGGlyph *glyphRef);

/*
 * Translates a Unicode into a CGGlyph/CGFontRef pair.  Returns the
 * substituted font, and places the appropriate glyph into "glyphRef".
 */
CGFontRef
CTS_GetFontAndGlyphForUnicode(const NSFont *font,
                              const UniChar *uniCharRef,
                              CGGlyph *glyphRef);

/*
 * Retrieves advances for translated unicodes.  Uses "glyphs" as a
 * temporary buffer for the glyph-to-unicode translation.
 */
void
CTS_GetAdvancesForUnichars(const NSFont *font,
                           const CGAffineTransform *tx,
                           const CGFS_FontRenderingMode mode,
                           const UniChar uniChars[],
                           CGGlyph glyphs[], const size_t length,
                           CGSize advances[]);

/*
 * Returns the corresponding CGFont for a given NSFont.
 */
CGFontRef
CTS_GetCGFontForNSFont(const NSFont *font);
