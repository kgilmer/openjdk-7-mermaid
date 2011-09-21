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
 @summary Verify the proper number of ComponentEvents are delivered
 @summary com.apple.junit.java.awt.Event;
 @library ../../../regtesthelpers
 @build VisibilityValidator
 @run main ContainerAddedTest
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.AWTEventListener;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class ContainerAddedTest extends TestCase {
    static final int LOOP = 100;

    class ContainerEventCounter {
        static final int TIMEOUT = 5000; // Wait up to five seconds
        private int counter = 0;
        private AWTEventListener listener = null;

        public ContainerEventCounter() {
            listener = new AWTEventListener() {
                public void eventDispatched( AWTEvent e ) {
                    // System.out.println("AWTEvent: " + e);
                    doEvent(e);
                }
            };
            Toolkit.getDefaultToolkit().addAWTEventListener( listener, AWTEvent.CONTAINER_EVENT_MASK );
        }

        public void dispose() {
            Toolkit.getDefaultToolkit().removeAWTEventListener( listener );
        }
        
        synchronized public boolean requireEvents( int total ) {
            long endtime = System.currentTimeMillis() + TIMEOUT;
            try {
                while (counter < total) {
                    if (System.currentTimeMillis() < endtime) {
                        wait( TIMEOUT );
                    }
                    else {
                        break;
                    }
                }
            }
            catch (InterruptedException ix) {
            }
            if (counter != total) {
                System.out.println("### ERROR: expected " + total + " events but received " + counter + " events.");
            }
            return (counter == total);
        }

        synchronized void doEvent(AWTEvent e) {
            counter++;
            notify();
        }
    }

    public void testContainerEvents01() throws Exception {
        Frame frame = null;
        ContainerEventCounter collector = null;
        try {
            // Thread.currentThread().setName( "ContainerAddedTest Thread" );

            // Bring up a test frame
            frame = new Frame( "ContainerAddedTest " );
            frame.setLayout( new FlowLayout() );
            frame.setSize( 300, 400 );
            VisibilityValidator.setVisibleAndConfirm(frame);

            // Add components and count the events
            collector = new ContainerEventCounter();
            assertTrue( "Nothing added yet, no container events expected", collector.requireEvents( 0 ) );
            for (int i = 0; i < LOOP; i++) {
                frame.add( new Label( "-" + i + "-" ) );
            }
            assertTrue( LOOP + " events expected", collector.requireEvents( LOOP ) );

            // ### todo -- add a test which collects events with eventListener

            /*
            // Add these tests after Greg's fix is done
            
            assertFalse("The frame should not be valid yet", frame.isValid());
    
            Thread.sleep(500);    // <-- should be smarter than just waiting .5 seconds here...  
            */

            frame.validate();
            assertTrue( "The frame should be valid now", frame.isValid() );
        }
        finally {
            if (frame != null) {
                frame.setVisible( false );
                frame.dispose( );
            }

            if (collector != null) {
                collector.dispose();
            }

        }
    }

    public static Test suite() {
        return new TestSuite( ContainerAddedTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
