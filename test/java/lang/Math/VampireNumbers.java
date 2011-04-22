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
 * @summary Play around with vampire numbers
 * @summary http://en.wikipedia.org/wiki/Vampire_number
 * @summary com.apple.junit.java.lang.Math
 */

import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashSet;

public class VampireNumbers extends TestCase {

    public static Test suite() {
        return new TestSuite(VampireNumbers.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    // For convenince, we use a utility for composing and decomposing
    // an integer into a collection of digits.  This takes a seed
    // number, such as 123128, and increments a counter for each
    // digit in the seed. For example, with an increment of 1, 
    // the seed 123128 digitizes into:
    //
    //  digits[1]  --> +2
    //  digits[2]  --> +2
    //  digits[3]  --> +1
    //  digits[8]  --> +1
    //
    //
    // It will be handy to know the number of digits in the number
    // so keep count and return this value. For example, 123128 is
    // a six digit number, so return "6" 

    private static int digitize(int seed, int incr, int digits[]) {
        int cnt = 0;
        while (true) {
            int i = seed % 10;
            seed = seed / 10;
            digits[i] +=incr;
            cnt++;
            if (seed == 0) {
                break;
            }
        }
        return cnt;
    }

    // 1st crack at a function that takes 2 possible fangs and 
    // returns whether or not they make a vampire number 

    private static boolean isVampire(int a, int b) {
        if ((a % 10 ==0) && (b % 10 ==0) ) {
            return false;
        }
        final int[] digitCounts = new int[10];
        final int aNumDigits = digitize(a, 1, digitCounts);
        final int bNumDigits = digitize(b, 1, digitCounts );
        if (aNumDigits != bNumDigits) {
            return false;
        }
        digitize(a*b, -1, digitCounts );        
        return( Arrays.equals( ZEROS, digitCounts )); 
    }

    static final int[] ZEROS =new int[10];  

    /*
     *  Play with Vampire Numbers some.  Collect all the small ones, and show them
     *  visually as a product of 2 fangs in a frame.  Throw in some asserts so that
     *  we have a testcase, even if it is sort of silly and should never fail.
     */
    
    static final int TOP = 999;
    HashSet<Integer> vamps= new HashSet<Integer>();
    HashSet<Point> fangs = new HashSet<Point>();
    
    public void testVampires() throws Exception {
        for (int a = 0; a < TOP; a++) {
            for (int b = 0; b <= a; b++) {
                if (isVampire(a,b)) {
                    fangs.add(new Point(a,b));
                    fangs.add(new Point(b,a));
                    vamps.add(new Integer(a*b));
                }
            }
        }       
        
        /*
         * Check some interesting values
         * The 7 vampires with 4 digits:
         *  1260=21 x 60, 1395=15 x 93, 1435=35 x 41, 1530=30 x 51, 1827=21 x 87, 2187=27 x 81, 6880=80 x 86  
         *
         * The 5 prime vampires with 6 digits:
         * 117067 = 167 x 701, 124483 = 281 x 443, 146137 = 317 x 461, 371893 = 383 x 971, 536539 = 563 x 953
         *
         * Make sure that we don't over-zealously throw things out, either
         *      473 x 800 = 378400 is a valid number
         *
         */
        assertTrue("expected v: 35, 41", fangs.contains( new Point(35,41)));
        assertTrue("expected v: 30, 51", fangs.contains( new Point(30,51)));
        assertTrue("expected v: 21, 60", fangs.contains( new Point(21,60)));
        assertTrue("expected v: 80, 86", fangs.contains( new Point(80,86)));
        assertTrue("expected v: 21, 87", fangs.contains( new Point(21,87)));
        assertTrue("expected v: 15, 93", fangs.contains( new Point(15,93)));

        assertTrue("expected v: 167, 701", fangs.contains( new Point(21,60)));
        assertTrue("expected v: 281, 443", fangs.contains( new Point(21,60)));
        assertTrue("expected v: 317, 461", fangs.contains( new Point(21,60)));
        assertTrue("expected v: 383, 971", fangs.contains( new Point(21,60)));
        assertTrue("expected v: 563, 953", fangs.contains( new Point(21,60)));

        assertTrue("expected v: 473, 800", fangs.contains( new Point(473,800)));
        
        if (TOP == 999) {
            assertEquals("There are 7 four-digit and 148 six-digit vampire numbers", 7+148, vamps.size());
        }
        else if (TOP == 9999) {
            assertEquals("There are 7 four-digit, 148 six-digit, and 3228 eight-digit vampire numbers", 7+148+3228, vamps.size());          
        }

        /*
         *  Do some negative checks as well, to make sure garbage doesn't creep in.
         */
        
        assertTrue("Watch out for zeros: 210 x 600 = invalid", !fangs.contains( new Point(210,600)));  // both end in zeros
        assertTrue("Should not have v: 10, 10", !fangs.contains( new Point(10,10)));
        assertTrue("Should not have v: 15, 15", !fangs.contains( new Point(15,15)));
        assertTrue("Should not have v: 123, 321", !fangs.contains( new Point(123,321)));
        assertTrue("Should not have v: 999, 999", !fangs.contains( new Point(999,999)));
        
    }   
}

