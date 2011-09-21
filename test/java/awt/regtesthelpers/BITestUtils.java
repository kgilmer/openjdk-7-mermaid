/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @summary A collection of utilities for testing the contents of BufferedImages
 * @summary com.apple.junit.utils
 */

package test.java.awt.regtesthelpers;

import java.awt.image.BufferedImage;
import java.io.*;

public class BITestUtils {

    //
    //    A set of functions for determining the location
    //      of a drawing inside a buffered image.  These
    //      methods look for white pixel.
    //
    
    /* Get location of top white pixel */
    
    public static int getTopMost(BufferedImage bi) {
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgb = bi.getRGB(x, y);
                rgb = rgb & 0x00ffffff;
                if (rgb == 0x00ffffff) {
                    return(y);
                }
            }
        }
        return (-1);
    }

    /* Get location of bottom white pixel */

    public static int getBottomMost(BufferedImage bi) {
        for (int y = bi.getHeight()-1; y >= 0; y--) {
            for (int x = 0; x < bi.getWidth(); x++) {
                int rgb = bi.getRGB(x, y);
                rgb = rgb & 0x00ffffff;
                if (rgb == 0x00ffffff) {
                    return(y);
                }
            }
        }
        return (-1);
    }

    /* Get location of leftmost white pixel */

    public static int getLeftMost(BufferedImage bi) {
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int rgb = bi.getRGB(x, y);
                rgb = rgb & 0x00ffffff;
                if (rgb == 0x00ffffff) {
                    return(x);
                }
            }
        }
        return (-1);
    }

    /* Get location of rightmost white pixel */

    public static int getRightMost(BufferedImage bi) {
        for (int x = bi.getWidth()-1; x >=0 ; x--) {
            for (int y = 0; y < bi.getHeight(); y++) {
                int rgb = bi.getRGB(x, y);
                rgb = rgb & 0x00ffffff;
                if (rgb == 0x00ffffff) {
                    return(x);
                }
            }
        }
        return (-1);
    }


    //
    //    A collection of methods for displaying the contents
    //    of BufferedIages in human readable form.
    //

    public static void pixelDisplay(BufferedImage bi, String name)
    {
        System.err.println(name + " buffered image");
        for (int y = 0; y < bi.getHeight(); y++)
        {
            System.err.print("  ");
            for (int x = 0; x < bi.getWidth(); x++)
            {
                int rgb = bi.getRGB(x, y);
                rgb = rgb & 0x00ffffff;
                String c = (rgb == 0xffffff ? "X" : (rgb == 0x000000 ? "." : "?"));
                System.err.print(c + " ");
            }
            System.err.println("");
        }
    
    }

    static void pixelDisplayInt(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y+=1) {
            for (int x = 0; x < width; x+=1) {
                System.out.print( img.getRGB(x,y) + ", ");
            }
            System.out.println();
        }
    }

    static void pixelDisplayHex(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y+=1) {
            for (int x = 0; x < width; x+=1) {
                int pixelValue = img.getRGB(x,y);
                System.out.print( "0x" + Hex(pixelValue) + ", ");
            }
            System.out.println();
        }
    }

    public static void pixelLogfileHex(BufferedImage img, String filename) throws Exception {
        File datafile = File.createTempFile(filename, ".txt");
        PrintStream streamer =    new PrintStream( new FileOutputStream( datafile ), true, "UTF-16");
        System.out.println("Creating log file :" + datafile.getCanonicalPath() );

        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y+=1) {
            for (int x = 0; x < width; x+=1) {
                int pixelValue = img.getRGB(x,y);
                streamer.print( "0x" + Hex(pixelValue) + ", ");
            }
            streamer.println();
        }

    }


    // Note that this is used to '0' pad an integer properly
    static final String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
    public static String Hex( int x ) {
        String s = "";
        for (int i = 0; i < 8; i += 1) {
            s = hexDigits[x & 0xF] + s;
            x = x >> 4;
        }
        return s;
    }

}
