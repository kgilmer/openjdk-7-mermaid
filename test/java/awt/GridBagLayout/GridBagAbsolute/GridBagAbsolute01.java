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
 @summary Test q0001GridBagAbsolute, a simple test to increase our codecoverage and hit GridBagLayout.
 @summary com.apple.junit.java.awt;
 @library ../../regtesthelpers
 @build RobotUtilities
 @build VisibilityValidator
 @build Waypoint
 @run main GridBagAbsolute01
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class GridBagAbsolute01 extends TestCase {

    public static Test suite() {
        return new TestSuite(GridBagAbsolute01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    Button btn1,btn2,btn3,btn4,btn5;
    String[] bsArray = {"This Test","is designed","to test","the class","GridBagLayout"};
    Waypoint last_click = new Waypoint();


    public void testGridBagAbsolute() throws Exception {
        
        Frame windowf = new Frame("GridBagLayout Absolute test");
        VisibilityValidator win_checkpoint = new VisibilityValidator(windowf);

        GridBagLayout gridbag = new GridBagLayout();
        windowf.setLayout(gridbag);
    
        GridBagConstraints constraints = new GridBagConstraints();
    
        btn1 = new Button("Button 1");
        btn1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn1.setLabel(bsArray[0]);
            }
        });
        windowf.add(btn1);
        // set at (0,0)
        constraints.gridx = 0;
        constraints.gridy = 0;
        gridbag.setConstraints(btn1, constraints);
    
        btn2 = new Button("Button 2");
        btn2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn2.setLabel(bsArray[1]);
            }
        });
        windowf.add(btn2);
        // set at (2,0)
        constraints.gridx = 2;
        constraints.gridy = 0;
        gridbag.setConstraints(btn2, constraints);
    
    
        btn3 = new Button("Button 3");
        btn3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn3.setLabel(bsArray[2]);
            }
        });
        windowf.add(btn3);
        // set at (0,2)
        constraints.gridx = 0;
        constraints.gridy = 2;
        gridbag.setConstraints(btn3, constraints);
    
        btn4 = new Button("Button 4");
        windowf.add(btn4);
        btn4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn4.setLabel(bsArray[3]);
            }
        });            
        // set at (2,2)
        constraints.gridx = 2;
        constraints.gridy = 2;
        gridbag.setConstraints(btn4, constraints);
    
        btn5 = new Button("Button 5");
        btn5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btn5.setLabel(bsArray[4]);
                last_click.clear();
            }
        });
        windowf.add(btn5);
        // set at (1,1)
        constraints.gridx = 1;
        constraints.gridy = 1;
        gridbag.setConstraints(btn5, constraints);
    
        windowf.setSize(500, 300);
        windowf.setLocation(200, 0);
        windowf.pack();
        windowf.setVisible(true);

        win_checkpoint.requireVisible();
        boolean windowShown = win_checkpoint.isClear();
        if (!windowShown) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            java.io.File f = RobotUtilities.screenshot("q0001GridBagAbsolute", new java.io.File(tmpdir));
            assertNotNull(f);
        }
        assertTrue( "Could not confirm test window was visible", windowShown );

        // *** TO DO -- remove hack and wait for event indicating that layout is done
        RobotUtilities.delay(500);
        // *** end hack
                    
        RobotUtilities.click(btn1);
        RobotUtilities.click(btn2);
        RobotUtilities.click(btn3);
        RobotUtilities.click(btn4);
        RobotUtilities.click(btn5);    // <-- clears last_click Waypoint.

        last_click.requireClear("Did not receive last click event.");
        
        /* testing something about the gridbag... */
        
        assertEquals("Button failed to get a new name.", btn5.getLabel(), bsArray[4] );
        assertEquals("Button failed to get a new name.", btn4.getLabel(), bsArray[3] );
        assertEquals("Button failed to get a new name.", btn3.getLabel(), bsArray[2] );
        assertEquals("Button failed to get a new name.", btn2.getLabel(), bsArray[1] );
        assertEquals("Button failed to get a new name.", btn1.getLabel(), bsArray[0] );

        windowf.dispose();
    }
}



