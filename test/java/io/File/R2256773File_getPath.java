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
 * @summary <rdar://problem/2256773> File.getPath() on volume returns trailing slash
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;
import java.io.File;

public class R2256773File_getPath extends TestCase {
    static final boolean DEBUG = false;
    
    public static Test suite() {
        return new TestSuite(R2256773File_getPath.class);
    }

    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }

    public void testR2256773()  {
        String fileString;
        String pathString;
        
        // directory
        fileString = "testDir";
        File    currentDir = new File( fileString );

        pathString = currentDir.getPath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        pathString = currentDir.getAbsolutePath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        currentDir = new File( fileString + "/" );      
        pathString = currentDir.getPath();
        
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        pathString = currentDir.getAbsolutePath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );


        // volume
        fileString = "/Annette";
        File    currentVol = new File( fileString );
        
        pathString = currentVol.getPath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        pathString = currentVol.getAbsolutePath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        currentVol = new File( fileString +"/" );
        
        pathString = currentVol.getPath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );

        pathString = currentVol.getAbsolutePath();
        assertNotNull(pathString);
        if (DEBUG)  System.out.println(pathString);

        assertTrue( pathString.lastIndexOf( fileString ) != -1 );
        assertTrue( pathString.charAt( pathString.length() - 1 ) != '/' );
    }
}
