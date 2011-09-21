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
 * @summary Test case flips into fullscreen mode, and then runs 50 frames of animation in that mode
 * @summary com.apple.junit.java.graphics.fullscreen
 * @library ../regtesthelpers
 * @build MovingPoints
 * @run main TestFullScreen
 */

import junit.framework.*;
import test.java.awt.regtesthelpers.MovingPoints;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.Random;

public class TestFullScreen extends TestCase {

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public static Test suite() {
        return new TestSuite(TestFullScreen.class);
    }

    GraphicsDevice dev = null;
    Frame fullFrame = null;
    GraphicsEnvironment ge = null;

    // Use 32bit, 1024 x 768 as the standard mode for this particular test.
    static final int STDDEPTH = 32;
    static final int STDHEIGHT = 768;
    static final int STDWIDTH = 1024;

    private DisplayMode getStandardMode(DisplayMode[] modes) {
        DisplayMode stdMode = null;
        for (int i = 0; i < modes.length; i++) {
            if ((stdMode == null) && (modes[i].getWidth() == STDWIDTH)
                    && (modes[i].getHeight() == STDHEIGHT)
                    && (modes[i].getBitDepth() == STDDEPTH)) {
                stdMode = modes[i];
            }
        }
        return (stdMode);
    }

    private Frame fullScreenFrame(DisplayMode newMode) throws Exception {
        Frame full = new Frame(dev.getDefaultConfiguration());
        full.setSize(newMode.getWidth(), newMode.getHeight());
        full.setUndecorated(true);
        full.setIgnoreRepaint(true);
        dev.setFullScreenWindow(full);
        if (dev.isDisplayChangeSupported() && (newMode != null)) {
            dev.setDisplayMode(newMode);
        } else {
            throw (new Exception("Display Change Problem"));
        }

        int expected = newMode.getHeight();
        int actual = full.getHeight();
        assertEquals("Bad height: expected " + expected + ", actual: " + actual, expected, actual);

        expected = newMode.getWidth();
        actual = full.getWidth();
        assertEquals("Bad width: expected " + expected + ", actual: " + actual, expected, actual);

        return full;
    }


    protected void setUp() throws Exception {
        super.setUp();
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        assertNotNull(ge);
        dev = ge.getDefaultScreenDevice();
        assertNotNull(dev);
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();

        assertNotNull(dev);
        dev.setFullScreenWindow(null);
        if (fullFrame != null) {
            fullFrame.dispose();
            fullFrame = null;
        }
    }

    public void testFullScreen() throws Exception {
        DisplayMode[] modes = dev.getDisplayModes();
        //  Standard Screen Size (1024 x 768 x 32)
        long start, animate, render;

        DisplayMode stdMode = getStandardMode(modes);
        fullFrame = fullScreenFrame(stdMode);
        assertNotNull(fullFrame);
        initScene(fullFrame);

        // Time the animation overhead
        start = System.currentTimeMillis();
        animateLoop();
        animate = (System.currentTimeMillis() - start);

        // Time real rendering
        start = System.currentTimeMillis();
        renderLoop(fullFrame);
        render = (System.currentTimeMillis() - start);

    }

    // setup the scene
    private void initScene(Frame full) {
        for (int j = 0; j < ovals.length; j++) {
            ovals[j] = new ActiveOval(full.getBounds());
        }
        for (int j = 0; j < lines.length; j++) {
            lines[j] = new ActiveLine(full.getBounds());
        }
    }

    // loop to just do the math required to animate the scene (for comparision)
    private void animateLoop() {
        for (int i = 0; i < FRAMES; i++) {
            for (int j = 0; j < ovals.length; j++) {
                ovals[j].move();
            }
            for (int j = 0; j < lines.length; j++) {
                lines[j].move();
            }
        }
    }

    // loop to render the scene
    private void renderLoop(Frame full) {
        Rectangle bounds = full.getBounds();
        full.createBufferStrategy(2);
        BufferStrategy bufferStrategy = full.getBufferStrategy();
        for (int i = 0; i < FRAMES; i++) {
            Graphics g = bufferStrategy.getDrawGraphics();
            if (!bufferStrategy.contentsLost()) {
                g.setColor(Color.white);
                g.fillRect(0, 0, bounds.width, bounds.height);
                g.setColor(Color.red);
                for (int j = 0; j < ovals.length; j++) {
                    ovals[j].move();
                    ovals[j].render(g);
                }
                g.setColor(Color.blue);
                for (int j = 0; j < lines.length; j++) {
                    lines[j].move();
                    lines[j].render(g);
                }
                bufferStrategy.show();
                g.dispose();
            }
        }
    }

    //  Constants controlling the scene
    static final int FRAMES = 50;
    static final int LINES = 500;
    static final int OVALS = 500;
    ActiveLine[] lines = new ActiveLine[LINES];
    ActiveOval[] ovals = new ActiveOval[OVALS];
    static final Random rand = new Random(12345);
    static final int SPEED = 5;

    class ActiveLine extends MovingPoints {

        public ActiveLine(Rectangle r) {
            super(r, 2 /* vertices */, 5 /*speed*/);
        }

        public void render( Graphics g ) {
            int x[] = getXs();
            int y[] = getYs();
            g.drawLine( x[0], y[0], x[1], y[1] );
        }       
    }

    class ActiveOval extends MovingPoints {
        int h;
        int w;

        public ActiveOval(Rectangle r) {
            super(r, 1 /* vertices */, 5 /*speed*/);
            w = rand.nextInt(20) + 5;
            h = rand.nextInt(20) + 5;       }

        public void render( Graphics g ) {
            int x = getXs()[0];
            int y = getYs()[0];
            g.fillOval(x, y, w, h);
        }       
 
    }
}
