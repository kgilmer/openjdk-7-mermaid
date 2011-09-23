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
 * @test
 * @summary Simple test that draws an orange rectangle into an image, does a ColorConvertOp using ColorSpace.CS_GRAY and then tests for the converted color.
 * @summary This test is gonna vary based on the color space profile, therefore when we test for the expected color we are testing for a very wide range.
 * @summary This is supposed to catch big errors such as endianess issues.
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;

public class TestColorConvertOp extends TestCase {
    
    static final int        w = 400;
    static final int        h = 400;
    GraphicsEnvironment     ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice          gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration   gc = gd.getDefaultConfiguration();
    
    
    public void testColorConvertOp() {
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);     
        performTest(bi);
        
        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        performTest(bi);
        
        bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        performTest(bi);
        
        bi = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        performTest(bi);
    }
    
    public void performTest(BufferedImage bi) {
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.orange);
        g2d.fillRect(0, 0, w, h);
        
        ColorConvertOp grayOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        grayOp.filter(bi, bi);

        Color c = new Color(bi.getRGB(100, 100));
        
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
                
        int threshold = 10; // treshold for the color mismatch on difference machines
        boolean success = true;
        
        // with a gray transform the color space should be
        if (((r - threshold) > b) || ((r + threshold) < b)) {
            success = false;
        }
        
        if (((b - threshold) > g) || ((b + threshold) < g)) {
            success = false;
        }
        
        //System.err.println(" r= " + r + ", g= " + g + ", b= " + b + " success= " + success);      
        
        assertEquals( "There was an error with the ColorConvertOp", true, success);                
    }
    
    
    public static Test suite() {
        return new TestSuite(TestColorConvertOp.class);
    }
    
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}