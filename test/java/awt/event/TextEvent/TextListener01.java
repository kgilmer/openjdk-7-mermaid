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
 @summary Simple test for recieving TextEvents
 @summary com.apple.junit.java.awt.Event;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main TextListener01
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class TextListener01 extends TestCase implements TextListener {
    public static Test suite() {
        return new TestSuite(TextListener01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    protected int textValueChangedCount = 0;
    final Waypoint didEvent = new Waypoint();

    public void textValueChanged( TextEvent e ) {
        textValueChangedCount++;
        didEvent.clear();
    }

    public void xxtestRepeatedly() throws Exception {
        for( int i = 0; i < 1; i++) {
            testTextListener();
        }
    }

    public void testTextListener() throws Exception {
        Frame frame;
        TextField tf;
        frame = new Frame( "TextListener01" );
 
        try {
            frame.setSize( 300, 150 );
            frame.setLocation( new Point( 100, 0 ) );
            tf = new TextField( "Initial text." );
            frame.add( tf, BorderLayout.NORTH );
    
            VisibilityValidator.setVisibleAndConfirm(frame);

            tf.addTextListener( this );
            tf.requestFocus();
            tf.setCaretPosition( 0 );
            RobotUtilities.delay( 1500 );
    
            // Generate a textValueChanged event using Robot to generate a key stroke
            RobotUtilities.typeKey( KeyEvent.VK_I );
    
            didEvent.requireClear("Did not receive textValueChanged event.");
            didEvent.reset();
    
            assertEquals("Expected 1 textValueChangedCount event after key press but received " + textValueChangedCount + ".", textValueChangedCount, 1 );
    
            // Generate a second textValueChanged event by calling setText
            tf.setText( "Text changed by setText()" );

            didEvent.requireClear("Did not receive textValueChanged event.");
            didEvent.reset();

            assertEquals("Expected 2 textValueChangedCount events but received " + textValueChangedCount + ".", textValueChangedCount, 2 );

        }
        finally {
            frame.dispose();
        }
    }
}
