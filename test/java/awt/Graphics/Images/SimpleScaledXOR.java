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
 * @summary Very simple test of RoundRect functionality.
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SimpleScaledXOR extends TestCase {

    public static Test suite() {
        return new TestSuite(SimpleScaledXOR.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    static final int                w = 20;
    static final int                h = 10;
    static final Rectangle  r = new Rectangle(0,0,w,h);

    protected BufferedImage             bi = null;

    static final int[] expectedRGB = {
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -16711681, -16711681, -16711681, -16711681, -16711681, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, 
            -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536, -65536
    };



    public void setUp() {
        GraphicsEnvironment     ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice          gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration   gc = gd.getDefaultConfiguration();
        bi = gc.createCompatibleImage( w, h );
    }

    void initBaseImage(BufferedImage buffIm) {
        Graphics2D ig = (Graphics2D) buffIm.createGraphics();
        ig.setColor(Color.red);
        ig.fill( r );
        ig.scale ( 0.5, 0.5 );
        ig.setColor(Color.white);
        ig.setXORMode(Color.black);
        ig.fill( r );
        ig.dispose();
    }

    void pixelCheck(BufferedImage img) throws Exception {
        assertNotNull( img );
        int width = img.getWidth();
        int height = img.getHeight();
        int index = 0;
        for (int x = 0; x < width; x+=1) {
            for (int y = 0; y < height; y+=1) {
                assertEquals("Unexpected results after scaled xor at x="+x+" y="+y, img.getRGB(x,y) , expectedRGB[index] );
                index++;
            }
        }
    }

    public void testScaledXOR() throws Exception {
        initBaseImage(bi);
        pixelCheck(bi);
    }
}   




