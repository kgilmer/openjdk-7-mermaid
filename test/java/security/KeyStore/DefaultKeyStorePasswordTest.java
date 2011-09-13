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
 @summary <rdar://problem/7481203> Default password for cacerts changed in Update 1
 @summary com.apple.junit.apple.crypto;
 @run main DefaultKeyStorePasswordTest
 */

import java.io.FileInputStream;
import java.security.KeyStore;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class DefaultKeyStorePasswordTest extends TestCase {

    public void testExistance() throws Exception {
        try {
            KeyStore trustStore = KeyStore.getInstance("jks");
            String javahome = System.getProperty("java.home");
            FileInputStream in = new FileInputStream(
                    javahome + "/lib/security/cacerts");
            trustStore.load(in, "changeit".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Default password for cacerts should be \"changeit\"");
        }

    }

    public static Test suite() {
        return new TestSuite(DefaultKeyStorePasswordTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
}
