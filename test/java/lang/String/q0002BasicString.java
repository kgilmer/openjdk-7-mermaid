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
 @summary A simple reflection testcase
 @summary com.apple.junit.java.lang.String;
 @run main q0002BasicString
 */

import junit.framework.*;

public class q0002BasicString extends TestCase {
    protected String stringOriginal;
    protected String stringUppered;
    protected String stringLowered;

    protected void setUp() {
          stringOriginal    = "Happy ha123pPy He#l&Lo worLD";
        stringUppered    = "HAPPY HA123PPY HE#L&LO WORLD";
        stringLowered    = "happy ha123ppy he#l&lo world";
     }

    public void testSelfString() throws Exception {
        assertEquals("String not equal to itself", stringLowered, stringLowered);
    }

    public void testEqualsIgnoreCaseString() throws Exception {
        assertTrue("string.equalsIgnoreCase() doesn't work for lowered string", (stringLowered.equalsIgnoreCase(stringOriginal)) );
        assertTrue("string.equalsIgnoreCase() doesn't work for uppered string", (stringUppered.equalsIgnoreCase(stringOriginal)) );
    }

    public void testUpperedString() throws Exception {
        assertEquals("toUpperCase() doesn't give expected string", stringUppered, stringOriginal.toUpperCase());
    }
    
    public void testLoweredString() throws Exception {
        assertEquals("toLowerCase() doesn't give expected string", stringLowered, stringOriginal.toLowerCase());
    }
    
    public void testLoweredStringNotSameAsUpper() throws Exception {
        assertTrue("toLowerCase() gives unexpected uppercased string", (!stringUppered.equals(stringOriginal.toLowerCase())) );
    }

    public void testUpperedStringNotSameAsLower() throws Exception {
        assertTrue("toUpperCase() gives unexpected lowercased string", (!stringLowered.equals(stringOriginal.toUpperCase())) );
    }

    protected void tearDown() {
    }

    public static Test suite() {
        return new TestSuite(q0002BasicString.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }    
}
