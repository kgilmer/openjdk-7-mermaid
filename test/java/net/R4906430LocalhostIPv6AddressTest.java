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
 * @summary <rdar://problem/4906430> [JavaJDK15] No IPv6 address returned for local machine
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.*;

public class R4906430LocalhostIPv6AddressTest extends TestCase
{
    public static Test suite() {
        return new TestSuite(R4906430LocalhostIPv6AddressTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void testLocalhostIPv6() throws Exception {
        String preferIPv4Stack = System.getProperty("java.net.preferIPv4Stack", "false");   
        String preferIPv6Addresses = System.getProperty("java.net.preferIPv6Addresses", "false");   

        InetAddress host = InetAddress.getLocalHost();
        InetAddress[] allAddr = InetAddress.getAllByName(host.getHostName());

        boolean foundIPv4 = false, foundIPv6 = false;
        int ipv4Index = -1, ipv6Index = -1;

        for (int i=0; i < allAddr.length; i++) {
            if (allAddr[i] instanceof Inet4Address) {
                foundIPv4 = true;
                ipv4Index = i; 
            }
            else if (allAddr[i] instanceof Inet6Address) {
                foundIPv6 = true;
                ipv6Index = i;
            }
            else
                assertTrue("Unknown InetAddress found", false);
        }
        assertTrue("No IPv4 address found for local machine host", foundIPv4);
        if (preferIPv4Stack.equals("false")) {
            assertTrue("No IPv6 address found for local machine host", foundIPv6);
            if (preferIPv6Addresses.equals("true")) {
                assertTrue("Found IPv4 address before IPv6 address when preferIPv6Addresses was set to true", (ipv6Index < ipv4Index));
            } else {
                assertTrue("Found IPv6 address before IPv4 address when preferIPv6Addresses was set to false", (ipv4Index < ipv6Index));
            }
        } else {
            System.out.println("java.net.preferIPv4Stack was set to true. This test did not check if an IPv6 address was present.");
        }
    }
}
