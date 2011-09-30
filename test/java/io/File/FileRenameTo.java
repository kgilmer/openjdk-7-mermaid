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
 * @summary Simple test of File.renameTo
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;

import java.io.File;

public class FileRenameTo extends TestCase
{
    public static Test suite() {
        return new TestSuite(FileRenameTo.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    protected File myFirstFile;
    protected File mySecondFile;

    protected void setUp() {
        myFirstFile = new File(System.getProperty("java.io.tmpdir"), "FileRenameToJUnitFileTEMP");
        mySecondFile = new File(System.getProperty("java.io.tmpdir"), "FileRenameToNEWFILEJUnitFileTEMP");
    }

    public void testRenameTo() throws Exception 
    {
        assertTrue("Original file creation failed", myFirstFile.createNewFile());
        assertTrue("Original file not reported to exist after creation", myFirstFile.exists());
        assertTrue("Original file not reported to be a file after creation", myFirstFile.isFile());
        assertTrue("Original file not reported to be writeable", myFirstFile.canWrite());
        assertTrue("Original file not reported to be readable", myFirstFile.canRead());

        assertFalse("Second file unexpectedly exists on disk after only File object has been created", mySecondFile.exists());
        
        assertTrue("Renaming file to new name failed", myFirstFile.renameTo(mySecondFile));
        
        assertTrue("Second file not reported to exist after rename", mySecondFile.exists());
        assertTrue("Second file not reported to be a file after creation", mySecondFile.isFile());
        assertTrue("Second file not reported to be writeable", mySecondFile.canWrite());
        assertTrue("Second file not reported to be readable", mySecondFile.canRead());
        
        assertFalse("Original file unexpectedly still exists on disk after rename to second file", myFirstFile.exists());
    }
    
    protected void tearDown() {
        if (myFirstFile.exists()) {
            assertTrue("First file deletion failed", myFirstFile.delete());
        }
        if (mySecondFile.exists()) {
            assertTrue("Second file deletion failed", mySecondFile.delete());
        }
    }
}

