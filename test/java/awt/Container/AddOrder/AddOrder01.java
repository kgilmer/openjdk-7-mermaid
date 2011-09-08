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
 @summary Zorder test
 @summary com.apple.junit.java.awt.Container
 @library ../../regtesthelpers
 @build RobotUtilities
 @build Waypoint
 @run main AddOrder01
 */

// classes necessary for this test
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.Waypoint;

public class AddOrder01 {
    
    static volatile String result = null;
    static Waypoint waypoint = null;

    protected static void setUp() throws Exception {        
        if (waypoint == null) {
            waypoint = new Waypoint();
        }
        else {
            waypoint.reset();
        }
    }
    
    public static void testAddOrder() throws Exception {
        doZOrder(false);
    }
    
    public static void testSetVisibleOrder() throws Exception {
        doZOrder(true);
    }
    
    private static void doZOrder(boolean shouldDoVisible) throws RuntimeException  {
        Frame frame = null;
        Panel panel;
        Dimension dimension;
        
        Button topButton;
        Button bottomButton;
        Button topBottomButton;
        Button topTopButton;
            
        // Final order should be (top to bottom) topTopButton, topButton, topBottomButton, panel, bottomButton
        
        try {
            frame = new Frame("Add Order Test");
            
            frame.setLayout(new FlowLayout());
            frame.setBounds(400, 100, 400, 150);
            
            panel = new Panel();
            panel.setBackground(Color.yellow);
            panel.setPreferredSize(new Dimension(400,150));
            frame.add(panel);
            
            frame.setVisible(true);

            
            // Test 1 - this should be under the panel
            result = null;
            bottomButton =    new Button("  Bottom Button  ");
            bottomButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { result = "bottomButton"; } });
            frame.add(bottomButton);
            bottomButton.setSize(bottomButton.getPreferredSize());
            bottomButton.setLocation(150, 35);
            if (shouldDoVisible) bottomButton.setVisible(false);
            if (shouldDoVisible) bottomButton.setVisible(true);
            RobotUtilities.click(bottomButton);
            pause(500); // hard to use waypoint for negative test, so just wait...
            if (result != null) {
                throw new RuntimeException("bottomButton got clicked when it should NOT have!");
            }

            // Test 2 - this should be on top of the panel
            result = null;
            topButton =       new Button("   Top Button    ");
            topButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { result = "topButton";  waypoint.clear(); } });
            frame.add(topButton, 0);
            topButton.setSize(topButton.getPreferredSize());
            topButton.setLocation(150, 35);
            if (shouldDoVisible) topButton.setVisible(false);
            if (shouldDoVisible) topButton.setVisible(true);
            pause(300); // allow some time for events to propagate
            waypoint.reset();
            RobotUtilities.click(topButton);
            String expectTopMessage = "topButton did NOT get clicked when it should have; waypoint timed out.  Check double-click speed in control panel is not too slow. "; 
            waypoint.requireClear(expectTopMessage); // wait for the waypoint to clear
            if (!("topButton".equals(result))) {
                throw new RuntimeException(expectTopMessage);
            }

            // Test 3 - this should be between the panel and the topButton
            result = null;
            topBottomButton = new Button("Top Bottom Button");
            topBottomButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { result = "topBottomButton"; } });
            frame.add(topBottomButton, 1);
            topBottomButton.setSize(topBottomButton.getPreferredSize());
            topBottomButton.setLocation(150, 35);
            if (shouldDoVisible) topBottomButton.setVisible(false);
            if (shouldDoVisible) topBottomButton.setVisible(true);
            RobotUtilities.click(topBottomButton);
            pause(500);  // hard to use waypoint for negative test, so just wait...
            if ("topBottomButton".equals(result)) {
                throw new RuntimeException("bottomButton got clicked when it should NOT have!");
            }

            // Test 4 - this should be on top of everything
            result = null;
            topTopButton =    new Button(" Top Top Button  ");
            topTopButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { result = "topTopButton"; waypoint.clear(); } });
            frame.add(topTopButton, 0);
            topTopButton.setSize(topTopButton.getPreferredSize());
            topTopButton.setLocation(150, 35);
            if (shouldDoVisible) topTopButton.setVisible(false);
            if (shouldDoVisible) topTopButton.setVisible(true);
            pause(300); // allow some time for events to propagate
            waypoint.reset();
            RobotUtilities.click(topTopButton);
            String expectTopTopMessage = "topTopButton did NOT get clicked when it should have; waypoint timed out.  Check double-click speed in control panel is not too slow. ";            waypoint.requireClear(expectTopMessage); // wait for the waypoint to clear
            waypoint.requireClear(expectTopTopMessage); // wait for the waypoint to clear
            if (!("topTopButton".equals(result))) {
                throw new RuntimeException(expectTopMessage);
            }
        }
        finally {
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
            }
        }
    }
    
    public static void pause( int duration ) {
        try {
            Thread.sleep(duration);                
        } catch(Throwable t) {}
    }
    
    public static void main (String[] args) throws RuntimeException {
        try {
            setUp();
            testAddOrder();
            setUp();
            testSetVisibleOrder();
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}


