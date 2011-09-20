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
 @summary <rdar://problem/4432247> Modal dialogs are application-modal, but not top-most
 @summary com.apple.junit.java.awt.Dialog;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main ModalZOrder
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class ModalZOrder extends TestCase {
    public static Test suite() {
        return new TestSuite(ModalZOrder.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    volatile Frame frameA = null;
    volatile Frame frameB = null;
    volatile Dialog dialogA = null;
    volatile Button buttonA = null;
    volatile VisibilityValidator checkpoint = null;
    volatile Waypoint didClick = new Waypoint();

    public void testModalZOrder() throws Exception {
        try {
            frameA = new Frame( "Frame A" );
            frameA.setBounds( 0, 0, 100, 100 );
            VisibilityValidator.setVisibleAndConfirm(frameA);
            
            frameB = new Frame( "Frame B" );
            frameB.setBounds( 0, 0, 400, 400 );
            VisibilityValidator.setVisibleAndConfirm(frameB);
            
            dialogA = new Dialog( frameA, "Modal Dialog", true );
            buttonA = new Button("Click");
            buttonA.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                didClick.clear();
            }});
            dialogA.setLayout(new FlowLayout());
            dialogA.add(buttonA);
            dialogA.pack();
            dialogA.setBounds(50,50,100,50);
            
            checkpoint = new VisibilityValidator(dialogA);

            // A bit awkward here, but since dialogA.setVisible(true) won't return...
            new Thread(new Runnable() { public void run() {
                // Wait for dialogA to become visible
                checkpoint.requireVisible();
                
                // Try to bring frameB above dialogA, then try to click the button
                // Can't use Waypoint here because this isn't supposed to work!
                RobotUtilities.click(frameB);

                pause(1000);
                
                didClick.reset();
                RobotUtilities.click(buttonA);

                // Go ahead and swallow the exception here, as we assert
                // in just a little bit (and on the test thread) that we
                // got through the waypoint
                try { didClick.requireClear(); } catch (RuntimeException e) {}

                // hide the dialog so we can test the condition
                dialogA.setVisible(false);

            }}).start();

            dialogA.setVisible( true );

            assertTrue("Modal Dialog is covered!", didClick.isClear());
            
        } finally {
            if (dialogA != null) {
                dialogA.setVisible(false);
                dialogA.dispose();
            }
            if (frameB != null) {
                frameB.setVisible(false);
                frameB.dispose();
            }
            if (frameA != null) {
                frameA.setVisible(false);
                frameA.dispose();
            }
        }
    }
    
    public static void pause( int duration ) {
        try { Thread.sleep(duration); } catch(Throwable t) {}
    }
}
