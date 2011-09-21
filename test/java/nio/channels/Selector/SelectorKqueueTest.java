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
 @library ../../regtesthelpers
 @run main SelectorKqueueTest
 */

import junit.framework.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class SelectorKqueueTest extends TestCase
{
	public static final boolean DEBUG =	false;
	public static final int	PORT	=	1234;
    
	private Pipe	signal;
	private Thread	listener;
	private volatile boolean	running;
    
    public static Test suite() {
        return new TestSuite( SelectorKqueueTest.class);
    }
    
    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }
    
    public void testSelector() {
		SelectorKqueueTest	test;
        
		try {
			test=new SelectorKqueueTest();
			test.start();
			Thread.sleep(3000);
			test.stop();
        }
		catch( Throwable x ) {
			x.printStackTrace(System.err);
        }
	}
    
	public SelectorKqueueTest() {
		super();
		signal=null;
		listener=null;
		running=false;
	}
    
	public void start() throws IOException {
		if (DEBUG) {System.out.println("start");};
        
		running=true;
        
		signal=Pipe.open();
		signal.source().configureBlocking(false);
        
		listener=new Thread() {
			public void run() {
				try {
					listen();
                }
				catch( Throwable x ) {
					x.printStackTrace(System.err);
                }
            }
        };
		listener.setPriority(Thread.NORM_PRIORITY);
		listener.setDaemon(true);
		listener.start();
	}
    
	public void stop() throws IOException, InterruptedException {
		if (DEBUG) {System.out.println("stop");};
        
		running=false;
		signal();
		listener.join();
        
		signal.sink().close();
		signal.source().close();
	}
    
	protected void listen() throws IOException {
		ServerSocketChannel	server;
		Selector			selector;
		SelectionKey		key;
		Iterator			iter;
		int					ret;
        
		if (DEBUG) {System.out.println("start listening");};
        
		selector=Selector.open();
        
		server=ServerSocketChannel.open();
		server.configureBlocking(false);
		server.socket().setReuseAddress(true);
		server.socket().bind(new InetSocketAddress(PORT),5);
        
		while (running) {
			server.register(selector,SelectionKey.OP_ACCEPT);
			signal.source().register(selector,SelectionKey.OP_READ);
            
			ret=selector.select();
			if (DEBUG) {System.out.println("got #"+ret);};
            
			iter=selector.selectedKeys().iterator();
			while (iter.hasNext()) {
				key=(SelectionKey) iter.next();
				iter.remove();
                
				if (key.isValid() && key.isAcceptable()) {
					if (DEBUG) {System.out.println(" accept");};
                }
                
				if (key.isValid() && key.isReadable()) {
					if (DEBUG) {System.out.println(" read");};
                }
                
				if (key.isValid() && key.isWritable()) {
					if (DEBUG) {System.out.println(" write");};
                }
            }
        }
        
		server.close();
		selector.close();
        
		if (DEBUG) {System.out.println("stop listening");};
	}
    
	protected void signal() throws IOException {
		byte[]	dummy = new byte[] { (byte) 0 };
        
		//System.out.println("signal server");
		signal.sink().write(ByteBuffer.wrap(dummy));
	}
}
