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
 @summary <rdar://problem/4664546> Make sure Thread.interrupt() works
 @summary com.apple.junit.java.vm
 @library ../awt/regtesthelpers
 @build Waypoint
 @run main ThreadInterrupt_R4664546
 */
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;

public class ThreadInterrupt_R4664546 extends TestCase {
    
    final static Object o = new Object();
    static volatile boolean waitingForException = false;
    static volatile Exception intEx = null;
    static volatile Waypoint threadStarted = new Waypoint();
    static volatile Waypoint exceptionHappened = new Waypoint();

    
    public void testThreadInterrupt_R4664546() throws Exception {
        Thread t = new Thread(new Runnable() { 
            public void run() {
                try {
                    threadStarted.clear();
                    Thread.sleep(5000);
                }
                catch (Exception e) {
                    intEx = e;
                    exceptionHappened.clear();
                }
            }
        });

        t.start();

        threadStarted.requireClear();
        assertTrue("We timed out without starting the test thread", threadStarted.isClear());

        t.interrupt();

        exceptionHappened.requireClear();
        assertTrue("We timed out without gettng an exception", exceptionHappened.isClear());

        assertTrue("t.interrupt() did not result in an Exception being thrown", (intEx != null));
        assertTrue("t.interrupt() did not result in InterruptedException", Class.forName("java.lang.InterruptedException").isInstance(intEx));
    }
    
    public static Test suite() {
        return new TestSuite(ThreadInterrupt_R4664546.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
