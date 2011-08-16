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
 @summary See <rdar://problem/4187162> Make sure all (recursive) children of a modal window are functional.
 @summary com.apple.junit.java.awt.Window
 @library ../../regtesthelpers
 @build RobotUtilities
 @run main ChildOfModal
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import test.java.awt.regtesthelpers.RobotUtilities;

public class ChildOfModal extends TestCase {
    public static Test suite() {
        return new TestSuite(ChildOfModal.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.failureCount() != 0) || (tr.errorCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures");
        }
    }
    
    volatile boolean result = false;
    Frame baseFrame = null;
    volatile Dialog baseDialog = null;
    Window childWindow = null;
    Window childOfChildWindow = null;
    Button childOfChildButton = null;
    
    public void testChildOfModal() {
        try {
            // Create the base Frame
            baseFrame = new Frame("ChildOfModal");
            baseFrame.setLayout(new FlowLayout());
            baseFrame.setSize(100,100);
            baseFrame.setVisible(true);
            
            Thread t = new Thread(new Runnable() { public void run() {
                // Make a new modal dialog based on that Frame
                baseDialog = new Dialog(baseFrame, true);
                baseDialog.setSize(200,200);
                baseDialog.setVisible(true);
            }});
            t.start();
            pause(1000); // Wait for dialog to show up - should use events

            
            // Make a new Window based on the modal Dialog
            childWindow = new Window(baseDialog);
            childWindow.setSize(75, 75);
            childWindow.setVisible(true);
            
            // Make a new Window based on the previous Window
            // This is the key - this window should still be functional
            childOfChildWindow = new Window(childWindow);
            childOfChildWindow.setSize(100, 100);
            childOfChildButton = new Button("test");
            childOfChildButton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { result = true; }} );
            childOfChildWindow.add(childOfChildButton);
            childOfChildWindow.setVisible(true);
            
            // Click the button!
            RobotUtilities.click(childOfChildButton);
            pause(1000);
            assertTrue("Failed to click childOfChildOfModal button!", result);

        } finally {
            if (childOfChildWindow != null) {
                childOfChildWindow.setVisible(false);
                childOfChildWindow.dispose();
                childOfChildWindow = null;
            }
            if (childWindow != null) {
                childWindow.setVisible(false);
                childWindow.dispose();
                childWindow = null;
            }
            if (baseDialog != null) {
                baseDialog.setVisible(false);
                baseDialog.dispose();
                baseDialog = null;
            }
            if (baseFrame != null) {
                baseFrame.setVisible(false);
                baseFrame.dispose();
                baseFrame = null;
            }
        }
    }

    public static void pause( int duration ) {
        try {
            Thread.sleep(duration);                
        } catch(Throwable t) {}
    }
}
