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
 @summary <rdar://problem/2208170> JITC problem with arrays (initialization?)
 @summary com.apple.junit.java.vm
 @run main ArrayInit_R2208170
 */

import junit.framework.*;

public class ArrayInit_R2208170 extends TestCase {

    public static Test suite() {
        return new TestSuite(ArrayInit_R2208170.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
   public void testArrayInit() {
        R2208170ThingMap    theMap = new R2208170ThingMap();
        assertEquals("Array initialization is messed up","(100)", (theMap.getThingRecords())[0].toString());
    }
}

class R2208170ThingMap {
    public R2208170ThingRec[]    getThingRecords() {
        return mThingList; 
    }

    R2208170ThingRec    mThingList[] = {
        new R2208170ThingRec( 1, 1, new R2208170Thing( "1", new Object[] { new Integer(100) } ) ),
        new R2208170ThingRec( 2, 2, new R2208170Thing( "2", new Object[] { new Integer(200) } ) ),
        new R2208170ThingRec( 3, 3, new R2208170Thing( "3", new Object[] { new Integer(300) } ) )
    };
}

class R2208170ThingRec {
    R2208170Thing    mThing;

    public R2208170ThingRec(
            @SuppressWarnings("unused") int inDummy1, 
            @SuppressWarnings("unused") int inDummy2, 
            R2208170Thing inThing
    ) {
        mThing = inThing;
    }

    public String    toString()    {
        return mThing.toString();
    }
}

class R2208170Thing {
    Object[]    mArray;

    public    R2208170Thing( 
            @SuppressWarnings("unused") String inDummy,
            Object[]    inArray 
    )    {
        mArray = inArray;
    }

    public String    toString()    {
        try {
            StringBuffer sbuf = new StringBuffer();
            sbuf.append('(');
            if (mArray != null) {
                for (int i=0; i<mArray.length; i++) {
                    if (i != 0)
                        sbuf.append(" ,");
                    sbuf.append(mArray[i] != null ? mArray[i].toString() : "null");
                }
            }
            sbuf.append(')');
            return sbuf.toString();
        } catch(Exception e) {
            return null;
        }
    }
} 
