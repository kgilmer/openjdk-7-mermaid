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
 * @summary Fullscreen Mode Flipping Test
 * @summary com.apple.junit.java.graphics.fullscreen
 */

import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.AWTEventListener;

public class TestFullScreenFlip extends TestCase {
    private FullScreenFlip flipFrame_ = null;

    ///////////////////////////////////////////////////
    // Static initialization block to set properties //
    ///////////////////////////////////////////////////
    static {
        System.setProperty("apple.awt.fakefullscreen", "false");
        //System.setProperty("com.apple.macos.useScreenMenuBar", "false");
    }

    public static Test suite() {
        return new TestSuite(TestFullScreenFlip.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }


    public void testFullScreenFlip() throws Exception {
        flipFrame_ = new FullScreenFlip();

        // Auto-test:  flip window mode back-and-forth a few times
        for (int i=0; i<=2; i++) {
            flipFrame_.flipMode();
            Thread.sleep(FullScreenFlip.PAUSE);
        }

        flipFrame_.dispose();
    }


    private class FullScreenFlip extends JFrame {
        /////////////////
        // Global vars //
        /////////////////
        public static final boolean DEBUG = false;
        public static final boolean TRAP_EVENTS = false;

        public static final long PAUSE = 1000;

        private static final int WIDTH  = 400;
        private static final int HEIGHT = 300;

        /////////////////////////
        // Class instance vars //
        /////////////////////////
        private int LEFT = 0;
        private int TOP  = 0;

        private volatile boolean isFullScreen = false;
        private GraphicsDevice device;
        private int timesFlipped;

        /////////////////
        // Constructor //
        /////////////////
        public FullScreenFlip() throws Exception {
            super("Fullscreen Mode Flipping Test");

            //      if (DEBUG) printThread("constructor");

            // Get graphics device
            device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            assertTrue( "Unable to run in fullscreen mode", device.isFullScreenSupported());

            // Initialize times flipped
            setTimesFlipped(-1);

            ////////////////////////////////////////
            // Event trap for diagnosing problems //
            ////////////////////////////////////////
            if (TRAP_EVENTS) {
                Toolkit.getDefaultToolkit().addAWTEventListener
                (
                        new AWTEventListener()
                        {
                            public void eventDispatched(AWTEvent event)
                            {
                                System.out.println("Event: " + event);
                            }
                        },
                        -1
                );
            }

            // General window settings
            setUndecorated(true);
            setBackground(Color.blue);
            setForeground(Color.white);
        }

        /////////////////////////////////////////////////
        // Flip window mode:  fullscreen <--> windowed //
        /////////////////////////////////////////////////
        public void flipMode() throws Exception {
            // Increment flip count
            incrementFlips();

            if (isFullScreen) {
                // Full-screen mode
                if (DEBUG) System.out.println(getTimesFlipped() + ". Fullscreen mode: ON device= ");
                device.setFullScreenWindow(this);

                Thread.sleep(100);  // Give it time to adjust
                setBorders();
                validate();
            }
            else {
                // Windowed mode
                if (DEBUG) System.out.println(getTimesFlipped() + ". Fullscreen mode: OFF");
                device.setFullScreenWindow(null);   // <-- Problem!!!
                Thread.sleep(100);  // Give it time to adjust

                setBorders();
                resizeToInternalSize(WIDTH, HEIGHT);
                //          setSize(WIDTH, HEIGHT);
                setVisible(true);
            }

            // Flip mode flag
            isFullScreen = !isFullScreen;
        }


        ////////////////////////////////////////////
        // Accessor, mutator, incrementer methods //
        ////////////////////////////////////////////
        public int getTimesFlipped() {
            return timesFlipped;
        }

        public void setTimesFlipped(final int tf) {
            timesFlipped = tf;
        }

        public synchronized void incrementFlips() {
            timesFlipped++;
        }

        ///////////////////////////
        // Adjust window borders //
        ///////////////////////////
        private void setBorders() {
            final Insets insets = getInsets();
            LEFT = insets.left;
            TOP  = insets.top;
        }

        ///////////
        // paint //
        ///////////
        public void paint(final Graphics g) {
            //      if (DEBUG) printThread("paint");

            // Move pen's starting point to account for insets
            g.translate(LEFT, TOP);

            g.drawString("Times flipped: " + getTimesFlipped(), 20, 50);
            g.drawString("Press captial F key to flip window mode.", 20, 75);
            g.drawString("Press <esc> key to quit.", 20, 100);
        }

        /////////////////////////////////////////
        // Resize window to account for insets //
        /////////////////////////////////////////
        public void resizeToInternalSize(final int internalWidth, final int internalHeight) {
            Insets insets = getInsets();
            final int newWidth  = internalWidth  + insets.left + insets.right;
            final int newHeight = internalHeight + insets.top  + insets.bottom;

            Runnable resize = new Runnable() {
                public void run() {
                    setSize(newWidth, newHeight);
                }
            };

            if (!SwingUtilities.isEventDispatchThread()) {
                try {
                    SwingUtilities.invokeAndWait(resize);
                }
                catch (Exception ignore) {}
            }
            else
                resize.run();

            validate();
        }
    }
}
