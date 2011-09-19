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
 * @summary <rdar://problem/1680855> EXT: GetLocalHost() returns incorrect information
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.InetAddress;

public class R1680855GetLocalHost extends TestCase {

    public static Test suite() {
        return new TestSuite(R1680855GetLocalHost.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testTimeout() throws Exception {
        String kLoopbackAddress = "127.0.0.1";
        
        InetAddress myAddress = InetAddress.getLocalHost() ;
        assertFalse("getLocalHost() returned loopback address", kLoopbackAddress.equals(myAddress.getHostAddress()));
    }

    public void testLooped() throws Exception {
        for (int i = 0; i < 10; i++) {
            testTimeout();
        }
    }

}

