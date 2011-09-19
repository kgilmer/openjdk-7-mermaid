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
 @summary Make sure that we can handle mouse and key events in a relatively simple component heirarchy
 @summary This test creates a frame with several nested panels (as well as several labels that tell where things are)
 @summary It then makes sure the frame is visible, requests focus in the top panel, and then either clicks or types in this panel
 @summary com.apple.junit.java.awt.Event;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @run main EventHeirachyWithListeners
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class EventHeirachyWithListeners extends TestCase {
    static final Random rand = new Random(161566);
    static final boolean DEBUG = false;
    private static final int TIMEOUT = 5000; // Timeout for expected events
    protected Timer ticktock;
    protected TestFrame f;


    // Make a frame with several nested panels
    // These panels needs some knowledge of what focus events and click events the test will generate
    @SuppressWarnings("serial")
    class TestFrame extends Frame {
        static final int NUM_NESTED_PANELS = 6;
        static final int BOTTOM_PANEL = 0;
        static final int TOP_PANEL = NUM_NESTED_PANELS-1;
        TestPanel fPanels[] = new TestPanel[NUM_NESTED_PANELS];
        Label fStatusLabel = new Label("Waiting for the test to get around to putting some status into this label.");
        private volatile int fEventCount;
        private volatile boolean fFocused;

        // This is a sub-panel set up with listeners which watch for our test events
        class TestPanel extends Panel {
            // Panel identifier, for debug purposes
            int id;

            public TestPanel( int id)  {
                this.id = id;
                setMinimumSize(new Dimension(200, 100));

                // Give it a vaguely interesting color 
                Color color = new Color( 0x80 + rand.nextInt(0x80), 0x80 + rand.nextInt(0x80), 0x80 + rand.nextInt(0x80) );
                setBackground(color);

                // Hack in some spacers so we can see the nesting
                setLayout( new BorderLayout());
                add( new Label( "-"+id+"-", Label.CENTER), BorderLayout.NORTH);
                add( new Label( "-"+id+"-", Label.CENTER), BorderLayout.SOUTH);
                add( new Label( "-"+id+"-", Label.CENTER), BorderLayout.EAST);
                add( new Label( "-"+id+"-", Label.CENTER), BorderLayout.WEST);

                // Add the listners
                TestFrame.this.doListeners( this );
            }

            // Painting, mostly for debug purposes
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Dimension size = getSize();
                g.setColor( Color.BLACK );
                g.drawString (" Panel " + id , 30, size.height/2 );
            }

            // A more informative toString() for debugging
            @Override
            public String toString() {
                String me = "Test Panel " + id;
                if (id == BOTTOM_PANEL) {
                    me += " - bottom ";
                }
                if (id == TOP_PANEL) {
                    me += " - top";                        
                }
                return me;
            }
        }

        // Constructor to build the frame and add the sub-panels
        public TestFrame( int width, int height ) throws Exception {
            setLayout( new BorderLayout());
            add(fStatusLabel,BorderLayout.NORTH);

            for (int i = 0; i < fPanels.length; i++) {
                fPanels[i] = new TestPanel(i);
                if (i != BOTTOM_PANEL) {
                    fPanels[i-1].add(fPanels[i],BorderLayout.CENTER);                
                }
                else {
                    add( fPanels[BOTTOM_PANEL],BorderLayout.CENTER);            
                }
            }

            // explicitly watch for a focus event on the top panel
            // this get us out of our loopForFocus() in the tests below
            fPanels[TOP_PANEL].addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (DEBUG) {System.out.println(e);}
                    handleFocusEvent();
                }
            });

            setMinimumSize(new Dimension(width, height));
            pack();
        }

        // events will get us out of the loopForEvents below
        synchronized void handleRobotEvent() {
            fEventCount++;
            notify();
        }

        // events will get us out of the loopForFocus loop below
        synchronized void handleFocusEvent() {
            fFocused = true;
            notify();
        }

        // Accessor for the top panel
        public TestPanel getTopPanel() {
            return (fPanels[TOP_PANEL]);
        }

        // Add in the key/mouse listeners
        void doListeners( Component c) {
            c.addKeyListener( new KeyAdapter() {
                @Override
                public void keyPressed( KeyEvent e ) {
                    if (DEBUG) {System.out.println(e);}
                    handleRobotEvent();
                }
            } );
            c.addMouseListener( new MouseAdapter() {
                @Override
                public void mousePressed( MouseEvent e ) {
                    handleRobotEvent();
                    if (DEBUG) {System.out.println(e);}
                }
            } );

        }

        // smart loop waiting for focus event
        synchronized public boolean loopForFocus() throws Exception {
            long endtime = System.currentTimeMillis() + TIMEOUT;
            int count = 0;
            while (fFocused == false) {
                // Debugging feedback
                if (System.currentTimeMillis() < endtime) {
                    fStatusLabel.setText((count++) + " Waiting for the top panel to get focus.");
                    fStatusLabel.repaint();
                    wait( TIMEOUT / 100 );
                }
                else {
                    break;
                }
            }
            return (fFocused);
        }
        
        // smart loop waiting for key/mouse events
        synchronized public boolean loopForEvents( int total, long timeout ) throws Exception {
            long endtime = System.currentTimeMillis() + timeout;
            int count = 0;
            while (fEventCount < total) {
                // Debugging feedback
                if (System.currentTimeMillis() < endtime) {
                    fStatusLabel.setText((count++) + " Waiting for the Robot to press any key or click mouse...");
                    fStatusLabel.repaint();
                    wait( timeout / 100 );
                }
                else {
                    break;
                }
            }
            return (fEventCount == total);
        }
    }


    // setUp delivers a test frame with a focused top panel
    @Override
    protected void setUp() throws Exception {
        ticktock = new Timer();
        f = new TestFrame( 400, 500 );
        VisibilityValidator.setVisibleAndConfirm(f);

        // In a few seconds, make the top panel the fFocused
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                Panel p = f.getTopPanel();
                if (DEBUG) {System.out.println("Grabbing Focus" + p);}
                p.requestFocus();

            }
        };
        ticktock.schedule( action, TIMEOUT/4 );

        // Meanwhile, wait for the event
        boolean focusOK = f.loopForFocus( );
        assertTrue( "Top panel never got a focus event", focusOK );

    }

    // tearDown destroys a test frame
    @Override
    protected void tearDown() throws Exception {
        // Thread.sleep(50000);
        f.dispose();
        ticktock.cancel();
    }

    // Make sure we can handle a simple mouse click
    public void testMouseEvent() throws Exception {

        // In a few more seconds, click the mouse
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                Panel p = f.getTopPanel();                
                RobotUtilities.click(p);
            }
        };
        ticktock.schedule( action, TIMEOUT/4 );

        // Meanwhile, wait for the event
        boolean gotEvent = f.loopForEvents( 1, TIMEOUT );
        assertTrue( "We should have gotten a single mouse click", gotEvent );
    }


    // Make sure we can handle a simple key event
    public void testKeyEvent() throws Exception {
    
        // In a few more seconds, hit the space bar
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                RobotUtilities.typeKey( KeyEvent.VK_SPACE );
            }
        };
        ticktock.schedule( action, TIMEOUT/4 );

        // Meanwhile, wait for the event
        boolean gotEvent = f.loopForEvents( 1, TIMEOUT );
        assertTrue( "We should have gotten a single keyboard space bar pressed", gotEvent );
    }


    // Make sure diableEvent works
    public void testKeyEventDisabled() throws Exception {
    
        // Disable the top panel 
        Panel p = f.getTopPanel();    
        p.setEnabled(false);
        
        // In a few more seconds, hit the space bar
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                RobotUtilities.typeKey( KeyEvent.VK_SPACE );
            }
        };
        ticktock.schedule( action, TIMEOUT/4 );

        // We expect to hit the timeout, as we have disabled the component that gets the event
        // Use a shorter timeout -- 5 seconds is a long time...
        boolean gotEvent = f.loopForEvents( 1, TIMEOUT/3 );
        
        // We should not get events, as we are disabled
        assertFalse( "We should time out without ever getting any events", gotEvent );
    }

    public static Test suite() {
        return new TestSuite( EventHeirachyWithListeners.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
