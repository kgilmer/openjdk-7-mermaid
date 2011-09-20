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
 @summary Implements all test in the spec at
 @summary http://download.java.net/jdk6/docs/api/java/awt/doc-files/Modality.html
 @summary com.apple.junit.java.awt.Dialog;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main ModalitySpecTests
 */

import java.awt.*;
import java.awt.event.*;

import junit.framework.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class ModalitySpecTests extends TestCase {
    private static int SPACING = 20;
    
    int xLocation = 0;
    
    TestFrame controlFrame; // This is the frame to click on to push events past blocked windows
    
    TestFrame f;
    TestDialog d1;
    TestDialog d2;
    TestDialog d3;
    
    private void reallyShow(final Window w) {
        VisibilityValidator v = new VisibilityValidator(w);
        if (w instanceof Dialog) {
            new Thread() { public void run() {
                w.setVisible(true);
            }}.start();
        } else {
            w.setVisible(true);
        }
        v.requireVisible();
    }
    
    private void cleanup() {
        xLocation = 0;
        if (d3 != null) { d3.setVisible(false); d3.dispose(); d3 = null; }
        if (d2 != null) { d2.setVisible(false); d2.dispose(); d2 = null; }
        if (d1 != null) { d1.setVisible(false); d1.dispose(); d1 = null; }
        if (f != null) { f.setVisible(false); f.dispose(); f = null; }
        if (controlFrame != null) { controlFrame.setVisible(false); controlFrame.dispose(); controlFrame = null; }
        try {Thread.sleep(500); } catch(Exception e) {} // cleanup paranoia so next test doesn't have to deal with our undisposed bits
    }
    
    public void xtest1() throws Exception {
        try {
            controlFrame = new TestFrame("1: ControlFrame");
            controlFrame.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            reallyShow(controlFrame);
            checkSuccess(controlFrame);
            
            f = new TestFrame("1: Frame");
            reallyShow(f);
            checkSuccess(f);
            
            d1 = new TestDialog(f, "1: d1 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d1);
            
            // f blocked by d1
            checkSuccess(d1);
            checkFailure(f, controlFrame);
            
            d2 = new TestDialog(f, "1: d2 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d2);
            
            // d1 blocked by d2
            checkSuccess(d2);
            checkFailure(d1, controlFrame);
            checkFailure(f, controlFrame);
            
//        } catch (Exception e) { 
//            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    public void xtest2() throws Exception {
        try {
            controlFrame = new TestFrame("2: ControlFrame");
            controlFrame.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            reallyShow(controlFrame);
            checkSuccess(controlFrame);
            
            f = new TestFrame("2: Frame");
            reallyShow(f);
            checkSuccess(f);
            
            d1 = new TestDialog(f, "2: d1 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d1);
            
            // f blocked by d1
            checkSuccess(d1);
            checkFailure(f, controlFrame);
            
            d2 = new TestDialog(d1, "2: d2 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d2);
            
            // d1 blocked by d2
            checkSuccess(d2);
            checkFailure(d1, controlFrame);
            checkFailure(f, controlFrame);
            
            d1.setVisible(false);
            
            //////////////////////////////////////////////////
            // 
            // Modality spec says that "f blocked by d2", but javadoc for setVisible() says
            // "hides this Window, its subcomponents, and all of its owned children"
            // We current follow the javadoc, not the modality spec in behavior. 
            // An alternate test (xtest2b) for the case where d2 is parented by f instead of d1
            // 
            //////////////////////////////////////////////////
            
            // f blocked by d2
            // checkSuccess(d2);
            // checkFailure(f, controlFrame);
            
            // Best guess at this point is that both d1 and d2 should be hidden
            // and f should be unblocked
            checkSuccess(f);
            
//        } catch (Exception e) { 
//            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    public void xtest2b() throws Exception {
        try {
            controlFrame = new TestFrame("2b: ControlFrame");
            controlFrame.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            reallyShow(controlFrame);
            checkSuccess(controlFrame);
            
            f = new TestFrame("2b: Frame");
            reallyShow(f);
            checkSuccess(f);
            
            d1 = new TestDialog(f, "2b: d1 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d1);
            
            // f blocked by d1
            checkSuccess(d1);
            checkFailure(f, controlFrame);
            
            d2 = new TestDialog(f, "2b: d2 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d2);
            
            // d1 blocked by d2
            checkSuccess(d2);
            checkFailure(d1, controlFrame);
            checkFailure(f, controlFrame);
            
            d1.setVisible(false);
            
            //////////////////////////////////////////////////
            // 
            // An alternate test (xtest2b) for the case where d2 is parented by f instead of d1
            // 
            //////////////////////////////////////////////////
            
            // f blocked by d2
            checkSuccess(d2);
            checkFailure(f, controlFrame);
            
//        } catch (Exception e) { 
//            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    public void xtest3() throws Exception {
        try {
            controlFrame = new TestFrame("3: ControlFrame");
            controlFrame.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            reallyShow(controlFrame);
            checkSuccess(controlFrame);
            
            f = new TestFrame("3: Frame");
            reallyShow(f);
            checkSuccess(f);
            
            d1 = new TestDialog(f, "3: d1 toolkit modal", Dialog.ModalityType.TOOLKIT_MODAL);
            
            d2 = new TestDialog(d1, "3: d2 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d2);
            
            // f blocked by d2
            checkSuccess(d2);
            checkFailure(f, controlFrame);
            
            d3 = new TestDialog(f, "3: d3 application modal", Dialog.ModalityType.APPLICATION_MODAL);
            reallyShow(d3);
            
            // d2 blocked by d3
            checkSuccess(d3);
            checkFailure(d2, controlFrame);
            checkFailure(f, controlFrame);
            
            reallyShow(d1);
            
            // d1 blocked by d2
            checkSuccess(d3);
            checkFailure(d2, controlFrame);
            checkFailure(d1, controlFrame);
            checkFailure(f, controlFrame);
            
//        } catch (Exception e) { 
//            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    public void xtest4() throws Exception {
        try {
            controlFrame = new TestFrame("4: ControlFrame");
            controlFrame.setModalExclusionType(Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
            reallyShow(controlFrame);
            checkSuccess(controlFrame);
            
            f = new TestFrame("4: Frame");
            reallyShow(f);
            checkSuccess(f);
            
            d1 = new TestDialog(f, "4: d1 toolkit modal", Dialog.ModalityType.TOOLKIT_MODAL);
            
            d2 = new TestDialog(f, "4: d2 document modal", Dialog.ModalityType.DOCUMENT_MODAL);
            reallyShow(d2);
            
            // f blocked by d2
            checkSuccess(d2);
            checkFailure(f, controlFrame);
            
            d3 = new TestDialog(f, "4: d3 application modal", Dialog.ModalityType.APPLICATION_MODAL);
            reallyShow(d3);
            
            // d2 blocked by d3
            checkSuccess(d3);
            checkFailure(d2, controlFrame);
            checkFailure(f, controlFrame);
            
            reallyShow(d1);
            
            // d3 blocked by d1
            // d1 NOT blocked
            checkSuccess(d1);
            checkFailure(d3, controlFrame);
            checkFailure(d2, controlFrame);
            checkFailure(f, controlFrame);
            
//        } catch (Exception e) { 
//            e.printStackTrace();
        } finally {
            cleanup();
        }
    }
    
    public void testStuff() throws Exception {
        xtest1();
        xtest2();
        xtest2b(); // See note in xtest2
        xtest3();
        xtest4();
    }
    
    interface TestWindow {
        public Waypoint getWaypoint();
        public Button getButton();
    }
    
    class TestFrame extends Frame implements TestWindow {
        Waypoint _waypoint;
        Button _button;
        
        public TestFrame(String title) {
            super(title);
            
            _waypoint = new Waypoint();
            setLayout(new FlowLayout());
            _button = new Button("Test Button");
            _button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                _waypoint.clear();
            }});
            add(_button);
            pack();
            
            setLocation(xLocation, 30);
            xLocation += getWidth();
        }
        
        public Waypoint getWaypoint() { return _waypoint; }
        public Button getButton() { return _button; }
    }
    
    class TestDialog extends Dialog implements TestWindow {
        Waypoint _waypoint;
        Button _button;
        
        public TestDialog(Window parent, String title, Dialog.ModalityType type) {
            super(parent, title, type);
            
            _waypoint = new Waypoint();
            setLayout(new FlowLayout());
            _button = new Button("Test Button");
            _button.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                _waypoint.clear();
            }});
            add(_button);
            pack();
            
            setLocation(xLocation, 30);
            xLocation += getWidth();
        }
        
        public Waypoint getWaypoint() { return _waypoint; }
        public Button getButton() { return _button; }
    }
    
    static class ModalitySpecException extends Exception {
        public ModalitySpecException(String reason) {
            super(reason);
        }
    }
    
    // return true if button was clicked
    public static void checkSuccess(TestWindow win) throws Exception {
        win.getWaypoint().reset();
        RobotUtilities.click(win.getButton());
        win.getWaypoint().requireClear();
        
        if (!win.getWaypoint().isClear()) throw new ModalitySpecException("Failed to click " + win);
    }
    
    // return true if button was NOT clicked
    public static void checkFailure(TestWindow win, TestWindow control) throws Exception {
        // To avoid a wait, click here, then click on another "known" frame and see if that succeeds.
        win.getWaypoint().reset();
        RobotUtilities.click(win.getButton());
        
        // Doesn't matter if this succeeds or fails
        checkSuccess(control);
        
        if (win.getWaypoint().isClear()) throw new ModalitySpecException("Got unexpected click on " + win);
    }
    
    
    
    public static Test suite() {
        return new TestSuite(ModalitySpecTests.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}