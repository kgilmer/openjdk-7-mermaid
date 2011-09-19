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
 * @summary <rdar://problem/3651645> multiple ServerSockets can bind to same address/port
 * @summary <rdar://problem/4236458> [JavaJDK15] REGR: JavaJDK15: setReuseAddress() has no effect on DatagramChannel sockets 
 * @summary com.apple.junit.java.net
 */

import junit.framework.*;
import java.net.*;
import java.nio.channels.*;

public class ReuseAddressTest extends TestCase {
    private static int port = 11000;
    
    public static Test suite() {
        return new TestSuite( ReuseAddressTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
    public void testSocketAndServerSocket() {
        port++;
        checkPort(port);
        // ServerSocketChannel, ServerSocket, Socket - can't bind no matter what the setting
        //  reuse=true
        port++;
        assertTrue("Reuse, ServerSocketChannel, ServerSocket. Failed first bind.", getServerSocketChannel(true, port));
        assertFalse("Reuse, ServerSocketChannel, ServerSocket. Succeeded second bind.", getServerSocket(true, port));
        
        port++;
        assertTrue("Reuse, ServerSocketChannel, Socket. Failed first bind.", getServerSocketChannel(true, port));
        assertFalse("Reuse, ServerSocketChannel, Socket. Succeeded second bind.", getSocket(true, port));
        
        port++;
        assertTrue("Reuse, ServerSocket, ServerSocketChannel. Failed first bind.", getServerSocket(true, port));
        assertFalse("Reuse, ServerSocket, ServerSocketChannel. Succeeded second bind.", getServerSocketChannel(true, port));
        
        port++;
        assertTrue("Reuse, ServerSocket, Socket. Failed first bind.", getServerSocket(true, port));
        assertFalse("Reuse, ServerSocket, Socket. Succeeded second bind.", getSocket(true, port));
        
        port++;
        assertTrue("Reuse, Socket, ServerSocketChannel. Failed first bind.", getSocket(true, port));
        assertFalse("Reuse, Socket, ServerSocketChannel. Succeeded second bind.", getServerSocketChannel(true, port));
        
        port++;
        assertTrue("Reuse, Socket, ServerSocket. Failed first bind.", getSocket(true, port));
        assertFalse("Reuse, Socket, ServerSocket. Succeeded second bind.", getServerSocket(true, port));
    }

    public void testSocketAndServerSocketNoReuse() {
        port++;
        checkPort(port);
        // ServerSocketChannel, ServerSocket, Socket - can't bind no matter what the setting
        // reuse = false
        port++;
        assertTrue("No Reuse, ServerSocketChannel, ServerSocket. Failed first bind.", getServerSocketChannel(false, port));
        assertFalse("No Reuse, ServerSocketChannel, ServerSocket. Succeeded second bind.", getServerSocket(false, port));
        
        port++;
        assertTrue("No Reuse, ServerSocketChannel, Socket. Failed first bind.", getServerSocketChannel(false, port));
        assertFalse("No Reuse, ServerSocketChannel, Socket. Succeeded second bind.", getSocket(false, port));
        
        port++;
        assertTrue("No Reuse, ServerSocket, ServerSocketChannel. Failed first bind.", getServerSocket(false, port));
        assertFalse("No Reuse, ServerSocket, ServerSocketChannel. Succeeded second bind.", getServerSocketChannel(false, port));
        
        port++;
        assertTrue("No Reuse, ServerSocket, Socket. Failed first bind.", getServerSocket(false, port));
        assertFalse("No Reuse, ServerSocket, Socket. Succeeded second bind.", getSocket(false, port));
        
        port++;
        assertTrue("No Reuse, Socket, ServerSocketChannel. Failed first bind.", getSocket(false, port));
        assertFalse("No Reuse, Socket, ServerSocketChannel. Succeeded second bind.", getServerSocketChannel(false, port));
        
        port++;
        assertTrue("No Reuse, Socket, ServerSocket. Failed first bind.", getSocket(false, port));
        assertFalse("No Reuse, Socket, ServerSocket. Succeeded second bind.", getServerSocket(false, port));
    }

    public void testDatagramChannel() throws Exception {
        port++;
        checkPort(port);
        // DatagramChannel - can bind reuseAddress is specified for all UDP sockets
        port++;
        assertTrue("Reuse, DatagramChannel. (true,true) Failed first bind.", getDatagramChannel(true, port));
        assertTrue("Reuse, DatagramChannel. (true,true) Failed second bind.", getDatagramChannel(true, port));

        port++;
        assertTrue("No Reuse, DatagramChannel. (false,false) Failed first bind.", getDatagramChannel(false, port));
        assertFalse("No Reuse, DatagramChannel. (false,false) Succeded second bind.", getDatagramChannel(false, port));
        
        port++;
        assertTrue("Reuse, DatagramChannel. (true,false) Failed first bind.", getDatagramChannel(true, port));
        assertFalse("No Reuse, DatagramChannel. (true,false) Succeded second bind.", getDatagramChannel(false, port));
        
        port++;
        assertTrue("No Reuse, DatagramChannel. (false,true) Failed first bind.", getDatagramChannel(false, port));
        assertFalse("Reuse, DatagramChannel. (false,true) Succeded second bind.", getDatagramChannel(true, port));
    }
    
    public void testMulticastSocket() throws Exception {
        port++;
        checkPort(port);
        // MulticastSocket - can bind if all MS have reuseAddress
        // Watch carefully which are assertTrue, and which are assertFalse!
        port++;
        assertTrue("Reuse, MulticastSocket (true, true). Failed first bind.", getMulticastSocket(true, port));
        assertTrue("Reuse, MulticastSocket (true, true). Failed second bind.", getMulticastSocket(true, port));

        port++;
        assertTrue("No Reuse, MulticastSocket (false, false). Failed first bind.", getMulticastSocket(false, port));
        assertFalse("No Reuse, MulticastSocket (false, false). Succeeded second bind.", getMulticastSocket(false, port));
        
        port++;
        assertTrue("Reuse, MulticastSocket (true, false). Failed first bind.", getMulticastSocket(true, port));
        assertFalse("No Reuse, MulticastSocket (true, false). Succeeded second bind.", getMulticastSocket(false, port));
    }
    
    private void checkPort(int port) {  
        // Make sure that we aren't running too soon after another test...
        boolean okToGo = false;
        int tooMany = 5;
        do {
            try {
                Socket socket = new Socket();
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress(port));
                okToGo = true;
            } catch (Exception e) {
                // We can't bind because someone is sitting on the port
                // Wait a minute and try again
                try {
                    Thread.sleep(1000);
                } catch (Exception e2) {
                    // ignore
                }
                tooMany--;
            }
        } while ( (!okToGo) && (tooMany > 0));
        
        assertTrue("Cannot bind to " + port + ", giving up on this test.", okToGo);
    }
    
    private static boolean getServerSocketChannel(boolean reuse, int port) {
        try {
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverChannel.socket();
            serverSocket.setReuseAddress(reuse);
            serverSocket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private static boolean getDatagramChannel(boolean reuse, int port) {
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            DatagramSocket datagramSocket = datagramChannel.socket();
            datagramSocket.setReuseAddress(reuse);
            datagramSocket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    private static boolean getServerSocket(boolean reuse, int port) {
        try {
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(reuse);
            serverSocket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean getSocket(boolean reuse, int port) {
        try {
            Socket socket = new Socket();
            socket.setReuseAddress(reuse);
            socket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static boolean getMulticastSocket(boolean reuse, int port) {
        try {
            MulticastSocket socket = new MulticastSocket(null);
            socket.setReuseAddress(reuse);
            socket.bind(new InetSocketAddress(port));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
}
