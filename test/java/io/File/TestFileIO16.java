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
 * @summary Tests some of the new File IO functionality that was added in 1.6 (setPermissions, getExecutable, get___Space). 
 * @summary Thie file creates a file in /tmp/testPermissions in and then sets/gets the permissions
 * @summary com.apple.junit.java.io.File
 */

import java.io.*;

import junit.framework.*;

public class TestFileIO16 extends TestCase 
{    
    protected void setUp() {

    }
    
    protected void tearDown() {
    }


    public static Test suite() {
        return new TestSuite(TestFileIO16.class);
    }
    
    public void testFileIO() throws Exception {
        File tpFile = File.createTempFile("testPermissions", "");
        assertTrue("Failed to create tmp file testPermissions", tpFile.exists());

        boolean success = tpFile.setWritable(false, false);
        assertTrue("Failed to set write permissions to false on /tmp/testPermissions", success);
            
        success = tpFile.canWrite();
        assertTrue("Failed: /tmp/testPermissions should not be writable", !success);
            
        success = tpFile.setWritable(true, false);
        assertTrue("Failed to set write permissions to true on /tmp/testPermissions", success);
            
        success = tpFile.canWrite();
        assertTrue("Failed: /tmp/testPermissions should be writable", success);

        success = tpFile.setReadable(false, false);
        assertTrue("Failed to set read permissions to false on /tmp/testPermissions", success);

        success = tpFile.canRead();
        assertTrue("Failed: /tmp/testPermissions should not be readable", !success);
                    
        success = tpFile.setReadable(true, false);
        assertTrue("Failed to set read permissions to true on /tmp/testPermissions", success);

        success = tpFile.canRead();
        assertTrue("Failed: /tmp/testPermissions should be readable", success);
        
        success = tpFile.setExecutable(false, false);
        assertTrue("Failed to set execute permissions to false on /tmp/testPermissions", success);

        success = tpFile.canExecute();
        assertTrue("Failed: /tmp/testPermissions should not be executable", !success);
                    
        success = tpFile.setExecutable(true, false);
        assertTrue("Failed to set execute permissions to true on /tmp/testPermissions", success);

        success = tpFile.canExecute();
        assertTrue("Failed: /tmp/testPermissions should be executable", success);
            
        long totalSpace = tpFile.getTotalSpace();
        long freeSpace = tpFile.getFreeSpace();
        long usableSpace = tpFile.getUsableSpace();
        
        assertTrue("There should be at least 10000000 bytes of total space", totalSpace > 1000000);
        assertTrue("There should be at least 10000 bytes of free space", freeSpace > 10000);
        assertTrue("There should be at least 100000 bytes of usable space", usableSpace > 100000);    
        
        tpFile.deleteOnExit();
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

