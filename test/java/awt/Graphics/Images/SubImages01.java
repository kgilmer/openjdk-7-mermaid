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
 * @summary Asserts that a constructed frame with many buffered sub images does not returns null
 * @summary com.apple.junit.java.graphics.images
 */

import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Vector;

public class SubImages01 extends TestCase {

    public static Test suite() {
        return new TestSuite(SubImages01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public class SubImageFrame extends JFrame {
        static final int w = 400;
        static final int h = 400;
        static final int rows = 20;
        static final int cols = 20;
        static final int siw = w / cols;
        static final int sih = h / rows;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        BufferedImage bi = gc.createCompatibleImage( w, h );

        boolean initialized = false;
        boolean painted = false;
        Vector<BufferedImage> imageCache = new Vector<BufferedImage>( 16 );
        Vector<Point> pointCache = new Vector<Point>( 16 );

        class TestPanel extends JPanel {
            public TestPanel() {
                setPreferredSize( new Dimension( w + (rows * cols), h + (rows * cols) ) );
            }

            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                if (initialized) {
                    if (bi != null) {
                        for (int i = 0; i < imageCache.size(); i++) {
                            BufferedImage si = (BufferedImage) imageCache.get( i );
                            Point p = (Point) pointCache.get( i );
                            g.drawImage( si, p.x + i, p.y + i, null );
                        }
                    }
                    if (!painted) {
                        painted = true;
                    }
                }
                g.setColor( Color.red );
                g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
            }
        }

        public SubImageFrame() throws Exception {
            super( "SubImageFrame" );
            initBaseImage();
            initSubImages();
            getContentPane().add( new TestPanel() );
            pack();
            initialized = true;
            setVisible( true );            
        }

        // Intialize the base image with a gradient fill.  We could use anything here.
        void initBaseImage() {
            Graphics2D ig = (Graphics2D) bi.createGraphics();
            GradientPaint gp = new GradientPaint( 0, 0, Color.red, w, h, Color.cyan, false );
            ig.setPaint( gp );
            ig.fillRect( 0, 0, w, h );
            ig.dispose();
        }

        // Chop the base image into 16 imageCache and cache them in a Vector
        void initSubImages() {
            for (int x = 0; x < w; x += siw) {
                int ww = x + siw < w? siw: w - x;
                for (int y = 0; y < h; y += sih) {
                    int hh = y + sih < h? sih: h - y;
                    BufferedImage subi = bi.getSubimage( x, y, ww, hh );
                    imageCache.addElement( subi );
                    pointCache.addElement( new Point( x, y ) );
                }
            }
        }

    }

    public void testSubImages() throws Exception {
        SubImageFrame sif = null;
        try {
            sif = new SubImageFrame();
            Thread.sleep(1000);
        }
        finally  {
            assertNotNull(sif);
            sif.dispose();
        }
    }
}
