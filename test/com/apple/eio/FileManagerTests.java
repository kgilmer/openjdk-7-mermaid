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
 @run main FileManagerTests
 */
import com.apple.eawt.Application;
import com.apple.eio.FileManager;
import junit.framework.*;

import java.io.File;

public class FileManagerTests extends TestCase {

    public static Test suite() {
        return new TestSuite(FileManagerTests.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected junit errors or failures.");
        }
    }    

    protected void tearDown() {
        try {
            // Close all Finder windows.
            Runtime.getRuntime().exec(new String[] {  "/usr/bin/osascript", "-e", "tell application \"Finder\" to close windows"});
            // Activate the harness
            Application.getApplication().requestForeground(true);
            Thread.sleep(1000);
        }
        catch (Exception e) {
            // e.printStackTrace();
        }
    }
    
    
    /**
     * Tests the API FileManager.revealInFinder
     * @throws Exception
     */
    public void testRevealInFinder() throws Exception {
        File f  = File.createTempFile("testRevealInFinder", null);
        assertTrue( "Tempfile should have been revealed" , FileManager.revealInFinder(f));
        Thread.sleep(1000);
        f.delete();
        File nonExistentFile = new File("/tmp/testRevealInFinder" + ((int)Math.random() * 100000));
        if (nonExistentFile.exists()) {
            nonExistentFile.delete();
        }
        try {
            FileManager.revealInFinder(nonExistentFile);
            fail("Should throw FileNotFoundException");
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        // TODO add more comprehensive testcases..
    }        

    /**
     * Tests the API FileManager.moveToTrash
     * @throws Exception
     */
    public void testMoveToTrash() throws Exception {
        File f  = File.createTempFile("testMoveToTrash", null);
        assertTrue( "Tempfile should have been moved to trash" , FileManager.moveToTrash(f));

        File nonExistentFile = new File("/tmp/testRevealInFinder" + ((int)Math.random() * 100000));
        if (nonExistentFile.exists()) {
            nonExistentFile.delete();
        }
        try {
            FileManager.moveToTrash(nonExistentFile);
            fail("Should throw FileNotFoundException");
        }
        catch (Exception e) {
            //e.printStackTrace();
        }
        // TODO add more comprehensive testcases..
    }        

}


