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
 @summary See rdar://4404074 and rdar://4408322 and rdar://4486195
 @summary This test ensures that we can call Runtime.exec() with a command 
 @summary (and arguments) that contains extended characters like é, ü, ç etc
 @summary It also tests whether environment variables with extended chars 
 @summary are passed correctly.
 @summary com.apple.junit.java.lang.Runtime;
 @run main RuntimeExecTest002
 */

import junit.framework.*;

import java.io.*;

public class RuntimeExecExtendedCharsTest extends TestCase { 
    private static int fileCount = 0;

    String arg1 = "hélløöüç";
    String arg2 = "noaccent";
    String env1 = arg1;
    String env2 = arg2;
  
    String writeLine1 = "echo \"ARG1=$1\"";
    String writeLine2 = "echo \"ARG2=$2\"";
    String writeLine3 = "echo \"ENV1=$ENV1\"";
    String writeLine4 = "echo \"ENV2=$ENV2\"";
 
    String readLine1 = new String("ARG1=" + arg1);
    String readLine2 = new String("ARG2=" + arg2);
    String readLine3 = new String("ENV1=" + env1);
    String readLine4 = new String("ENV2=" + env2);   
   
    public static Test suite() {
        return new TestSuite(RuntimeExecExtendedCharsTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        if (System.getProperty("os.name").equals("Mac OS X")) {
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        else {
            System.out.println("Not running on Mac OS X. Skipping this test.");
        }
    }
    
    public void testRuntimeExec() throws Exception{
        // "HelloÇüéâäà.txt"
        // this should work on local volumes in <= 1.4.2
        execFile("Hello\u00C7\u00FC\u00E9\u00E2\u00E4\u00E0.sh");

        // Decomposed "HelloÇüéâäà.txt"
        // this should work on local volumes AND remote AFP volumes in <= 1.4.2
        execFile("HelloC\u0327u\u0308e\u0301a\u0302a\u0308a\u0300.sh");
    }

    private File createFile(String aName) throws Exception {
        String fullFileName = new String(System.getProperty("java.io.tmpdir") + "/" + aName + fileCount);
        fileCount++;
        File aFile = new File(fullFileName);
        aFile.deleteOnExit();

        // Warn folks that previous test did not clean up properly, probably because of a harness crash
        if (aFile.exists()) {
            System.err.println("WARNING: deleting file "+ aFile.getCanonicalPath() +" , as a copy was left from earlier test.");
            aFile.delete();
        }
        
        FileOutputStream aFileOutputStream = null;
        aFileOutputStream = new FileOutputStream(aFile);
        PrintWriter aPrintWriter = new PrintWriter(new OutputStreamWriter(aFileOutputStream), true);
        aPrintWriter.println(writeLine1);
        aPrintWriter.println(writeLine2);
        aPrintWriter.println(writeLine3);
        aPrintWriter.println(writeLine4);
        
        aFileOutputStream.close();
        
        String execArgs[] = new String[] {"chmod", "a+x", aFile.getAbsolutePath()};
        Process aProcess = Runtime.getRuntime().exec(execArgs);
        aProcess.waitFor();
        assertEquals("Could not chmod file", aProcess.exitValue(), 0);
        
        return aFile;
    } 

      private void execFile(String name) throws Exception {
          execFile(name, false);
      }
      
      private void execFile(String name, boolean testArgs) throws Exception {
          execFile(name, testArgs, false);
      }
      
      private void execFile(String name, boolean testArgs, boolean testEnv) throws Exception {
          execFile(name, testArgs, testEnv, false);
      }
      
      private void execFile(String aName, boolean testArgs, boolean testEnv, @SuppressWarnings("unused") boolean testDir) throws Exception 
      {
            File aFile = createFile(aName);
            String execArgs[] = null;
            if (testArgs) {
                execArgs = new String[] {aFile.getAbsolutePath(), arg1, arg2};
            } else {
                execArgs = new String[] {aFile.getAbsolutePath()};
            }
            
            {
            Process aProcess = null;
            try {
            aProcess = Runtime.getRuntime().exec(execArgs);
            aProcess.waitFor();

            assertEquals("Runtime.exec failed on a filename with accented chars", aProcess.exitValue(), 0);


            InputStream anInputStream = aProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(anInputStream));
            if (testArgs || testEnv) {
              assertEquals("Did not read the line (1) I expected to read", readLine1, reader.readLine());
              assertEquals("Did not read the line (2) I expected to read", readLine2, reader.readLine());
            }
            if (testEnv) {
              assertEquals("Did not read the line (3) I expected to read", readLine3, reader.readLine());
              assertEquals("Did not read the line (4) I expected to read", readLine4, reader.readLine());
            }
            }
            catch (Exception e) {
            e.printStackTrace();
            }
            }
      }
}
