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
 * @summary <rdar://problem/3569904> 142: threaded append double to String causes divide by zero on dual G5 (-Xint wo
 * @summary com.apple.junit.java.vm
 */

import junit.framework.*;
import java.util.Timer;
import java.util.TimerTask;

public class R3569904ThreadedAppendDoubleDivByZeroTest extends TestCase {
    static volatile boolean continueTest = true;
    
    public static Test suite() {
        return new TestSuite(R3569904ThreadedAppendDoubleDivByZeroTest.class);
    }

    public static void main(String argv[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public void testDivByZeroException() throws Exception {
        String doubleBits = "0100000011001011111010111100110101110000010111111001000011100000";
        long doubleLong = 0l;
        for (int i = 0; i < doubleBits.length(); i++) {
            if (doubleBits.substring(i, i+1).equals("1")) {
                doubleLong += 1l << (63-i);
            }
        }
        double d = Double.longBitsToDouble(doubleLong);
        // System.out.println("double value is "+d);

        int numThreads = 2;
        TestThread[] threads = new TestThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new R3569904ThreadedAppendDoubleDivByZeroTest.TestThread(d);
            threads[i].start();
        }
        Timer t = new Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                R3569904ThreadedAppendDoubleDivByZeroTest.continueTest = false;
            }
        };
        t.schedule(task, 2000);

        for (int i = 0; i < numThreads; i++) {
            threads[i].join();
            assertFalse("Thread got an ArithmeticException", threads[i].gotArithmeticException);
        }
    }

    static class TestThread extends Thread {
        boolean gotArithmeticException = false;

        TestThread(double val) {
            this.val = val;
        }

        double val;

        public void run() {
            while(R3569904ThreadedAppendDoubleDivByZeroTest.continueTest){
                doit(val);
            }
        }

        public void doit(double v) {
            try
            {
                String s = "";
                s+=v;
            } catch (ArithmeticException ae) {
                ae.printStackTrace();
                gotArithmeticException = true;
                return;
            }
        }
    }   
}
