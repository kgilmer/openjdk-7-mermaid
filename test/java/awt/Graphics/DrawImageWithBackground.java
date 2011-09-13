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
 @summary <rdar://problem/3612381>: Graphics.drawImage ignores bgcolor parameter
 @summary com.apple.junit.java.graphics.DrawImage;
 @run main DrawImageWithBackground
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawImageWithBackground extends TestCase {

    static GraphicsEnvironment        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static GraphicsDevice             gd = ge.getDefaultScreenDevice();
    static GraphicsConfiguration    gc = gd.getDefaultConfiguration();

    protected JFrame frame; 
    protected Image src,dst; 

    //the panel with the image on it.    
    private class ImagePanel extends JComponent {

        public ImagePanel() {
            setOpaque(true);
            setVisible(true);
        }
        
        public Dimension getPreferredSize()  {
            return new Dimension(150, 150);
        }
        
        protected void paintComponent(Graphics g)  {
            g.drawImage(dst, 0,  0 , null);
        }
    } 
    
    protected void setUp() {
        frame = new JFrame( "TestCase" );
        src = gc.createCompatibleImage( 150, 150, Transparency.TRANSLUCENT );
        dst = gc.createCompatibleImage( 150, 150, Transparency.TRANSLUCENT );
                
        JComponent content = (JComponent) frame.getContentPane();
        content.setLayout(new FlowLayout());
        content.add(new ImagePanel());    
    }
    
    public void testDrawImage() throws InterruptedException {

        Graphics g = dst.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, 150, 150);
        g.drawImage(src, 0,  0 , Color.orange, null); // <--- this is the meat of the test
        g.dispose();

        frame.setLocation( 100, 100 );
        frame.pack(); 
        frame.setVisible( true ); 

        Thread.sleep(250); //make sure the frame shows up on the screen

        int currColor = ((BufferedImage)dst).getRGB(75,75);
        // System.out.println( "Actual\t" + Integer.toHexString(currColor));
        // System.out.println( "Expected\t" + Integer.toHexString(0xffffc800));
        assertEquals("pixel in middle should be orange", 0xffffc800, currColor);
    }
    
    public void tearDown() {
        frame.dispose();
    }
    
    // Boilerplate below so it can be run from the command line
    public static Test suite() {
        return new TestSuite(DrawImageWithBackground.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
