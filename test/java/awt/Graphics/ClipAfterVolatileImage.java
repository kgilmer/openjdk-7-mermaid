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
 * @summary <rdar://problem/4589628> [JavaJDK16] GLPipe: Clip reset after drawing VolatileImage
 * @summary com.apple.junit.java.graphics
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.VolatileImage;

public class ClipAfterVolatileImage extends TestCase {
    static final boolean DEBUG = false;
    int w = 50, h = 50;
    int x1 = 100, y1 = 100;
    
    public void test() throws Exception {
        Thread.sleep(1000);
        
        Color c1 = frame.getPixel(x1-(w/2), y1-(h/2));
        if (DEBUG) System.err.println("c1="+c1);
        Color c2 = frame.getPixel(x1+(w/2)+w, y1+(h/2)+h);
        if (DEBUG) System.err.println("c2="+c2);
        assertEquals( "The colors "+c1+" and "+c1+" should be the same ", c1.getRGB(), c2.getRGB());            
    }
    
    public static Test suite() {
        return new TestSuite(ClipAfterVolatileImage.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
        
    public void setUp() {
        new MyFrame("ClipAfterVolatileImage");
    }
    
    protected void tearDown() {
        frame.dispose();
    }
    
    MyFrame frame;
    
    class MyFrame extends Frame {
        Robot robot;
        
        public MyFrame(String str) {
            super(str);
            
            frame = this;
            
            init();
            
            // give the frame a chance to get up
            try {
                Thread.sleep(1000);
            } 
            catch (Exception e) {
                throw new Error(e.getMessage());
            }
            
            try {
                robot = new Robot();
            }
            catch (AWTException e) {
                throw new Error(e.getMessage());
            }
            
            robot.setAutoWaitForIdle(true);
        }
        
        public void init() {
            setSize(400, 400);                  
            setVisible(true);
        }
        
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            VolatileImage vimg = g2d.getDeviceConfiguration().createCompatibleVolatileImage(w, h);
            g2d.setColor(Color.red);
            g2d.fillRect(0, 0, 10000, 10000);
            g2d.setClip(x1, y1, w, h);
            g2d.drawImage(vimg, x1, y1, null);
            g2d.setColor(Color.blue);
            g2d.fillRect(x1, y1, 10000, 10000);
        }
        
        public Color getPixel(int x, int y) {
            return robot.getPixelColor(x, y);
        }       
    }
}
