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
 * @summary Tests the positioning of custom composites.
 * @summary com.apple.junit.java.graphics.Positioning
 * @library ../regtesthelpers
 * @build BITestUtils
 * @run main CustomCompositePositioningTest
 */

import test.java.awt.regtesthelpers.BITestUtils;
import junit.framework.*;
import java.awt.*;
import java.awt.image.*;

public class CustomCompositePositioningTest extends TestCase {
    
    static final String[] sImageNames = {
        "TYPE_3BYTE_BGR",
        "TYPE_4BYTE_ABGR",
        "TYPE_4BYTE_ABGR_PRE",
        "TYPE_BYTE_BINARY",
        "TYPE_BYTE_GRAY",
        "TYPE_BYTE_INDEXED",
        "TYPE_INT_ARGB",
        "TYPE_INT_ARGB_PRE",
        "TYPE_INT_BGR",
        "TYPE_INT_RGB",
        "TYPE_USHORT_555_RGB",
    //  "TYPE_USHORT_565_RGB",
        "TYPE_USHORT_GRAY"
    };
    
    static final int[] sImageConstants = {
        BufferedImage.TYPE_3BYTE_BGR,
        BufferedImage.TYPE_4BYTE_ABGR,
        BufferedImage.TYPE_4BYTE_ABGR_PRE,
        BufferedImage.TYPE_BYTE_BINARY,
        BufferedImage.TYPE_BYTE_GRAY,
        BufferedImage.TYPE_BYTE_INDEXED,
        BufferedImage.TYPE_INT_ARGB,
        BufferedImage.TYPE_INT_ARGB_PRE,
        BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_USHORT_555_RGB,
    //  BufferedImage.TYPE_USHORT_565_RGB,
        BufferedImage.TYPE_USHORT_GRAY
    };
    
    int COMPOSITE_X = 0;
    int COMPOSITE_Y = 0;
    int COMPOSITE_WIDTH = 4;
    int COMPOSITE_HEIGHT = 4;
    
    public void testCompositePosition() throws Exception {

        for (int i = 0; i < sImageConstants.length; i++) {
            BufferedImage image = new BufferedImage(COMPOSITE_WIDTH, 2*COMPOSITE_HEIGHT, sImageConstants[i]);
            Graphics2D big = (Graphics2D)image.getGraphics();
            
            big.setColor(Color.white);
            big.fillRect(0, 0, image.getWidth(), image.getHeight());
            
            big.setColor(Color.black);
            big.drawLine(0, 0, image.getWidth(), image.getHeight());
            
            big.setComposite(BlueBlend.DEFAULT);
            
            big.fill(new Rectangle(COMPOSITE_X, COMPOSITE_Y, COMPOSITE_WIDTH, COMPOSITE_HEIGHT));
            
            // if the composited rect is at the top of the image as it should be, the 
            // topmost white pixel will be just below 
            // the composited part
            //BITestUtils.pixelDisplay(image, sImageNames[i]);
            assertEquals(sImageNames[i], COMPOSITE_Y+COMPOSITE_HEIGHT, BITestUtils.getTopMost(image)); 
            
            big.dispose();          
        }
    }
    
    // Boilerplate below so it can be run from the command line
    public static Test suite() {
        return new TestSuite(CustomCompositePositioningTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
}

// 
// Custom composite by Mikey
//
class BlueBlend implements Composite {
    public static BlueBlend DEFAULT = new BlueBlend();
    
    public CompositeContext createContext(ColorModel srcColorModel,ColorModel dstColorModel,RenderingHints hints) {
        
        return new CompositeContext(){
            public void compose(Raster src,Raster dstIn,WritableRaster dstOut){
                int[] srcPixels = new int[4];
                int[] dstPixels = new int[4];
                int[] newPixels = new int[4];
                
                for (int x=0;x<dstOut.getWidth();x++){
                    for (int y=0;y<dstOut.getHeight();y++){
                        // Get the source pixels
                        src.getPixel(x,y,srcPixels);
                        dstIn.getPixel(x,y,dstPixels);
                        // Ignore transparent pixels
                        if (srcPixels[3] != 0)
                        {
                            // Lighten each color by a bit, and increase the blue
                            newPixels[0] = srcPixels[0] / 4 + dstPixels[0] / 4;
                            newPixels[1] = srcPixels[1] / 4 + dstPixels[1] / 4;
                            newPixels[2] = srcPixels[2] / 4 + dstPixels[2] / 4 + 64;
                            newPixels[3] = srcPixels[3]; // need to set alpha, otherwise it will be transparent!
                            dstOut.setPixel(x,y,newPixels);                 
                        }
                    }
                }
            }
            public void dispose(){
            }
        };
        
    }
}
