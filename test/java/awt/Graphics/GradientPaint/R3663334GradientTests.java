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
 * @summary <rdar://problem/3663334> SJSC: 1.4.2DP1: Using a GradientPaint (x, y, color1, x, y, color2, true) hangs AWT thread
 * @summary com.apple.junit.java.graphics.GradientPaint
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main R3663334GradientTests
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;


public class R3663334GradientTests extends TestCase {

        private static final boolean DEBUG = false;

        // Use primary colors to simplify the check that we get back a reasonable gradient
        private static final Color kGradientPrimary1 = Color.blue;
        private static final Color kGradientPrimary2 = Color.green;
        private static final int kColorThreshhold = 32;

        // A color used to check that things draw at all.  Since we use green and blue elsewhere, we choose red here
        private static final Color kBackgroundColor = Color.red;

        private Robot robot;
        private DisplayFrame m = null;
        
        // State machine for testXXX() & DisplayFrame
        private volatile int state = 0;
        static final int BEFORE_TEST = 0;
        static final int DOING_TEST = 1;
        static final int AFTER_TEST = 2;
        
        class DisplayFrame extends JFrame {
            int width;
            int height;
            
            public DisplayFrame( Dimension gradientSize ) {
                super("DisplayFrame");
                this.width  = (int) gradientSize.getWidth();
                this.height = (int) gradientSize.getHeight();
                setBounds (20, 20, 400, 400);
            }

            public void makeVisible() throws Exception {
            }

            public void drawBackdrop (Graphics g) {
                    Dimension size = getSize();
                    g.setColor(kBackgroundColor);
                    g.fillRect(0, 0, size.width, size.height);
            }

            public void drawGradient (Graphics g) {
                    // fill with white
                    Dimension size = getSize();
                    g.setColor(Color.white); // anything but kBackgroundColor
                    g.fillRect(0, 0, size.width, size.height);
                    
                    // note that a zero-sized gradient paint may be transparent
                    Graphics2D g2d = (Graphics2D) g;
                    if (DEBUG) System.err.println("Constructing gradient paint");
                    GradientPaint gp = new GradientPaint (20, 20, kGradientPrimary1, 20+width, 20+height, kGradientPrimary2, true);
                    if (DEBUG) System.err.println("Setting gradient paint");
                    g2d.setPaint(gp);
                    if (DEBUG) System.err.println("Using gradient paint");
                    g.fillRect (0,0,getWidth(),getHeight());
            }

            public void paint (Graphics g) {
                switch (state) {
                    case BEFORE_TEST:
                        drawBackdrop(g);
                        break;

                    case DOING_TEST:
                        drawGradient(g);
                        break;

                    case AFTER_TEST:
                        drawBackdrop(g);
                        break;
                }
            }
        }

        protected void setUp() throws Exception {
            robot = new Robot();
        }
                
        protected void tearDown() throws Exception {
            m.dispose();
        }

        public void testGradientPaintDrawsReasonableColors() throws Exception {
            Dimension gradientSize = new Dimension(100,10);
            m = new DisplayFrame(gradientSize);
            VisibilityValidator.setVisibleAndConfirm(m);

            // Now do some heavily bullet-proofed checks that we are visible

            // 1 make sure the frame is up - loop until the frame is and we see the red backdrop
            state = BEFORE_TEST;
            assertTrue("Problems waiting for background to draw.", waitOnCenterColor(m, EXPECT_BACKGROUND) );

            // 2 loop until the frame is changes from red to something else
            state = DOING_TEST;
            m.repaint();
            assertTrue("Problems waiting for gradient to draw.", waitOnCenterColor(m, !EXPECT_BACKGROUND));
            // checkForGoodColors();

            // 3 loop until the frame changes back to the red backdrop
            state = AFTER_TEST;
            m.repaint();
            assertTrue("Problems waiting for old background to draw.", waitOnCenterColor(m, EXPECT_BACKGROUND) );
        }

