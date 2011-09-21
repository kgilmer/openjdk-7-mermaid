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

/**
 @test
 @summary Test to make sure kqueues work in 64-bit. We create two socket channels and write to them. 
 @summary We then use a Selector on those two channels and make sure select() returns us a non-zero value.
 @summary com.apple.junit.java.nio.Selector
 @run main Kqueues64bitTest_R4258155
 */

import junit.framework.*;

import java.io.IOException;
import java.net.*;
import java.nio.channels.*;
import java.util.Set;

public class Kqueues64bitTest_R4258155 extends TestCase
{
	public static Test suite() {
		return new TestSuite( Kqueues64bitTest_R4258155.class);
	}
    
	public static void main( String[] args ) {
		junit.textui.TestRunner.run( suite() );
	}
    
	public static final int PORT = 10000;
    
	static Object lock = new Object();
	static volatile boolean startTest = false;
	static volatile boolean parttwo = false;
    
	public void test64bitKqueues() throws Exception {
		String preferSelect = System.getProperty("java.nio.preferSelect", "false");
		if (preferSelect.equals("true")) {
			System.err.println("WARNING! java.nio.preferSelect=true! We're not using kqueues. Test results could be incorrect!");
		}
		
		Selector selector = Selector.open();
        
		ServerThread st = new ServerThread();
		st.start();
        
		synchronized (Kqueues64bitTest_R4258155.lock) {
			while (startTest == false)
				Kqueues64bitTest_R4258155.lock.wait();
		}
        
		SocketChannel channel1 = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), Kqueues64bitTest_R4258155.PORT));
		SocketChannel channel2 = SocketChannel.open(new InetSocketAddress(InetAddress.getLocalHost(), Kqueues64bitTest_R4258155.PORT));
		channel1.configureBlocking(false);
		channel2.configureBlocking(false);
        
		SelectionKey channel1Key = channel1.register(selector, channel1.validOps());
		SelectionKey channel2Key = channel2.register(selector, channel2.validOps());
        
		synchronized (Kqueues64bitTest_R4258155.lock) {
			while (Kqueues64bitTest_R4258155.parttwo == false)
				Kqueues64bitTest_R4258155.lock.wait();
		}
        
		int count = selector.select();
		assertFalse("Should get at least one selected channel", (count==0));
		// System.out.println("selector.select() returned : " + count);
		Set <SelectionKey> selectedKeys = selector.selectedKeys();
		for (SelectionKey key : selectedKeys) {
			boolean IKnowTheKey = key.equals(channel1Key) || key.equals(channel2Key);
			assertTrue("Got a key I know nothing about", IKnowTheKey);
            
		}
		selector.close();
	}
}

class ServerThread extends Thread
{
	public void run() {
		try {
			ServerSocket server = new ServerSocket(Kqueues64bitTest_R4258155.PORT);
            
			synchronized (Kqueues64bitTest_R4258155.lock) {
				Kqueues64bitTest_R4258155.startTest = true;
				Kqueues64bitTest_R4258155.lock.notify();
			}
            
			// Wait for two connections
			Socket conn1 = server.accept();
			Socket conn2 = server.accept();
            
            
			conn1.getOutputStream().write(61);
			conn2.getOutputStream().write(62);
            
			synchronized (Kqueues64bitTest_R4258155.lock) {
				Kqueues64bitTest_R4258155.parttwo = true;
				Kqueues64bitTest_R4258155.lock.notify();
			}
            
			server.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
