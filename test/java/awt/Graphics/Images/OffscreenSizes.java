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
 * @summary Tests offscreen images (VolatileImages and BufferedImages) of different sizes
 * @summary <rdar://problem/4566762> pbuffers with dimensions <= 32 don't work
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

public class OffscreenSizes extends TestCase {
    public static Test suite() {
        return new TestSuite(OffscreenSizes.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
        
    OffscreenSizesFrame f;
    public void setUp() {
        f = new OffscreenSizesFrame();
        f.setVisible(true);
    }
    
    public void test() throws Exception {
        Thread.sleep(1000);
        
        f.test();       
    }

    protected void tearDown() {
        f.dispose();
    }
    
    class OffscreenSizesFrame extends Frame {
        public OffscreenSizesFrame() {
            super("OffscreenSizesFrame");
            setSize(512, 512);
        }
        
        public void paint(Graphics g) {
            g.setColor(Color.red);
            g.fillRect(0, 0, 1000, 1000);
        }
        
        public void test() {
            int sizes[] = {1, 2, 3, 4, 7, 8, 15, 16, 31, 32, 63, 64, 127, 128, 511, 512, 1023, 1024};
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            
            for (int i=0; i<sizes.length; i++) {
                int size = sizes[i];
                VolatileImage vImage1 = createVolatileImage(size, size);
                Graphics2D vImage1G = vImage1.createGraphics();
                {
                    vImage1G.setColor(Color.green);
                    vImage1G.fillRect(0, 0, size, size);
                }
                vImage1G.dispose();
                
                VolatileImage vImage2 = createVolatileImage(size, size);
                Graphics2D vImage2G = vImage2.createGraphics();
                {
                    vImage2G.drawImage(vImage1, 0, 0, null);
                }
                vImage2G.dispose();
                
                BufferedImage bImage = gc.createCompatibleImage(size, size);
                Graphics2D bImageG = bImage.createGraphics();
                {
                    bImageG.drawImage(vImage2, 0, 0, null);
                }
                bImageG.dispose();
                
                int rgb = bImage.getRGB(0, 0);
                assertEquals("wrong color read back from the image", rgb, Color.green.getRGB());
                rgb = bImage.getRGB(size-1, size-1);
                assertEquals("wrong color read back from the image", rgb, Color.green.getRGB());
            }
        }
    }
}


