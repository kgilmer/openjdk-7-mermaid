/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @test
 * @summary <rdar://problem/1651062> Date parsing bug; parse() for string with numeric month is off by one
 * @summary com.apple.junit.java.util.Date
 */

import junit.framework.*;
import java.util.Date;

public class R1651062DateMonthParse extends TestCase {
    protected Date d1, d2;
    protected String s1, s2;
    
    protected void setUp() {
        s1 = "4/28/97";
        s2 = "April 28, 1997";
    }

    public static Test suite() {
        return new TestSuite(R1651062DateMonthParse.class);
    }
    
    @SuppressWarnings("deprecation")
    // Compatiblity test -- intentionally uses the old API
    public void testDateMonthParse() throws Exception {
        d1 = new Date(s1);
        d2 = new Date(s2);
        //! don't want to print this to System.out blindly; convert to assertion, use logwrapper, or drop it
        //ref.println("string '" + s1 + "' parsed to: " + d1);
        //ref.println("string '" + s2 + "' parsed to: " + d2);
        assertEquals("Date objects constructed from equivalent strings are not equal.", d1.getTime(), d2.getTime());
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
