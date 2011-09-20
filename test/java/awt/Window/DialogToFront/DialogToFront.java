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
 * @summary Makes sure that an owned non-modal dialog comes to front properly
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build RobotUtilities VisibilityValidator Waypoint
 * @run main DialogToFront
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

public class DialogToFront extends TestCase {
    public static Test suite() {
        return new TestSuite(DialogToFront.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    volatile Waypoint didClick = new Waypoint();
    volatile VisibilityValidator dialogVV = null;
    
    
    volatile Frame frame1 = null;
    volatile Frame frame2 = null;
    
    volatile Button button1 = null;
    volatile Button button2 = null;
    
    volatile Dialog dialog = null;
    
    volatile Button buttonClicked = null;
    
    
    public void testDialogToFront() throws Exception {
        try {
            frame1 = new Frame("Test Frame 1");
            frame1.setBounds(400,400,400,400);
            VisibilityValidator.setVisibleAndConfirm(frame1);
            
            frame2 = new Frame("Test Frame 2");
            frame2.setSize(300,250);
            button1 = new Button("Button");
            button1.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                                      buttonClicked = button1;
                                      didClick.clear();
                                      }});
            frame2.add(button1);
            VisibilityValidator.setVisibleAndConfirm(frame2);
            
            dialog = new Dialog(frame1, "Dialog", false);
            dialogVV = new VisibilityValidator(dialog);
            dialog.setSize(180,200);
            button2 = new Button("Button");
            button2.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                                      buttonClicked = button2;
                                      didClick.clear();
                                      }});
            dialog.add(button2);
            dialog.setVisible(true);
            dialogVV.requireVisible();
            if (!dialogVV.isValid())  throw new Exception("Dialog not visible");
            
            didClick.reset();
            RobotUtilities.click(button1); // We actually expect to click button 2, since dialog (and button2) should be covering button1
            didClick.requireClear();
            
            assertTrue("Failed to click button2; likely that Dialog is under Frame 2", (buttonClicked == button2));
        } finally {
            dialog.setVisible(false);
            dialog.dispose();
            frame2.setVisible(false);
            frame2.dispose();
            frame1.setVisible(false);
            frame1.dispose();
        }
    }
}
