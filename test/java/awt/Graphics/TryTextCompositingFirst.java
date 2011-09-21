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
 * @summary <rdar://problem/4756727> [JavaJDK16] Drawing text first with a custom composite might break
 * @summary com.apple.junit.java.graphics.composite
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

public class TryTextCompositingFirst extends TestCase {

    public void testGetRGB() {
        try {
            BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setXORMode(Color.red);
            g2d.drawString("Blah", 30, 30);
            g2d.dispose();
        }
        catch (Exception e) {
            fail("Failed with exception: " + e.getMessage());
        }
    }

    public static Test suite() {
        return new TestSuite(TryTextCompositingFirst.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
