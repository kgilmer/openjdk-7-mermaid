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
 @summary This test tests the repainting and clipping mechanism in AWT. The frame which is red, has on AWT component which is green. After a delay a
 @summary repaint is sent to the frame. The frame is gonna repaint itself and fill the background with red. if the clip is not set up correctly,
 @summary the green component will be painted over. The test should fail if the whole frame is red. 
 @summary com.apple.junit.java.awt;
 @library ../../regtesthelpers
 @build VisibilityValidator
 @build Waypoint
 @run main HeavyweightRepaintTest
 */
import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class HeavyweightRepaintTest extends TestCase
{
    Color color = Color.yellow;
    Waypoint painted = new Waypoint();

    public void test() throws Exception
    {
        VisibilityValidator.setVisibleAndConfirm(frame);    
        Thread.sleep(125);
        frame.repaint();

        assertTrue("It looks like the green component is not visible", VisibilityValidator.waitForColor(frame, 100, 100, Color.green));
    }
    
    public static Test suite() 
    {
        return new TestSuite(HeavyweightRepaintTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    public void setUp()
    {
        frame = new MyFrame();
        frame.setLayout(null);
        frame.setLocation(100, 100);
        frame.setSize(500, 500);
        Panel myPanel = new MyHPanel();
        myPanel.setSize(200, 200);
        frame.add(myPanel);
    }
    
    protected void tearDown() 
    {
        frame.dispose();
    }
    
    JFrame frame;
            
    class MyFrame extends JFrame {        
        public MyFrame() {
            frame = this;
            addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { System.out.println(" mouseDown"); repaint(); } });
        }
        
        public void paint(Graphics g) {    
            int width = getWidth();
            int height = getHeight();
            
            g.setColor(Color.red);
            g.fillRect(0, 0, width, height);
        }
    }
        
    class MyHPanel extends Panel {
        
        public MyHPanel() {
            super();
        }
        
        public void paint(Graphics g) {    
        
            //System.out.println(" paiting green !!!");
            
            int width = getWidth();
            int height = getHeight();
            
            g.setColor(Color.green);
            g.fillRect(0, 0, width, height);
        }
    }
}