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

package sun.font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import javax.swing.plaf.FontUIResource;

import sun.awt.FontConfiguration;
import sun.font.FontConfigManager;
import sun.font.FontUtilities;
import sun.font.SunFontManager;

public class CFontManager extends SunFontManager {

    private FontConfigManager fcManager = null;

    @Override
    protected FontConfiguration createFontConfiguration() {
        FontConfiguration fc = new CFontConfiguration(this);
        fc.init();
        return fc;
    }

    @Override
    public FontConfiguration createFontConfiguration(boolean preferLocaleFonts,
                                                     boolean preferPropFonts)
    {
        return new CFontConfiguration(this, preferLocaleFonts, preferPropFonts);
    }
    
    private static String[] defaultPlatformFont = null;


    /*
     * Returns an array of two strings. The first element is the
     * name of the font. The second element is the file name.
     */
    @Override
    public synchronized String[] getDefaultPlatformFont() {
        if (defaultPlatformFont == null) {
	    // TODO: not implemented
	    defaultPlatformFont = new String[2];
	    defaultPlatformFont[0] = "Dialog";
	    defaultPlatformFont[1] = "/dialog.ttf";
        }
        return defaultPlatformFont;
    }

    @Override
    public Font2D findFont2D(String name, int style, int fallback){
	// TODO: for now we skip font search and return to the Font.getFont2D()
	// some fake native font but still sufficient for metrics calculation.
        return new CFont("Arial");
    }
    
    /*
    public synchronized FontConfigManager getFontConfigManager() {
        if (fcManager  == null) {
            fcManager = new FontConfigManager();
        }
        return fcManager;
    }
    */

    @Override
    public String getFontPath(boolean noType1Fonts) {
	// TODO: not implemented
        return "/usr/X11R6/lib/X11/fonts/TrueType";
    }

    @Override
    protected FontUIResource getFontConfigFUIR(
            String family, int style, int size)
    {
        String mappedName = FontUtilities.mapFcName(family);
        if (mappedName == null) {
            mappedName = "sansserif";
        }
        return new FontUIResource(mappedName, style, size);
    }

    @Override
    protected void populateFontFileNameMap(
            HashMap<String,String> fontToFileMap,
            HashMap<String,String> fontToFamilyNameMap,
	    HashMap<String,ArrayList<String>>
	    familyToFontListMap,
	    Locale locale)
    {
        // TODO: not implemented
    }
}
