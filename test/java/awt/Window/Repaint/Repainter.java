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
 * @author  mmcdougall; ported to jtreg by David Durrence
 * @summary <rdar://problem/5191355>
 * @summary Testcase for a repaints. 1.5 & 1.6: Failures in Nightly automated testing (OnTop)
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main Repainter
 */

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.Vector;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class Repainter {
    private static final int NUM_REPAINTS = 12;

    public static void main(String[] args) throws Exception {
        Vector<Window> windows = null;

        // create and initialize a variety of windows
        windows = new Vector<Window>();
        Frame tf = new Frame("Test Frame");
        windows.add(tf);
        Window tw = new Window(tf);
        windows.add(tw);
        Dialog td = new Dialog(tf);
        windows.add(td);
        
        // Just bring up several windows, and trigger (and watch for) repaints
        try {
            // set up some default values
            int offset = 10;
            for (Window w : windows) {
                if (w == null) {
                    throw new RuntimeException("Should have a valid window");
                }
                offset += 30;
                w.setBounds(offset, offset, 200, 200);
            }
            
            // first paint red, and wait for it to appear
            for (Window w : windows) {
                w.setBackground(Color.red);
                if (w instanceof Frame) {
                    VisibilityValidator.setVisibleAndConfirm( (Frame) w);
                } else {
                    w.setVisible(true);
                }
                if (!VisibilityValidator.waitForColor(w, Color.red)) {
                    throw new RuntimeException("Timed out without seeing our red background");
                }
                
                for (int i = 0; i < NUM_REPAINTS; i++) {
                    // now paint blue, and wait for it to appear
                    w.setBackground(Color.blue);
                    w.repaint();
                    if (!VisibilityValidator.waitForColor(w, Color.blue)) {
                        throw new RuntimeException("Timed out without seeing our blue background");
                    }
                    
                    // now paint green, and wait for it to appear
                    w.setBackground(Color.green);
                    w.repaint();
                    if (!VisibilityValidator.waitForColor(w, Color.green)) {
                        throw new RuntimeException("Timed out without seeing our green background");
                    }
                }
            }
        } finally {
            // get rid of our collection of windows
            for (Window w : windows) {
                w.setVisible(false);
                w.dispose();
            }
        }
    }
}
