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
 @summary rdar://problem/3343813> [JavaJDK16] J2SE50R4DP7: setMaximizedBounds size should not be used as the maximum size
 @summary com.apple.junit.java.awt.Frame;
 @library ../../regtesthelpers
 @build VisibilityValidator
 @build Waypoint
 @run main MaximizedBoundsTest
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.ComponentEvent;

import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;

public class MaximizedBoundsTest extends TestCase {
    Waypoint waypoint = new Waypoint();
    
    public void testMaximizedBounds() {
        Frame f = new Frame("Maximized bounds test");
        Rectangle bounds = new Rectangle( 200, 200, 600, 400 );
        try {
            f.setLayout(null);
            f.setSize(300,175);
            f.addComponentListener(new java.awt.event.ComponentAdapter() { public void componentResized(ComponentEvent e) {
                waypoint.clear();
            }});
            
            f.setMaximizedBounds(bounds);
            
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            waypoint.reset();
            f.setExtendedState(Frame.MAXIMIZED_BOTH);
            try { EventQueue.invokeAndWait(new Runnable() { public void run() {}}); } catch(Exception e) {}
            waypoint.requireClear("Didn't get resize event after zoom");
            assertTrue("Maximized bounds should be " + bounds +", but is instead " + f.getBounds(), f.getBounds().equals(bounds));
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }
    
    public static Test suite() {
        return new TestSuite(MaximizedBoundsTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
