/*
 * Copyright (c) 2006, 2007, Oracle and/or its affiliates. All rights reserved.
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
 @summary Zorder test
 @summary com.apple.junit.java.awt.Container
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main ZOrder01
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class ZOrder01 {
    
    public static void main (String[] args) throws RuntimeException {
        try {
            setUp();
            testBasicZOrder();
            tearDown();
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // instance variables
    static int             cDelay = 1000;    // Note that this must be under "double-click" delay, or AppKit will think we are double-clicking
    static int             cPressed = -1;
    static Waypoint        cDidPress = new Waypoint();
    static Frame             cFrame = null;
    static Panel            cPanel = null;    
    static KeyedButton[]     cButtons = new KeyedButton[3];
    
    
    // A frame with buttons
    protected static void setUp() {
        cFrame = new Frame("Z Order Test");
        cFrame.setBounds(50, 50, 170, 170);
        cPanel = new Panel();
        cPanel.setBackground(Color.cyan);

        cFrame.add(cPanel);
        cPanel.setLayout(null);
        
        for (int i=0; i < cButtons.length; i+=1) {
            cButtons[i] = new KeyedButton( Integer.toString(i), i);
            cButtons[i].setBounds(25,25, 100, 100);
        }
    }

    protected static void tearDown() {
        if (cFrame != null) {
            cFrame.setVisible(false);
            cFrame.dispose();
        }
    }

    public static void testBasicZOrder() throws Exception {
        for (int i=0; i < cButtons.length; i+=1) {
            cPanel.add(cButtons[i]);
        }
    
        if (cFrame == null)
            throw new RuntimeException("cFrame is null; aborting test.");
        VisibilityValidator.setVisibleAndConfirm(cFrame);
        Thread.sleep(500); //<-- might be unneeded delay here

        // Click button 0 and verify result
        doRobotCheck( 0 );

        for (int i=0; i < cButtons.length; i+=1) {
            cPanel.setComponentZOrder(cButtons[i], 0);
            Thread.sleep(cDelay);    // Be careful not to fool AppKit into thinking that we are double-clicking.  See 4303796
            doRobotCheck(i);
            // doQuickTestReport();
        }
        
    }
    
    // ### TODO -- add testcase for ZOrder != 0;
    
    // Debugging: dump Z-Order of cButtons
    void doQuickTestReport() {
        System.out.println("Delay" + cDelay);
        for (int i=0; i < cButtons.length; i+=1) {
            System.out.println( "\t" +cPanel.getComponentZOrder( cButtons[i] ) );
        }    
    }

    // Utility function for checking the top button.
    // Waits for the button action event to fire
    static void doRobotCheck( int expected) throws RuntimeException  {
        RobotUtilities.click(cButtons[expected]);
        cDidPress.requireClear("ActionListener for button was not called");
        if (expected != cPressed) {
            throw new RuntimeException("Should have clicked button.  See radar 4303796 if failure occurs on a slow machine.  Also check that double-click speed in control panel is not too slow.");
        }
        cPressed = -1;
        cDidPress.reset();
    }
    
    // Utility class for tracking pressed cButtons.
    // Updates the global variable "cPressed"
    static class KeyedButton extends Button {
        int key = -1;
        KeyedButton(String label, int key) {
            super(label);
            this.key = key;
            addActionListener(
                              new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    KeyedButton button = (KeyedButton) e.getSource();
                    cPressed = button.key;
                    cDidPress.clear();
                    // System.out.println( cPressed );
                }
            }
                              );
        }
    }    
}




