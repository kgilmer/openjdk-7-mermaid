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
 * @summary <rdar://problem/3597830> graphics.drawImage() throws exception drawn with gradient background
 * @summary com.apple.junit.java.graphics.DrawImage
 * @library ../regtesthelpers
 * @build VisibilityValidator
 * @run main DrawImageOverGradient
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
 
public class DrawImageOverGradient extends TestCase {
      
    protected JFrame frame; 
    
    protected void setUp() {
        frame = new JFrame( "TestCase" );
                
        JComponent content = (JComponent) frame.getContentPane();
        content.setLayout(new FlowLayout());
        content.add(new ImagePanel());    
    }
    
    public void testDrawImage() throws Exception {
        frame.setLocation( 100, 100 );
        frame.pack();
        VisibilityValidator.setVisibleAndConfirm(frame);
        Thread.sleep(50); //make sure the frame shows up on the screen
    }
    
    public void tearDown() {
        frame.dispose();
    }
    
    // Boilerplate below so it can be run from the command line
    public static Test suite() {
        return new TestSuite(DrawImageOverGradient.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
        
    //the panel with the image on it.    
    private static class ImagePanel extends JComponent {
        BufferedImage image;
        
        public ImagePanel() {
            
            // exceptions used to be thrown when I tried to draw the image created below, 
            // but only when I tried to draw it atop a GRADIENT:
            image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.orange);
            g.drawLine(1, 1, image.getWidth()-1, image.getHeight()-1);
            g.drawLine(image.getWidth()-1, 1, 1, image.getHeight()-1);
            
            setOpaque(true);
            setVisible(true);
            setPreferredSize(new Dimension(80, 80));
        }
        
        protected void paintComponent(Graphics g)  {
            //the gradient
            Graphics2D g2 = (Graphics2D)g;
            GradientPaint gradient = new GradientPaint(0, 0, Color.white, getWidth()/3, getHeight()/3, Color.green, true); //true is for cyclic
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight()); 
            
            //draw the image and pray for no exceptions.
            g.drawImage(image, 0,  0 , null);
           
        }
    } 
}