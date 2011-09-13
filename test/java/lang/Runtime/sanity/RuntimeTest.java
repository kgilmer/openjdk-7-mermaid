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
 @summary This test runs through the simplest of the functions
 @summary in java.util.Runtime and does some  trivial checks
 @summary com.apple.junit.java.util;
 @run main RuntimeTest
 */

import junit.framework.*;

public class RuntimeTest extends TestCase {
    Runtime running = null;
    
    protected void setUp() {
        running = Runtime.getRuntime();
    }

    // Simple accessors
    public void testSystemStuff() {
        assertTrue("availableProcessors should be positive!", running.availableProcessors() > 0 );
        assertTrue("maxMemory should be positive!", running.maxMemory() > 0 );
        assertTrue("totalMemory should be positive!", running.totalMemory() > 0 );
        assertTrue("freeMemory should be positive!", running.freeMemory() >= 0 );
    }    
    

    // A simple class to do a little work with tracing turned on
    class ByeBye extends Thread {
         public void run() {
             System.out.println("An error occured, shutdown hook was not removed");
             fail("An error occured, shutdown hook was not removed");
         }
    }

    // adding and removing a shutdown hook should throw no exceptions
    public void testShutdownHooks() {
        Thread hook = new ByeBye();
        running.addShutdownHook(hook); 
        running.removeShutdownHook(hook);
    }
    

    // A simple class to do a little work with tracing turned on
    class SimpleClass {
        int counter = 0;
        public void recursiveMethod(int depth) {
            if (depth > 0) {
                recursiveMethod( depth-1);
            }
            counter++;
        }
        
        public int getCounter() {
            return(counter);
        }
        
    }

    // turning tracing on and off should work fine
    public void testTrace() {
        running.traceInstructions(true); 
        running.traceMethodCalls(true);
        
        SimpleClass easy = new SimpleClass();
        easy.recursiveMethod(20);
        assertTrue( "We should have called recursiveMethod() 21 times", easy.getCounter() == 21 );
        
        running.traceInstructions(false); 
        running.traceMethodCalls(false);
    }

    // boilerplate
    
    public static Test suite() {
        return new TestSuite(RuntimeTest.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