        public void testZeroSizedGradientPaintDoesntHang() throws Exception {
            Dimension gradientSize = new Dimension(0,0);
            m = new DisplayFrame(gradientSize);
            VisibilityValidator.setVisibleAndConfirm(m);

            // Now do some heavily bullet-proofed checks that we are visible

            // 1) make sure the frame is up - loop until the frame is and we see the red backdrop
            state = BEFORE_TEST;
            assertTrue("Problems waiting for background to draw.", waitOnCenterColor(m, EXPECT_BACKGROUND) );

            // 2) loop until the frame is changes from red to something else
            state = DOING_TEST;
            m.repaint();
            assertTrue("Problems waiting for gradient to draw.", waitOnCenterColor(m, !EXPECT_BACKGROUND));

            // 3) loop until the frame changes back to the red backdrop
            state = AFTER_TEST;
            m.repaint();
            assertTrue("Problems waiting for old background to draw.", waitOnCenterColor(m, EXPECT_BACKGROUND));
        }


        // Test for valid colors.  Poke around the center some.   Note that the colors used in the gradient for this testcase were specially
        // chosen to make these checks easier; this code will not work for any random Gradient fill.
        
        void checkForGoodColors() throws Exception {
            Rectangle r = m.getBounds();
            
            assertTrue( "Needs a reasonable sized rectangle", r.getWidth() > 10);
            assertTrue( "Needs a reasonable sized rectangle", r.getHeight() > 10);
            
            int x = (int) r.getX() + (int) r.getWidth() / 2;
            int y = (int) r.getY() + (int) r.getHeight() / 2;
           
            for (int i = -4; i <=4; i++) {
                for (int j = -4; j <=4; j++) {
                    Color c = null;
                    c = robot.getPixelColor(x+i, y+j);
                    // Is c reasonable based on gradient parameters?
                    // We used primary colors for the two ends of the gradient, so the sum of the colors should be close to 255
                    assertTrue( "Unexpected color in gradient fill", c.getRed() + c.getGreen() + c.getBlue() > (255 - kColorThreshhold) );
                    assertTrue( "Unexpected color in gradient fill", c.getRed() + c.getGreen() + c.getBlue() <= 255 );
                    
                    // We avoided red altogether, so we shouldnt have much red
                    assertTrue( "Unexpected color in gradient fill", c.getRed() < kColorThreshhold );
                }
            }
        }
        
        // A loop that actively tries to repaint the component until the color of the center pixel does something interesting.
        // Note the the result needs to be checked afterwards, as the loop times out after a bit (kPaintingTimeoutMillis)
        // 
        //  a) expectBackground == true  ( waits for us to match the background)
        //  b) expectBackground == false ( waits for us to NOT match the background)
        
        static final int kPaintingTimeoutMillis = 10000;
        static final boolean EXPECT_BACKGROUND = true;
        
        public boolean waitOnCenterColor(Component comp, boolean expectBackground) throws Exception {
            boolean gotExpectedColor = false;
            Rectangle r = comp.getBounds();
            int x = (int) r.getX() + (int) r.getWidth() / 2;
            int y = (int) r.getY() + (int) r.getHeight() / 2;
           
            Color c = null;
            long endtime = System.currentTimeMillis() + kPaintingTimeoutMillis;
            while (System.currentTimeMillis() < endtime) {
                c = robot.getPixelColor(x, y);
                if (DEBUG) System.err.println(c);
                // Loop until we see what we expect to see, or we timeout
                if (VisibilityValidator.colorMatch(kBackgroundColor, c) == expectBackground) {
                    gotExpectedColor = true;
                    break;  // we see what we expect, continue
                }

                // loop needs to actively repaint, as sometimes paints are not synchronous
                m.repaint(); 
                Thread.sleep(250); // a vaguely reasonable time to wait for a repaint to finish
            }

            return (gotExpectedColor);
        }

        // Boilerplate below
        
        public static Test suite() {
            return new TestSuite(R3663334GradientTests.class);
        }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }


}
