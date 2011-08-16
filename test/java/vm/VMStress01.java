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
 @summary <rdar://problem/4476303> <rdar://problem/4468245>
 @summary ( 4476303 ) Hotspot crash while deoptimization on Intel-based Macs
 @summary ( 4468245 ) Hotspot crash related to NullPointerException handling on PowerPC-based Macs
 @summary com.apple.junit.java.vm
 @run main VMStress01
 */

import junit.framework.*;

import java.io.*;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

public class VMStress01 extends TestCase {
    PrintStream streamer = null;
    static final Random rand = new Random();

    protected void setUp() throws Exception {
        File datafile = File.createTempFile("VMStress01Log", ".txt");
        datafile.deleteOnExit();
        streamer =    new PrintStream( new FileOutputStream( datafile ), true, "UTF-16");
    }

    protected void tearDown() throws Exception {
    }

    protected int             counter = 0;
    protected BaseInterface    testobj = null;

    interface  BaseInterface {
        public void virtualMethod0();
    }

    //
    //    This will be compiled, and uses instructions where result is in R3 on PPC, not ITOS
    //
    
    class BaseClass implements BaseInterface {
        Integer[][] arrays = null;

        public void virtualMethod0() {
            arrays = new Integer[20][4];

            for (int i = 0; i < arrays.length; i+=1) { 
                arrays[i][0] = counter;
                arrays[i][1] = counter + 1;
            }
            counter++;
        }
    }

    //
    //    This will trigger Deopt
    //

    class DerivedClass extends BaseClass {

        public void virtualMethod0() {
            arrays = new Integer[202][22];
            for (int i = 0; i < arrays.length; i+=1) { 
                arrays[i][0] = counter + 1;
                arrays[i][1] = counter;
            }
            counter++;
            streamer.println( "Pop");
        }
    }

    //
    //    A relatively boring little thread that lives until killed. used to create ThreadDeath exceptions
    //

    static int currID = 0; 
    static volatile boolean done = false;
    static final int    MAX_THREADS = 30;
    static final int    MIN_THREADS = 20;
    int killed = 0;

    class Tree extends Thread {        
        public int id = currID++;
        public int lifespan = 0;
        Object wacky = null;

        public void run() {
            try {
                streamer.println( id + "Started");

                while(true) {
                    if (lifespan++ % 1000 == 0) {
                        streamer.println( id + " growing " + lifespan + " in a forest of " + forest.size() );

                        if (rand.nextInt(3) == 1) {
                            wacky = null;
                        }
                        else {
                            wacky = new Object();
                        }

                        try {
                            streamer.println( id + " " + wacky.hashCode() );
                        }
                        catch (NullPointerException pop) {                        
                            streamer.println( id + "Got expected exception");
                        }

                        Thread.yield();
                    }    
                }
            }
            catch (ThreadDeath err) {
                killed+=1;
                streamer.println(id + " argggghhhh..." + lifespan);
            }

            streamer.println(id + "end of thread...");
        }
    }    

    LinkedBlockingQueue<Tree> forest = new LinkedBlockingQueue<Tree>(MAX_THREADS);

    //
    //    This thread kills other threads (Trees in Forest)
    //
    class GrimReaper extends Thread {

        @SuppressWarnings("deprecation")
        private void chopTree() throws InterruptedException {
            Tree oldGrowth = forest.peek();

            if (oldGrowth != null ) {
                streamer.println( "Reaper visits " + oldGrowth.id  + " at " + oldGrowth.lifespan + " in a forest of " +  forest.size()  );

                if ( oldGrowth.lifespan > 10000 ) {
                    oldGrowth = forest.poll();
                    oldGrowth.stop();
                    streamer.println("    chop chop");
                }
                else {
                    streamer.println("    come back next year");
                    Thread.sleep(200);
                }
            }
        }

        public void run() {
            try {
                while(!done) {
                    if ( forest.size() > MIN_THREADS ) {
                        chopTree();
                    }
                }

                while ( forest.size() > 0 ) {
                    chopTree();
                }

            }
            catch (InterruptedException ix ) {
                System.out.println( "Unexpected Exception" + ix);
            }                
        }
    }                

    //
    //    This thread starts other threads (Trees in Forest)
    //
    class JohnnyApple extends Thread {
        public void run() {
            try {
                while(!done) {
                    if ( forest.size() < MAX_THREADS ) {
                        Tree sapling = new Tree();
                        sapling.start();
                        forest.put(sapling);
                    }
                    else {
                        streamer.println("Johny is resting");
                        Thread.sleep(100);
                    }
                }
            }
            catch (InterruptedException ix ) {
                System.out.println( "Unexpected Exception" + ix);
            }                
        }
    }                

    // basic deopt case
    public void testVMStress01() throws Exception {
        done = false;        
        //
        //    Generate a bunch of async ThreadDeaths
        //
        JohnnyApple john = new JohnnyApple();
        john.start();

        GrimReaper reaper = new GrimReaper();
        reaper.start();
        Thread.sleep(500);

        //
        //    Compile / Deoptimize part of the test
        //
        testobj = new BaseClass();
        while( counter < 50000) {
            testobj.virtualMethod0();
        }

        Thread popper = new Thread() {
            public void run() {
                testobj = new DerivedClass();            
                testobj.virtualMethod0();
            }
        };

        assertTrue( "counter inlined"+counter, counter == 50000 );

        streamer.println("Here we go.");
        popper.start();
        popper.join();        
        // should be deoptimized here...

        //
        //    Clean up and go home
        //        
        Thread.sleep(1000);
        done = true;
        streamer.println( "Shutting down");        
        john.join();
        reaper.join();

        streamer.println("Kill count " + killed);
    }

    
    // really extreme stress test, disabled...
    public void xxxtestLooped() throws Exception {
        int i = 0;
        while (currID < 200000) {
            testVMStress01();
            if (i++ % 20 == 0) {
                System.out.println(i + " " + currID);
            }
        }
    }


    // Boilerplate for running JUnit test cases
    
    public static Test suite() {
        return new TestSuite(VMStress01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    

}

