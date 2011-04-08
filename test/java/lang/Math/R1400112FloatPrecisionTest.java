/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @test
 * @summary <rdar://problem/1400112> EXT a2: Math package error is causing this applet to print out the wrong value
 * @summary com.apple.junit.java.lang.Math
 */

import junit.framework.*;

public class R1400112FloatPrecisionTest extends TestCase {
    protected static final double kLog10 = Math.log(10);
    protected static double ERRMARGIN;
    protected double origDouble;
    protected double resultingDouble;

    protected void setUp() {
        ERRMARGIN = .000000001;
        origDouble = -1.56;
        resultingDouble    = origDouble;
    }
    
    public static Test suite() {
        return new TestSuite(R1400112FloatPrecisionTest.class);
    }

    final public static double roundToXSigDigs(double resultingDouble, int sigs) {
        double  absDouble = Math.abs(resultingDouble);
        double roundFactor = Math.log(absDouble);
        
        if( absDouble < ERRMARGIN ) { // it's close enough to zero for us
            return(0);
        }   
        roundFactor /= kLog10;
        roundFactor = Math.floor(roundFactor);
        roundFactor = sigs-1-roundFactor;
        roundFactor *= kLog10;
        roundFactor = Math.exp(roundFactor);
        resultingDouble *= roundFactor;
        resultingDouble = Math.round(resultingDouble);
        resultingDouble /= roundFactor;
        return (resultingDouble);
    }

    public void testR1400112FloatPrecision() {
        resultingDouble = roundToXSigDigs(resultingDouble, 4);
        assertTrue ("Math on doubles is functional", Math.abs(resultingDouble - origDouble) <  ERRMARGIN );
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

