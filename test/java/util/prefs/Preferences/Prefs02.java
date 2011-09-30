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
 @summary Tests for the 1.4 prefs package
 @summary com.apple.junit.java.util.prefs;
 @run main Prefs02
 */

import junit.framework.*;

import java.util.prefs.Preferences;

public class Prefs02 extends TestCase {

    public static Test suite() {
            return new TestSuite(Prefs02.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    // Preference key name
    final static String STRING_KEY = "KEY_STRING";          // String
    final static String BOOLEAN_KEY = "KEY_BOOLEAN";        // boolean
    final static String INT_KEY = "KEY_INT";                // int
    final static String LONG_KEY = "KEY_LONG";              // long
    final static String FLOAT_KEY = "KEY_FLOAT";            // float
    final static String DOUBLE_KEY = "KEY_DOUBLE";          // double
    final static String BYTE_ARRAY_KEY = "KEY_BYTE_ARRAY";  // byte[]

    final static String sString = "a string";               // String
    final static boolean sBoolean = true;                   // boolean
    final static int sInt = 123;                            // int
    final static long sLong = 123L;                         // long
    final static float sFloat = 12.3F;                      // float
    final static double sDouble =  12.3;                    // double
    final static byte[] sByteArray = new byte[1024];        // byte[]
        
    private Preferences prefs = Preferences.userNodeForPackage( Prefs02.class ).node("Prefs02");

    public void testPrefs02 () throws Exception {
        
        // Initialize the byte array
        for(int i = 0; i < 1024; i++) {
            sByteArray[i] = 90;
        }
        
        // Write out some preference values
        prefs.put(STRING_KEY, sString);                  // String
        prefs.putBoolean(BOOLEAN_KEY, sBoolean);         // boolean
        prefs.putInt(INT_KEY, sInt);                     // int
        prefs.putLong(LONG_KEY, sLong);                  // long
        prefs.putFloat(FLOAT_KEY, sFloat);               // float
        prefs.putDouble(DOUBLE_KEY, sDouble);            // double
        prefs.putByteArray(BYTE_ARRAY_KEY, sByteArray);  // byte[]
        prefs.flush();

              
        // System.out.println("Checking the integrity of all the values we stored.");
        byte[] bytes = new byte[10];

        assertEquals( prefs.get(STRING_KEY, "foo"),         sString );
        assertEquals( prefs.getBoolean(BOOLEAN_KEY, false), sBoolean );
        assertEquals( prefs.getInt(INT_KEY, 0),             sInt );
        assertEquals( prefs.getLong(LONG_KEY, 0L),          sLong );
        assertTrue( prefs.getFloat(FLOAT_KEY, 0.0F) ==      sFloat );
        assertTrue( prefs.getDouble(DOUBLE_KEY, 0.0) ==     sDouble );
        assertTrue( prefs.getLong(LONG_KEY, 0L) ==          sLong );

        bytes = prefs.getByteArray(BYTE_ARRAY_KEY, bytes);

        assertEquals( bytes.length,    sByteArray.length );
        for(int index = 0; index < bytes.length; index ++) {
            assertEquals( "Byte value at index " + index + "does not match original value.", bytes[index], sByteArray[index]);
        }

        
        // Clean up after ourselves. Remove the node
        prefs.removeNode();
        assertFalse("nodeExists() returned true true after node was supposedly removed.", Preferences.userRoot().nodeExists("/com/apple/java/test/harness/tests/java/util/prefs/Prefs02"));
    }
    
}



