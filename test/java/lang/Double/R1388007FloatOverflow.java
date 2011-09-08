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
