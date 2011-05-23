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
 * @summary <rdar://problem/2408635> Fail to find File.list() item if it contains special characters
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.Collator;

public class R2408635SpecialCharacters extends TestCase {
    public static Test suite() {
        return new TestSuite(R2408635SpecialCharacters.class);
    }

    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }

    private final  String tmp = System.getProperty("java.io.tmpdir");
    private final  String filenames[] = {
        "NO_SPECIAL_CHARS",
		"SPECIAL_CHARS_ü AND é",
		"SPECIAL_CHARS_ü AND é AND î AND ñ AND `` INSIDE A_REALLY_LONG FILENAME"
    };
    protected File[] probes = new File[filenames.length];

    public void testR2408635() throws Exception {
        // create test probes
        for (int i = 0; i < filenames.length; i++ ) {
            probes[i] = new File(tmp, filenames[i]);
            RandomAccessFile raf = new RandomAccessFile(probes[i], "rw");
            assertNotNull( raf);
            raf.close();    
        }

        // locate and cleanup test probes
        for (File probe : probes) {
            assertTrue("Should have found " + probe.getCanonicalPath(), checkExists(probe));
            probe.delete();
            assertFalse("Should not find " + probe.getCanonicalPath(), checkExists(probe));
        }
    }

    // Parse the probes in the tmp directory
    public boolean checkExists( File f ){
        // Collator performs locale-sensitive String comparisons.
        boolean found = false;
        Collator myCollator = Collator.getInstance();

        String[] list = new File(tmp).list();
        assertNotNull( list );

        for (String item : list) {
            if ((f.getName()).equalsIgnoreCase(item)) {
                found = true;
                break;
            }

            if (myCollator.compare(f.getName(), item) == 0) {
                found = true;
                break;
            }
        }
        
        return(found);
    }
}
