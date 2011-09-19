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
 @run main FileTypeUtils01
 */

import com.apple.eio.FileManager;
import junit.framework.*;

import java.io.File;

public class FileTypeUtils01 extends TestCase {

    public static Test suite() {
        return new TestSuite(FileTypeUtils01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected junit errors or failures.");
        }
    }    
    
    public void testExistance() throws Exception {
        int    fileTypeToSet = 0x40414243;
        int    fileCreatorToSet = 0x60616263;

        File f  = File.createTempFile("HarnessTestTemp02", null);
        assertTrue( "Tempfile shold have been created" , f.exists() );

        String filename = f.getAbsolutePath();

        int    fileType = FileManager.getFileType(filename);
        int    fileCreator = FileManager.getFileCreator(filename);

        // System.out.println( "Before: <" + filename + "> + type: " + Long.toHexString(fileType) + ", creator: " + Long.toHexString(fileCreator));

        FileManager.setFileType(filename, fileTypeToSet);
        FileManager.setFileCreator(filename, fileCreatorToSet);


        fileType = FileManager.getFileType(filename);
        assertEquals("getFileType() result didn't match earlier setFileType() value", fileType, fileTypeToSet );

        fileCreator = FileManager.getFileCreator(filename);
        assertEquals("getFileCreator() result didn't match earlier setFileCreator() value", fileCreator, fileCreatorToSet );

        f.deleteOnExit();
        // System.out.println( "After: <" + filename + "> + type: " + Long.toHexString(fileType) + ", creator: " + Long.toHexString(fileCreator));
    }        
}


