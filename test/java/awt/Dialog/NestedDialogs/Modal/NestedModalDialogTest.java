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
 @summary See <rdar://problem/3429130>: Events: actionPerformed() method not called when it is button is clicked (system load related)
 @summary com.apple.junit.java.awt.Frame
 @library ../../../regtesthelpers
 @build VisibilityValidator
 @build RobotUtilities
 @build Waypoint
 @run main NestedModalDialogTest
 */

///////////////////////////////////////////////////////////////////////////////////////
//  NestedModalDialogTest.java   
// The test launches a parent frame. From this parent frame it launches a modal dialog
// From the modal dialog it launches a second modal dialog with a text field in it and tries
// to write into the text field. The test succeeds if you are successfully able to write 
// into this second Nested Modal Dialog
/////////////////////////////////////////////////////////////////////////////////////

// classes necessary for this test
import junit.framework.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
    

public class NestedModalDialogTest extends TestCase {

    public static Test suite() {
        return new TestSuite(NestedModalDialogTest.class);
    }

    Waypoint[]             event_checkpoint = new Waypoint[3];
    VisibilityValidator[] win_checkpoint = new VisibilityValidator[2];
    
    IntermediateDialog     interDiag;
    TextDialog             txtDiag;
    
    // Global variables so the robot thread can locate things.
    Button[]             robot_button = new Button[2];
    TextField            robot_text = null;
    
    /*
     * @throws InterruptedException 
     * @throws WaypointException 
     */        
             
    public void testModalDialogs() throws Exception  {
        Frame        frame = null;
        String        result = "";
      
        event_checkpoint[0] = new Waypoint(); // "-Launch 1-"
        event_checkpoint[1] = new Waypoint(); // "-Launch 2-"

        try {
            // Thread.currentThread().setName("NestedModalDialogTest Thread");
            // launch first frame with firstButton
            frame = new StartFrame();
            VisibilityValidator.setVisibleAndConfirm(frame);
            
            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.click (robot_button[0]);

            // Dialog must be created and onscreen before we proceed.
            //   The event_checkpoint waits for the Dialog to be created.
            //   The win_checkpoint waits for the Dialog to be visible.

            event_checkpoint[0].requireClear("TestFrame actionPerformed() never called, see <rdar://problem/3429130>");
            Thread.sleep(250); // shouldn't need this, but pause adds stability

            win_checkpoint[0].requireVisible();
            assertTrue( "Could not confirm intermediate dialog was visible", win_checkpoint[0].isValid() );
            
            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.click (robot_button[1]);

            // Again, the Dialog must be created and onscreen before we proceed.
            //   The event_checkpoint waits for the Dialog to be created.
            //   The win_checkpoint waits for the Dialog to be visible.

            event_checkpoint[1].requireClear("IntermediateDialog actionPerformed() never called, see <rdar://problem/3429130>");
            Thread.sleep(250); // shouldn't need this, remove when 3429130 is addressed

            win_checkpoint[1].requireVisible();
            assertTrue( "Could not confirm test dialog was visible", win_checkpoint[1].isValid() );

            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.click(robot_text);

            // I'm really not sure whether the click is needed for focus
            // but since it's asynchronous, as is the actually gaining of focus
            // we might as well do our best
            Thread.sleep(250); // shouldn't need this, but pause adds stability
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {} }); } catch (Exception e) {}
            
            RobotUtilities.pressKey(KeyEvent.VK_SHIFT);
            RobotUtilities.typeKey(KeyEvent.VK_H);
            RobotUtilities.releaseKey(KeyEvent.VK_SHIFT);
            RobotUtilities.typeKey(KeyEvent.VK_E);
            RobotUtilities.typeKey(KeyEvent.VK_L);
            RobotUtilities.typeKey(KeyEvent.VK_L);
            RobotUtilities.typeKey(KeyEvent.VK_O);

            //
            // NOTE THAT WE MAY HAVE MORE SYNCHRONIZATION WORK TO DO HERE.
            // CURRENTLY THERE IS NO GUARANTEE THAT THE KEYEVENT THAT THAT
            // TYPES THE 'O' HAS BEEN PROCESSED BEFORE WE GET THE RESULT
            // 
            
            // This is a (lame) attempt at waiting for the last typeKey events to propagate
            // It's not quite right because robot uses CGRemoteOperations, which are asynchronous
            // But that's why I put in the pause
            Thread.sleep(250); // shouldn't need this, but pause adds stability
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {} }); } catch (Exception e) {}
            
            // We really need to call this before the dialog that robot_text is in is disposed
            result = robot_text.getText();
            
            
            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.clickAt(txtDiag, 14,10); // Click Close box of modal dialog with textField

            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.clickAt(interDiag, 14,10); // Click Close box of intermediate modal dialog

            Thread.sleep(250); // shouldn't need this, but pause adds stability
            RobotUtilities.clickAt(frame, 14,10); // Click Close box of intermediate modal dialog
        }
        finally {
            frame.setVisible(false);
            frame.dispose();
        }
        
        String expected = "Hello";
        assertEquals("Bad result from getText().\n\nExpected: \n\t*" + expected +"*\nActual:\n\t*"+result +"*\n",expected, result );

    }
    
    
