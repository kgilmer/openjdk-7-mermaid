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
 * @summary <rdar://problem/3967850> Confirm that windows of 0,0 size correctly report themselves as 0,0
 * @summary com.apple.junit.java.awt.Window
 */

import junit.framework.*;
import java.awt.*;

public class R3967850MinWindowSize extends TestCase {

    // This test confirms that we correctly report windows with 0,0 size
    //
    // See bug <rdar://problem/3967850> JCK: Window2003 test started failing between 1/11 and 1/14
    //
    // Even though when creating windows of size 0,0 we may still internally be creating windows that 
    // have a minimum size of 1,1 (see also bug 3969845), we should report the size as 0,0 if asked.
    //
    // TODO: This begs to be generalized a little more, so we create several windows with various parameters,
    // and expect specific size reports from them.  A loop with a vector of parameters and expected results.
    // 

    public static Test suite() {
        return new TestSuite(R3967850MinWindowSize.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testDefaultWindow() throws Exception {
        Frame frame = new Frame("Default Window");
        Window win = new Window(frame);
        win.pack();
        Dimension winSize = win.getSize();
        Dimension winPreferredSize = win.getPreferredSize();
        // System.out.println("win.getSize " + win.getSize());
        // System.out.println("win.getPreferredSize " + win.getPreferredSize());
        assertEquals("expected default window width to be 0", 0, winSize.width);
        assertEquals("expected default window height to be 0", 0, winSize.height);
        assertEquals("expected window size and preferred size to match", winSize, winPreferredSize);
        win.dispose();
        frame.dispose();
    }

}

