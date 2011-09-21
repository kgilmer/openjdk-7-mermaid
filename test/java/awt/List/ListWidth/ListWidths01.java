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
 @summary This is for <rdar://problem/3966067> jre1.4.2_05: Zoomed JFrame reports wrong location
 @summary com.apple.junit.java.awt.List
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main ListWidths01
 */

import junit.framework.*;

import test.java.awt.regtesthelpers.VisibilityValidator;

import java.awt.*;
import java.awt.event.ActionEvent;

public class ListWidths01 extends TestCase
{
    protected Frame f;
    protected TextField title;
    protected List[] lists;
    protected Button[] buttons;
    protected Panel[] panels;
    
    protected void setUp() {
        try {
            f = new Frame("Test Frame");
            lists = new List[2];
            title = new TextField("This is a very very very long item indeed.  Oh yes indeedy.  Very very lengthy");
             panels = new Panel[2];
             
             panels[0] = new Panel(new FlowLayout());
            for( int i = 0; i < lists.length; i++) {
                lists[i] = new java.awt.List(10, false);
                panels[0].add(lists[i]);
            }
            
            // Make list0 long horizontally
            
            lists[0].add("Short 1");
            lists[0].add("Short 2");
            lists[0].add("This is a very long list indeed.  Oh yes.  Quite long");

            // Make list1 long horizontally
            for(int i = 0; i < 35; i++) {
                lists[1].add(i + "item");
            }

            buttons = new Button[6];
             panels[1] = new Panel(new GridLayout(2, (buttons.length) / 2));

            buttons[0] = new Button("Add Left");
            buttons[0].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    lists[0].add( title.getText());
                }
            });
            
            buttons[1] = new Button("Add Right");
            buttons[1].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    lists[1].add( title.getText());
                }
            });

            buttons[2] = new Button("Pack");
            buttons[2].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    f.pack();
                }
            });

            buttons[3] = new Button("Shrink Left");
            buttons[3].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Dimension d = lists[0].getSize();
                    int w = (int) ((d.getWidth() * 2) / 3);
                    int h = (int) ((d.getHeight() * 2) / 3);
                    lists[0].setSize( w, h );
                }
            });
            
            buttons[4] = new Button("Shrink Right");
            buttons[4].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Dimension d = lists[1].getSize();
                    int w = (int) ((d.getWidth() * 2) / 3);
                    int h = (int) ((d.getHeight() * 2) / 3);
                    lists[1].setSize( w, h );
                }
            });

            buttons[5] = new Button("Report");
            buttons[5].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    for( int i = 0; i < lists.length; i++) {
                        System.out.println(
                            i + ": \n"
                            + "min\t" + lists[i].getMinimumSize() + "\n" 
                            + "max\t" + lists[i].getMaximumSize() + "\n"  
                            + "prf\t" + lists[i].getPreferredSize() + "\n"
                        );
                    }
                }
            });


            for( int i = 0; i < buttons.length; i++) {
                panels[1].add(buttons[i]);
            }
     
            f.setLayout(new BorderLayout());
            f.add(title, BorderLayout.NORTH);
            f.add(panels[0], BorderLayout.CENTER);
            f.add(panels[1],BorderLayout.SOUTH);
        }
        catch (Exception e) {
            fail("Unexpected exception during test setup");
        }
     }

    public void testBasicDisplay() throws Exception {
        f.pack();
        VisibilityValidator.setVisibleAndConfirm(f);
        Thread.sleep(250);
    }
    
    protected void tearDown() {
        f.dispose();
    }

    public static Test suite() {
        return new TestSuite(ListWidths01.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}




