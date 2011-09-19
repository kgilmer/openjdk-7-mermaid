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
 * @summary Verifies simple localhost lookups
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.InetAddress;

public class SimpleGetLocalHost extends TestCase {

    public static Test suite() {
        return new TestSuite(SimpleGetLocalHost.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void test01() throws Exception {
        String kLoopbackAddress = "127.0.0.1";
        
        InetAddress localHostAddress = InetAddress.getLocalHost();
        assertNotNull( localHostAddress.toString() );
        assertNotNull( localHostAddress.getHostName() );
        assertNotNull( localHostAddress.getHostAddress() ) ;
        
        // was it a real address, or just loopback?
        assertFalse( "getLocalHost() returned loopback address; this is probably wrong.", localHostAddress.getHostAddress().equals(kLoopbackAddress));
        // look up the hostname we got back, and see if it's the same address as for localhost
        
        String localHostName = localHostAddress.getHostName();
        InetAddress myAddress = InetAddress.getByName(localHostName);
        
        assertEquals( "lookup of hostname provided by getLocalHost() gave us a different address.", localHostAddress, myAddress);
    }
}

