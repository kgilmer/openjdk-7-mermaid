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
 @summary <rdar://problem/3284378> Robot generates incorrect events for BUTTON2/3_MASK
 @summary com.apple.junit.java.awt.Event;
 @run main RobotROMouse3284378
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class RobotROMouse3284378 extends TestCase {
    private JFrame frame = null;
    private JLabel label = null;
    private static String resultString = null;
    
    private static HashMap<Integer, String> map = new HashMap<Integer, String>();
    
    private static class Watcher extends MouseAdapter {
        public void mousePressed(MouseEvent ev) {
            resultString = (String) map.get(new Integer(ev.getModifiers()));
            // System.out.println("received button press " + resultString);
        }
    }
    
    public void testROMouse() throws Exception {
        map.put(new Integer(InputEvent.BUTTON1_MASK), "button 1");
        map.put(new Integer(InputEvent.BUTTON2_MASK), "button 2");
        map.put(new Integer(InputEvent.BUTTON3_MASK), "button 3");
        
        frame.setVisible(true);
        Thread.sleep(1000);
        Robot robot = new Robot();
        Point pt = label.getLocationOnScreen();
        robot.setAutoDelay(100);
        robot.mouseMove(pt.x + label.getWidth()/2,
                        pt.y + label.getHeight()/2);
        // System.out.println("roboto press button 2");
        // We no longer test button 2, as it is now mapped to Dashboard by default. 4207099
        // robot.mousePress(InputEvent.BUTTON2_MASK);
        // robot.mouseRelease(InputEvent.BUTTON2_MASK);
        // robot.waitForIdle();
        // assertSame("These should both be \"button 2\"", resultString, map.get(new Integer(InputEvent.BUTTON2_MASK)));
        
        //System.out.println("roboto press button 3");
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
        robot.waitForIdle();
        assertSame("These should both be \"button 3\"", resultString, map.get(new Integer(InputEvent.BUTTON3_MASK)));
        
        Thread.sleep(1000);
    }
    
    protected void setUp() {
        frame = new JFrame("Modifier test");
        label = new JLabel("Modifier test");
        label.addMouseListener(new Watcher());
        frame.getContentPane().add(label);
        frame.pack();
        assertNotNull( frame);
    }
    
    protected void tearDown() {
        if (frame != null) {
            frame.dispose();
        }
        frame=null;
    }
    
    public static Test suite() {
        return new TestSuite( RobotROMouse3284378.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }    
    
}

