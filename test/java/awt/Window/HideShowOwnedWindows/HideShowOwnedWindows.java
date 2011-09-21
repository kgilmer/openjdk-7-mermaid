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
 * @test
 * @summary <rdar://problem/4555794> [JavaJDK16] Hiding / Showing owned (child) windows is not sufficiently recursive
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build RobotUtilities VisibilityValidator Waypoint
 * @run main HideShowOwnedWindows
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

public class HideShowOwnedWindows extends TestCase {
    Waypoint waypoint = new Waypoint();

    public void testHideShowOwnedWindows() throws RuntimeException {
        Frame f = null;
        Dialog d1 = null;
        Dialog d2 = null;
        Dialog d3 = null;
        VisibilityValidator vis = null;
        
        try {
            f = new Frame("Frame");
            f.setBounds(50,50,450,450);
            vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            d1 = new Dialog(f, "1", false);
            d1.setBounds(100,100,100,100);
            vis = new VisibilityValidator(d1);
            d1.setVisible(true);
            vis.requireVisible();
            
            d2 = new Dialog(d1, "2", false);
            d2.setBounds(150,150,100,100);
            vis = new VisibilityValidator(d2);
            d2.setVisible(true);
            vis.requireVisible();
            
            d3 = new Dialog(d2, "3", false);
            d3.setBounds(200,200,100,100);
            Button b = new Button("b");
            b.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
                waypoint.clear();
            }});
            d3.add(b);
            vis = new VisibilityValidator(d3);
            d3.setVisible(true);
            vis.requireVisible();
            
            f.setVisible(false);
            // No inverse VisibilityValidator
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {}}); } catch (Exception e) {}
            
            f.setVisible(true);
            // Can't use VisibilityValidator here since f has already been shown once.
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {}}); } catch (Exception e) {}

            waypoint.reset();
            RobotUtilities.click(b);
            waypoint.requireClear("Owned dialog didn't come back after hide/show.");
        } finally {
            if (d3 != null) { d3.setVisible(false); d3.dispose(); d3 = null; }
            if (d2 != null) { d2.setVisible(false); d2.dispose(); d2 = null; }
            if (d1 != null) { d1.setVisible(false); d1.dispose(); d1 = null; }
            if (f != null) { f.setVisible(false); f.dispose(); f = null; }
        }
    }
    
    
    public static Test suite() {
        return new TestSuite(HideShowOwnedWindows.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
