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
 @summary See <rdar://problem/4500267> jdk16bringup: new WindowPeer.updateMinimumSize method
 @summary com.apple.junit.java.awt.Frame
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main MinSizeTest
 */

import java.awt.Dimension;
import java.awt.Frame;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class MinSizeTest extends TestCase {
    
    // No minimum size
    public void testNoMin() {
        Frame f = null;
        try {
            f = new Frame("No minimum size");
            assertNotNull(f);
            f.setSize(300,300);
            f.setLocation(100,100);
            
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            // Check isMinimumSizeSet
            assertTrue("No minimum size", f.isMinimumSizeSet() == false);
            
            // Check dimensions
            Dimension actual = f.getSize();
            Dimension expected = new Dimension(300,300);
            assertEquals("Unexpected size for frame", expected, actual);
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }        


    // 200,200 minimum during creation
    public void testMinimumDuringCreation() {
        Frame f = null;
        try {
            f = new Frame("200,200 minimum during creation");
            assertNotNull(f);
            f.setMinimumSize(new Dimension(200,200));
            f.setLocation(150,150);

            // Try to resize smaller
            f.setSize(150,150);
            
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            // Check isMinimumSizeSet
            assertTrue("Minimum size set", f.isMinimumSizeSet());
            
            // Check dimensions
            Dimension actual = f.getSize();
            Dimension expected = new Dimension(200,200);
            assertEquals("Unexpected size for frame", expected, actual);
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }        


    // 300,300 minimum after creation
    public void testMinimumAfterCreation() throws Exception {
        Frame f = null;
        try {
            f = new Frame("300,300 minimum after creation");
            assertNotNull(f);
            f.setSize(200,200);
            f.setLocation(200,200);
            
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();

            // Puff it up
            // ### may not be required to be synchronous -- if it ever fails, add waypoint on resize event 
            f.setMinimumSize(new Dimension(300,300));
            
            // Check isMinimumSizeSet
            assertTrue("Minimum size set", f.isMinimumSizeSet());
            
            // Check dimensions
            Dimension actual = f.getSize();
            Dimension expected = new Dimension(300,300);
            assertEquals("Unexpected size for frame", expected, actual);
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }        

    // 200,200 minimum, non-resizable before creation
    public void testMinimumDuringCreationNoResize() throws Exception {
        Frame f = null;
        try {
            f = new Frame("200,200 minimum, non-resizable before creation");
            assertNotNull(f);
            f.setMinimumSize(new Dimension(200,200));
            f.setResizable(false);
            f.setLocation(250,250);
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            // Try to resize smaller
            f.setSize(new Dimension(150,150));
            
            // Check isMinimumSizeSet
            assertTrue("Minimum size set", f.isMinimumSizeSet());
            
            // Check dimensions
            Dimension actual = f.getSize();
            Dimension expected = new Dimension(200,200);
            assertEquals("Unexpected size for frame", expected, actual);
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }        

    // handle changes
    public void testMinimumChanges() throws Exception {
        Frame f = null;
        try {
            f = new Frame("Various different");
            assertNotNull(f);
            f.setMinimumSize(new Dimension(200,200));
            f.setLocation(300,300);
            VisibilityValidator vis = new VisibilityValidator(f);
            f.setVisible(true);
            vis.requireVisible();
            
            // Try to resize smaller
            f.setSize(new Dimension(150,150));
            
            // Check isMinimumSizeSet
            assertTrue("Minimum size set", f.isMinimumSizeSet());
            
            // Check dimensions
            Dimension actual = f.getSize();
            Dimension expected = new Dimension(200,200);
            assertEquals("1- Unexpected size for frame", expected, actual);

            // reset minimum to slightly larger value
            // ### may not be required to be synchronous -- if it ever fails, add waypoint on resize event 
            f.setMinimumSize(new Dimension(250,250));

            // Check dimensions
            actual = f.getSize();
            expected = new Dimension(250,250);
            assertEquals("2- Unexpected size for frame", expected, actual);

            // Try to resize smaller (should not take)
            f.setSize(new Dimension(200,200));
            actual = f.getSize();
            expected = new Dimension(250,250);
            assertEquals("3- Unexpected size for frame", expected, actual);

            // Try to resize larger than 250, 350 (should take)
            // ### may not be required to be synchronous -- if it ever fails, add waypoint on resize event 

            f.setSize(new Dimension(300,300));
            actual = f.getSize();
            expected = new Dimension(300,300);
            assertEquals("4- Unexpected size for frame", expected, actual);

            // reset minimum to null
            f.setMinimumSize(null);

            // Check isMinimumSizeSet
            assertTrue("Minimum size unset", f.isMinimumSizeSet() == false);
            
            // Try to resize smaller
            f.setSize(new Dimension(150,150));
            // ### may not be required to be synchronous -- if it ever fails, add waypoint on resize event 

            actual = f.getSize();
            expected = new Dimension(150,150);
            assertEquals("5- Unexpected size for frame", expected, actual);
            
        } finally {
            f.setVisible(false);
            f.dispose();
            f = null;
        }
    }        



    public static Test suite() {
        return new TestSuite(MinSizeTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.failureCount() != 0) || (tr.errorCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures");
        }
    }
}