//////////////////// Start Frame ///////////////////
/**
 * Launches the first frame with a button in it
 */
    
class StartFrame extends Frame {
     /**
     * Constructs a new instance.
     */
    public StartFrame() {
        super("First Frame");
        setLayout(new GridBagLayout());      
        setLocation(375, 200);
        setSize(271,161);
        Button but = new Button("Make Intermediate");
        but.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                interDiag = new IntermediateDialog(StartFrame.this);
                win_checkpoint[0] = new VisibilityValidator(interDiag);
                interDiag.setSize(300, 200);
                
                // may need listener to watch this move.
                interDiag.setLocation(getLocationOnScreen());
                interDiag.pack();
                event_checkpoint[0].clear();
                interDiag.setVisible(true);        // <--- thread stops here until the modal dialog is closed
            }
        });
        Panel  pan = new Panel();
        pan.add(but);
        add(pan);
        robot_button[0] = but;
        addWindowListener(new WindowAdapter() { 
            public void windowClosing(WindowEvent e) { 
                setVisible(false);
                dispose();
            } 
        });
    }
    
 }
    
///////////////////////////// MODAL DIALOGS //////////////////////////////////    
 

/* A Dialog that launches a sub-dialog */
 class IntermediateDialog extends Dialog {
    Dialog m_parent;

    public IntermediateDialog(Frame parent) {
        super(parent, "Intermediate Modal", true /*Modal*/);
        m_parent = this;
        Button but = new Button("Make Text");
        but.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                txtDiag = new TextDialog(m_parent); 
                win_checkpoint[1] = new VisibilityValidator(txtDiag);
                txtDiag.setSize(300, 100);
                event_checkpoint[1].clear();
                txtDiag.setVisible(true);    // <--- thread stops here until the modal dialog is closed
            }
        });
        Panel  pan = new Panel();
        pan.add(but);
        add(pan);
        pack();
        addWindowListener(new WindowAdapter() { 
            public void windowClosing(WindowEvent e) { 
                setVisible(false);
                dispose();
            } 
        });
        
        // The robot needs to know about us, so set global
        robot_button[1] = but;
    }
  }

/* A Dialog that just holds a text field */
 class TextDialog extends Dialog {
    public TextDialog(Dialog parent) {
        super(parent, "Modal Dialog", true /*Modal*/);
        TextField txt = new TextField("",10);
        Panel  pan = new Panel();
        pan.add(txt);
        add(pan);
        pack();
        addWindowListener(new WindowAdapter() { 
            public void windowClosing(WindowEvent e) { 
                setVisible(false); 
                dispose();
            } 
         });
         
         
        // The robot needs to know about us, so set global
         robot_text = txt;
  }
  
}  

    public static void main (String[] args) throws RuntimeException {
        try {
            TestResult tr = junit.textui.TestRunner.run(suite());
            
            int numErrors = tr.errorCount();
            if (numErrors != 0) {
                Enumeration e = tr.errors();
                while (e.hasMoreElements()) {
                    System.out.println(e.nextElement());
                }
                
            }
            int numFailures = tr.failureCount();
            if (numFailures != 0) {
                Enumeration e = tr.failures();
                while (e.hasMoreElements()) {
                    System.out.println(e.nextElement());
                }
            }
            if ((numErrors != 0) || (numFailures != 0)){
                throw new RuntimeException("Test encountered " + numErrors + " errors and " + numFailures + " failures.");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}


