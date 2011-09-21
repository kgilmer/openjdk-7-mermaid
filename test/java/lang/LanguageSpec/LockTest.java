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
 @summary Some tests for the Lock spec
 @summary com.apple.junit.java.lang.LanguageSpec;
 @run main LockTest
 */

import junit.framework.*;

import java.util.Timer;
import java.util.TimerTask;

//Lock class representation of Lock, with flag to indicate whether locked or not, and with
//count of number of times lock acquired.
class Lock {
        static int numLocks = 0;
        static boolean isLocked = false;

        //Acquires lock and increments numLock
        public static synchronized void acquire() throws Exception {
            if ( numLocks < 0) {
                throw new Exception("Locking logic error");
            }
            numLocks++;
            isLocked = true;
        }         
        
        //Releases current lock, if all locks released, Lock is unlocked
        public static synchronized void release() throws Exception {
            if ( numLocks < 1) {
                throw new Exception("Locking logic error");
            }
            numLocks--;

            if(numLocks == 0)
                isLocked = false;
        }
 }
 
 
public class LockTest extends TestCase {
    Thread t1;
    Thread t2;
    Lock l1;
    TimerTask stopper;
    static volatile boolean done;
    Timer timer;

    class Stopper extends TimerTask{
        public void run(){
            done = true;
        }
    }

    //Sets up new threads t1 and t2.
    public void setUp(){
        l1 = new Lock();
        done = false;
    }


    //Create two threads. One uses synchronized to aqcuire multiple locks on the instance l1 of Lock
    //The other thread attempts to acquire a lock on l1 while it is already locked by the first thread
    //If the second thread manages to acquire a lock on l1 while already locked by the first thread,
    //the test fails. Also tests number of locks acquired.
    //Tests JVM spec as specified in 8.5 and 8.6
    
    Exception cachedException;

    public void testLock() throws Exception {
        timer = new Timer();
        t1 = new Thread( new Runnable() {
            public void run() {
                try{
                    while (!done) {
                        synchronized(l1) {
                            assertFalse(Lock.isLocked);
                            Lock.acquire();
                            assertEquals(Lock.numLocks, 1);
                            Thread.sleep(10);
                            synchronized(l1) {
                                Lock.acquire();
                                assertEquals(Lock.numLocks, 2);
                                Thread.sleep(10);
                                synchronized(l1) {
                                    Lock.acquire();
                                    assertEquals(Lock.numLocks, 3);
                                    Thread.sleep(10);
                                    Lock.release();
                                    assertEquals(Lock.numLocks, 2);
                                }
                                Thread.sleep(10);
                                Lock.release(); 
                                assertEquals(Lock.numLocks, 1);
                            }
                        Thread.sleep(10);
                        Lock.release();
                        assertEquals(Lock.numLocks, 0);
                        }
                    }
                } catch (Exception e) {
                    cachedException = e;
                }
            }
        });

        t2 = new Thread ( new Runnable() {
            public void run() {
                try {
                    while(!done) {
                        synchronized(l1){
                            assertFalse(Lock.isLocked);
                            Lock.acquire();
                            assertEquals(Lock.numLocks, 1);
                            Lock.release();
                            assertEquals(Lock.numLocks, 0);
                        }
                    }
                } catch (Exception e) {
                    cachedException = e;
                }
            }
        });
                
        try {
            timer.schedule(new Stopper(), 500);
            t1.start();
            t2.start();
            t1.join();
            t2.join();
            
            if (cachedException != null) {
                throw cachedException;
            }
        }

        finally {
            timer.cancel();
        }

    }

    public static Test suite() {
        return new TestSuite( LockTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

