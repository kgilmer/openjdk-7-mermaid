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
 * @summary <rdar://problem/4619360> [JavaJDK16] JavaJDK16: Need to set prefixlen for IPv6 address
 * @summary com.apple.junit.java.net
 */

import java.net.*;
import java.util.*;
import junit.framework.*;

public class IPv6PrefixLenTest extends TestCase { 
    
    public static Test suite() {
        return new TestSuite(IPv6PrefixLenTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void testPrefixLen() throws Exception
    {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface i = interfaces.nextElement();
            List<InterfaceAddress> interfaceAddresses = i.getInterfaceAddresses();
            for (InterfaceAddress ia : interfaceAddresses) {
                InetAddress addr = ia.getAddress();
                if (addr instanceof Inet6Address) {
                    assertTrue("IPv6 address [ " + addr + " ] had 0 prefixlen. This is probably wrong.", (ia.getNetworkPrefixLength() != 0));
                    assertNull("IPv6 address has non-null broadcast address", ia.getBroadcast());
                }
            }
        }
    }

}
