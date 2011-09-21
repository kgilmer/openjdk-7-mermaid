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
 @summary Some tests of the Volatile Language Spec
 @summary com.apple.junit.java.lang.LanguageSpec;
 @run main VolatileTest
 */

import junit.framework.*;

import java.util.Timer;
import java.util.TimerTask;

public class VolatileTest extends TestCase {
    Thread t1;
    Thread t2;
    TimerTask stopper;
    boolean done = false;
    boolean failed= false;
    volatile long sharedLong;
    Timer timer;
    
    class Stopper extends TimerTask{
    
        public void run(){
            done = true;
        }
    }
    
    public void setUp(){
    //create two threads which change the value of sharedLong
    //If sharedLong not declared as volatile, then VM splits the
    //long into two 32 bit sections, and performs operations on them
    //seperately. The volatile decleration ensures that the actions
    //performed on sharedLong occur in the same order dictated by the thread,
    //regardless of whether the long was split into two sections or not. 
    
         t1 = new Thread( new Runnable() {
            public void run() {
               while(!done && !failed){ 
                 sharedLong = Long.MAX_VALUE;
                 long temp = sharedLong;
                 //depending on the order of thread execution, if
                 //sharedLong is declared as volatile, temp can assume
                 //only one of the the two valid values below, and cannot
                 //have an intermediate state. 
                 
                    if ((temp != 0L) && (temp != Long.MAX_VALUE)) {
                       System.out.println( "t1" + temp);
                       failed = true;
                    }
                }
            }
        } );
        
         t2 = new Thread( new Runnable() {
            public void run() {
               while(!done && !failed){ 
                 sharedLong = 0L;
                 long temp = sharedLong;
                 //depending on the order of thread execution, if
                 //sharedLong is declared as volatile, temp can assume
                 //only one of the the two valid values below, and cannot
                 //have an intermediate state. 
                    if ((temp != 0L) && (temp != Long.MAX_VALUE)) {
                       System.out.println( "t2" + temp);
                       failed = true;
                    }
                }
            }
        } );
    }
    
    public void testThread() throws Exception{
        timer = new Timer();
        //check version, if less than 1.5.0, then ignore test. Change this in
        //leopard
        String version = System.getProperty("java.vm.version").substring(0,3);
        if(Double.parseDouble(version) < 1.5){
            assertTrue(true);
            timer.cancel();
        }
        else{
            try{
            timer.schedule(new Stopper(), 2000);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            //check that the volatile declaration of sharedLong
            //prevented intermediate states from appearing.  
            assertFalse("volatile long got clobbered", failed);
            }
                finally{
                    timer.cancel();
            }

        }
    }    
    
    public static Test suite() {
        return new TestSuite(VolatileTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
