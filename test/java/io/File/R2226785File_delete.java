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
 * @summary <rdar://problem/2226785> EXT: delete() fails to delete directory
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;
import java.io.File;

public class R2226785File_delete extends TestCase {
    static final boolean DEBUG = false;
    
    public static Test suite() {
        return new TestSuite(R2226785File_delete.class);
    }
    
    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }

    private String STARTINGPOINT = System.getProperty("java.io.tmpdir");
    
    public void MakeFolder( File inFolder ) throws Exception {
        File dot = new File(STARTINGPOINT);
        assertTrue( "ERROR: the directory " + dot + " is not writeable!", dot.canWrite() );
        assertTrue( "ERROR: inFolder.mkdirs() " + inFolder , inFolder.mkdirs() );
    }

    public boolean TestDelete( String absolutePath ) {  
        File f = new File( absolutePath );

        if( f.exists() ) {
            if(!f.delete()) {
                if (DEBUG) System.out.println("Unable to delete (" + absolutePath +").");
                if (f.isDirectory() && (f.list().length > 0)) {
                    if (DEBUG) System.out.println("Ah ha! It's a non-empty directory. So we shouldn't be able to delete it!");
                }
                return false;
            }
        } else {
            if (DEBUG) System.out.println("ERROR: (" + absolutePath + ") does not appear to exist!");
            return false;
        }
        return true;
    }
    
    public void testCreateDelete() throws Exception {
        File testFolder1 = new File(STARTINGPOINT, "testFolder1");
        
        assertFalse("Please delete previously created test files and folders.\nTheir existence indicates a previous failure.", testFolder1.exists() );
        MakeFolder( testFolder1 );

        
        File testFolder2 = new File( testFolder1, "testFolder2" );
        if (DEBUG) System.out.println( "Making " + testFolder2.toString());

        MakeFolder( testFolder2 );
        assertTrue( "Both folders successfully created.", testFolder1.exists() && testFolder2.exists());

        if (DEBUG) System.out.println( "Deleting a folder which contains a sub-folder: " + testFolder1);

        assertFalse( "ERROR: File.delete() deleted a non-empty folder.",  TestDelete( testFolder1.getAbsolutePath() ));
        
        if (DEBUG) System.out.println( "Deleting " + testFolder2);

        assertTrue("ERROR: File.delete() failed.", TestDelete( testFolder2.getAbsolutePath() ));
        assertFalse( "ERROR: (" + testFolder2 + ") exists after being deleted.", testFolder2.exists() );

        if (DEBUG) System.out.println( "Deleting " + testFolder1);
        
        assertTrue("ERROR: File.delete() failed.", TestDelete( testFolder1.getAbsolutePath() ) );
        assertFalse("ERROR: (" + testFolder1 + ") exists after being deleted.", testFolder1.exists() );
    }
}

