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
 * @summary Test the positioning of complex strokes. 
 * @summary <rdar://problem/4732981> [JavaJDK15] 1.5 [clone for Leopard]- SAP: drawing problem using BasicStroke
 * @summary com.apple.junit.java.graphics.primitives
 */

import junit.framework.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DrawRectWithComplexStroke  extends TestCase {
    public void testCopyArea() throws Exception {
        GraphicsEnvironment environment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice device =
                environment.getDefaultScreenDevice();

        GraphicsConfiguration config = device.getDefaultConfiguration();

        // Create an image that does not support transparency (Opaque)
        BufferedImage bi = config.createCompatibleImage(500, 500, Transparency.OPAQUE);

        Graphics2D big = bi.createGraphics();

        big.setColor(Color.white);
        big.fillRect(0, 0, 1000, 1000);

        big.setColor(Color.red);

        Rectangle2D r = new Rectangle2D.Double(10, 10, 20, 20);
        Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);

        big.setStroke(stroke);
        big.draw(r);

        int testPixel = bi.getRGB(15, 10);
        int redPixel = Color.red.getRGB();
        assertEquals("The line of the rectangle should be red", testPixel, redPixel);

    }

    public static Test suite() {
        return new TestSuite(DrawRectWithComplexStroke.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
