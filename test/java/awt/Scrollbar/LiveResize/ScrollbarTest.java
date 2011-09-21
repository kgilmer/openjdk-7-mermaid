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
 @summary <rdar://problem/3155258> [JavaJDK15] AWT scrollbars have redraw problems (not opaque)
 @summary after resizing the scrollbars would leave garbage.
 @summary Note that the whole test is filled with numbers tuned to the current size; be careful about changing these.
 @summary com.apple.junit.java.awt;
 @library ../../../../java/awt/regtesthelpers
 @build VisibilityValidator
 @run main ScrollbarTest
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.InputEvent;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class ScrollbarTest extends TestCase {

    private Frame frame;
    private Robot robot;
    
    public void setUp() throws Exception {
        robot = new Robot();

        frame = new Frame();
        frame.setBackground(Color.orange);

        frame.setLayout(new GridLayout(1, 2));
        frame.add(new Scrollbar(Scrollbar.HORIZONTAL));
        frame.add(new Scrollbar(Scrollbar.VERTICAL));

        frame.setSize(300, 200);        
    }
    
    public void testResize() throws Exception {
        VisibilityValidator.setVisibleAndConfirm(frame);
        assertTrue("Timed out without seeing our orange background", VisibilityValidator.waitForColor(frame, 50, 150, Color.orange));

        LiveResizeThread lrt = new LiveResizeThread();
        lrt.start();
        lrt.join();
        // wait a second before testing for the color result. If all goes well, there shouldn't be any garbage (screen dirt on the screen)
        // and test whether the color is white
        
        Thread.sleep(1000);
        
        VisibilityValidator.assertColorEquals(" Color should be orange at (50, 190) ", Color.orange, robot.getPixelColor(50, 210));
        VisibilityValidator.assertColorEquals(" Color should be orange at (70, 190) ", Color.orange, robot.getPixelColor(70, 210));
        VisibilityValidator.assertColorEquals(" Color should be orange at (50, 205) ", Color.orange, robot.getPixelColor(50, 220));
    }
    
    protected void tearDown() {
        frame.dispose();
    }
    
    public static Test suite() {
        return new TestSuite(ScrollbarTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test makes a Mac OS X assumption about the growbox location
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
    
    class LiveResizeThread extends Thread {
        public void run() {                        
            int locX = (int)(frame.getLocationOnScreen().getX());
            int locY = (int)(frame.getLocationOnScreen().getY());
            
            boolean mouseDown = false;
            try {
                        
                robot.setAutoWaitForIdle(true);

                int width = (int)(frame.getBounds().getWidth());
                int height = (int)(frame.getBounds().getHeight());
                
                int x = locX + width-6;
                int y = locY + height-6;
                
                robot.mouseMove(x, y);
                mouseDown = true;
                robot.mousePress(InputEvent.BUTTON1_MASK);
                
                Thread.sleep(2000);

                robot.mouseMove(x+100, y+100);
                    
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally {
                if (mouseDown)
                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
        }
    }
}
