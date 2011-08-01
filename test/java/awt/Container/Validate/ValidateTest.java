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
 @run main ValidateTest
 */

// classes necessary for this test

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;

public class ValidateTest {
    private static Frame frame = null;
    public static boolean wasMoved = false;
    public static boolean wasResized = false;
    
    public static void testValidation() throws Exception {
        try {
            // Bring up a test frame
            frame = new Frame( "Test Frame");
            
            frame.setLayout(new BorderLayout());      
            frame.setLocation(75, 75);
            frame.setSize(300, 200);
            
            Panel  pan = new Panel(new FlowLayout());
            pan.setBackground(new Color(255, 0, 0));
            frame.add("Center", pan);
            
            frame.setVisible(true);
            
            Button button = new Button("A Button");
            pan.add(button);
            pan.validate();

            Thread.sleep(1000);

            // For some reason, the validation behavior only kicks in _after_ the first component is added
            Button button2 = new Button("A Button");
            button2.addComponentListener(new ComponentAdapter() {
                public void componentMoved(ComponentEvent e) {
                    wasMoved = true;
                }
                public void componentResized(ComponentEvent e) {
                    wasResized = true;
                }
            });
            pan.add(button2);
            pan.validate();
            
            Thread.sleep(1000);
            
            if (!wasResized) {
                System.out.println("Component wasn't resized.");
                throw new RuntimeException("Component wasn't resized.");
            }
            if (!wasMoved) {
                throw new RuntimeException("Component wasn't moved.");
            }
        }
        finally {
            if (frame != null) {
                frame.setVisible(false);
                frame.dispose();
                frame = null;
            }
        }
    }
    
    public static void main (String[] args) throws RuntimeException {
        try {
            testValidation();
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

