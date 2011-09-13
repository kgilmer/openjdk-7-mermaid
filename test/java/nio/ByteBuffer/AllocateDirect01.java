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
 @summary <rdar://problem/3992478>
 @summary Make sure that memory allocated by ByteBuffer.allocateDirect can be reclaimed by the garbage collector
 @summary Note that this test starts by allocating very large chunks of memory, so this may disturb other tests slightly
 @summary com.apple.junit.java.nio;
 @run main AllocateDirect01
 */

import junit.framework.*;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class AllocateDirect01 extends TestCase {

    // boilerplate
    public static Test suite() {
        return new TestSuite(AllocateDirect01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }

    // Use a timer to end the test
    volatile boolean endtest = false;
    Timer ticktock = null;
    protected void setUp() throws Exception {
        ticktock = new Timer();

        TimerTask stopper = new TimerTask()        {
            public void run() {
                endtest = true;
            }
        };
        ticktock.schedule( stopper, 1500L );
    }

    protected void tearDown() throws Exception {
        ticktock.cancel();
    }


    // loop, allocating memory we expect to get reclaimed by GC
    public void testBuffers() throws Exception {

        // find some suitable size to loop with by using up all of memory
        int size = 4;
        while (endtest == false) {
            try {
                ByteBuffer bb = ByteBuffer.allocateDirect(size);
                assertNotNull(bb);
                size *= 1.5;
            }
            catch( OutOfMemoryError e) {
                size /= 32;
                break;
            }
        }
        
        // We should be able to loop as long as we want, since buf can get reclaimed by gc
        while (endtest == false) {
          ByteBuffer buf = ByteBuffer.allocateDirect(size);
          assertNotNull(buf);
        }

    }
}

