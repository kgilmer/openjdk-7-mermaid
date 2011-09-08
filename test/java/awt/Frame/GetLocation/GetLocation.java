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
 @summary Verify that getLocationOnScreen and getLocation return the value that was passed to setLocation
 @summary com.apple.junit.java.awt.Frame
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main GetLocation
 */

// classes necessary for this test
import test.java.awt.regtesthelpers.VisibilityValidator;
import java.awt.*;

public class GetLocation {
    // Note that these are all chosen to be valid points for most Mac 
    // windows. A more rigorous test would use 
    //       GraphicsEnvironment.getLocalGraphicsEnvironment()
    // and make sure these points were valid
    
    static Point[] set_loc = {
        new Point(  25, 110 ),
        new Point(  22, 145 ), 
        new Point(  25, 112 ), 
        new Point(  21, 141 ), 
        new Point(  25, 120 ), 
        new Point(  27, 100 ), 
        new Point(  25, 110 ), 
        new Point(  28,  54 ), 
        new Point(  25, 110 ), 
        new Point(  20,  34 ), 
        new Point(  23, 110 ), 
        new Point(  22,  24 ), 
        new Point(  25,  59 ), 
        new Point( 112,  61 ), 
        new Point(  25, 110 ), 
        new Point(  27, 114 )
    };
    
    static Point[]  get_loc = new Point[set_loc.length];
    
    static int ITERATIONS = 10;
    
    public static void main( String[] args ) throws Exception{
        Frame windowf = new Frame();
        try {
            windowf.setSize(100, 100);
            
            VisibilityValidator.setVisibleAndConfirm(windowf);
            
            // getLocationOnScreen test
            for (int iter=0; iter<ITERATIONS; iter++) {
                for (int i = 0; i < set_loc.length; i++) {        
                    windowf.setLocation( set_loc[i] );
                    get_loc[i] = windowf.getLocationOnScreen();
                    
                    if ((get_loc[i].getX() != set_loc[i].getX()) || (get_loc[i].getY() != set_loc[i].getY())) {
                        throw new RuntimeException("Unexpected getLocationOnScreen() problem: set_loc = " + set_loc[i] + "; get_loc = " + get_loc[i]);
                    }
                }
            }
            
            // getLocation test
            for (int iter=0; iter<ITERATIONS; iter++) {
                for (int i = 0; i < set_loc.length; i++) {        
                    windowf.setLocation( set_loc[i] );
                    get_loc[i] = windowf.getLocation();
                    
                    if ((get_loc[i].getX() != set_loc[i].getX()) || (get_loc[i].getY() != set_loc[i].getY())) {
                        throw new RuntimeException("Unexpected getLocation() problem: set_loc = " + set_loc[i] +"; get_loc = " + get_loc[i]);
                    }
                }
            }
        }
        finally {
            windowf.setVisible(false);
            windowf.dispose();
        }
    }
}
