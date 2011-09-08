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
 * @summary <rdar://problem/4659174> getLocalAddress() on a connected socket was
 * @summary returning 0.0.0.0 instead of the actual local address.
 * @summary com.apple.junit.java.net
 * @author Pratik Solanki, ported to jtreg by David Durrence
 */
import java.net.*;

public class GetLocalAddressTest
{
    public static void main(String[] args) throws Exception {
        String sHostname = "java.net";
        InetAddress ina = InetAddress.getByName( sHostname );
        InetSocketAddress isa = new InetSocketAddress( ina, 80 );
        Socket s = new Socket();
        s.connect( isa, 1000 );
        InetAddress iaLocal = s.getLocalAddress(); // if this comes back as 0.0.0.0 this would demonstrate issue
        String hostname = iaLocal.getHostName();
        String preferIPv6addresses = System.getProperty("java.net.preferIPv6Addresses");
        if (preferIPv6addresses != null && preferIPv6addresses.equals("true")) {
            if (hostname.equals("::")) {
                throw new Exception("Invalid anyLocalAddress returned from getLocalAddress (IPv6)");
            }
        } else {
            if (hostname.equals("0.0.0.0")) {
                throw new Exception("Invalid anyLocalAddress returned from getLocalAddress (IPv4)");
            }
        }
    }
}