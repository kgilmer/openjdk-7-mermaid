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

#import <AppKit/AppKit.h>
#import <CoreText/CTFont.h>

#import "CoreTextSupport.h"

// Basic struct that holds everything CoreText is interested in
typedef struct CTS_ProviderStruct {
    const UniChar         *unicodes;
    CFIndex                length;
    CFMutableDictionaryRef attributes;
} CTS_ProviderStruct;

/*
 * Callback for CoreText which uses the CoreTextProviderStruct to
 * feed CT UniChars.  We only use it for one-off lines, and don't
 * attempt to fragment our strings.
 */
const UniChar *
CTS_Provider(CFIndex stringIndex, CFIndex *charCount,
             CFDictionaryRef *attributes, void *refCon)
{
    // if we have a zero length string we can just return NULL for the string
    // or if the index anything other than 0 we are not using core text
    // correctly since we only have one run.	
    if (stringIndex != 0) {
        return NULL;
    }
	
    CTS_ProviderStruct *ctps = (CTS_ProviderStruct *)refCon;
    *charCount = ctps->length;
    *attributes = ctps->attributes;
    return ctps->unicodes;
}


#pragma mark --- Retain/Release CoreText State Dictionary ---

/*
 * Gets a Dictionary filled with common details we want to use for CoreText
 * when we are interacting with it from Java.
 */
static inline CFMutableDictionaryRef
GetCTStateDictionaryFor(const NSFont *font, BOOL useFractionalMetrics)
{
    NSNumber *gZeroNumber = [NSNumber numberWithInt:0];
    NSNumber *gOneNumber = [NSNumber numberWithInt:1];
	
    CFMutableDictionaryRef dictRef = (CFMutableDictionaryRef)
        [[NSMutableDictionary alloc] initWithObjectsAndKeys:
        font, NSFontAttributeName, 
        // TODO(cpc): following attribute is private...
        //gOneNumber,  (id)kCTForegroundColorFromContextAttributeName,
        // force integer hack in CoreText to help with Java integer assumptions
        useFractionalMetrics ? gZeroNumber : gOneNumber, @"CTIntegerMetrics",
        gZeroNumber, NSLigatureAttributeName, 
        gZeroNumber, NSKernAttributeName, 
        NULL];
    CFRetain(dictRef); // GC
    [(id)dictRef release];
    
    return dictRef;
}

/*
 * Releases the CoreText Dictionary - in the future we should hold on
 * to these to improve performance.
 */
static inline void
ReleaseCTStateDictionary(CFDictionaryRef ctStateDict)
{
    CFRelease(ctStateDict); // GC
}

static CGFontRef
GetCTFallbackFont(const NSFont *nsFont, const UniChar *uniChar)
{
    CFStringRef str = CFStringCreateWithCharacters(NULL, uniChar, 1);
    
    CTFontRef fallbackFont =
        CTFontCreateForString((CTFontRef)nsFont, str, CFRangeMake(0, 1));
    CFRelease(str);
    
    CGFontRef fallbackCGFont = CTFontCopyGraphicsFont(fallbackFont, NULL);
    CFRelease(fallbackFont);
    
    [(id)fallbackCGFont autorelease];
    return fallbackCGFont;
}

/*
 * Pass-though to get individual glyphs for unicode values, given
 * this particular font (no hot-character-substitution).
 */
static void
CTS_GetGlyphsForCharacters(const NSFont *font,
                           const UniChar u[], CGGlyph glyphs[], size_t count)
{
    CTFontGetGlyphsForCharacters((CTFontRef)font, u, glyphs, count);
}

/*	
 * Transform Unicode characters into glyphs.
 *
 * Fills the "glyphsAsInts" array with the glyph codes for the current font,
 * or the negative unicode value if we know the character can be
 * hot-substituted.
 *
 * This is the heart of "Universal Font Substitution" in Java.
 */
