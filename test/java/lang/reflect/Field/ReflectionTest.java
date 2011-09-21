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
 @summary A simple reflection testcase
 @summary com.apple.junit.java.lang.Reflection;
 @run main ReflectionTest
 */

import junit.framework.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


// We will trigger if args are "Hi", "There"
class ReflectionTestClass01 {
    public static boolean theTrigger = false;
    
    public static void pullTrigger(String[] args) {
        if ( ( args.length == 2) && args[0].equals("Hi") && args[1].equals("There") ) {
            theTrigger = true;
        }
    }
}
 
public class ReflectionTest extends TestCase {
    public void testReflection() throws Exception {
        boolean triggered;
    
        Class c = Class.forName("ReflectionTestClass01");
        assertNotNull(c);
                
        Method m = c.getMethod("pullTrigger", new Class[] {String[].class});
        assertNotNull(m);

        Field f = c.getField("theTrigger");
        assertNotNull(f);

        triggered =f.getBoolean(null);
        assertFalse("ReflectionTestClass01.triggered should be false", triggered);
        
        Object[] args = new Object[] { new String[] { "Hi", "There" } };
        m.invoke(null, args);         

        triggered =f.getBoolean(null);
        assertTrue("ReflectionTestClass01.triggered should be true", triggered);
    }
            
    public static Test suite() {
        return new TestSuite(ReflectionTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

