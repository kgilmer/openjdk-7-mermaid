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
 @summary com.apple.junit.java.graphics;
 @library ../regtesthelpers
 @build BITestUtils
 @run main CopyAreaOnScreen
 */

import junit.framework.*;

import java.awt.*;

public class CopyAreaOnScreen extends TestCase
{
    int w = 50, h = 50;
    int x1 = 100, y1 = 100;
    int dx = 50, dy = 0;
    
    public void test() throws Exception
    {
        Thread.sleep(1000);
        
        // start location
        final int c1x = x1+(w/2); 
        final int c1y = y1+(h/2);
        
        //copy location
        final int c2x = c1x+dx; 
        final int c2y = c1y+dy;
        
        Color c1 = frame.getPixel(c1x, c1y);
        Color c2 = frame.getPixel(c2x, c2y);
        
        // human readable messages
        String c1msg = "@(" + c1x + "," + c1y + ") : " + BITestUtils.Hex(c1.getRGB());
        String c2msg = "@(" + c2x + "," + c2y + ") : " + BITestUtils.Hex(c2.getRGB());
        assertEquals( "Colors " + c1msg + " and " + c2msg + " should match", c1.getRGB(), c2.getRGB());            
    }
    
    public static Test suite() 
    {
        return new TestSuite(CopyAreaOnScreen.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected errors or failures.");
        }
    }
        
    public void setUp()
    {
        new MyFrame("CopyArea");
    }
    
    protected void tearDown() 
    {
        frame.dispose();
    }
    
    MyFrame frame;
    
    class MyFrame extends Frame
    {
        Robot robot;
        
        public MyFrame(String str)
        {
            super(str);
            
            frame = this;
            
            init();
            
            // give the frame a chance to get up
            try
            {
                Thread.sleep(1000);
            } 
            catch (Exception e)
            {
                throw new Error(e.getMessage());
            }
            
            try
            {
                robot = new Robot();
            }
            catch (AWTException e)
            {
                throw new Error(e.getMessage());
            }
            
            robot.setAutoWaitForIdle(true);
        }
        
        public void init()
        {
            setSize(400, 400);                    
            setVisible(true);
        }
        
        public void paint(Graphics g)
        {
            g.setColor(Color.red);
            g.fillRect(0, 0, 1000, 1000);
            
            g.setColor(Color.green);
            g.fillRect(x1, y1, w, h);
            
            g.setColor(Color.blue);
            g.fillRect(x1+dx, y1+dy, w, h);
            
            g.copyArea(x1, y1, w, h, dx, dy);
        }
        
        public Color getPixel(int x, int y)
        {
            int locX = (int)(getLocationOnScreen().getX())+x;
            int locY = (int)(getLocationOnScreen().getY())+y;
            return robot.getPixelColor(locX, locY);
        }        
    }
}