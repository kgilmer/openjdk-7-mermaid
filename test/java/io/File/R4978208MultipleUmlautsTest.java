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
 * @summary <rdar://problem/4978208> Filename containing extended char problem using java io
 * @summary com.apple.junit.java.io.File
 */

import java.io.File;
import java.io.FileWriter;
import java.text.Collator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class R4978208MultipleUmlautsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(R4978208MultipleUmlautsTest.class);
    }

    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }

    public void testFileWithMultipleUmlauts() throws Exception  {
		String fileName = "Test-&üäöß§&.txt";
        String tmpDir = System.getProperty("java.io.tmpdir");
        createFile(tmpDir + File.separator + fileName);
        
        File f = new File(tmpDir);
        boolean found = false;
        String[] files = f.list();
        for (String file : files) {
            if (Collator.getInstance().equals(file, fileName))
                found = true;
        }
        assertTrue("File should exist but it doesn't", found);
    }

    /** Create the file with dummy content, will remove existing file first if exists */
    private void createFile(String fileName) throws Exception {
        String data = "This is a test file";

        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        file.deleteOnExit();

        FileWriter fw = new FileWriter(file);
        fw.write(data);
        fw.close();
    }
}
