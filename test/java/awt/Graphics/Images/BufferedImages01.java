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
 * @summary Test creates a few buffered images and renders into them, 
 * @summary then does a check that the images correctly report back the colors we just rendered
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main BufferedImages01
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImages01 extends TestCase {

    public static Test suite() {
        return new TestSuite(BufferedImages01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    /* 
        The idea here is to chop a bufferedImage into stripes and columns, and
        to paint into these columns and rows, then check for the expected results
        and make sure that the colors make sense
     */

    static final boolean TEST_DEBUG  = false;

    static final Rectangle[] cols = new Rectangle[3];   
    static final Rectangle[] rows = new Rectangle[4];   
    static final Dimension  IMAGE_DIM = new Dimension(cols.length*10, rows.length*10);

    static final int col_width = (IMAGE_DIM.width / cols.length);   
    static {
        for (int i=0; i < cols.length; i+=1) {
            cols[i] = new Rectangle( col_width*i, 0, col_width, IMAGE_DIM.height);
        }
    }

    static final int band_height = (IMAGE_DIM.height / rows.length);    
    static {
        for (int i=0; i < rows.length; i+=1) {
            rows[i] = new Rectangle( 0, band_height*i, IMAGE_DIM.width, band_height);
        }
    }

    /*
        A list of buffered image types, and the expected results of rendering into them.  
     */

    static final int[] sImageConstants = {
        BufferedImage.TYPE_INT_ARGB,
        BufferedImage.TYPE_INT_ARGB_PRE,
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_3BYTE_BGR,
        BufferedImage.TYPE_4BYTE_ABGR,
        BufferedImage.TYPE_4BYTE_ABGR_PRE,
        BufferedImage.TYPE_BYTE_INDEXED,
        BufferedImage.TYPE_BYTE_BINARY,
        BufferedImage.TYPE_USHORT_GRAY,
        BufferedImage.TYPE_BYTE_GRAY
    };

    static final String[] sImageNames = {
        "TYPE_INT_ARGB",
        "TYPE_INT_ARGB_PRE",
        "TYPE_INT_RGB",
        "TYPE_INT_BGR",
        "TYPE_3BYTE_BGR",
        "TYPE_4BYTE_ABGR",
        "TYPE_4BYTE_ABGR_PRE",
        "TYPE_BYTE_INDEXED",
        "TYPE_BYTE_BINARY",
        "TYPE_USHORT_GRAY" ,
        "TYPE_BYTE_GRAY",
        "COMPATIBLE"    // Must be last, see 
    };


    static final int[][][] sExpectedRGB = {
        /* TYPE_INT_ARGB */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_INT_ARGB_PRE */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_INT_RGB */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_INT_BGR */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_3BYTE_BGR */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_4BYTE_ABGR */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_4BYTE_ABGR_PRE */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        },
        /* TYPE_BYTE_INDEXED */
        {
            { 0xffffffff, 0xffff0000, 0xffdedede },
            { 0xff000000, 0xffff0000, 0xffdedede },
            { 0xff00ffff, 0xffff0000, 0xffdedede },
            { 0xff99ccff, 0xffff0000, 0xffdedede }
        },
        /* TYPE_BYTE_BINARY */
        {
            { 0xffffffff, 0xff000000, 0xffffffff },
            { 0xff000000, 0xff000000, 0xffffffff },
            { 0xffffffff, 0xff000000, 0xffffffff },
            { 0xffffffff, 0xff000000, 0xffffffff }
        },
        /* TYPE_USHORT_GRAY */
        {
            { 0xffffffff, 0xff959595, 0xfff4f4f4 },
            { 0xff000000, 0xff959595, 0xfff4f4f4 },
            { 0xffdadada, 0xff959595, 0xfff4f4f4 },
            { 0xffe0e0e0, 0xff959595, 0xfff4f4f4 }
        },
        /* TYPE_BYTE_GRAY */
        {
            { 0xffffffff, 0xff949494, 0xfff4f4f4 },
            { 0xff000000, 0xff949494, 0xfff4f4f4 },
            { 0xffdadada, 0xff949494, 0xfff4f4f4 },
            { 0xffe0e0e0, 0xff949494, 0xfff4f4f4 }
        },
        /* COMPATIBLE */
        {
            { 0xffffffff, 0xffff0000, 0xffdfeed0 },
            { 0xff000000, 0xffff0000, 0xffdfeed0 },
            { 0xff00ffff, 0xffff0000, 0xffdfeed0 },
            { 0xffadbeef, 0xffff0000, 0xffdfeed0 }
        }
    };
    final int[][][] sActualRGB = new int[ sImageConstants.length+1] [rows.length ] [cols.length ];

    volatile Exception cachedException = null;
    protected BufferedImage[] bis;

    protected void setUp() throws Exception {

        int imageWidth = (int) IMAGE_DIM.getWidth();
        int imageHeight = (int) IMAGE_DIM.getHeight();

        // create the images
        bis = new BufferedImage[sImageConstants.length+1];
        for( int i = 0; i < sImageConstants.length; i+=1)  {
            bis[i] = new BufferedImage( imageWidth,imageHeight, sImageConstants[i] );
        }

        // tack a native image on the end of the list..
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        bis[sImageConstants.length] = gc.createCompatibleImage( imageWidth,imageHeight );


        // render into the images
        for( int i = 0; i < bis.length; i+=1)  {
            Graphics2D big = (Graphics2D) bis[i].getGraphics();
            big.setColor( Color.white );
            big.fill( rows[0]);
            big.setColor( Color.black );
            big.fill( rows[1]);
            big.setColor( Color.cyan );
            big.fill( rows[2]);
            big.setColor( new Color (0xDEADBEEF) );
            big.fill( rows[3]);
            // skip col 0 -- this is the unaltered case.
            big.setColor(Color.red);
            big.fill( cols[1]);
            big.setColor(new Color (0xBADFEED0));
            big.fill( cols[2]);
            big.dispose();
        }

    }

    public class TestFrame extends JFrame   {

        class TestPanel extends JPanel {
            Image image = null;

            public TestPanel( Image image ) {
                this.image = image;
            }

            public void paint( Graphics g ) {
                try {
                    super.paint( g );
                    Dimension dim = getSize();
                    g.drawImage( image, 0, 0, dim.width-1, dim.height-1,this );
                }
                catch(Exception x) {
                    cachedException = x;
                }
            }

            public Dimension getPreferredSize() {
                return (IMAGE_DIM);
            }
        }


        class LabelledTestPanel extends JPanel {
            public LabelledTestPanel( Image image, String name ) {
                super();
                setLayout( new BorderLayout() );
                add( "North", new JLabel(name) );
                add( "Center", new TestPanel(image));
            }
        }

        public TestFrame() {
            super("");
            Container contentPane = getContentPane();
            contentPane.setLayout( new GridLayout( ((bis.length + 1) / 4) + 1, 4, 10, 10 ) );

            for( int i = 0; i < bis.length; i+=1)  {
                String name = sImageNames[i];
                contentPane.add( new LabelledTestPanel(bis[i], name) );
            }

            // contentPane.setBackground( Color.red );
            setSize(740, 540);
            setLocation(40, 40);
            pack();
        }
    }

    public void testBufferedImages() throws Exception {
        TestFrame  tFrame = new TestFrame();
        assertNotNull(tFrame);
        try {
            tFrame.setVisible(true);

            // Gather up the results.
            boolean  allpass = true;
            boolean[] testpass = new boolean[  bis.length ];
            String result = "Unexpected results for";
            for( int i = 0; i < bis.length; i+=1)  {
                for (int row = 0; row < rows.length; row +=1 ) {
                    for (int col = 0; col < cols.length; col +=1 ) {
                        sActualRGB[i][row][col]  = bis[i].getRGB( (col*col_width)+5, (row*band_height)+5 );
                        testpass[i] = VisibilityValidator.colorMatch(new Color(sActualRGB[i][row][col]), new Color(sExpectedRGB[i][row][col]));
                    }
                }
                if (!testpass[i]) {
                    allpass = false;
                    result += " " + sImageNames[i];

                    if (TEST_DEBUG == true) {
                        System.out.println( sImageNames[i] );
                        System.out.println( "EXPECTED" );
                        for (int row = 0; row < rows.length; row +=1 ) {
                            for (int col = 0; col < cols.length; col +=1 ) {
                                System.out.print( "0x"+Integer.toHexString(sExpectedRGB[i][row][col])+" ");
                            }
                            System.out.println();
                        }
                        System.out.println( "\nACTUAL" );
                        for (int row = 0; row < rows.length; row +=1 ) {
                            for (int col = 0; col < cols.length; col +=1 ) {
                                System.out.print( "0x"+Integer.toHexString(sActualRGB[i][row][col])+" ");
                            }
                            System.out.println();
                        }
                        System.out.println();
                    }
                }
            }
            assertNull("Unexpected exception encountered while painting.", cachedException);
            assertTrue(result, allpass);            
        }
        finally {
            if (TEST_DEBUG == true) {
                Thread.sleep(5000);
            }
            tFrame.setVisible(false);
            tFrame.dispose();
        }
    }
}


