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
 @run main Prefs01
 */

import junit.framework.*;

import java.util.prefs.Preferences;

public class Prefs01 extends TestCase {

    public static Test suite() {
        return new TestSuite(Prefs01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    

    private static final boolean DEBUG = false;

    // Preference keys for this package
    private static final String NUM_ROWS = "num_rows";
    private static final String NUM_COLS = "num_cols";
    private Preferences prefs = Preferences.userNodeForPackage( Prefs01.class ).node("Prefs01");
    private static int DEFAULTROWS = -1;
    private static int DEFAULTCOLS = -1;
    private static int STARTROWS = 0;
    private static int STARTCOLS = 0;

    // inner class for data structure
    class prefData {
             int rows;
             int cols;
            
            // Getters, Setters, toString
            public int getRows() { return rows; }
            public int getCols() { return cols; }
            public void setRows(int r) { rows = r; }
            public void setCols(int c) { cols = c; }
            public String toString() { return ("rows: " + rows + ", cols: " + cols); }
            
            public void clear() {
                rows = STARTROWS;
                cols = STARTCOLS;
            }
            
            public prefData() {
                rows = STARTROWS;
                cols = STARTCOLS;
            }

            public prefData(int r, int c) {
                rows = r;
                cols = c;
            }
            
    };
    
    prefData readPrefs() {
        prefData pd = new prefData();
        pd.setRows( prefs.getInt(NUM_ROWS, DEFAULTROWS) );
        pd.setCols( prefs.getInt(NUM_COLS, DEFAULTCOLS) );
        return pd;
    }
    
    void writePrefs(int rows, int cols) throws Exception {
        prefs.putInt( NUM_ROWS, rows );
        prefs.putInt( NUM_COLS, cols );
        prefs.flush();
    }

    public void testPrefs01 () throws Exception {
        prefData myPrefs = new prefData(0,0);
        int r = 100, c = 100; // values that we're hoping to get back
        
        if (DEBUG) System.out.println("Starting with initial prefs of: " + myPrefs);
        if (DEBUG) System.out.println("writing non-zero values to disk, r = " + r + ", c = " + c + " ...");

        writePrefs(r, c);
                                
        myPrefs.clear();

        if (DEBUG) System.out.println("Clearing prefs... Now they are: " + myPrefs);
        myPrefs = readPrefs();

        if (DEBUG) System.out.println("Reading prefs from disk... Now they are: " + myPrefs);
        

        String problem = "Some of the preferences were not correctly set! " + myPrefs;
        if ( (myPrefs.getRows() == STARTROWS) && (myPrefs.getCols() == STARTCOLS) ) {
            problem = "Test returned initial values rather than those saved to the prefs file!";
        }
        else if ( (myPrefs.getRows() == DEFAULTROWS) && (myPrefs.getCols() == DEFAULTCOLS) ) {
            problem = "Test returned default values rather than those saved to the prefs file!";
        }

        assertTrue(problem,  myPrefs.getRows() == r);
        assertTrue(problem, myPrefs.getCols() == c);
                
        prefs.removeNode();

        assertFalse("nodeExists() returned true after the node was supposedly removed.", Preferences.userRoot().nodeExists( "com/apple/java/test/harness/tests/java/util/prefs/Prefs01" ) );
    }
    
}




