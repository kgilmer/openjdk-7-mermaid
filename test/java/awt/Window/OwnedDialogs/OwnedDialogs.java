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
 * @summary Tests various owned modal dialogs
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build RobotUtilities VisibilityValidator Waypoint
 * @run main OwnedDialogs
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;


public class OwnedDialogs extends TestCase {
    public static Test suite() {
        return new TestSuite(OwnedDialogs.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testChainedModalDialogs() throws Exception {
        _doLotsOfModalDialogs(true, true, "Chained Modal Dialogs Frame");
    }

    public void testCommonModalDialogs() throws Exception {
        _doLotsOfModalDialogs(false, false, "Common Modal Dialogs Frame");
    }

    public void testMix1ModalDialogs() throws Exception {
        _doLotsOfModalDialogs(true, false, "Mix-1 Modal Dialogs Frame");
    }

    public void testMix2ModalDialogs() throws Exception {
        _doLotsOfModalDialogs(false, true, "Mix-2 Modal Dialogs Frame");
    }

    volatile Waypoint didClick = new Waypoint();
    volatile VisibilityValidator checkpoint = null;

    volatile Frame frame = null;
    
    volatile Button modal1Button = null;
    volatile Button modal2Button = null;
    volatile Button modal3Button = null;
    
    volatile Button closeModal1Button = null;
    volatile Button closeModal2Button = null;
    volatile Button closeModal3Button = null;
    
    volatile Dialog modal1 = null;
    volatile Dialog modal2 = null;
    volatile Dialog modal3 = null;
    
    public void _doLotsOfModalDialogs(boolean modal2chained, boolean modal3chained, String frameName) throws Exception {

        frame = new Frame(frameName);

        modal1Button = new Button("Open Modal 1");
        modal2Button = new Button("Open Modal 2");
        modal3Button = new Button("Open Modal 3");

        closeModal1Button = new Button("Close Modal 1");
        closeModal2Button = new Button("Close Modal 2");
        closeModal3Button = new Button("Close Modal 3");
        
        // Set up the dialogs!
        modal1 = new Dialog(frame,  "Modal 1", true);
        
        modal2 = null;
        if (modal2chained) 
            modal2 = new Dialog( modal1, "Modal 2", true);
        else 
            modal2 = new Dialog( frame, "Modal 2", true);
        
        modal3 = null;
        if (modal3chained) 
            modal3 = new Dialog( modal2, "Modal 3", true);
        else 
            modal3 = new Dialog( frame, "Modal 3", true);

        
        try {
            // Setup buttons to open dialogs
            modal1Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal1.setVisible(true);
            }});
            modal2Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal2.setVisible(true);
            }});
            modal3Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal3.setVisible(true);
            }});
            
            // Setup buttons to close dialogs
            closeModal1Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal1.setVisible(false);
                didClick.clear();
            }});
            closeModal2Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal2.setVisible(false);
                didClick.clear();
            }});
            closeModal3Button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                modal3.setVisible(false);
                didClick.clear();
            }});

            // Set up the main frame and dialogs
            frame.setLayout(new FlowLayout());
            frame.setSize(400, 300);
            frame.add(modal1Button);
            frame.pack();
            
            modal1.setLayout(new FlowLayout());
            modal1.setSize(300, 200);
            modal1.add(modal2Button);
            modal1.add(closeModal1Button);
            modal1.validate();
            
            modal2.setLayout(new FlowLayout());
            modal2.setSize(300, 200);
            modal2.add(modal3Button);
            modal2.add(closeModal2Button);
            modal2.validate();
            
            modal3.setLayout(new FlowLayout());
            modal3.setSize(300, 200);
            modal3.add(closeModal3Button);
            modal3.validate();

            VisibilityValidator.setVisibleAndConfirm(frame);
            
            // Show the dialogs in order, close and re-open the last one, then close in reverse order
            // Note that the setVisible(false) actions have pauses, since we don't have an InvisibilityValidator
            checkpoint = new VisibilityValidator(modal1);
            RobotUtilities.click(modal1Button);
            checkpoint.requireVisible();
            assertTrue("Modal 1 is not showing! Check double-click speed in control panel is not too slow.", checkpoint.isValid());

            checkpoint = new VisibilityValidator(modal2);
            RobotUtilities.click(modal2Button);
            checkpoint.requireVisible();
            assertTrue("Modal 2 is not showing! Check double-click speed in control panel is not too slow.", checkpoint.isValid());

            checkpoint = new VisibilityValidator(modal3);
            RobotUtilities.click(modal3Button);
            checkpoint.requireVisible();
            assertTrue("Modal 3 is not showing! Check double-click speed in control panel is not too slow.", checkpoint.isValid());

            didClick.reset();
            RobotUtilities.click(closeModal3Button);
            didClick.requireClear();
            pause(500);
            assertFalse("Modal 3 is still showing! Check double-click speed in control panel is not too slow.", modal3.isShowing());

            // VisibilityValidator only works for new windows (windowOpened), so we fake our own
            modal3.addWindowFocusListener(new WindowAdapter() {
                public void windowGainedFocus( WindowEvent e ) {
                    didClick.clear();
                }
            });

            didClick.reset();
            RobotUtilities.click(modal3Button);
            didClick.requireClear();
            assertTrue("Modal 3 is not showing 2! Check double-click speed in control panel is not too slow.", didClick.isClear());

            didClick.reset();
            RobotUtilities.click(closeModal3Button);
            didClick.requireClear();
            pause(500);
            assertFalse("Modal 3 is still showing 2! Check double-click speed in control panel is not too slow.", modal3.isShowing());
            
            didClick.reset();
            RobotUtilities.click(closeModal2Button);
            didClick.requireClear();
            pause(500);
            assertFalse("Modal 2 is still showing! Check double-click speed in control panel is not too slow.", modal2.isShowing());

            didClick.reset();
            RobotUtilities.click(closeModal1Button);
            didClick.requireClear();
            pause(500);
            assertFalse("Modal 1 is still showing! Check double-click speed in control panel is not too slow.", modal1.isShowing());

        } finally {
            if (modal1 != null) {
                modal1.setVisible(false);
                modal1.dispose();
                modal1 = null;
            }
            if (modal2 != null) {
                modal2.setVisible(false);
                modal2.dispose();
                modal2 = null;
            }
            if (modal3 != null) {
                modal3.setVisible(false);
                modal3.dispose();
                modal3 = null;
            }
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
                frame = null;
            }
        }
    }
    
    public static void pause( int duration ) {
        try { Thread.sleep(duration); } catch(Throwable t) {}
    }
}
