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
 * @summary <rdar://problem/2254176> Some files are still not being closed after quitting MRJ
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;
import java.io.*;

public class R2254176FileClosing extends TestCase {
    public static Test suite() {
        return new TestSuite(R2254176FileClosing.class);
    }

    public static void main(String argv[]) {
      junit.textui.TestRunner.run(suite());
    }

    public void testLeaveFileOpen() throws Exception {
        File testfile = File.createTempFile("TestFile", null);

        if (!testfile.exists()) {
            FileOutputStream outOne = new FileOutputStream(testfile);
            PrintWriter myPrint = new PrintWriter(outOne);
            assertNotNull(myPrint);
            //myPrint.close();
        }

        // read some from it
        FileInputStream testInputStream = new FileInputStream(testfile);
        int testInt = testInputStream.read();
        assertNotNull(new Integer(testInt));    // make trivial use of the result
    }
}
