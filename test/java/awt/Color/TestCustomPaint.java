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
 * @summary Asserts the proper behavior of a custom paint context.
 * @summary com.apple.junit.java.graphics.color
 * @library ../regtesthelpers
 * @build VisibilityValidator
 * @run main TestCustomPaint
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;

/**
* 
*/
public class TestCustomPaint extends TestCase {

    private JFrame frame = null;

    protected void setUp() {
        frame = new MyFrame();
        frame.setSize(200, 100);
    }


    public void testColor() throws Exception {
        VisibilityValidator.setVisibleAndConfirm(frame);
        Thread.sleep(125); // Let humans see it too...
        assertTrue("Timed out without seeing our custom paint background", VisibilityValidator.waitForColor(frame, 50, 50, Color.red));
    }

    protected void tearDown() {
           frame.dispose();
        }

    public static Test suite() {
        return new TestSuite(TestCustomPaint.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public class MyFrame extends JFrame {
        public void paint(Graphics g) {
            ((Graphics2D) g).setPaint(new MyPaint());
            g.fillRect(0, 0, 100, 100);
        }
    }
    
    private class MyPaint implements Paint {
        public PaintContext createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
            return new MyPaintContext();
        }
        
        public int getTransparency() {
            return Transparency.OPAQUE;
        }
    }
    
    private class MyPaintContext implements PaintContext {
        public void dispose() {}
  
        public ColorModel getColorModel() { 
            return ColorModel.getRGBdefault(); 
        }
  
        public Raster getRaster(int x, int y, int w, int h) {
            WritableRaster raster = getColorModel().createCompatibleWritableRaster(w, h);
            int[] data = new int[w * h * 4];
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int base = (j * w + i) * 4;
                    data[base + 0] = 255;
                    data[base + 1] = 0;
                    data[base + 2] = 0;
                    data[base + 3] = 255;
                }
            }
            raster.setPixels(0, 0, w, h, data);
            return raster;
        }
    }
}