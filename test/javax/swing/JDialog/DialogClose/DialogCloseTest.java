/*
 * Copyright (c) 2006, 2007, Oracle and/or its affiliates. All rights reserved.
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
 @summary Test dialog closing. Close the dialog, which prompts before closing.
 @summary This was causing a crash. <rdar://problem/3893473>
 @summary com.apple.junit.java.awt.Frame
 @library ../../../../java/awt/regtesthelpers
 @build VisibilityValidator
 @build RobotUtilities
 @run main DialogCloseTest
 */

import junit.framework.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DialogCloseTest extends TestCase  {
    JFrame f = null;
    static volatile JDialog d = null;
    static volatile Exception cachedException = null;
    static volatile Error cachedError = null;
    
    protected static boolean prompt(JDialog parent) {
        int rc = JOptionPane.showConfirmDialog(parent,
                "Don't cause a bug?", "Scew up menu?",
                JOptionPane.YES_NO_OPTION);            
        return (rc == JOptionPane.YES_OPTION);
    }
    
    public void testDialogClosing() throws Exception {
        assertNotNull(f);
        assertNotNull(d);
        VisibilityValidator.setVisibleAndConfirm(f);
        
        RobotSequence s = new RobotSequence();
        s.start();
        d.setVisible(true);
        
        if (cachedException != null) {
            throw cachedException ;
        }

        if (cachedError != null) {
            throw cachedError ;
        }

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        f = new JFrame("Main App");
        f.setBounds(100, 50, 400,400);
        
        d = new JDialog(f, "A Modal Dialog", true);
        d.setBounds(100, 50, 200,200);
        d.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                DialogCloseTest.prompt(d);
                d.dispose();
            }
        });
        }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();        
        if (f != null) { f.dispose(); }
        if (d != null) { d.dispose(); }
    }

    private static class RobotSequence extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                
                RobotUtilities.clickAt(d, 15, 9); // close the window
                Thread.sleep(1000);
    
                assertNotNull("Modal Dialog not initialized", d);
                JButton bye = extractYesButton(d);
                assertNotNull("Yes button not found", bye);
                RobotUtilities.click(bye); // close the dialog - this was causing a crash
            }
            catch( Exception x) {
                cachedException = x;
            }
            catch( Error err) {
                cachedError = err;
            }
            finally {
                d.dispose();
            }
        }        
    }


    /*
     * Utility function to walk the window and container heirarchy looking for a "Yes" button
     */
    
    static JButton extractYesButton( Container parent ) {
        JButton yesyes = null;
        if (parent instanceof Window) {
            Window[] w = ((Window) parent).getOwnedWindows();
            for (int i = 0; i < w.length && yesyes == null; i += 1) {
                yesyes = extractYesButton( w[i] );
            }
        }        
        Component[] c = parent.getComponents();
        for (int i = 0; i < c.length && yesyes == null; i += 1) {
            if (c[i]instanceof JButton) {
                JButton b = (JButton) c[i];
                if (b.getText().equals( "Yes" )) {
                    yesyes = (JButton) c[i];
                    break;
                }
            }
            else if (c[i]instanceof Container) {
                yesyes = extractYesButton( (Container) c[i] );
            }
        }
        return yesyes;
    }

    
    public static Test suite() {
        return new TestSuite(DialogCloseTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test makes an assumption of the location of the close widget on Mac OS X
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
}
