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
 @run main RuntimeExecTest001
 */

import junit.framework.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class RuntimeExecTest001 extends TestCase {

    //Run a simple unix command, let it report back some result
    public void testSimpleRuntimeExec() throws Exception {
        Process scriptProcess;
        
        if (System.getProperty("os.name").equals("Mac OS X")) {
            scriptProcess = Runtime.getRuntime().exec("/usr/bin/sw_vers");
            BufferedReader in = new BufferedReader(new InputStreamReader(scriptProcess.getInputStream()));        
            
            String line = null; 
            
            if ((line = in.readLine()) != null ) {
                boolean status = line.endsWith("Mac OS X") || line.endsWith("Mac OS X Server");
                assertTrue("Expected string to end with 'Mac OS X' or 'Mac OS X Server'", status);
            }
            while ((line = in.readLine()) != null) { // read any other data returned
            }    
        }
        else {
            System.out.println("Not running on Mac OS X. Skipping this test.");
        }
    }
        
    public static Test suite() {
        return new TestSuite(RuntimeExecTest001.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

