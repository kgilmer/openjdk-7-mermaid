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

/**
 @test
 @summary Simple test for using derivedFonts
 @summary com.apple.junit.java.awt.Font;
 @run main FractionalFont01
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.GlyphVector;

public class FractionalFont01 extends TestCase {
    static final int numSizes = 10;
    volatile boolean painted = false;
    volatile float[] sizes = new float[numSizes];

    public static Test suite() {
        return new TestSuite(FractionalFont01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test uses a font that may not exist on other platforms
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
    
    public void testFractionalMetrics() throws Exception {
        JFrame f = new JFrame("FontTest");
        // front and center
        f.setLocation(f.getToolkit().getScreenSize().width / 4, f.getToolkit().getScreenSize().height / 4);
        f.setSize(new Dimension(f.getToolkit().getScreenSize().width / 2, f.getToolkit().getScreenSize().height / 2));

        try {
            f.getContentPane().setLayout(new BorderLayout());
            FontTestPanel fontC = new FontTestPanel();
            fontC.setBackground(Color.white);
            f.getContentPane().add(BorderLayout.CENTER, fontC);
            f.setVisible(true);

            // wait for a paint call
            int count = 0;
            while (painted == false && count < 10) {
                Thread.sleep(500);
                count += 1;
            }

            assertTrue("Should have painted by now", painted);
            // loop through the sizes and confirm that each is larger than the last
            for (int i = 1; i < numSizes; i++) {
                assertTrue("Expected a non-zero size string", sizes[i] > 0);
                assertTrue("Expected a wider string when using a wider fractional font", sizes[i] > sizes[i - 1]);
            }
        } finally {
            f.dispose();
        }
    }

    class FontTestPanel extends Canvas {
        Font thisFont;

        public FontTestPanel() {
            thisFont = new Font("Arial", Font.PLAIN, 12);
            thisFont = thisFont.deriveFont(Font.PLAIN, 12.5f);
        }

        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.blue);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            int x = 10;
            int y = 20;
            float currY = y;
            for (int i = 0; i < numSizes; i++) {
                float fontSize = 12f + (float) i / 10;
                currY += fontSize + 5;
                String fullText = "Text width with " + fontSize + " pixel font = ";
                
                thisFont = thisFont.deriveFont(Font.PLAIN, fontSize);
                GlyphVector msg = thisFont.createGlyphVector(g2.getFontRenderContext(), fullText);
                g2.drawGlyphVector(msg, x, currY);
                
                float width = (float) msg.getLogicalBounds().getBounds2D().getWidth();
                msg = thisFont.createGlyphVector(g2.getFontRenderContext(), Float.toString(width));
                g2.drawGlyphVector(msg, x + width, currY);
                sizes[i] = width;
                painted = true;
            }
        }
    }
}
