/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @test
 * @summary <rdar://problem/1388007> EXT: Floating-point overflow or weirdness?
 * @summary com.apple.junit.java.lang.Double
 */

import junit.framework.*;

public class R1388007FloatOverflow extends TestCase
{
    protected double n1, n2;
    
    protected void setUp() {
        n1 = -1;
        n2 = (double) ((long) n1);
    }
    
    public void testDoubleCastOverflow() throws Exception {
        //! don't want to print this to System.out blindly; convert to assertion, use logwrapper, or drop it
        //ref.println( "n1 = " + n1 + ", n2 = " + n2);
        assertEquals(n2, -1.0, 0.0);
    }

    public static Test suite() {
        return new TestSuite(R1388007FloatOverflow.class);
    }
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}