void
CTS_GetGlyphsAsIntsForCharacters(const NSFont *font, const UniChar unicodes[],
                                 CGGlyph glyphs[], jint glyphsAsInts[],
                                 const size_t count)
{
    CTS_GetGlyphsForCharacters(font, unicodes, glyphs, count);

    size_t i;
    for (i = 0; i < count; i++) {
        CGGlyph glyph = glyphs[i];
        if (glyph > 0) {
            glyphsAsInts[i] = glyph;
            continue;
        }
	
        UniChar unicode = unicodes[i];
        // TODO(cpc): do we really need to use GetCTFallbackFont here,
        // or is the given NSFont sufficient for getting what we need?
        //cgFont = GetCTFallbackFont(font, &unicode);
        CTS_GetGlyphsForCharacters(font, &unicode, &glyph, 1);
        
        if (glyph > 0) {
            // set the glyph code to the negative unicode value
            glyphsAsInts[i] = -unicode;
        } else {
            // CoreText couldn't find a glyph for this character either
            glyphsAsInts[i] = 0;
        }
    }
}

/*
 * Retrieves advances for translated unicodes.
 * Uses "glyphs" as a temporary buffer for the glyph-to-unicode translation.
 */
void
CTS_GetAdvancesForUnichars(const NSFont *font, const CGAffineTransform *tx,
                           const CGFS_FontRenderingMode mode,
                           const UniChar uniChars[], CGGlyph glyphs[],
                           const size_t length, CGSize advances[])
{
    // cycle over each spot, and if we discovered a unicode to substitute,
    // we have to calculate the advance for it
    size_t i;
    for (i = 0; i < length; i++) {
        UniChar uniChar = uniChars[i];
        if (uniChar == 0) {
            continue;
        }
        
        CGFontRef cgFont = GetCTFallbackFont(font, &uniChar);
	if (cgFont) {
            CFRetain(cgFont);
	}
        // WORKAROUND: passing size should not be necessary once we move to JRS
	CGFloat size = [font pointSize];
        CGGlyph glyph;
        CTS_GetGlyphsForCharacters(font, &uniChar, &glyph, 1);
        CGFS_GetAdvancesForGlyphs(cgFont, size,
                                  tx, mode, &glyph, 1, &(advances[i]));
        glyphs[i] = glyph;
    }
}

/*
 * Returns the corresponding CGFont for a given NSFont.
 */
CGFontRef
CTS_GetCGFontForNSFont(const NSFont *font)
{
    return CTFontCopyGraphicsFont((CTFontRef)font, NULL);
}

/*
 * Translates a Unicode into a CGGlyph/CGFontRef pair.
 * Returns the substituted font, and places the appropriate glyph
 * into "glyphRef".
 */
CGFontRef
CTS_GetFontAndGlyphForUnicode(const NSFont *font,
                              const UniChar *uniCharRef, CGGlyph *glyphRef)
{
    CGFontRef cgFont = GetCTFallbackFont(font, uniCharRef);
    if (cgFont) {
        CFRetain(cgFont);
    }
    CTS_GetGlyphsForCharacters(font, uniCharRef, glyphRef, 1);
    return cgFont;
}

/*
 * Translates a Java glyph code int (might be a negative unicode value) 
 * into a CGGlyph/CGFontRef pair.
 * Returns the substituted font, and places the appropriate glyph into "glyph".
 */
CGFontRef
CTS_GetFontAndGlyphForJavaGlyphCode(const NSFont *font,
                                    const jint glyphCode,
                                    const CGFontRef cgFont,
                                    CGGlyph *glyphRef)
{
    // negative glyph codes are really unicodes, which were placed there
    // by the mapper to indicate we should use CoreText to substitute
    // the character
    if (glyphCode >= 0) {
        *glyphRef = glyphCode;
        return cgFont;
    }
    
    UniChar uniChar = -glyphCode;
    return CTS_GetFontAndGlyphForUnicode(font, &uniChar, glyphRef);
}
