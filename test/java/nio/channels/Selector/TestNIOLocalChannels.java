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
 @summary This tests the basic functionality of sockets and selectors via local channels. 
 @summary It starts a server on one Thread, then it starts a client on another thread. 
 @summary It then checks for the validity of the data transfered betweenthem. 
 @summary If the data is not transmitted correctly the tests reports an error.
 @summary com.apple.junit.java.nio.Selector
 @library ../../regtesthelpers
 @run main TestNIOLocalChannels
 */

import junit.framework.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class TestNIOLocalChannels extends TestCase 
{
    static final int PORT = 10000;
    
    public static Object lock = new Object();
    public static boolean finishedReading = false;
    public static int correctAnswers = 0;
    
    
    protected void setUp()
    {
    	new Server().start();
        
    }
    
    protected void tearDown()
    {
    }
    
    public void testTestMe() throws Exception
    {
    	int count = 0;
    	while (true)
    	{
    		count++;
    		synchronized(lock)
    		{
				if (finishedReading)
				{
					assertTrue(" The transfered data from the server to the client didn't match exactly", correctAnswers > 50);
					break;
				}
			}
    		Thread.sleep(200);
    		
    		// this means that the test hung - ABORT
    		if (count == 1000)  
    		{
    			finishedReading = true;
    			assertTrue(" The test hung - ABORTING", false);
    			break;
    		}
    	}
    }
    
    public static Test suite()
    {
        return new TestSuite(TestNIOLocalChannels.class);
    }
    
    public static void main (String[] args)
    {
        junit.textui.TestRunner.run(suite());
    }
}

/*
 * Simple HTTP response server, adapted from O'Reilly NIO book's echo
 * server example.
 */
class Server extends Thread {
    
    static final byte [] RESPONSE_BYTES =
    "HTTP/1.0 200 OK\r\nContent-Type: image/gif\r\nContent-Length: 2\r\nServer: D1\r\nConnection: close\r\n\r\nHello".getBytes();
    
    final ByteBuffer mRequestBuf;
    final ByteBuffer mResponseBuf;
    
    Server() {
        mRequestBuf = ByteBuffer.allocateDirect(4096);
        mResponseBuf = ByteBuffer.allocateDirect(RESPONSE_BYTES.length);
        mResponseBuf.put(RESPONSE_BYTES);
        mResponseBuf.flip();
    }
    
    public void run() {
        try {
            
            final ServerSocketChannel serverChannel = ServerSocketChannel.open();
            
            serverChannel.socket().bind(new InetSocketAddress(TestNIOLocalChannels.PORT));
            serverChannel.configureBlocking(false);
            
            final Selector selector = Selector.open();
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            // now we can run our client thread
            new Client().start();
            
            while (true) {
                
				synchronized(TestNIOLocalChannels.lock)
				{
					if (TestNIOLocalChannels.finishedReading)
					{
						break;
					}
				}
                
                // Block and spin until at least one key is ready.
                if (selector.select(1000) == 0)
                    continue;
                
                final Iterator keyItr = selector.selectedKeys().iterator();
                while (keyItr.hasNext()) {
                    
                    final SelectionKey key = (SelectionKey) keyItr.next();
                    
                    // Set new connections to readable.
                    if (key.isAcceptable()) {
                        final SocketChannel ch = ((ServerSocketChannel) key.channel()).accept();
                        if (ch == null)
                            return;
                        ch.configureBlocking(false);
                        ch.register(selector, SelectionKey.OP_READ);
                    }
                    
                    // Handle readable connections.
                    else if (key.isReadable()) {
                        final SocketChannel ch = (SocketChannel) key.channel();
                        ch.read(mRequestBuf);
                        mRequestBuf.clear(); // don't need data for testing.
						mResponseBuf.rewind();
                        ch.write(mResponseBuf);
                        ch.close();
                    }
                    
                    // assert increment();
                    keyItr.remove();
                }
            }
            
        } catch (Exception e) {
            System.err.println("PixelServer: exception in main select loop!");
            e.printStackTrace();
        }
    }
}

/**
 * Simple HTTP client with statistics logging.
 */
class Client extends Thread {
    public void run() {
        try {
            final byte [] buf = new byte[4096];
            char[] expectedResponse = { 'H', 'e', 'l', 'l', 'o' };
            int count = 0;
            while (true) {
                
            	synchronized(TestNIOLocalChannels.lock)
				{
					if (TestNIOLocalChannels.finishedReading)
					{
						break;
					}
				}
                
            	final InputStream is = new URL("http://localhost:"+TestNIOLocalChannels.PORT+"/" + count).openStream();
                int num = 0;
                while (num != -1)
                {
                	num = is.read(buf);
                	for (int i = 0; i < num; i++)
                	{
                		if (((char) buf[i]) == expectedResponse[i])
                		{
                			TestNIOLocalChannels.correctAnswers++;
                		}
                		else
                		{
                			TestNIOLocalChannels.correctAnswers = -1;
                			break;
                		}
                	}
                }
                is.close();
                
                // we got 50 correct answers, we can break now
                if (TestNIOLocalChannels.correctAnswers > 50)
                {
                	break;
                }
                
                if (TestNIOLocalChannels.correctAnswers == -1)
                { 
                	break;
                }
                //                 if (count % 5000 == 0) {
                //                     long t = System.currentTimeMillis();
                //                     System.out.println("5k hits in " + (t - time) + "ms");
                //                     time = t;
                //                 }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		synchronized(TestNIOLocalChannels.lock)
		{
			TestNIOLocalChannels.finishedReading = true;
		}
    }
}