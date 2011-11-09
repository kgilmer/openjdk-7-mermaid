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
 * @summary Very simple test of RoundRect functionality.
 * @summary com.apple.junit.java.graphics.primitives
 */
	
import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;


public class FillRoundRect01 extends TestCase {

    public static Test suite() {
        return new TestSuite(FillRoundRect01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

	static final int 		w = 15;
	static final int 		h = 15;
	static final Rectangle	r = new Rectangle(0,0,w,h);

	protected BufferedImage 			bi = null;

    public void setUp() {
		GraphicsEnvironment		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice 			gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration   gc = gd.getDefaultConfiguration();
		bi = gc.createCompatibleImage( w, h );
    }

    public void testFillRoundRect() {
		assertNotNull(bi);
		
		Graphics2D ig = (Graphics2D) bi.createGraphics();
		ig.setColor(Color.red);
		ig.fillRoundRect( 2,2, w-4,h-4, w/2, h/2);
		ig.dispose();
		
		assertTrue("Center of fillRoundRect should be red.", bi.getRGB(w/2,h/2) == 0xFFFF0000 );
	}
}	




