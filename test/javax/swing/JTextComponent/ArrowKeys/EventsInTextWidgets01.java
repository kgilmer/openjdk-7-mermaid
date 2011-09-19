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
 @summary <rdar://problem/4335048> [JavaJDK16] JTextField: Up/down arrows in edit-text fields are not the same as MacOS
 @summary com.apple.junit.java.awt.Event;
 @library ../../../../java/awt/regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main EventsInTextWidgets01
 */

import junit.framework.*;

import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class EventsInTextWidgets01 extends TestCase
{
    // boiler plate
    public static Test suite() {
        return new TestSuite(EventsInTextWidgets01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test makes an assumption about text component navigation on Mac OS X
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
    
    // variables needed for the test
    Waypoint caretWatcher = new Waypoint();
    static final String testData = 
        "Three blank lines follow\n" +
        "\n" +
        "\n" +
        "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen\n" +
        "1\n" +
        "2\n" +
        "3\n" +
        "4\n" +
        "5\n" +
        "6\n" +
        "7\n" +
        "8\n" +
        "9\n" +
        "0\n" +
        "The quick brown fox jumped over the lazy programmer";

    // for debugging
    static final boolean HUMAN_SPEED = false; 
    static final int HUMAN_INTERVAL = 500; 
    
    // arrow around a bit, see if we get what we expect
    public void testSimpleMovesInJTextArea() throws Exception {
        final TestFrame f = new TestFrame(0);
        
        try {
            VisibilityValidator.setVisibleAndConfirm(f);

            // Install a Waypoint that gets tickled when the caret moves
            JTextComponent text = f.getText();
            text.addCaretListener( new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    caretWatcher.clear();                    
                }
            });
            
            // Start at the "r" of "three"
            text = f.getText();
            int start = text.getText().indexOf("three");            
            caretWatcher.reset();
            text.setCaretPosition(start+2);
            caretWatcher.requireClear("Expected a caret change in response to setCaretPosition", 500);
            doEqualsCheck("Should be at 37", 37, text.getCaretPosition());
            
            // Move the text caret around, using the Robot to generate a key strokes
            doType(KeyEvent.VK_DOWN);
            doEqualsCheck("Down and over to after 1 expected", 117, text.getCaretPosition());

            doType(KeyEvent.VK_UP);
            doEqualsCheck("Back to original position expected", 37, text.getCaretPosition());

            doType(KeyEvent.VK_LEFT);
            doEqualsCheck("Left a bit expected", 36, text.getCaretPosition());

            doType(KeyEvent.VK_RIGHT);
            doEqualsCheck("Back right a bit expected", 37, text.getCaretPosition());

        }
        finally {
            f.dispose();
        }
    }
    
    
    // arrow around a bit, see if we get what we expect
    public void testSimpleMovesInJTextField() throws Exception {
        final TestFrame f = new TestFrame(1);
        
        try {
            VisibilityValidator.setVisibleAndConfirm(f);

            // Install a Waypoint that gets tickled when the caret moves
            JTextComponent text = f.getText();
            text.addCaretListener( new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    caretWatcher.clear();                    
                }
            });
            
            // Start at the "r" of "three"
            text = f.getText();
            int start = text.getText().indexOf("three");            
            caretWatcher.reset();
            text.setCaretPosition(start+2);
            caretWatcher.requireClear("Expected a caret change in response to setCaretPosition", 500);
            doEqualsCheck("Should be at 37", 37, text.getCaretPosition());
            
            // Move the text caret around, using the Robot to generate a key strokes
            doType(KeyEvent.VK_DOWN);
            doEqualsCheck("End of string expected", 187, text.getCaretPosition());

            doType(KeyEvent.VK_UP);
            doEqualsCheck("Start of string expected", 0, text.getCaretPosition());

            doType(KeyEvent.VK_RIGHT);
            doEqualsCheck("Back right a bit expected", 1, text.getCaretPosition());

            doType(KeyEvent.VK_LEFT);
            doEqualsCheck("Back to original position expected", 0, text.getCaretPosition());
        }
        finally {
            f.dispose();
        }
    }

    
    // arrow around a bit, see if we get what we expect
    public void testSimpleMovesInJEditorPane() throws Exception {
        final TestFrame f = new TestFrame(2);
        
        try {
            VisibilityValidator.setVisibleAndConfirm(f);

            // Install a Waypoint that gets tickled when the caret moves
            JTextComponent text = f.getText();
            text.addCaretListener( new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    caretWatcher.clear();                    
                }
            });
            
            // Start at the "r" of "three"
            text = f.getText();
            int start = text.getText().indexOf("three");            
            caretWatcher.reset();
            text.setCaretPosition(start+2);
            caretWatcher.requireClear("Expected a caret change in response to setCaretPosition", 500);
            doEqualsCheck("Should be at 37", 37, text.getCaretPosition());
            
            // Move the text caret around, using the Robot to generate a key strokes
            doType(KeyEvent.VK_DOWN);
            doChangeCheck("We should move down some", 37, text.getCaretPosition());

            doType(KeyEvent.VK_UP);
            doEqualsCheck("Back to original position expected", 37, text.getCaretPosition());

            doType(KeyEvent.VK_LEFT);
            doEqualsCheck("Left a bit expected", 36, text.getCaretPosition());

            doType(KeyEvent.VK_RIGHT);
            doEqualsCheck("Back right a bit expected", 37, text.getCaretPosition());

        }
        finally {
            f.dispose();
        }
    }
    
    // arrow around a bit, see if we get what we expect
    public void testSimpleMovesInJTextPane() throws Exception {
        final TestFrame f = new TestFrame(3);
        
        try {
            VisibilityValidator.setVisibleAndConfirm(f);

            // Install a Waypoint that gets tickled when the caret moves
            JTextComponent text = f.getText();
            text.addCaretListener( new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    caretWatcher.clear();                    
                }
            });
            
            // Start at the "r" of "three"
            text = f.getText();
            int start = text.getText().indexOf("three");            
            caretWatcher.reset();
            text.setCaretPosition(start+2);
            caretWatcher.requireClear("Expected a caret change in response to setCaretPosition", 500);
            doEqualsCheck("Should be at 37", 37, text.getCaretPosition());
            
            // Move the text caret around, using the Robot to generate a key strokes
            doType(KeyEvent.VK_DOWN);
            doEqualsCheck("Down and over to after 1 expected", 117, text.getCaretPosition());

            doType(KeyEvent.VK_UP);
            doEqualsCheck("Back to original position expected", 37, text.getCaretPosition());

            doType(KeyEvent.VK_LEFT);
            doEqualsCheck("Left a bit expected", 36, text.getCaretPosition());

            doType(KeyEvent.VK_RIGHT);
            doEqualsCheck("Back right a bit expected", 37, text.getCaretPosition());

        }
        finally {
            f.dispose();
        }
    }
    
    private void doType(int num) throws InterruptedException {
        Thread.sleep(100);
        if (HUMAN_SPEED) { Thread.sleep(HUMAN_INTERVAL); }
        caretWatcher.reset();
        RobotUtilities.typeKey( num );
        caretWatcher.requireClear("Expected a caret change in response to key event " + num, 500);
    }
    
    private void doEqualsCheck(String msg, int expected, int actual) throws InterruptedException {
        if (HUMAN_SPEED) { Thread.sleep(HUMAN_INTERVAL); }
        assertEquals(msg, expected, actual);
    }
    
    private void doChangeCheck(String msg, int before, int after) throws InterruptedException {
        if (HUMAN_SPEED) { Thread.sleep(HUMAN_INTERVAL); }
        assertTrue(msg, before != after);
    }

    
    class TestFrame extends JFrame {
        JTextComponent text = null;

        public TestFrame(int kind) {
            switch( kind % 4) {
            case 0:  text = new JTextArea();    break;
            case 1:  text = new JTextField();    break;
            case 2:  text = new JEditorPane();    break;
            case 3:  text = new JTextPane();    break;
            }

            text.setText(testData );
            int startPos = testData.indexOf("three");
            text.setCaretPosition(startPos);
            JScrollPane scrollPane = new JScrollPane(text);
            getContentPane().add(scrollPane);
            setTitle( text.getClass().getName());
            pack();
        }
        
        public JTextComponent getText() {
            return text;
        }

    }

}




