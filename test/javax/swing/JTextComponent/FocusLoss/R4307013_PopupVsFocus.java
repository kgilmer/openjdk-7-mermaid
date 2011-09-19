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
 @summary <rdar://problem/4307013> When hiding popup, focus is gone JEditorPane
 @summary com.apple.junit.java.awt.Event;
 @library ../../../../java/awt/regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @run main R4307013_PopupVsFocus
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class R4307013_PopupVsFocus extends TestCase {

    public static Test suite() {
        return new TestSuite( R4307013_PopupVsFocus.class);
    }

    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test makes a Mac OS X assumption about the trigger for popup menus
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
    
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    volatile MyPanel panel;
    volatile JFrame jframe;

    public void testPopupVsFocus() throws Exception {
        jframe = new JFrame();
        try {
            jScrollPane1 = new javax.swing.JScrollPane();
            jEditorPane1 = new javax.swing.JEditorPane();

            jEditorPane1.setText("TESTING");
            jScrollPane1.setViewportView(jEditorPane1);

            jframe.getContentPane().add(jScrollPane1);
            jframe.pack();
            jframe.setSize(300,300);

            jEditorPane1.getActionMap().put("XXXPopup", new PopupAction());
            jEditorPane1.getInputMap().put(KeyStroke.getKeyStroke("control shift X"), "XXXPopup");

            
            VisibilityValidator.setVisibleAndConfirm(jframe);

            jEditorPane1.requestFocusInWindow();
            pause(500);

            // activate the popup
            RobotUtilities.pressKey(KeyEvent.VK_CONTROL);
            RobotUtilities.pressKey(KeyEvent.VK_SHIFT);
            RobotUtilities.typeKey(KeyEvent.VK_X);
            RobotUtilities.releaseKey(KeyEvent.VK_SHIFT);
            RobotUtilities.releaseKey(KeyEvent.VK_CONTROL);
            pause(500);

            // click on the popup, dismissing it
            RobotUtilities.click(panel);
            pause(500);

            // see if we got back focus
            RobotUtilities.typeKey(KeyEvent.VK_H);
            RobotUtilities.typeKey(KeyEvent.VK_I);

            String hiString = jEditorPane1.getText();
            assertTrue("EditorPane didn't get focus back after popup: " + hiString, (hiString.indexOf("hi")!=-1) );
        } finally {
            if (jframe != null) {
                jframe.setVisible(false);
                jframe.dispose();
                jframe = null;
            }
        }
    }


    private class PopupAction extends AbstractAction {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            MyPanel pnl = new MyPanel();
            panel = pnl;
            Point point = new Point(100, 100);
            SwingUtilities.convertPointToScreen(new Point(10, 10), jEditorPane1);
            final Popup popup = PopupFactory.getSharedInstance().getPopup(jframe, pnl, point.x, point.y);
            pnl.list.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent me) {
                    popup.hide();
                    me.consume();
                    panel = null;
                }
                public void mouseEntered(MouseEvent me) {}
                public void mouseExited(MouseEvent me) {}
                public void mousePressed(MouseEvent me) { me.consume(); }
                public void mouseReleased(MouseEvent me) { me.consume(); }
            });
            popup.show();
        }

    }

    private class MyPanel extends JPanel {
        public JList list = new JList(new Object[] {
            "itemx1",
            "itemx2",
            "item11",
            "item22",
            "item01",
            "item02",
            "item1",
            "item2",
            "item3"
        });
        public MyPanel() {
            setLayout(new BorderLayout());
            add(list, BorderLayout.CENTER);

        }
    }

    private void pause(long time) {
        try {
            Thread.sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
