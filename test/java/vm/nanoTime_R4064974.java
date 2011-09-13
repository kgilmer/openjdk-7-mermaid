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
 @summary <rdar://problem/4064974>
 @summary Tests nano time and also makes sure that it does not take too long to make consecutive time calls
 @summary com.apple.junit.java.vm
 @run main nanoTime_R4064974
 */

import junit.framework.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;

public class nanoTime_R4064974 extends TestCase {
    public void testnanoTime_R4064974() throws Exception {
        long time = 0;
        long newTime;
        long smaller = 9999999999999L;

        for (int i = 0; i < 10000; i++) {
            time = nanoTime();
            newTime = nanoTime();
            smaller = Math.min(smaller, newTime - time);
        }
        
        long milliTime = currentTimeMillis();
        long nanoTime = nanoTime();
        assertTrue("it's taking to long to call nanoTime() after calling currentTimeMillis()", (nanoTime - (milliTime * 1000000)) < 5000000);
    }
    
    public static Test suite() {
        return new TestSuite(nanoTime_R4064974.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
