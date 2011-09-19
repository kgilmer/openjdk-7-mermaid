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
 @summary Simple test for recieving TextEvents
 @summary com.apple.junit.java.awt.Font;
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main MixedFonts01
 */

import junit.framework.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Random;

import test.java.awt.regtesthelpers.VisibilityValidator;

class AllFonts {
    static final Random rand = new Random( 62566 );
    static final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static final String[] fontNames = env.getAvailableFontFamilyNames();
    static final int SIZES = 4;
    static Font[] fonts = null;
    static    {
        fonts = new Font[ fontNames.length * SIZES ];
        for (int i = 0; i < fontNames.length; i++) {
            int ii = i * SIZES;
            fonts[ii] = new Font( fontNames[i], Font.PLAIN, 10 );
            fonts[ii + 1] = new Font( fontNames[i], Font.ITALIC, 15 );
            fonts[ii + 2] = new Font( fontNames[i], Font.PLAIN, 20 );
            fonts[ii + 3] = new Font( fontNames[i], Font.PLAIN, 25 );
        }
    }

    static int index = 0;
    synchronized static public Font getNextFont() {
        return (fonts[index++ % fonts.length]);
    }
    
    synchronized static public Font getRandomFont() {
        int i = rand.nextInt(fonts.length);
        return (fonts[i]);
    }

}

public class MixedFonts01 extends TestCase {
    class FontButton extends Button {
        Font f = AllFonts.getRandomFont();
    
        public FontButton(String name) {
            super(name + "nopqrstuvwxyz");
            setFont(f);
            
            addActionListener( new java.awt.event.ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    System.out.println( f );
                }
            } );        
        }
    }
    
    class FontLabel extends Label {
        Font f = AllFonts.getRandomFont();
    
        public FontLabel(String name) {
            super(name + "nopqrstuvwxyz");
            setFont(f);
            
            addMouseListener( new java.awt.event.MouseAdapter() {
                public void mouseClicked( MouseEvent e ) {
                    System.out.println( f );
                }
            } );        
        }
    }

    
    class BusyPanel extends Panel {
        public BusyPanel(int num_objects) {
            setLayout(new GridLayout( num_objects/5, 5, 4, 4 ));
            
            for (int i = 1; i <= num_objects; i++) {
                Component item;
                
                if ( i % 2 == 0) {
                    item = new FontButton("B" + i);
                }
                else {
                    item = new FontLabel("L" + i);
                }
                add(item);
            }
        }
    }

    public void testMixedFonts() throws Exception {
        Frame frame = null;
        // Thread.currentThread().setName( "testMixedFonts" );

        for (int i = 0; i < 1; i++) {
            try {
                // Bring up a test frame
                frame = new Frame( "testMixedFonts " + i );
                assertNotNull(frame);
                
                frame.add( new BusyPanel(30) );
                frame.pack();

                VisibilityValidator.setVisibleAndConfirm(frame);
    
                pause(500);
                frame.dispose();
                frame = null;
            }
            finally {
                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        }
    }

    public static void pause( int duration ) {
        try {
            Thread.sleep( duration );
        }
        catch (Throwable t) {
        }
    }

    public static Test suite() {
        return new TestSuite( MixedFonts01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

