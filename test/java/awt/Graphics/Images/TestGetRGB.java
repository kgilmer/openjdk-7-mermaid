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
 * @summary Simple test that draws couple of rectangle and then does getRGB() to verify that the correct color is returned.
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class TestGetRGB extends TestCase {
    
    static final int        w = 400;
    static final int        h = 400;
    GraphicsEnvironment     ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice          gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration   gc = gd.getDefaultConfiguration();
    BufferedImage           bi = gc.createCompatibleImage( w, h );
    
    
    public void testGetRGB() {
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.gray);
        g2d.fillRect(0, 0, 400, 400);
        g2d.setColor(Color.red);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.green);
        g2d.fillRect(110, 0, 100, 100);
        
        assertEquals( "Testing for red at 10, 10", Color.red, new Color(bi.getRGB(10, 10)));
        assertEquals( "Testing for gray at 105, 10", Color.gray, new Color(bi.getRGB(105, 10)));
        assertEquals( "Testing for green at 150, 10", Color.green, new Color(bi.getRGB(150, 10)));
                
        g2d.setColor(Color.blue);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.black);
        g2d.fillRect(110, 0, 100, 100);

        assertEquals( "Testing for blue at 10, 10", Color.blue, new Color(bi.getRGB(10, 10)));
        assertEquals( "Testing for black at 150, 10", Color.black, new Color(bi.getRGB(150, 10)));
    }
    
    
    public static Test suite() {
        return new TestSuite(TestGetRGB.class);
    }
    
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}