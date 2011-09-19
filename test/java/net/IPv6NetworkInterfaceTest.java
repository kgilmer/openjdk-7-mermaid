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
 * @summary <rdar://problem/3887227> 1.4.2: Class NetworkInterface can not handle IPv6 addresses correctly
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.*;
import java.util.Enumeration;

public class IPv6NetworkInterfaceTest extends TestCase { 
    
    public static Test suite() {
        return new TestSuite( IPv6NetworkInterfaceTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testNetworkInterface() throws Exception {
        String ipv6_string = null;
        
        try {
            // Get the local host and grab the IPv6 address string
            InetAddress localhost = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(localhost);
            for (Enumeration e = ni.getInetAddresses() ; e.hasMoreElements() ;) {
                InetAddress ia = (InetAddress) e.nextElement();
                if (ia instanceof Inet6Address) {
                    ipv6_string = ia.getHostAddress();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            //Swallow the exception, as the next assert checks that ipv6_string isn't null
        }
        
        assertTrue("Could not find IPv6 address string. Test cannot run.", (ipv6_string != null));
        
        InetAddress ipv6_addr = InetAddress.getByName(ipv6_string);
        
        assertTrue("Could not lookup IPv6 address from " + ipv6_string + ". Test cannot run.", (ipv6_addr != null));
        
        // Whew! Finally we get to the bug!
        NetworkInterface ipv6_ni = NetworkInterface.getByInetAddress(ipv6_addr);
        
        assertTrue("Could not lookup NetworkInterface from " + ipv6_addr, (ipv6_ni != null));
    }
}
