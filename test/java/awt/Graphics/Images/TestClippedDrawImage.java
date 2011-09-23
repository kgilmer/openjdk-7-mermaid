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
 * @summary <rdar://problem/4105729> [JavaJDK14] NetBeans: Setting the clip of a Graphics to a GeneralShape gives visual blotches
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main TestClippedDrawImage
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

public class TestClippedDrawImage extends TestCase {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;
    
    private JFrame frame = null;

    protected void setUp() throws Exception {
        frame = new MyFrame();
        frame.setSize(WIDTH, HEIGHT);
        VisibilityValidator.setVisibleAndConfirm(frame);
    }


    public void testColor() throws Exception {
        String msg = "Test failed to match some of it colors. There is a chance the complex clip is busted";
        Thread.sleep(125); // Let humans see it...
        
        assertTrue(msg, VisibilityValidator.waitForColor(frame, Color.yellow));
        assertTrue(msg, VisibilityValidator.waitForColor(frame, 366, 105, Color.black));
        assertTrue(msg, VisibilityValidator.waitForColor(frame, 316, 64, Color.black));
        assertTrue(msg, VisibilityValidator.waitForColor(frame, 329, 93, Color.yellow));
        assertTrue(msg, VisibilityValidator.waitForColor(frame, 366, 125, Color.yellow));
    }

    protected void tearDown() {
        frame.dispose();
    }


    public static Test suite() {
        return new TestSuite(TestClippedDrawImage.class);
    }
    
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    
    public class MyFrame extends JFrame {
        public void paint(Graphics g) {
            int width = getWidth();
            int height = getHeight();
            g.setColor(Color.yellow);
            g.fillRect(0, 0, width, height);
            
            // Initialize the shape and image composite...
            GeneralPath shape = new GeneralPath();
            shape.moveTo(379,125);
            shape.lineTo(347,114);
            shape.lineTo(348,111);
            shape.lineTo(350,104);
            shape.lineTo(350,97);
            shape.lineTo(347,90);
            shape.lineTo(342,84);
            shape.lineTo(336,78);
            shape.lineTo(328,74);
            shape.lineTo(319,70);
            shape.lineTo(310,68);
            shape.lineTo(300,67);
            shape.lineTo(297,67);
            shape.lineTo(297,53);
            shape.lineTo(302,53);
            shape.lineTo(309,54);
            shape.lineTo(317,55);
            shape.lineTo(324,56);
            shape.lineTo(331,58);
            shape.lineTo(338,60);
            shape.lineTo(344,63);
            shape.lineTo(350,66);
            shape.lineTo(356,69);
            shape.lineTo(362,73);
            shape.lineTo(367,77);
            shape.lineTo(371,81);
            shape.lineTo(375,86);
            shape.lineTo(378,91);
            shape.lineTo(381,97);
            shape.lineTo(382,102);
            shape.lineTo(383,108);
            shape.lineTo(382,114);
            shape.lineTo(381,120);
            shape.closePath();  
    
            // Create a BufferedImage so we can create a composite image with a background
            //    and a foreground clipped to the shape(s)
            BufferedImage backgroundComposite = 
                        GraphicsEnvironment.
                        getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().
                        getDefaultConfiguration().
                        createCompatibleImage(width, height, Transparency.TRANSLUCENT);
            Graphics2D backG = backgroundComposite.createGraphics();
            
            BufferedImage blackImage = 
                        GraphicsEnvironment.
                        getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().
                        getDefaultConfiguration().
                        createCompatibleImage(width, height, Transparency.OPAQUE);
                        
            
            Shape oldClip = backG.getClip();
            backG.setClip(shape);
            backG.drawImage(blackImage, 0, 0, width, height, this);
            backG.setClip(oldClip);
            
            g.drawImage(backgroundComposite, 0, 0, width, height, this);        
        }
    }
}