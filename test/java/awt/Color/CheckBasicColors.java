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
 @test
 @summary Check each of the labels for screen color matching real color.
 @summary com.apple.junit.java.graphics.color;
 @library ../regtesthelpers
 @build VisibilityValidator
 @run main CheckBasicColors
 */

import junit.framework.*;

import java.awt.*;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class CheckBasicColors extends TestCase {
    
        public static Test suite() 
        {
            return new TestSuite(CheckBasicColors.class);
        }
        
        public static void main (String[] args) throws RuntimeException {
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected errors or failures.");
            }
        }
    
        static Color[] colors =     {
            Color.CYAN ,
            Color.DARK_GRAY ,
            Color.GRAY ,
            Color.GREEN ,
            Color.LIGHT_GRAY ,
            Color.MAGENTA ,
            Color.ORANGE ,
            Color.PINK ,
            Color.RED ,
            Color.WHITE ,
            Color.YELLOW,
            new Color( 17, 23, 123),
            new Color( 0xF0, 0x0F, 0x03),        
            new Color( 0x03, 0xF0, 0x03),        
            new Color( 0x03, 0x03, 0xF0),        
            new Color( 3, 0, 0),        
            new Color( 0, 3, 0),        
            new Color( 0, 0, 3),        
            new Color( 0xFB, 0xFC, 0xFD),
            new Color( 251, 253, 252),
        };

        public void testOnScreenColor() throws Exception {
            Frame f = new Frame();
            try {
                f.setSize(300, 200);
                f.setLayout( new GridLayout( ((colors.length) / 4) + 1, 4, 10, 10 ) );        
                
                // Toss a bunch of colored labels into our frame
                Label[] labels = new Label[colors.length];
                for (int i = 0; i < colors.length; i+=1) {
                    labels[i] = new Label();
                    labels[i].setBackground(colors[i]);
                    f.add(labels[i]);
                }
    
                // Just for fun, center frame
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension frameSize = f.getSize();
                int fx = (screenSize.width - frameSize.width) / 2;
                int fy = (screenSize.height - frameSize.height) / 2;
                f.setLocation( fx, fy );
                
                VisibilityValidator.setVisibleAndConfirm(f);
                
                // Sometimes we still need a few millis for the screen refresh
                Thread.sleep(12500);
                
                // Check each of the labels for screen color matching real color
                for (int i = 0; i < colors.length; i+=1) {
                    Label l = labels[i];
                    System.out.println("labels[" + i + "]: " + labels[i].getBounds());
                    Color c = colors[i];
                    assertTrue("Onscreen color should match color set as background: " + c, VisibilityValidator.waitForColor(l, c));
                }
            }
            finally {
                f.dispose();
            }
        }
}
