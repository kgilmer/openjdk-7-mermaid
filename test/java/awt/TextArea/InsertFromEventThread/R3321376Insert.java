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
 @summary <rdar://problem/3321376> Cannot insert into TextArea from event thread
 @summary com.apple.junit.java.text.Insertion;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 */

import com.apple.junit.utils.*;

import junit.framework.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class R3321376Insert extends TestCase {
    volatile Waypoint didKey = new Waypoint();

    class TestFrame extends Frame {
        TextArea cc;

        public TestFrame( String s ) {
            super( s );
            setSize( 300, 200 );
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = getSize();
            frameSize.height = Math.min( frameSize.height, screenSize.height );
            frameSize.width = Math.min( frameSize.width, screenSize.width );
            setLocation( (screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2 );
            cc = new TextArea( 4, 40 );
            add( "Center", cc );
            cc.setFont( new Font( "Courier", Font.PLAIN, 12 ) );
            cc.addKeyListener( new TEKeyListener() );
        }

        void doTestAction() throws RuntimeException {
            RobotUtilities.click( cc );
            RobotUtilities.delay(50); // shouldn't need this, but pause adds stability
            didKey.reset();
            RobotUtilities.pressKey( KeyEvent.VK_ENTER );
            didKey.requireClear();
            didKey.reset();
            RobotUtilities.pressKey( KeyEvent.VK_ENTER );
            didKey.requireClear();
        }

        String getTestText() {
            return (cc.getText());
        }
    }

    class TEKeyListener extends KeyAdapter {
        public void keyPressed( KeyEvent e ) {
            TextArea cc = (TextArea) e.getComponent();
            char key = e.getKeyChar();
            if (key == '\n') {
                cc.insert( "handle-cr", 0 );
                e.consume();
                didKey.clear();
            }
        }
    }

    // Note that this test will FAIL if a global modal dialog (like CrashCatcher!) is onscreen when it runs
    // We could conceivably take a screenshot just before running the test, and save it if the test fails...
    public void testKeyAdaptor() throws Exception {
        TestFrame frame = null;
        String result = "";
        try {
            frame = new TestFrame( "TestFrame" );
            frame.pack();
            VisibilityValidator.setVisibleAndConfirm(frame);

            // This makes sure that the textfield has focus 
            // (since this will happen behind the FOCUS_GAINED event sent by frame.setVisible(true)())
            // Note that doTestAction does a redundant "click", since the textarea has focus by default
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {} }); } catch (Exception e) {}
            
            frame.doTestAction();
            Thread.sleep( 500 ); // Should do something smarter here.  500 ms should be enough, though

            // This is a (lame) attempt at waiting for the last typeKey events to propagate
            // It's not quite right because robot uses CGRemoteOperations, which are asynchronous
            // It also might not be enough because I think the KeyAdaptor relies on a double-event dispatch
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {} }); } catch (Exception e) {}
            
            result = frame.getTestText();
            String expected = "handle-crhandle-cr";
            assertEquals( "Bad result from getText().\nExpected: \n\t*" + expected + "*\n Actual:\n\t*" + result + "*\n", expected, result );
        }
        finally {
            frame.dispose();
        }
    }

    public static Test suite() {
        return new TestSuite( R3321376Insert.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
