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
 @summary Simple tests for HashSet for an empty collection, one item collection and empty list
 @summary com.apple.junit.java.util;
 @run main SimpleHashSetTest
 */

import junit.framework.*;

import java.util.HashSet;

public class SimpleHashSetTest extends TestCase {
    protected HashSet<String> mySet;

    protected void setUp() {
        mySet = new HashSet<String>();
    }

    public void tearDown() {
        mySet.clear();
    }
        
    public void testEmptyCollection() {
        assertTrue(mySet.isEmpty());
    }

    public void testOneItemCollection() {
        mySet.add("itemA");
        assertEquals(1, mySet.size());
    }
    
    public void testStillEmptyCollection() {
        assertTrue(mySet.isEmpty());
    }

    public void testNotEmptyCollection() {
        mySet.add("itemA");
        assertFalse(mySet.isEmpty());
    }

    public static Test suite() {
        return new TestSuite(SimpleHashSetTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

