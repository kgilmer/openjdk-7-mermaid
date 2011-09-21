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
 @summary make sure that Thread.stop() works
 @summary com.apple.junit.java.lang.Thread;
 @run main StopSleepTest
 */


import junit.framework.*;

public class StopSleepTest extends TestCase {
    public static Test suite() {
        return new TestSuite(StopSleepTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    final static Object o = new Object();
    static volatile boolean in_sleep = false;
    static volatile boolean stop_worked = false;
    @SuppressWarnings("deprecation")
    // test explicitly needs to use "stop", which is deprecated
    public static void testCase() {
        Thread t = new Thread(new Runnable() { public void run() {
            try {
                in_sleep = true;
                //System.out.println("sleep");
                Thread.sleep(3000); // just a bit
                in_sleep = false;
            } catch(Exception e) {
                //System.out.println(Thread.currentThread().getName() + " caught " + e);
                //e.printStackTrace();
                in_sleep = false;
            }
        }
        });
        t.start();
        while (!in_sleep) {} // Lame
        synchronized(o) {
            //System.out.println("stop");
            t.stop();
        }
        try {
            Thread.sleep(8000); // sleep longer than our sleep thread
        } catch (Exception e) {
            assertTrue(false);
        }
        assertTrue(in_sleep); // if we finished sleep, we failed to interrupt
    }
}

