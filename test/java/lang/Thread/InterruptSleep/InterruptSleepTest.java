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
 @summary Make sure that Thread.interrupt() works
 @summary Simple test for interrupting Thread.sleep
 @summary com.apple.junit.java.lang.Thread;
 @run main InterruptSleepTest
 */

import junit.framework.*;

public class InterruptSleepTest extends TestCase {
    public static Test suite() {
        return new TestSuite(InterruptSleepTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    final static Object o = new Object();
    static volatile boolean in_sleep = false;
    static volatile boolean interrupt_worked = false;
    public static void testCase() {
        Thread t = new Thread(new Runnable() { public void run() {
            try {
                    in_sleep = true;
                    //System.out.println("sleep");
                    Thread.sleep(6000); // just a bit
                    in_sleep = false;
            } catch(Exception e) {
                //System.out.println(Thread.currentThread().getName() + " caught " + e);
                //e.printStackTrace();
                interrupt_worked = true;
                in_sleep = false;
            }
        }});
        t.start();
        while (!in_sleep) {} // Lame
        synchronized(o) {
            //System.out.println("interrupt");
            t.interrupt();
        }
        while (in_sleep) {} // spin while we are still in the sleep
        assertTrue(interrupt_worked);
    }
}
