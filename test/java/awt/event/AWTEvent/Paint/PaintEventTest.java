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
 @summary Simple test for defect: <rdar://problem/3627947> Events: PaintEvents not delivered
 @summary com.apple.junit.java.awt.Event;
 @library ../../../regtesthelpers
 @build VisibilityValidator
 @run main PaintEventTest
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class PaintEventTest extends TestCase {
    static final int LOOP = 100;

    class PaintEventCounter {
        static final int TIMEOUT = 5000; // Wait up to five seconds
        
        private int counter = 0;
        private int buttonCounter = 0;
        private int panelCounter = 0;
        private int frameCounter = 0;
        private AWTEventListener listener = null;
        
        public PaintEventCounter() {
            listener = new AWTEventListener() {
                public void eventDispatched( AWTEvent e ) {
                    doEvent(e);
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener( listener , AWTEvent.PAINT_EVENT_MASK );
        }

        public void dispose() {
            Toolkit.getDefaultToolkit().removeAWTEventListener( listener );
        }

        synchronized public boolean requireEvents( int total ) {
            long endtime = System.currentTimeMillis() + TIMEOUT;
            try {
                while (counter < total) {
                    if (System.currentTimeMillis() < endtime) {
                        wait( TIMEOUT / 10 );
                    }
                    else {
                        break;
                    }
                }
            }
            catch (InterruptedException ix) {
            }
            return (counter == total);
        }

        synchronized void doEvent(AWTEvent e) {
            counter++;
            
            // System.out.println(e);
            
            if ( e.getSource() instanceof Button) {
                buttonCounter++;
            }
            
            if ( e.getSource() instanceof Panel) {
                panelCounter++;
            }

            if ( e.getSource() instanceof Frame) {
                frameCounter++;
            }
            notify();
        }

        public int getCounter() {
            return( counter );
        }

        public int getButtonCounter() {
            return( buttonCounter );
        }

        public int getPanelCounter() {
            return( panelCounter );
        }

        public int getFrameCounter() {
            return( frameCounter );        
        }


    }

    public void testPaintEvents01() throws Exception {
        Frame frame = null;
       PaintEventCounter collecter = null;

        try {
            // Thread.currentThread().setName( "PaintEventTest Thread" );

            // Bring up a test frame
            frame = new Frame( "PaintEventTest " );
            frame.setLayout( new FlowLayout() );
            frame.setSize( 300, 400 );

            // Add components and count the events
            collecter = new PaintEventCounter();

            assertTrue( "Nothing visible yet, no events expected", collecter.requireEvents( 0 ) );

            Button but = new Button("Do Action");
            but.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.out.println("\tStartFrame action.");
                }
            });
            Panel  pan = new Panel();
            pan.add(but);
            frame.add(pan);

            VisibilityValidator.setVisibleAndConfirm(frame);

            //
            //    We are getting too many paint events.  We need at least three to continue...
            //
            //    Put assert below back in when we have dealt with:
            //    <rdar://problem/3132190> Painting: We paint twice on startup
            //    <rdar://problem/3535283> Java does about 5x too much painting for each repaint request
            //
            //    Then delete the thread.sleep();
            
            // assertTrue( 3 + " events expected, but got " + collecter.getCounter() + " events", collecter.requireEvents( 3 ) );
            // assertEquals( "Single frame event expected", collecter.getFrameCounter(), 1 );
            // assertEquals( "Single panel event expected", collecter.getPanelCounter(), 1 );
            // assertEquals( "Single button event expected", collecter.getButtonCounter(), 1 );


            collecter.requireEvents(3);
            Thread.sleep(500);
            
            assertTrue( "At least one frame event expected", collecter.getFrameCounter() >= 1 );
            assertTrue( "At least one panel event expected", collecter.getPanelCounter() >= 1 );
            assertTrue( "At least one button event expected", collecter.getButtonCounter() >= 1 );
        }
        finally {
            if ( frame != null) {
                frame.setVisible( false );
                frame.dispose();
            }

            if ( collecter != null) {
                collecter.dispose();
            }

        }
    }
    
    public static Test suite() {
        return new TestSuite( PaintEventTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
