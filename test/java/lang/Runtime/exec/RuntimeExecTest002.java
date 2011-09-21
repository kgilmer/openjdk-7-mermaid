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

/**
 @test
 @summary RuntimeExecTest001 - basic tests of Runtime.exec(), see if results get returned through stdin
 @summary com.apple.junit.java.lang.Runtime;
 @run main RuntimeExecTest002
 */
 
import junit.framework.*;

import java.io.*;

public class RuntimeExecTest002 extends TestCase {
        
    public static Test suite() {
        return new TestSuite(RuntimeExecTest002.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    static boolean DEBUG = false;

    public void ls (String fileStr) throws Exception {
        InputStream ris = Runtime.getRuntime().exec("ls -al " + fileStr).getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(ris));
        String line;
        while ( (line = br.readLine()) != null) {
            if (DEBUG) System.out.println(line);
        }
    }

    public void testChmod() throws Exception {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            File tempf;
            tempf = File.createTempFile("runtimeexectesttemp", ".txt");
            tempf.deleteOnExit();
            String tempStr = tempf.getCanonicalPath();
    
            if (DEBUG) System.out.println("Before:" );
            ls( tempStr);
    
            Process proc = Runtime.getRuntime().exec("chmod a+x " + tempStr);
            int res = proc.waitFor();
    
            assertTrue("chmod returned an error value of " + res, res == 0);
    
            if (DEBUG) System.out.println("After:" );
            ls( tempStr);
        }
        else {
            System.out.println("Not running on Mac OS X. Skipping this test.");
        }
    }
}
