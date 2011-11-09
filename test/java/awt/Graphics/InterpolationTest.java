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
 * @summary Tests basic interpolation behavior 
 * @summary com.apple.junit.java.graphics.Interpolation
 */

import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class InterpolationTest extends TestCase {
	static final int 		srcWidth  = 5;
	static final int 		srcHeight = 3;
	static final Rectangle	srcRect = new Rectangle(0,0,srcWidth,srcHeight);
	
	static final int 		dstWidth  = srcWidth;
	static final int 		dstHeight = srcHeight * 3;
	static final Rectangle	dstRect = new Rectangle(0,0,dstWidth,dstHeight);

	static final int kThreshold = 3;

	protected	GraphicsEnvironment		ge;
	protected	GraphicsDevice 			gd;
	protected	GraphicsConfiguration	gc;
	protected	BufferedImage			src;
	protected	BufferedImage			dst;


	protected void setUp() {
		// Get the default (or compatible) buffered image
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();
		gc = gd.getDefaultConfiguration();

		// create source and destination images
		src = gc.createCompatibleImage( srcWidth,srcHeight );
		dst = gc.createCompatibleImage( dstWidth,dstHeight );

	}
	
	public void testExpandNN() {
		assertNotNull( src );
		assertNotNull( dst );

		// Draw some bits into the source image
		Graphics2D sg = (Graphics2D) src.createGraphics();
		sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		sg.setColor(Color.black);
		sg.fillRect(0, 0, srcWidth,srcHeight );

		// draw a single horizonal blue line
		sg.setColor(Color.blue);
		sg.drawLine(0, 1, srcWidth-1, 1);


		// Now stretch it using VALUE_INTERPOLATION_NEAREST_NEIGHBOR
		Graphics2D dg = (Graphics2D) dst.createGraphics();
		dg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		dg.drawImage(src, 0, 0, dstWidth, dstHeight, null); 
		dg.dispose();

		// spot check the values
		int blues[] = new int[9];
		for (int i = 0; i < 9; i++) {
			blues[i] = dst.getRGB(3,i) & 0x000000FF;
		}

		assertEquals("top part, pixel 0 should be clear", 0x00, blues[0]);
		assertEquals("top part, pixel 1 should be clear", 0x00, blues[1]);
		assertEquals("top part, pixel 2 should be clear", 0x00, blues[2]);
		
		assertEquals("middle part, pixel 3 should be blue", 0xFF, blues[3]);
		assertEquals("middle part, pixel 4 should be blue", 0xFF, blues[4]);
		assertEquals("middle part, pixel 5 should be blue", 0xFF, blues[5]);

		assertEquals("bottom part, pixel 6 should be clear", 0x00, blues[6]);
		assertEquals("bottom part, pixel 7 should be clear", 0x00, blues[7]);
		assertEquals("bottom part, pixel 8 should be clear", 0x00, blues[8]);

		sg.dispose();
	}
	
	public void testExpandBIL() {
		assertNotNull( src );
		assertNotNull( dst );

		// Draw some bits into the source image
		Graphics2D sg = (Graphics2D) src.createGraphics();
		sg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		sg.setColor(Color.black);
		sg.fillRect(0, 0, srcWidth,srcHeight );

		sg.setColor(Color.blue);
		sg.drawLine(0, 1, srcWidth-1, 1);


		// Now stretch it using VALUE_INTERPOLATION_BILINEAR
		Graphics2D dg = (Graphics2D) dst.createGraphics();
		dg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		dg.drawImage(src, 0, 0, dstWidth, dstHeight, null); 
		dg.dispose();		

		// spot check the values
		int blues[] = new int[9];
		for (int i = 0; i < 9; i++) {
			blues[i] = dst.getRGB(3,i) & 0x000000FF;
		}

		assertTrue("top part, pixel 0 should be almost completely clear", blues[0] < kThreshold);
		assertTrue("top part, pixel 1 should be almost completely clear", blues[1] < kThreshold);

		assertTrue("top part, pixel 1 should fade up to blue", blues[1] < blues[2]);
		assertTrue("top middle part should fade up to blue", blues[1] < blues[2]);
		assertTrue("top middle part should fade up to blue", blues[2] < blues[3]);
		assertTrue("top middle part should fade up to blue", blues[3] < blues[4]);

		assertTrue("middle part, pixel 4 should be almost completely blue" , blues[4] > (0xFF - kThreshold) );

		assertTrue("bottom middle part should fade down to white", blues[4] > blues[5]);
		assertTrue("bottom middle part should fade down to white", blues[5] > blues[6]);
		assertTrue("bottom part, pixel 7 should fade down to white", blues[6] > blues[7]);

		assertTrue("bottom part, pixel 7 should be almost completely clear", blues[7] < kThreshold);
		assertTrue("bottom part, pixel 8 should be almost completely clear", blues[8] < kThreshold);

		sg.dispose();
	}


	// boilerplate below
	
	public static Test suite() {
		return new TestSuite(InterpolationTest.class);
	}

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
