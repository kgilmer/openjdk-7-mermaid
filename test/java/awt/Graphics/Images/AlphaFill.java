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
 * @summary <rdar://problem/3239706> incorrect drawing of primitives with alpha < 1
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main AlphaFill
 */

/*  
 *  The idea here is repeatedly overlay smaller and smaller rectangles with
 *  color red at 1/2 alpha and make sure that the colors make sense. This 
 *  basically looks like
 *
 *      g.setColor(new Color(255, 0, 0, 128));
 *      g.fillRect(0, 0, 256, 256);
 *      g.fillRect(0, 0, 128, 128);
 *      g.fillRect(0, 0, 64, 64);
 *      g.fillRect(0, 0, 32, 32);
 *      g.fillRect(0, 0, 8, 8); 
 *
 *  Note that testcase should be augmented when the following is fixed:
 *      <rdar://problem/4172074> getRBG() causes TYPE_BYTE_BINARY to be drawn with 32 bit color
 *
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AlphaFill extends TestCase {

    public static Test suite() {
        return new TestSuite(AlphaFill.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    static final boolean TEST_DEBUG  = false;

    static final Rectangle  TEST_RECT = new Rectangle(40,40,256,256);

    static final int[][] corners = { 
        {256, 256 },
        {128, 128 },
        { 64,  64 },
        { 32,  32 },
        { 16,  16 },
        {  8,  8 }
    };

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
        BufferedImage.TYPE_USHORT_555_RGB,
        BufferedImage.TYPE_USHORT_565_RGB,
        //      BufferedImage.TYPE_BYTE_BINARY,
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
        "TYPE_USHORT_555_RGB",
        "TYPE_USHORT_565_RGB",
        //      "TYPE_BYTE_BINARY",
        "TYPE_USHORT_GRAY",
        "TYPE_BYTE_GRAY",
        "COMPATIBLE"    // Must be last -- see setUp()
    };


    static final int[][] sExpectedRGB_Quartz = {
        /* TYPE_INT_ARGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_ARGB_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_RGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_3BYTE_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_BYTE_INDEXED */
        {
            0xffff9999,
            0xffff3333,
            0xffff3333,
            0xffff0000,
            0xffff0000,
            0xffff0000 
        },
        /* TYPE_USHORT_555_RGB */
        {
            0xffff7b7b,
            0xffff3a3a,
            0xffff1919,
            0xffff1010,
            0xffff0808,
            0xffff0808 
        },
        /* TYPE_USHORT_565_RGB */
        {
            0xffff8284,
            0xffff4142,
            0xffff2021,
            0xffff1010,
            0xffff0808,
            0xffff0000 
        },
        //      /* TYPE_BYTE_BINARY */
        //      {
        //          0xffffffff,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000 
        //      },
        /* TYPE_USHORT_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9e9e9e,
            0xff9a9a9a,
            0xff979797 
        },
        /* TYPE_BYTE_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9e9e9e,
            0xff999999,
            0xff969696 
        },
        /* COMPATIBLE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        }
    };


    // these are the values that we were getting with the Quartz Renderer when
    // the BufferedImages were implemented on top of CGLayers. Probably not releveant for future development
    static final int[][] sExpectedRGB_Old_CGLayers_Quartz = {
        /* TYPE_INT_ARGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_ARGB_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_RGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_3BYTE_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_BYTE_INDEXED */
        {
            0xffff9999,
            0xffff3333,
            0xffff3333,
            0xffff0000,
            0xffff0000,
            0xffff0000 
        },
        /* TYPE_USHORT_555_RGB */
        {
            0xffff7b7b,
            0xffff3a3a,
            0xffff1919,
            0xffff1010,
            0xffff0808,
            0xffff0808 
        },
        /* TYPE_USHORT_565_RGB */
        {
            0xffff797b,
            0xffff393a,
            0xffff1819,
            0xffff1010,
            0xffff0808,
            0xffff0808 
        },
        //      /* TYPE_BYTE_BINARY */
        //      {
        //          0xffffffff,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000 
        //      },
        /* TYPE_USHORT_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9e9e9e,
            0xff9a9a9a,
            0xff979797 
        },
        /* TYPE_BYTE_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9e9e9e,
            0xff999999,
            0xff969696 
        },
        /* COMPATIBLE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        }
    };

    static final int[][] sExpectedRGB_Sun = {
        /* TYPE_INT_ARGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_ARGB_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_RGB */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_INT_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_3BYTE_BGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_4BYTE_ABGR_PRE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        },
        /* TYPE_BYTE_INDEXED */
        {
            0xffff9999,
            0xffff6666,
            0xffff3333,
            0xffff3333,
            0xffff3333,
            0xffff3333 
        },
        /* TYPE_USHORT_555_RGB */
        {
            0xffff8484,
            0xffff4242,
            0xffff2121,
            0xffff1010,
            0xffff0808,
            0xffff0000 
        },
        /* TYPE_USHORT_565_RGB */
        {
            0xffff8284,
            0xffff4142,
            0xffff2021,
            0xffff1010,
            0xffff0808,
            0xffff0400 
        },
        //      /* TYPE_BYTE_BINARY */
        //      {
        //          0xffffffff,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000,
        //          0xff000000 
        //      },
        /* TYPE_USHORT_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9e9e9e,
            0xff9a9a9a,
            0xff979797 
        },
        /* TYPE_BYTE_GRAY */
        {
            0xffd3d3d3,
            0xffb7b7b7,
            0xffa7a7a7,
            0xff9f9f9f,
            0xff9a9a9a,
            0xff979797 
        },
        /* COMPATIBLE */
        {
            0xffff8080,
            0xffff4040,
            0xffff2020,
            0xffff1010,
            0xffff0808,
            0xffff0404
        }
    };

    // Storage for actual results
    final int[][] sActualRGB = new int[ sImageConstants.length+1] [corners.length ];


    protected BufferedImage[] bis;

    //
    //  Intialize array of BufferedImages with various alpha filles
    //
    protected void setUp() throws Exception {

        int imageWidth = (int) TEST_RECT.getWidth();
        int imageHeight = (int) TEST_RECT.getHeight();

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

            // initialize to white
            big.setColor( Color.white );
            big.fillRect( 0, 0, imageWidth, imageHeight );

            // paint several layers of alphas
            big.setColor(new Color(255, 0, 0, 127));
            for (int j = 0; j < corners.length; j++) {
                big.fillRect(0, 0, corners[j][0], corners[j][1]);
            }
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
                super.paint( g );
                Dimension dim = getSize();
                g.drawImage( image, 0, 0, dim.width-1, dim.height-1,this );
            }

            public Dimension getPreferredSize() {
                int imageWidth = (int) TEST_RECT.getWidth();
                int imageHeight = (int) TEST_RECT.getHeight();
                return ( new Dimension( imageWidth / 2 , imageHeight / 2) );
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




    public void testAlphaFill() throws Exception {
        TestFrame  tFrame = new TestFrame();
        assertNotNull(tFrame);
        try {
            tFrame.setVisible(true);

            // Gather up the results.
            boolean  allpass = true;
            boolean[] testpass = new boolean[  bis.length ];
            String result = "Unexpected results for";
            for( int i = 0; i < bis.length; i+=1)  {
                for (int j = 0; j < corners.length; j++) {
                    int x = (corners[j][0] -1);
                    int y = (corners[j][1] -1);
                    sActualRGB[i][j]  = bis[i].getRGB( x, y);
                    testpass[i] = (VisibilityValidator.colorMatch(new Color(sActualRGB[i][j]), new Color(sExpectedRGB_Quartz[i][j])) && 
                            VisibilityValidator.colorMatch(new Color(sActualRGB[i][j]), new Color(sExpectedRGB_Sun[i][j])) && 
                            VisibilityValidator.colorMatch(new Color(sActualRGB[i][j]), new Color(sExpectedRGB_Old_CGLayers_Quartz[i][j])));
                }
                if (!testpass[i]) {
                    allpass = false;
                    result += " " + sImageNames[i];

                    if (TEST_DEBUG == true) {
                        System.out.println( sImageNames[i] );
                        System.out.println( "EXPECTED (OLD_CGLAYERS_QUARTZ)" );
                        for (int j = 0; j < corners.length; j++) {
                            System.out.print( "0x"+Integer.toHexString(sExpectedRGB_Old_CGLayers_Quartz[i][j])+" ");
                        }
                        System.out.println( "\nEXPECTED (QUARTZ)" );
                        for (int j = 0; j < corners.length; j++) {
                            System.out.print( "0x"+Integer.toHexString(sExpectedRGB_Quartz[i][j])+" ");
                        }
                        System.out.println( "\nEXPECTED (SUN)" );
                        for (int j = 0; j < corners.length; j++) {
                            System.out.print( "0x"+Integer.toHexString(sExpectedRGB_Sun[i][j])+" ");
                        }

                        System.out.println( "\nACTUAL" );
                        for (int j = 0; j < corners.length; j++) {
                            System.out.print( "0x"+Integer.toHexString(sActualRGB[i][j])+" ");
                        }
                        System.out.println("\n");
                    }
                }
            }
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
