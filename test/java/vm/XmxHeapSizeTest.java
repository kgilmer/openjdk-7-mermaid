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
 * @author Victor Hernandez; ported to jtreg by David Durrence
 * @summary <rdar://problem/4235868> This is a test to ensure that we can allocate a Java heap of size 2 Gb.
 * @summary com.apple.junit.java.vm
 */

import java.io.File;

public class XmxHeapSizeTest
{
    /*
     * These strings match the strings in the VM. If we change the error message
     * in the VM, we need to update these.  
     */
    String errorMsgLine1 = "Error occurred during initialization of VM";
    String errorMsgLine2 = "Could not reserve enough space for object heap";

    /*
     * Spawns 'java -Xmx2048M -version' and checks that a Java heap of max size 2 Gb can be allocated
     */
    public static void main(String[] args) throws Exception{
        String javaHome = System.getProperty("java.home");
        String path = javaHome + File.separator + "bin" + File.separator + "java";  
        
        // confirm that appropriate executable exists
        File executable = new File(path);
        if (!executable.exists()) {
            throw new RuntimeException("Problems locating " + path);
        }

        String[] command = {
            path,
            "-Xmx2048M",
            "-version"
        };

        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        int exitValue = p.exitValue();
        if (exitValue != 0) {
            throw new RuntimeException("Unable to allocate a Java heap of size 2 Gb");
        }
    }
}
