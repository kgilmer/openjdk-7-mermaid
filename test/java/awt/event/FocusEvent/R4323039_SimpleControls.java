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
 @summary <rdar://problem/4307013> Moving Focus Between AWT and Swing Controls Requires a Second <tab> or <shift+tab>
 @summary com.apple.junit.java.awt.Event;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @run main R4323039_SimpleControls
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class R4323039_SimpleControls extends TestCase {

    public static Test suite() {
        return new TestSuite( R4323039_SimpleControls.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    volatile int buttonPresses = 0;

    public void testTabFocusTransition() throws Exception {
        buttonPresses = 0;

        JFrame frame = new JFrame("Simple Controls Test");
        try {
            Container content = frame.getContentPane();
            content.setLayout(new FlowLayout());

            JButton jbutton = new JButton("JButton");
            jbutton.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                buttonPresses++;
            }});
            content.add(jbutton);

            content.add(new JComboBox(new String[] {"JComboBox", "Stuff", "Yoink"}));

            content.add(new Button("Button"));

            final Choice choice = new Choice();
            choice.add("Choice");
            choice.add("Stuff");
            choice.add("Yoink");
            content.add(choice);

            content.add(new JLabel(System.getProperty("java.version")));

            frame.setSize(400, 300);

            VisibilityValidator.setVisibleAndConfirm(frame);
            pause(500);

            // activate the first button
            RobotUtilities.typeKey(KeyEvent.VK_SPACE);
            pause(500);

            // tab back to the first button
            for (int i=0; i<4; i++) {
                RobotUtilities.typeKey(KeyEvent.VK_TAB);
                pause(200);
            }

            // activate the first button
            RobotUtilities.typeKey(KeyEvent.VK_SPACE);
            pause(500);

            // Wait for events to be delivered
            try {
                SwingUtilities.invokeAndWait(new Runnable() { public void run() {} });
            } catch (Exception e) {}

            // Test output
            assertTrue("JButton wasn't pressed exactly twice! " + buttonPresses, (buttonPresses == 2));
        } finally {
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
                frame = null;
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
