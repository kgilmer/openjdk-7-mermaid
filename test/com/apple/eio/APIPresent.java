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
 @summary Tests FileManager API
 @summary com.apple.junit.apple.eio;
 @run main APIPresent
 */

import com.apple.eio.FileManager;
import junit.framework.*;

import java.io.File;
import java.io.IOException;

public class APIPresent extends TestCase {

    public static Test suite() {
        return new TestSuite(APIPresent.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected junit errors or failures.");
        }
    }
    
    // Attempting to use FileManager.getFileType() call -- test suceeds if this does not throw exception

    public void testExistance() throws Exception {
        try {
            File f  = File.createTempFile("HarnessTestTemp01", null);
            int    fileType = FileManager.getFileType(f.getAbsolutePath());
            assertNotNull(new Integer(fileType)); // make trivial use of the returned result
            f.deleteOnExit();
        }
        catch (IOException e) {
            fail("### Error: Unexpected IOException attempting to createTempFile.");
        }
        catch (SecurityException e) {
            fail("### Error: Unexpected SecurityException attempting to createTempFile.");
        }
        catch (Exception e) {
            fail("### Failure: Unexpected exception during FileManager call.");
        }
    }
}

