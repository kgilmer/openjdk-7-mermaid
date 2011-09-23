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
 * @test
 * @summary Basic raster test 
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class WritableRaster01 extends TestCase {

    public static Test suite() {
        return new TestSuite(WritableRaster01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }


    class RasterFrame extends JFrame {
        static final int w = 200;
        static final int h = 500;

        BufferedImage bi16 = null;
        BufferedImage bi32 = null;

        boolean initialized = false;
        boolean painted16 = false;
        boolean painted32 = false;

        void initBaseImage16() {
        // Create an image just like they do in 
        //  Developer/Examples/Java/JFC/Java2D/java2d/Surface.java
            int rMask16 = 0xF800;
            int gMask16 = 0x07C0;
            int bMask16 = 0x003E;
            short[] imageDataUShort = new short[ w * h ];
            DirectColorModel dcm = new DirectColorModel( 16, rMask16, gMask16, bMask16 );
            DataBuffer db = new DataBufferUShort( imageDataUShort, imageDataUShort.length );
            WritableRaster wr = Raster.createPackedRaster( db, w, h, w, new int[] { rMask16, gMask16, bMask16 }, null );
            bi16 = new BufferedImage( dcm, wr, false, null );

            // Intialize the base image with a gradient fill.  We could use anything here.
            Graphics2D ig = (Graphics2D) bi16.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.red, w, h, Color.cyan, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.setColor( Color.black );
            ig.drawString( "16 bit raster", 30, 30 );
            ig.dispose();
        }

        void initBaseImage32() {
        // Create an image just like they do in 
        //  Developer/Examples/Java/JFC/Java2D/java2d/Surface.java
            int rMask32 = 0xFF000000;
            int gMask32 = 0x00FF0000;
            int bMask32 = 0x0000FF00;
            int[] imageDataInt = new int[ w * h ];
            DirectColorModel dcm = new DirectColorModel( 32, rMask32, gMask32, bMask32 );
            DataBuffer db = new DataBufferInt( imageDataInt, imageDataInt.length );
            WritableRaster wr = Raster.createPackedRaster( db, w, h, w, new int[] { rMask32, gMask32, bMask32 }, null );
            bi32 = new BufferedImage( dcm, wr, false, null );

            // Intialize the base image with a gradient fill.  We could use anything here.
            Graphics2D ig = (Graphics2D) bi32.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.red, w, h, Color.cyan, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.setColor( Color.black );
            ig.drawString( "32 bit raster", 30, 30 );
            ig.dispose();
        }

        class TestPanel16 extends JPanel {
            public TestPanel16() {
                setPreferredSize( new Dimension( w, h ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if ((initialized) && (bi16 != null)) {
                    g.drawImage( bi16, 0, 0, dim.width - 1, dim.height - 1, null );
                    if (!painted16) {
                        painted16 = true;
                    }
                }
                else {
                    g.setColor( Color.red );
                    g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                }
            }
        }

        class TestPanel32 extends JPanel {
            public TestPanel32() {
                setPreferredSize( new Dimension( w, h ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if ((initialized) && (bi32 != null)) {
                    g.drawImage( bi32, 0, 0, dim.width - 1, dim.height - 1, null );
                    if (!painted32) {
                        painted32 = true;
                    }
                }
                else {
                    g.setColor( Color.red );
                    g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                }
            }
        }

        public RasterFrame() throws Exception {
            super( "RasterFrame" );
            initBaseImage16();
            initBaseImage32();
            addPanels();
            pack();
            initialized = true;
            setVisible( true );
            int count = 0;
            while (((painted32 == false) || (painted16 == false)) && count < 10) {
                Thread.sleep( 500 );
                count++;
                repaint();
            }
            assertTrue("Should have painted a 32-bit raster image", painted32);
            assertTrue("Should have painted a 16-bit raster image", painted16);
        }

        void addPanels() {
            getContentPane().setLayout( new GridLayout( 1, 2, 10, 10 ) );
            getContentPane().add( new TestPanel16() );
            getContentPane().add( new TestPanel32() );
        }
    }

    public void testRaster() throws Exception {
        RasterFrame rf = null;
        try {
            rf = new RasterFrame();
            Thread.sleep( 500 );
        } finally {
            assertNotNull(rf);
            rf.dispose();
        }
    }

}
