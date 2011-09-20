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
 @summary Tests for http://java.sun.com/j2se/1.4.2/docs/api/java/awt/doc-files/FocusSpec.html
 @summary com.apple.junit.java.awt.Event;
 @run main FocusParity01
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.*;

public class FocusParity01 extends TestCase
{
    //
    // From http://java.sun.com/j2se/1.4.2/docs/api/java/awt/doc-files/FocusSpec.html
    //
    //
    // In addition, each event type will be dispatched in 1-to-1 correspondence with
    // its opposite event type. For example, if a Component receives a FOCUS_GAINED
    // event, under no circumstances can it ever receive another FOCUS_GAINED event
    // without an intervening FOCUS_LOST event.
    //
    
    static final int TIMEOUT = 5000; // Wait up to five seconds

    class DrawnFrame extends Frame {
        private boolean painted = false;
        private Object watcher = null;
    
        public DrawnFrame( String title) {
            super(title);
            watcher = new Object();
        }
    
        public void paint( Graphics g) {
            synchronized(watcher) {
                painted = true;
                watcher.notify();
            }
            Dimension r = getSize();
            g.setColor(Color.red);
            g.drawRect( 0, 0, r.width, r.height );
            g.drawLine( 0, 0, r.width, r.height );
            g.drawLine( r.width, 0, 0, r.height );
        }
        
        public boolean requirePainted() {
            synchronized(watcher) {
                long endtime = System.currentTimeMillis() + TIMEOUT;
                try {
                    while (!painted) {
                        if (System.currentTimeMillis() < endtime) {
                            // System.out.println("Waiting " + title);
                            repaint();
                            watcher.wait( 200 );
                        }
                    }
                    // System.out.println("Clear " + title);
                }
                catch (InterruptedException ix) {
                }
            }
            return (painted);
        }
        
    }

    class FocusParityCounter implements WindowFocusListener, FocusListener {
        int focus_parity  =0;
        int wfocus_parity  =0;
        
        synchronized public void focusGained(FocusEvent e) {
            focus_parity++;
        }
    
        synchronized public void focusLost(FocusEvent e) {
            focus_parity--;
        }
    
        synchronized public void windowGainedFocus(WindowEvent e) {
            wfocus_parity++;
        }
    
        synchronized public void windowLostFocus(WindowEvent e) {
            wfocus_parity--;
        }
    
        synchronized void check() {
            assertTrue("focus_parity <= 1; actually focus_parity ==" + focus_parity, focus_parity <= 1);
            assertTrue("wfocus_parity <= 1; actually wfocus_parity ==" + wfocus_parity, wfocus_parity <= 1);
        }
    }

    //
    //    Note that this is not truly general purpose.  It watches all events, not just container events
    //

    protected DrawnFrame[] frames;
    protected FocusParityCounter[] listeners;
    
    protected void setUp() {
        frames = new DrawnFrame[10];
        listeners = new FocusParityCounter[10];
        for( int i = 0; i < frames.length; i++) {
            frames[i] = new DrawnFrame("Test Frame " + i);
            listeners[i] = new FocusParityCounter();
            frames[i].addFocusListener( listeners[i] );
            frames[i].addWindowFocusListener( listeners[i] );
            frames[i].setBounds(25 + i*5, 25 + i*5, 200, 100);
        }
    }

    public void testBasicDisplay() throws Exception {
        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].setVisible(true);
            listeners[i].check();
            assertTrue("frame " + i + "painted by now", frames[i].requirePainted());
            listeners[i].check();
        }

        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
            frames[i].toBack();
            listeners[i].check();
        }

        Thread.sleep(100);


        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
            frames[i].toBack();
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);
    }
    
    public void testWithSubcomponents() throws Exception {
        for( int i = 0; i < frames.length; i++) {
            frames[i].setLayout( new GridLayout(3,3));
            for ( int j = 0; j < 9; j++) {
                frames[i].add(new Button("-"+j+"-"));
            }

            listeners[i].check();
            frames[i].pack();
            frames[i].setVisible(true);
            listeners[i].check();
            assertTrue("frame " + i + "painted by now", frames[i].requirePainted());
            listeners[i].check();
        }

        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
            frames[i].toBack();
            listeners[i].check();
        }

        Thread.sleep(100);


        for( int i = 0; i < frames.length; i++) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);

        for( int i = frames.length-1; i >= 0; i--) {
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
            frames[i].toBack();
            listeners[i].check();
            frames[i].toFront();
            listeners[i].check();
        }

        Thread.sleep(100);
    }
    
    
    protected void tearDown() {
        for( int i = frames.length-1; i >= 0; i--) {
            frames[i].dispose();
        }
    }

    public static Test suite() {
        return new TestSuite(FocusParity01.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}




