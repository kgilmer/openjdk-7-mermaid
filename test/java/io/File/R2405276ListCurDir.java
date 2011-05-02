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
 * @summary <rdar://problem/2405276> List of files in current directory using dot notation (".") is null
 * @summary com.apple.junit.java.io.File
 */

import junit.framework.*;
import java.io.File;

public class R2405276ListCurDir extends TestCase {
    public static Test suite() {
        return new TestSuite(R2405276ListCurDir.class);
    }

    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }

    /* 
     Original test
    */
    public void testR2405276() throws Exception {
        String[] list = (new File(".")).list();
        assertNotNull(list);
    }

    /* 
    Stress test to make it a little more fun to play
    */
    Exception cached = null;
    Thread threads[] = new Thread[100];
    
    public void testR2405276Stress() throws Exception {
        for( int i = 0; i < threads.length; i++) {
            threads[i] = new Thread( new Runnable() {
                public void run() {
                    try {
                        testR2405276();
                    }
                    catch( Exception x) {
                        cached = x;
                    }
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread1 : threads) {
            thread1.join();
        }

        if (cached != null) {
            throw cached;
        }
    }
}
