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
 * @summary <rdar://problem/2212454> Ext: Socket() > 4 minute delay!
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.*;
import java.util.Date;

public class R2212454NullSocket extends TestCase {

    public static Test suite() {
        return new TestSuite(R2212454NullSocket.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testNullSocket() throws Exception {
        long kMaxSeconds = 30; // the amount of time we could reasonably wait for the exception to be thrown
        Date startTime = new Date();

        try {
            InetAddress addr = null;
            Socket sock = new Socket(addr, 1720);
            fail("Exception not thrown with " + sock);
        }
        catch ( Exception e ) {
            String eName = e.toString();
            Date endTime = new Date();
            long elapsedSeconds = ( endTime.getTime() - startTime.getTime() ) / 1000 ;   // in seconds

            assertTrue("Expected a 'nullSomething' exception", (eName.toLowerCase().indexOf("null") > -1) );
            assertTrue("Expected an exception in less than " + kMaxSeconds + " seconds." , elapsedSeconds < kMaxSeconds );
        }   
    }
    
}

