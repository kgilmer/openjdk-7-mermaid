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
 @summary <rdar://problem/4082604> clearGlobalFocusOwner fails to remove focus from component
 @summary com.apple.junit.java.awt.Event;
 @library ../../../../java/awt/regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @run main R4082604_ClearFocusTest
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class R4082604_ClearFocusTest extends TestCase {
    
    public static Test suite() {
        return new TestSuite( R4082604_ClearFocusTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    public void testClearFocus() throws Exception {
        JFrame jframe = new JFrame("Clear Focus Test");
        try {
            jframe.getContentPane().setLayout(new FlowLayout());
            
            JTextField field = new JTextField("CLICK ME THEN PRESS ESCAPE");
            field.registerKeyboardAction(new ActionListener() { 
                public void actionPerformed(ActionEvent e) {
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                }}, 
                                         KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_FOCUSED);
            jframe.getContentPane().add(field);
            
            jframe.setSize(600, 75);
            
            VisibilityValidator.setVisibleAndConfirm(jframe);
            pause(500);
            
            // put focus in the textfield
            RobotUtilities.click(field);
            pause(500);
            
            // type
            RobotUtilities.typeKey(KeyEvent.VK_H);
            RobotUtilities.typeKey(KeyEvent.VK_I);
            pause(500);
            
            // test "hi"
            String hiString = field.getText();
            assertTrue("Failed to find \"hi\" in JTextField: " + hiString, (hiString.indexOf("hi") != -1));
            
            // remove focus
            RobotUtilities.typeKey(KeyEvent.VK_ESCAPE);
            pause(500);
            
            // type
            RobotUtilities.typeKey(KeyEvent.VK_B);
            RobotUtilities.typeKey(KeyEvent.VK_Y);
            RobotUtilities.typeKey(KeyEvent.VK_E);
            pause(500);
            
            // test "bye"
            String byeString = field.getText();
            assertTrue("Found \"bye\" in JTextField: " + byeString, (byeString.indexOf("bye") == -1));
        } finally {
            if (jframe != null) {
                jframe.setVisible(false);
                jframe.dispose();
                jframe = null;
            }
        }
    }
    
    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
