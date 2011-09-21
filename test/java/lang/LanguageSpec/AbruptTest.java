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
 @summary Some tests of the Abrupt Completion Language Spec
 @summary com.apple.junit.java.lang.LanguageSpec;
 @run main AbruptTest
 */

import junit.framework.*;

/*
    This testcase looks at various abrupt termination cases, for things such as Strings, floats,14.1 and doubles that are not handled by the JCK.
    
    See: 
        Java Language Spec  14.10.1 "Abrupt Completion"
*/

public class AbruptTest extends TestCase {
    private String a1;
    private String a2;
    private String a3;
    private double d1;
    private double d2;
    private double d3;
    private double d4;
    private double d5;
    private double d6;
    private final double THRESHOLD = 1.0/(1<<15) ; 

    public void setUp(){
        a1 = "test";
        a2 = "String";
        a3 = "result";
        d1 = 64.5672;
        d2 = 2.1;
        d3 = 3.1;
        d4 = 4.0;
        d5 = 6.3;
        d6 = 0.0;
    }
    
    //Strings stand for all object 
    public void testStringStatementFalse(){

        assertEquals(a1, "test");
        assertEquals(a2, "String");
        assertEquals(a3, "result");

        try{
            while (a1.length() > 4){
                a1 = a1.substring( 0, 2);
            }
            a2 = a2.substring(0,3);

        }
        catch (Exception e){
            a3 = a3.substring(0,2);
        }

        assertEquals(a1, "test");
        assertEquals(a2, "Str");
        assertEquals(a3, "result");
    }

    public void testDoubleBreak(){

        assertEquals(d1, 64.5672, THRESHOLD);
        assertEquals(d2, 2.1, THRESHOLD);
        assertEquals(d3, 3.1, THRESHOLD);
        assertEquals(d4, 4.0, THRESHOLD);

        while(d1 <= 64.5674){
            d2 += 0.1;
            if(d1 == 64.5674)
                break;
            d3 += 0.1;
            d1 += 0.0001;
        }
        d4 += 0.1;

        assertEquals(d1, 64.5674, THRESHOLD);
        assertEquals(d2, 2.4, THRESHOLD);
        assertEquals(d3, 3.3, THRESHOLD);
        assertEquals(d4, 4.1, THRESHOLD);
        assertEquals(d5, 6.3, THRESHOLD);
        assertEquals(d6, 0.0, THRESHOLD);
    }

    public void testDoubleContinue(){
        assertEquals(d1, 64.5672, THRESHOLD);
        assertEquals(d2, 2.1, THRESHOLD);
        assertEquals(d3, 3.1, THRESHOLD);
        assertEquals(d4, 4.0, THRESHOLD);

        try {
            while( d1 < 64.5674){
                d1 += 0.0001;
                if(d1 < 64.5674)
                    continue;
                d2 += 0.1;
            }

            d3 += 0.1;
        }
        catch(Exception e){
            d4 += 0.1;
        }
        assertEquals( d1, 64.5674, THRESHOLD);
        assertEquals( d2, 2.2, THRESHOLD);
        assertEquals( d3, 3.2, THRESHOLD);
        assertEquals( d4, 4.0, THRESHOLD);  
        assertEquals(d5, 6.3, THRESHOLD);
        assertEquals(d6, 0.0, THRESHOLD);
    }

    public void testDoubleContinueLabel(){
        assertEquals(d1, 64.5672, THRESHOLD);
        assertEquals(d2, 2.1, THRESHOLD);
        assertEquals(d3, 3.1, THRESHOLD);
        assertEquals(d4, 4.0, THRESHOLD);
        try {
            label: while( d1 < 64.5972){
                d1 += 0.01;
                if(d1 < 64.5972)
                    continue label;
                d2 = 7.2;
            }

        d3 = 4.33;
        }
        catch(Exception e){
            d4 -= 0.1;
        }
        assertEquals( d1, 64.5972, THRESHOLD);
        assertEquals( d2, 7.2, THRESHOLD);
        assertEquals( d3, 4.33, THRESHOLD);
        assertEquals( d4, 4.0, THRESHOLD);  
        assertEquals(d5, 6.3, THRESHOLD);
        assertEquals(d6, 0.0, THRESHOLD);
    }
    
    public static Test suite() {
        return new TestSuite(AbruptTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
