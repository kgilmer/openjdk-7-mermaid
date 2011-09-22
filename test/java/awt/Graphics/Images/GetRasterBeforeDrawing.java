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
 * @summary tests custom composite on screen
 * @summary <rdar://problem/4327495> [JavaJDK15] "CGContextGetPixelAccess returns NULL in syncToJavaPixels" messages on console
 * @summary <rdar://problem/4569471> [JavaJDK16] Custom Composite causes java.lang.InternalError: not implemented yet with sun2D
 * @summary <rdar://problem/4742100> [JavaJDK16] Leopard Seed: Java 1.5: NeoOffice Aqua beta 3
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build VisibilityValidator
 * @run main GetRasterBeforeDrawing
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.*;

public class GetRasterBeforeDrawing extends TestCase {
    private JFrame frame = null;

    private Robot robot = null;

    private boolean done = false;

    public void testForCrash() throws Exception {
        VisibilityValidator.setVisibleAndConfirm(frame);
        assertTrue("Timed out without seeing our orange background", VisibilityValidator.waitForColor(frame, 50, 50, Color.orange));
    
        robot = new Robot();
        
        SetSizeThread sst = new SetSizeThread();
        sst.start();

        while (!done) {
            Thread.sleep(300);
        }
    }

    public static Test suite() {
        return new TestSuite(GetRasterBeforeDrawing.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void setUp() {
        frame = new JFrame();
        frame.getContentPane().add( new MyComp() );
        frame.setSize(200, 200);
    }

    protected void tearDown() {
        frame.dispose();
    }

     private class SetSizeThread extends Thread {
        public void run() {
            try {
                int locX = (int)(frame.getLocationOnScreen().getX());
                int locY = (int)(frame.getLocationOnScreen().getY());

                int growX = locX + (int)(frame.getBounds().getWidth()) - 6;
                int growY = locY + (int)(frame.getBounds().getHeight()) - 6;

                robot.mouseMove(growX, growY);
                robot.waitForIdle();
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.waitForIdle();
                robot.mouseMove(growX + 2, growY + 2);  // this should cause a crash
                robot.mouseRelease(InputEvent.BUTTON1_MASK);

                done = true;
            }
            catch (Exception e) {
            }
        }
    }


    private class MyComp extends JComponent {
        private BufferedImage image;

        public MyComp() {
            image = new BufferedImage( 256, 256, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g2d = image.createGraphics();
            g2d.setPaint( new GradientPaint( 0, 0, Color.red, 256, 256, Color.blue ) );
            g2d.fillRect( 0, 100, 256, 256 );
            g2d.dispose();
        }

        public void paintComponent( Graphics g ) {
            Graphics2D g2d = (Graphics2D)g;
            g.setColor(Color.orange);
            g.fillRect(0, 0, 200, 100);
            g2d.setComposite( new TestComposite() );
            g2d.drawRenderedImage( image, null );
        }
    }

    // A silly custom Composite to demonstrate the problem - just inverts the RGB
    private class TestComposite implements Composite {

        public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
            return new TestCompositeContext();
        }

    }

    private class TestCompositeContext implements CompositeContext {

        public void dispose() {
        }

        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int w = src.getWidth();
            int h = src.getHeight();

            DataBufferInt srcDB = (DataBufferInt)src.getDataBuffer();
            DataBufferInt dstOutDB = (DataBufferInt)dstOut.getDataBuffer();
            int srcRGB[] = srcDB.getBankData()[0];
            int dstOutRGB[] = dstOutDB.getBankData()[0];
            int srcOffset = srcDB.getOffset();
            int dstOutOffset = dstOutDB.getOffset();
            int srcScanStride = ((SinglePixelPackedSampleModel)src.getSampleModel()).getScanlineStride();
            int dstOutScanStride = ((SinglePixelPackedSampleModel)dstOut.getSampleModel()).getScanlineStride();
            int srcAdjust = srcScanStride-w;
            int dstOutAdjust = dstOutScanStride-w;

            int si = srcOffset;
            int doi = dstOutOffset;

            for ( int i = 0; i < h; i++ ) {
                for ( int j = 0; j < w; j++ ) {
                    dstOutRGB[doi] = srcRGB[si] ^ 0x00ffffff;
                    si++;
                    doi++;
                }

                si += srcAdjust;
                doi += dstOutAdjust;
            }
        }
    }
}
