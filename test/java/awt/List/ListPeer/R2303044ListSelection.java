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
 @summary <rdar://problem/2303044 List selection not set when peer is created
 @summary com.apple.junit.java.awt.List;
 @run main R2303044ListSelection
 */

import junit.framework.*;

import java.awt.*;

public class R2303044ListSelection extends TestCase
{
    protected Frame f;
    protected List l;
    
    protected void setUp() {
        f = new Frame("Test Frame");
        l = new List();
        f.setSize(300, 200);
    }

    public void testListSelection() throws Exception {
        String myItemName = "myItem";
        l.add(myItemName);
        l.select(0);
        f.add(l);
        f.validate();
        f.setVisible(true);
        Thread.sleep(2000);
        assertEquals("List item not selected item.", myItemName, l.getSelectedItem());
    }
    
    protected void tearDown() {
        l.removeAll();
        f.dispose();
    }

    public static Test suite() {
        return new TestSuite(R2303044ListSelection.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
