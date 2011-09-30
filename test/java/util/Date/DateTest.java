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
 @summary Tests for the 1.5 Date class http://java.sun.com/j2se/1.5.0/docs/api/java/util/Date.html
 @summary com.apple.junit.java.util;
 @run main DateTest
 */

import junit.framework.*;

import java.util.Date;

public class DateTest extends TestCase {

    public static Test suite() {
        return new TestSuite(DateTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    Date d1;
    Date d2;
    Date d3;

    Object obj1;
    Object obj2;

    public void testBasicConstructor() {

        d1 = new Date();
        assertNotNull(d1);
    }

    public void testBeforeAndAfter() {
        d1 = new Date();
        long d1Millis = d1.getTime();
        d2 = new Date(d1Millis + 100000);
        assertTrue("d1 should be before d2", d1.before(d2));
        assertTrue("d2 should be after d1", d2.after(d1));

        try {
            d3 = null;
            boolean r = d1.before(d3);
            fail("if before(null) is called, should throw an NPE. r = " + r);
        } catch (NullPointerException e) { }

        try {
            d3 = null;
            boolean r = d1.after(d3);
            fail("if after(null) is called, should throw an NPE. r = " + r);
        } catch (NullPointerException e) { }
    }

    public void testClone() {
        d1 = new Date();
        assertNotNull(d1);

        Object dClone = d1.clone();
        assertEquals(d1, (java.util.Date)dClone);
    }

    public void testCompareTo() {
        d2 = new Date();
        d1 = new Date(d2.getTime() - 100000);
        assertTrue("d1 should compareTo() less than d2", (d1.compareTo(d2) < 0));
        assertTrue("d2 should compareTo() greater than d1", (d2.compareTo(d1) > 0));

        try {
            d3 = null;
            int r = d1.compareTo(d3);
            fail("if compareTo(null), should throw an NPE. r = " + r);
        } catch (NullPointerException e) { }
    }

    public void testEquals() {
        d1 = new Date();
        assertTrue("a Date instance should be equal to itself", d1.equals(d1));
        d2 = new Date(d1.getTime() + 10000);
        assertFalse(d1.equals(d2));

        d3 = null;
        assertFalse("a Date should never equal null", d1.equals(d3));
    }

    public void testGetTime() {
        d1 = new Date();
        long dMillis = d1.getTime();
        d2 = new Date(dMillis);
        assertEquals(d1, d2);
    }

    public void testHashCode() {
        d1 = new Date();
        d2 = new Date(d1.getTime() + 10000);
        d3 = new Date(d1.getTime() - 10000);

        assertFalse(d1.hashCode() == d2.hashCode());
        assertFalse(d1.hashCode() == d3.hashCode());
        assertFalse(d2.hashCode() == d3.hashCode());
    }

    public void testSetTime() {
        d1 = new Date();
        long d1m = d1.getTime();

        d2 = new Date(d1m + 10000);
        d3 = new Date(d1m);
        assertFalse(d2.equals(d3));

        d3.setTime(d1m + 10000);
        assertEquals(d2, d3);
    }

    public void testToString() {
        d1 = new Date();
        String d1str = d1.toString();
        assertNotNull(d1str);

        assertTrue(d1str.length() == 28);

        // TODO: further checks here...
    }
}
