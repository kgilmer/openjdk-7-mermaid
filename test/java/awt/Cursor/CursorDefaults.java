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
 @summary For each pre-defined Cursor, create a panel and set it's cursor to a predefined curosr
 @summary com.apple.junit.java.awt.cursor
 @library ../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @run main CursorDefaults
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;

public class CursorDefaults {
    static public final int[] cursors = {
    java.awt.Cursor.CROSSHAIR_CURSOR,
    java.awt.Cursor.DEFAULT_CURSOR,
    java.awt.Cursor.E_RESIZE_CURSOR,
    java.awt.Cursor.N_RESIZE_CURSOR,
    java.awt.Cursor.NE_RESIZE_CURSOR,
    java.awt.Cursor.NW_RESIZE_CURSOR,
    java.awt.Cursor.S_RESIZE_CURSOR,
    java.awt.Cursor.SE_RESIZE_CURSOR,
    java.awt.Cursor.SW_RESIZE_CURSOR,
    java.awt.Cursor.W_RESIZE_CURSOR,
    java.awt.Cursor.HAND_CURSOR,
    java.awt.Cursor.MOVE_CURSOR,
    java.awt.Cursor.TEXT_CURSOR,
    java.awt.Cursor.WAIT_CURSOR,
    };
    static public final int cols = 3;
    static public final int rows = 1 + (cursors.length)/3;
    static private int cnt = 0;
    static private Frame testFrame = null;
    
    public static void main (String[] args) throws RuntimeException {
        try {
            setUp();
            testCursors();
        }
        catch (Exception e) {
            if (testFrame != null) {
                testFrame.dispose();
            }

            throw new RuntimeException(e.getMessage());
        }
    }
    
    public static void setUp() throws Exception {
        Cursor c;
        Label l;
        Panel p;
        
        // Set up our frame
        testFrame = new Frame("CursorDefaults");
        testFrame.setLayout(new GridLayout(rows,cols));
        // 
        for (int i=0; i < cursors.length; i++ ) {
            c = new Cursor(cursors[i]);
            l = new Label(c.getName());
            p = new Panel();
            p.add(l);
            p.setCursor(c);
            if (i % 2 == 0)
                p.setBackground(Color.red);
            else
                p.setBackground(Color.blue);
            testFrame.add(p);
        }
        testFrame.pack();
        
    }
    
    // test passes if no exceptions are thrown
    public static void testCursors() throws Exception {
        VisibilityValidator.setVisibleAndConfirm(testFrame);

        // Have the Robot move the mouse over each Panel
        System.out.println("Number of Cursors: " + cursors.length);
        System.out.println("Number of Components: " + testFrame.getComponentCount());
        
        Component c = null;
        int componentCount = testFrame.getComponentCount();
        for (int i=0; i < componentCount; i++ ) {
            c = testFrame.getComponent(i);
            RobotUtilities.click(c);
            RobotUtilities.delay(250);

        }
        // To do -- add a go away button and have the Robot press it
    }
}

