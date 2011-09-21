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
 @run main q0001BasicString
 */

import junit.framework.*;

public class q0001BasicString extends TestCase {
    protected String stringUninitialized;
    protected String stringNull;
    protected String stringEmpty;
    protected String stringOne;
    protected String stringTwo;
    protected String     stringOneTwo;

    protected void setUp() {
          stringNull = null;
        stringEmpty = "";
        stringOne = "one";
         stringTwo = "Two";
         stringOneTwo = "oneTwo";
     }

    public void testUninitializedString() throws Exception {
        assertEquals("Uninitialized string is not null", null, stringUninitialized);
    }

    public void testNullString() throws Exception {
        assertEquals("Null string not null", null, stringNull);
    }
    
    public void testEmptyString() throws Exception {
        assertTrue("Empty string is null", (stringEmpty != null));
    }
    
    public void testSimpleConcatenate() throws Exception {
        assertEquals("Concatenating two strings did not give expected string", stringOneTwo, (stringOne + stringTwo));
    }

    public void testConcatenateWithEmpty() throws Exception {
        assertEquals("Concatenating stringOne with empty string should give original string", stringOne, stringOne + stringEmpty);
        assertEquals("Concatenating stringTwo with empty string should give original string", stringTwo, stringTwo + stringEmpty);
    }
    
    protected void tearDown() {
    }

    public static Test suite() {
        return new TestSuite(q0001BasicString.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }    
}
