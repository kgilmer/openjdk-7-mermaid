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
 @summary Test of java.nio.Channel.
 @summary com.apple.junit.java.nio.channels
 @run main FileChannel_transferFrom
 */

import junit.framework.*;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileChannel_transferFrom extends TestCase
{
    public static Test suite() {
        return new TestSuite( FileChannel_transferFrom.class);
    }
    
    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }
    
    private static final String src = System.getProperty("harness.runner.home") +
    File.separator + "data" + File.separator + "nio" + File.separator + "Franklin.txt";
    private String dst;
    
    public void testTransfer() throws Exception {
        File f1 = new File(src);
        assertNotNull(f1);
        assertTrue("File not found:" +  src, f1.exists());
        
        File f2 = File.createTempFile("Ben", ".txt");
        assertNotNull(f2);
        f2.deleteOnExit();
        dst = f2.getCanonicalPath();
        
        // Create channel on the source
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        
        // Create channel on the destination
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
        
        // Copy file contents from source to destination
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        
        // Close the channels
        srcChannel.close();
        dstChannel.close();
        
        assertTrue("Expected files to have same contents", contentsMatch() );
    }
    
    public boolean contentsMatch() throws Exception {
        int data1, data2;
        
		FileInputStream original = new FileInputStream(src);
		assertNotNull("Original file could not be opened", original);
		
		FileInputStream copy = new FileInputStream(dst);
		assertNotNull("Copy file could not be opened", copy);
		
		do {
			data1 = original.read();
			data2 = copy.read();
			if(data1 != data2) {
				original.close();
				copy.close();
				return false;
			}
		} while(data1 != -1);
        
        return true;
    }
}
