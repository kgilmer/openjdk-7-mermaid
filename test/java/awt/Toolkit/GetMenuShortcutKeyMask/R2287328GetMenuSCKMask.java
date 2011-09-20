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
 @summary rdar://2287328 Toolkit's menuShortcutKeyMask is Ctrl, not Meta
 @summary com.apple.junit.java.awt;
 @run main R2287328GetMenuSCKMask
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.InputEvent;

public class R2287328GetMenuSCKMask extends TestCase {

    public void testMenuShortCut() {
        // note -- mac specific
        assertEquals( "Meta is the menu shortcut", Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), InputEvent.META_MASK);
    }

    public static Test suite() {
        return new TestSuite( R2287328GetMenuSCKMask.class);
    }

    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test makes a Mac OS X assumption about menu shortcut key mask
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
}
