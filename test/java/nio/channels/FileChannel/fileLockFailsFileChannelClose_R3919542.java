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
 @test
 @summary <rdar://problem/3919542>
 @summary FileChannel.close() fails with a IOException if a FileLock was aquired using that channel and it wasn't released yet.
 @summary com.apple.junit.java.vm
 @run main fileLockFailsFileChannelClose_R3919542
 */
import junit.framework.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class fileLockFailsFileChannelClose_R3919542 extends TestCase {
    public void testfileLockFailsFileChannelClose_R3919542() throws Exception {
        File file = File.createTempFile("test", "");
        file.deleteOnExit();
        FileOutputStream out = new FileOutputStream(file);
        FileChannel channel = out.getChannel();
        FileLock lock = channel.lock();
        assertNotNull(lock);
        //lock.release();
        channel.close();
    }
    
    public static Test suite() {
        return new TestSuite(fileLockFailsFileChannelClose_R3919542.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
