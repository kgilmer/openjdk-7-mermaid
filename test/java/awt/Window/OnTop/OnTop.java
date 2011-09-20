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
 * @summary Testcases for a very simple of window operations, including alwaysOnTop()
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build RobotUtilities VisibilityValidator Waypoint
 * @run main OnTop
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class OnTop extends TestCase {
    Vector<Window> windows = null;
    Window topmost = null;
    Waypoint didClick = new Waypoint();

    public static Test suite() {
        return new TestSuite(OnTop.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }


    /*
         Just bring up several windows
     */
    public void testBasicWindows() throws Exception {
        initWindows();
        try {
            // first paint red, and wait for it to appear
            for (Window w : windows) {
                w.setBackground(Color.red);
                if (w instanceof Frame) {
                    VisibilityValidator.setVisibleAndConfirm( (Frame) w);
                } else {
                    w.setVisible(true);
                }
                assertTrue("Timed out without seeing our red window", VisibilityValidator.waitForColor(w, 40, 40, Color.red));

                // now paint blue, and wait for it to appear
                w.setBackground(Color.blue);
                w.repaint();
                assertTrue("Timed out without seeing our blue window", VisibilityValidator.waitForColor(w, 40, 40, Color.blue));

                // click in topmost window, w should get the click
                didClick.reset();
                RobotUtilities.click(w);
                didClick.requireClear();
                assertTrue("Click should get be processed by now ", didClick.isClear()); // if we hit the timeout in the waypoint, this will fail
                assertNotNull("Something should get clicked", topmost);

                assertSame("Should get a click event on window just made visible...", topmost, w);

                // now paint green, and wait for it to appear
                w.setBackground(Color.green);
                w.repaint();
                assertTrue("Timed out without seeing our green window", VisibilityValidator.waitForColor(w, 40, 40, Color.green));
            }
        } finally {
            disposeWindows();
        }
    }

    /*
         Several window with one set to alwaysOnTop
     */

    public void testChildOnTop01() throws Exception {
        initWindows();
        for (Window w : windows) {

            if (w instanceof JFrame || w instanceof JWindow || w instanceof JDialog ) {
                w.setBackground(Color.red);
            }
            else {
                w.setBackground(Color.blue);
            }
            assertFalse(w.isAlwaysOnTop());
        }

        Window big = windows.elementAt(1);  // pick one of the child windows
        big.setBounds(5, 5, 600, 600);
        big.setAlwaysOnTop(true); // <-- this is the meat of the test
        big.setBackground(new Color(0x80,0x80,0x80,0x80));  // this is translucent gray

        try {
            for (Window w : windows) {
                if (w instanceof Frame) {
                    VisibilityValidator.setVisibleAndConfirm( (Frame) w);
                } else {
                    w.setVisible(true);
                    Thread.sleep(400);  // can't use VisibilityValidator, so give it some time to appear
                }
                Thread.sleep(200);  // more time to paint

                // click, big should get the click if it is visible
                didClick.reset();
                RobotUtilities.click(w);
                didClick.requireClear();
                assertTrue("Click should get be processed by now ", didClick.isClear()); // if we hit the timeout in the waypoint, this will fail
                assertNotNull("Something should get clicked", topmost);

                // expected results could get kinda tricky here, since children
                // inherit alwaysOnTop from parent.  We keep things simple by making a
                // child window the alwaysOnTop window.
                if (big.isVisible() == false) {
                    assertSame("Should get a click event on window just made visible...", topmost, w);
                } else {
                    assertSame("Should get a click event on setAlwaysOnTop window...", topmost, big);
                }
            }
        } finally {
            disposeWindows();
        }
    }

    /*
         Several window with one set to alwaysOnTop, with mixed calls to toFront
     */

    public void testChildOnTop02() throws Exception {
        initWindows();
        for (Window w : windows) {

            if (w instanceof JFrame || w instanceof JWindow || w instanceof JDialog ) {
                w.setBackground(Color.green);
            }
            else {
                w.setBackground(Color.cyan);
            }
            assertFalse(w.isAlwaysOnTop());
        }

        Window big = windows.elementAt(1);  // pick one of the child windows
        big.setBounds(5, 5, 600, 600);
        big.setAlwaysOnTop(true); // <-- this is the meat of the test
        big.setBackground(new Color(0x80,0x80,0x80,0x80));  // this is translucent gray

        try {
            for (Window w : windows) {
                if (w instanceof Frame) {
                    VisibilityValidator.setVisibleAndConfirm((Frame) w);
                } else {
                    w.setVisible(true);
                    Thread.sleep(400);  // can't use VisibilityValidator, so give it some time to appear
                }
                Thread.sleep(200);  // more time to paint
                w.toFront();

                // click, big should get the click if it is visible
                didClick.reset();
                RobotUtilities.click(w);
                didClick.requireClear();
                assertTrue("Click should get be processed by now ", didClick.isClear()); // if we hit the timeout in the waypoint, this will fail
                assertNotNull("Something should get clicked", topmost);

                // expected results could get kinda tricky here, since children
                // inherit alwaysOnTop from parent.  We keep things simple by making a
                // child window the alwaysOnTop window.
                if (big.isVisible() == false) {
                    assertSame("Should get a click event on window just made visible...", topmost, w);
                } else {
                    assertSame("Should get a click event on setAlwaysOnTop window...", topmost, big);
                }
            }
        } finally {
            disposeWindows();
        }
    }

    // create and initialize a variety of windows
    public void initWindows() throws Exception {

        // create and store the windows
        windows = new Vector<Window>();
        Frame f = new Frame("Test Frame");
        windows.add(f);
        Window w = new Window(f);
        windows.add(w);
        Dialog d = new Dialog(f);
        windows.add(d);
        JFrame jf = new JFrame("Test JFrame");
        windows.add(jf);
        JWindow jw = new JWindow(jf);
        windows.add(jw);
        JDialog jd = new JDialog(jf);
        windows.add(jd);

        // set up some default values
        int offset = 10;
        for (Window win : windows) {
            assertNotNull("Should have a valid window", win);
            offset += 30;
            win.setBounds(offset, offset, 200, 200);
            win.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    Object source = e.getSource();
                    if (source instanceof Window) {
                        topmost = (Window) source;
                    } else {
                        System.out.println("Something weird happening here...");
                        topmost = null;
                    }
                    // System.out.println(e);
                    didClick.clear();

                }
            });
        }
    }

    // get rid of our collection of windows
    public void disposeWindows() throws Exception {
        for (Window w : windows) {
            w.setVisible(false);
            w.dispose();
        }
    }


}
