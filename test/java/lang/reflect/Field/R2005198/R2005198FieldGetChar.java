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
 @summary  <rdar://problem/2005198> CharFieldTest0709 fails
 @summary com.apple.junit.java.lang.Reflection;
 @run main R2005198FieldGetChar
 */

import junit.framework.*;

import java.lang.reflect.Field;


public class R2005198FieldGetChar extends TestCase
{

    public static Test suite() {
        return new TestSuite(R2005198FieldGetChar.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    // Test need supposedly "unused" fields to clober locals in the failure case
    class test07a {
        public char field1 = 'A';
        @SuppressWarnings("unused")
        private char field2 = '2';
        protected char field3 = '3';
        public Character field4 = new Character('4');
        @SuppressWarnings("unused")
        private Character field5 = new Character('5');
    }

    public void testR2005198() throws Exception {
        Field fields[] = test07a.class.getDeclaredFields();
        Field f = fields[0];
        char c = 'b';
        char c2 = 'b';
        char c3 = 'A';
        
        assertEquals("local variables c and c2 got clobbered", c, c2);
        
        
        c = f.getChar(new test07a());
        int conv1 = (int)c;
        int conv2 = (int)c3;

        assertEquals("Unable able to use reflection to get and convert a char field", conv1, conv2);
        assertEquals("Unable able to use reflection to get a char field", c, c3);
    }
}

