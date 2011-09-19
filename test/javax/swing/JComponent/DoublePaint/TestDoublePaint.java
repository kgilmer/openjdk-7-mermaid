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
 @summary <rdar://problem/3132190> Painting: We paint twice on startup
 @summary com.apple.junit.java.awt.Event;
 @run main TestDoublePaint
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;

public class TestDoublePaint extends TestCase 
{

    JFrame f = null;

    int numberOfRepaints = 0;
    
    protected void setUp() 
    {
        f = new JFrame("Double Paint Tester");
        f.setSize(600,300);
        TestPanel p = new TestPanel();
        JPanel root = new JPanel();
        root.setLayout(new BorderLayout());
        root.add(BorderLayout.CENTER, p);
        f.setContentPane(root);
        f.setVisible(true);
    }
    
    protected void tearDown() 
    {
        f.dispose();
    }


    public static Test suite() 
    {
        return new TestSuite(TestDoublePaint.class);
    }
    
    public void testDoubleRepaint() throws Exception
    {
        Thread.sleep(2000);  // 2 seconds should be plenty enough to catch 2 repaints
        // System.err.println(" numberOfRepaints = " + numberOfRepaints);
        assertTrue("There are 2 or more repaints on start-up", numberOfRepaints == 1);
    }
    
    
    class TestPanel extends JComponent
    {
        public void paint(Graphics g)
        {
            numberOfRepaints++;
        }
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected errors or failures.");
        }
    }
}

