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
 * @summary Verifies Frame and JFrame sizes relative to window size.
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main MinSizeTest
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;

public class MinSizeTest extends TestCase {
    VisibilityValidator checkpoint;
    
    public void testAWTMinSize() {
        Frame f = null;
        Window w = null;
        
        try {
            f = new Frame("frame");
            checkpoint = new VisibilityValidator(f);
            f.setBounds(50,50,10,10);
            f.setVisible(true);
            
            w = new Window(f);
            w.setBounds(250,50,10,10);
            w.setVisible(true);
            
            checkpoint.requireVisible();
            assertFalse("Frame minimum size is too small: " + f.getSize(), ( (f.getSize().height==10) || (f.getSize().width==10) ) );
            assertTrue("Window minimum size is too big: " + w.getSize(), ( (w.getSize().height==10) && (w.getSize().width==10) ) );
        } finally {
            if (w != null) {
                w.setVisible(false);
                w.dispose();
            }
            if (f != null) {
                f.setVisible(false);
                f.dispose();
            }
        }
    }
    
    public void testSwingMinSize() {
        JFrame jf = null;
        JFrame jf2 = null;
        
        try {
            jf = new JFrame("jframe");
            checkpoint = new VisibilityValidator(jf);
            jf.setBounds(50,250,10,10);
            jf.setVisible(true);
            
            jf2 = new JFrame("undecorated");
            jf2.setUndecorated(true);
            jf2.setBounds(250,250,10,10);
            jf2.setVisible(true);
            
            checkpoint.requireVisible();
            assertFalse("JFrame minimum size is too small: " + jf.getSize(), ( (jf.getSize().height==10) || (jf.getSize().width==10) ) );
            assertTrue("Undecorated JFrame minimum size is too big: " + jf2.getSize(), ( (jf2.getSize().height==10) && (jf2.getSize().width==10) ) );
        } finally {
            if (jf2 != null) {
                jf2.setVisible(false);
                jf2.dispose();
            }
            if (jf != null) {
                jf.setVisible(false);
                jf.dispose();
            }
        }
    }
    
    public static Test suite() {
        return new TestSuite(MinSizeTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}

