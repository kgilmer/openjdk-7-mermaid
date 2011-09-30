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
 * @summary Testing drawGlyphVector() with kinks
 * @summary com.apple.junit.java.text.GlyphVector;
 */

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

public class DrawGlyphVectorWorking extends TestCase {
    
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
    
    public static TestSuite suite() {
        return new TestSuite(DrawGlyphVectorWorking.class);
    }
    
    public void testDrawGlyphVector() throws Exception {
        final JFrame frame = new JFrame("Testing drawGlyphVector() with kinks");
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.getContentPane().add(new JPanel() {
                    public void paint(final Graphics g) {
                        final Graphics2D g2d = (Graphics2D)g;
                        super.paint(g2d);
                        
                        g2d.translate(100, 50);
                        g2d.rotate(0.2);
                        
                        final Font font = new Font("Lucida Grande", Font.ITALIC, 18);
                        g2d.setFont(font);
                        
                        final FontRenderContext frc = g2d.getFontRenderContext();
                        final GlyphVector gv = font.createGlyphVector(frc, "Kinking the Glyph Vector");
                        
                        gv.setGlyphTransform(2, AffineTransform.getRotateInstance(0.5));
                        gv.setGlyphTransform(5, AffineTransform.getRotateInstance(-0.25));
                        gv.setGlyphTransform(12, AffineTransform.getScaleInstance(2.5, 2.5));
                        
                        g2d.fill(gv.getOutline());
                        g2d.drawGlyphVector(gv, 0, 100);
                    }
                });
                
                frame.setSize(600, 300);
                frame.setVisible(true);
            }
        });
        
        (new Robot()).waitForIdle();
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.dispose();
            }
        });
    }
}
