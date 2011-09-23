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

public class WritableRaster02 extends TestCase {

    public static Test suite() {
        return new TestSuite(WritableRaster02.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    // Lookup tables for BYTE_BINARY 1, 2 and 4 bits.
    static byte[] lut1 = new byte[] {0, (byte)255 };
    static byte[] lut2 = new byte[] {0, (byte)85, (byte)170, (byte)255};

    static byte[] lut4 = new byte[] {0, (byte)17, (byte)34, (byte)51,
                                  (byte)68, (byte)85,(byte) 102, (byte)119,
                                  (byte)136, (byte)153, (byte)170, (byte)187,
                                  (byte)204, (byte)221, (byte)238, (byte)255};

    static byte[] lut4r = new byte[] {0, 0, 0, 0,
                                    0, 0, 0, 0,
                                    (byte)255, (byte)255, (byte)255, (byte)255,
                                    (byte)255, (byte)255, (byte)255, (byte)255 };




    class RasterFrame extends JFrame {
        static final int w = 200;
        static final int h = 500;

        BufferedImage bi1 = null;
        BufferedImage bi2 = null;
        BufferedImage bi4 = null;

        boolean initialized = false;
        boolean painted1 = false;
        boolean painted2 = false;
        boolean painted4 = false;

        void initBaseImage1() {
            int bytesPerRow = w / 8;
            if (w  % 8 != 0) {
                bytesPerRow++;
            }
            
            byte[] imageData = new byte[h * bytesPerRow];
            
            IndexColorModel cm = new IndexColorModel(1, 2, lut1, lut1, lut1);
            DataBuffer db = new DataBufferByte( imageData, imageData.length );
            WritableRaster wr = Raster.createPackedRaster(db, w, h, 1, null);
            bi1 = new BufferedImage( cm, wr, false, null );
            
            // Intialize the base image with a gradient fill.  We could use anything here.
            Graphics2D ig = (Graphics2D) bi1.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.white, w, h, Color.black, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.setColor( Color.black );
            ig.drawString( "1 bit IndexColorModel", 30, 30 );
            ig.dispose();
        }

        void initBaseImage2() {
            int bytesPerRow = (w * 2) / 8;
            if ((w * 2) % 8 != 0) {
                bytesPerRow++;
            }
            
            byte[] imageData = new byte[h * bytesPerRow];
            
            IndexColorModel cm = new IndexColorModel(2, 4, lut2, lut2, lut2);
            DataBuffer db = new DataBufferByte( imageData, imageData.length );
            WritableRaster wr = Raster.createPackedRaster(db, w, h, 2, null);
            bi2 = new BufferedImage( cm, wr, false, null );
            
            // Intialize the base image with a gradient fill.  We could use anything here.
            Graphics2D ig = (Graphics2D) bi2.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.white, w, h, Color.black, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.setColor( Color.black );
            ig.drawString( "2 bit IndexColorModel", 30, 30 );
            ig.dispose();
        }

        void initBaseImage4() {
            int bytesPerRow = (w * 4) / 8;
            if ((w * 4) % 8 != 0) {
                bytesPerRow++;
            }
            
            byte[] imageData = new byte[h * bytesPerRow];
            
            IndexColorModel cm = new IndexColorModel(4, 16, lut4r, lut4, lut4);
            DataBuffer db = new DataBufferByte( imageData, imageData.length );
            WritableRaster wr = Raster.createPackedRaster(db, w, h, 4, null);
            bi4 = new BufferedImage( cm, wr, false, null );
            
            // Intialize the base image with a gradient fill.  We could use anything here.
            Graphics2D ig = (Graphics2D) bi4.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.white, w, h, Color.black, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.setColor( Color.black );
            ig.drawString( "4 bit IndexColorModel", 30, 30 );
            ig.dispose();
        }


        class TestPanel1 extends JPanel {
            public TestPanel1() {
                setPreferredSize( new Dimension( w, h ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if ((initialized) && (bi1 != null)) {
                    g.drawImage( bi1, 0, 0, dim.width - 1, dim.height - 1, null );
                    if (!painted1) {
                        painted1 = true;
                    }
                }
                else {
                    g.setColor( Color.red );
                    g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                }
            }
        }

        class TestPanel2 extends JPanel {
            public TestPanel2() {
                setPreferredSize( new Dimension( w, h ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if ((initialized) && (bi2 != null)) {
                    g.drawImage( bi2, 0, 0, dim.width - 1, dim.height - 1, null );
                    if (!painted2) {
                        painted2 = true;
                    }
                }
                else {
                    g.setColor( Color.red );
                    g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                }
            }
        }

        class TestPanel4 extends JPanel {
            public TestPanel4() {
                setPreferredSize( new Dimension( w, h ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if ((initialized) && (bi4 != null)) {
                    g.drawImage( bi4, 0, 0, dim.width - 1, dim.height - 1, null );
                    if (!painted4) {
                        painted4 = true;
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
            initBaseImage1();
            initBaseImage2();
            initBaseImage4();
            addPanels();
            pack();
            initialized = true;
            setVisible( true );
            int count = 0;
            while (((painted1 == false) || (painted2 == false) || (painted4 == false)) && count < 50) {
                Thread.sleep( 500 );
                count++;
                repaint();
            }
            assertTrue("Should have painted a 1-bit indexed image", painted1);
            assertTrue("Should have painted a 2-bit indexed image", painted2);
            assertTrue("Should have painted a 4-bit indexed image", painted4);
        }

        void addPanels() {
            getContentPane().setLayout( new GridLayout( 1, 2, 10, 10 ) );
            getContentPane().add( new TestPanel1() );
            getContentPane().add( new TestPanel2() );
            getContentPane().add( new TestPanel4() );
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
