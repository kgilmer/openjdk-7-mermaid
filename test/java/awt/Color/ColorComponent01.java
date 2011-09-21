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
 * @summary 
 * @summary com.apple.junit.java.graphics.color.ColorComponent
 */

/*
 * Adapting simple color tests for the test harness.
 * Testcase for
 * <rdar://problem/3821083> JCK-Related: java.awt.java2d.awt.Color.GetColorComponentsTest fails with +ProtectJavaHeap
 * 
 * OPENJDK-MIGRATED
 * http://hg.openjdk.java.net/macosx-port/macosx-port/jdk/file/...
 */

import junit.framework.*;
import java.awt.*;
import java.awt.color.ColorSpace;

public class ColorComponent01 extends TestCase {

    public void testColorComponents_CS_sRGB() {

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

        float[] cp =  { 0.5f, 0.5f, 0.5f };
        Color c = new Color(cp[0], cp[1], cp[2]);
        float[] returned = c.getColorComponents(cs, null);
        
        for(int i = 0;i < returned.length;i++) {
            returned[i] = (float)((Math.round(returned[i] * 10))) / 10;
            String errorMessage = "Bad Result" + "\n"+
                "Problem with color component " + i + "\n"+
                "Requested " + cp[i] + ", rounded result is " + returned[i] +"\n"+
                "See <rdar://problem/3821083> before filing a new defect.";

            assertTrue(errorMessage, returned[i] == cp[i]);
        }
    }

    // boilerplate below
    
    public static Test suite() {
        return new TestSuite(ColorComponent01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
