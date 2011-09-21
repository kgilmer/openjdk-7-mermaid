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
 * @summary <rdar://problem/3338508> Events-mouse: Mouse events don't work at all in fullscreen mode
 * @summary <rdar://problem/3439508> 1.4.1 v.1: Full Screen Mode: can't catch mouse events
 * @summary com.apple.junit.java.graphics.fullscreen
 * @library ../regtesthelpers
 * @build RobotUtilities
 * @run main TestFullScreenEvents
 */

import junit.framework.*;
import test.java.awt.regtesthelpers.RobotUtilities;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.BufferStrategy;

public class TestFullScreenEvents extends TestCase {
    
    private GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private DisplayMode oldDisplayMode = device.getDisplayMode();
    private Graphics currentGraphics;

    private static final int TIMEOUT = 5000; // Wait up to five seconds
    private BufferStrategy strategy;

    private volatile int eventcount;

    protected Timer ticktock = null;
    protected TestFrame f = null;

    class TestFrame extends Frame {

        public TestFrame( int width, int height ) throws Exception {

            // Do a bunch of stuff to get us into Fulllscreen
            DisplayMode newDisplayMode = new DisplayMode( width, height, oldDisplayMode.getBitDepth(), oldDisplayMode.getRefreshRate() );
            //System.out.println("setting display mode: " + newDisplayMode.getWidth() + " " + newDisplayMode.getHeight() + " " + newDisplayMode.getBitDepth() + " " + newDisplayMode.getRefreshRate() );
            setUndecorated( true );
            device.setFullScreenWindow( this );
            device.setDisplayMode( newDisplayMode );
            createBufferStrategy( 2 );
            strategy = getBufferStrategy();

            // Add in the listeners
            addKeyListener( new KeyAdapter() {
                public void keyPressed( KeyEvent e ) {
                    doEvent();
                }
            } );
            addMouseListener( new MouseAdapter() {
                public void mousePressed( MouseEvent e ) {
                    doEvent();
                }
            } );
        }

        // events will get us out of the loop below
        synchronized void doEvent() {
            eventcount++;
            notify();
        }

        // smart loop waiting for events
        synchronized public boolean loopForEvents( int total ) throws Exception {
            long endtime = System.currentTimeMillis() + TIMEOUT;
            int count = 0;
            while (eventcount < total) {
                if (System.currentTimeMillis() < endtime) {
                    Graphics g = getGraphicsContext();
                    g.setColor( Color.black );
                    g.fillRect( 0, 0, oldDisplayMode.getWidth(), oldDisplayMode.getHeight() );
                    g.setColor( Color.white );
                    g.drawString( (count++) + " Waiting for the Robot to press any key or click mouse...", 100, 100 );
                    doDisplay();
                    wait( TIMEOUT / 100 );
                }
                else {
                    break;
                }
            }
            return (eventcount == total);
        }

        // display logic
        public Graphics getGraphicsContext() {
            if (currentGraphics == null) {
                currentGraphics = strategy.getDrawGraphics();
            }
            return currentGraphics;
        }

        // more display logic
        public void doDisplay() throws Exception {
            currentGraphics.dispose();
            currentGraphics = null;
            strategy.show();
        }

        // important cleanup -- this gets us back out of fullscreen
        public void dispose() {
            if (oldDisplayMode != null) {
                //System.out.println("re-setting display mode: " + oldDisplayMode.getWidth() + " " + oldDisplayMode.getHeight() + " " + oldDisplayMode.getBitDepth() + " " + oldDisplayMode.getRefreshRate() );
                device.setDisplayMode( oldDisplayMode );
                device.setFullScreenWindow( null );
            }
            super.dispose();
        }
    }

    protected void setUp() throws Exception {
        ticktock = new Timer();
        f = new TestFrame( oldDisplayMode.getWidth(), oldDisplayMode.getHeight() );
    }
    
    public void testFullScreenMouseEvent() throws Exception {

        // In a few seconds, click...
        TimerTask action = new TimerTask()        {
            public void run() {
                RobotUtilities.clickAt( f, 100, 100 );
            }
        };
        ticktock.schedule( action, 2500L );

        // Meanwhile, wait for the event
        boolean gotEvent = f.loopForEvents( 1 );
        assertTrue( "We should have gotten a mouse click", gotEvent );
    }
    
    public void testFullScreenKeyEvent() throws Exception {

        // In a few seconds, hit the space bar
        TimerTask action = new TimerTask()        {
            public void run() {
                RobotUtilities.typeKey( KeyEvent.VK_SPACE );
            }
        };
        ticktock.schedule( action, 2500L );

        // Meanwhile, wait for the event
        boolean gotEvent = f.loopForEvents( 1 );
        assertTrue( "We should have gotten a keyboard space bar pressed", gotEvent );
    }
    
    protected void tearDown() throws Exception {
        f.dispose();
        ticktock.cancel();
    }

    public static Test suite() {
        return new TestSuite( TestFullScreenEvents.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
