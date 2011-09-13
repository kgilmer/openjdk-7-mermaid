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
 @test
 @summary <rdar://problem/3489814> java.awt.image.ConvolveOp doesn't work unless Graphics.dispose() is called.
 @summary com.apple.junit.java.graphics.Convolve;
 @library ../../regtesthelpers
 @build BITestUtils
 @run main ConvolveOpTest01
 */

//import com.apple.junit.utils.BITestUtils;
import junit.framework.*;

import java.awt.*;
import java.awt.image.*;

public class ConvolveOpTest01 extends TestCase {
    static final int         imageWidth  = 15;
    static final int         imageHeight = 17;
    static final Rectangle    imageRect = new Rectangle(0,0,imageWidth,imageHeight);

    static final String[] sImageNames = { 
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
        "TYPE_USHORT_565_RGB",
        "TYPE_USHORT_GRAY" 
    };

    static final int[] sImageConstants = {
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
        BufferedImage.TYPE_USHORT_565_RGB,
        BufferedImage.TYPE_USHORT_GRAY 
    };


    protected    GraphicsEnvironment        ge;
    protected    GraphicsDevice             gd;
    protected    GraphicsConfiguration    gc;
    protected    BufferedImage[]            bis;


    protected void setUp() {
        bis = new BufferedImage[sImageConstants.length+1];

        for( int i = 0; i < sImageConstants.length; i++) {
            bis[i] = new BufferedImage( imageWidth,imageHeight, sImageConstants[i] );
        }

        // Get the default (or compatible) buffered image
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd = ge.getDefaultScreenDevice();
        gc = gd.getDefaultConfiguration();
        bis[sImageConstants.length] = gc.createCompatibleImage( imageWidth,imageHeight );

    }
    
    public static Test suite() {
        return new TestSuite(ConvolveOpTest01.class);
    }


    protected void blackOut(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(Color.black);
        g.fillRect(0, 0, imageWidth,imageHeight );
        g.setColor(Color.white);
    }


    protected String testInfo( int i, Rectangle r) {
        String result = null;
        try {
            result = sImageNames[i];
        }
        catch(ArrayIndexOutOfBoundsException x) {
            if (i == sImageConstants.length) {
                result = "COMPATIBLE";
            }
        }
        result += "(" + r.x + "," + r.y + " to " + r.width + "," + r.height + ")";
        return(result);
    }

    public void testIdentityConvole() {

        float[] identity = {
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f
        };
            
        Kernel dupe = new Kernel(3,3,identity);
        BufferedImageOp duplicate = new ConvolveOp(dupe,ConvolveOp.EDGE_NO_OP,null);

        
        for( int i = 0; i <= sImageConstants.length; i++) {
            BufferedImage bi= bis[i];
            Rectangle r = new Rectangle( 2, 2, imageWidth-2, imageHeight-2 );
            String    s = testInfo(i, r) + "\t";

            assertNotNull( bis[i] );

            // clear
            Graphics2D ig = (Graphics2D) bis[i].createGraphics();
            blackOut(ig);

            // draw a short line
            ig.drawLine(r.x, r.y, r.width, r.height); 
            // ig.dispose();
    
            BufferedImage    result = duplicate.filter(bi,null);    
            
            assertEquals( s + "left ",    r.x,        BITestUtils.getLeftMost  ( result ) );
            assertEquals( s + "top",    r.y,        BITestUtils.getTopMost   ( result ) );
            assertEquals( s + "right",  r.width,    BITestUtils.getRightMost ( result ) );
            assertEquals( s + "bottom", r.height,    BITestUtils.getBottomMost( result ) );

        }
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected errors or failures.");
        }
    }
}
