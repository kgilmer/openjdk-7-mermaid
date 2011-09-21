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
 * @summary <rdar://problem/4756727> [JavaJDK16] Drawing text first with a custom composite might break
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build BITestUtils
 * @run main CopyAreaOffScreen
 */

import test.java.awt.regtesthelpers.BITestUtils;
import junit.framework.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CopyAreaOffScreen extends TestCase {
    int w = 50, h = 50;
    int x1 = 100, y1 = 100;
    int dx = 50, dy = 0;

    public void testCopyArea() throws Exception {
        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice device = environment.getDefaultScreenDevice();

        GraphicsConfiguration config = device.getDefaultConfiguration();

        // Create an image that does not support transparency (Opaque)
        BufferedImage bi = config.createCompatibleImage(500, 500, Transparency.OPAQUE);

        Graphics2D big = bi.createGraphics();

        big.setColor(Color.red);
        big.fillRect(0, 0, 1000, 1000);

        big.setColor(Color.green);
        big.fillRect(x1, y1, w, h);

        big.setColor(Color.blue);
        big.fillRect(x1+dx, y1+dy, w, h);

        big.copyArea(x1, y1, w, h, dx, dy);

        Point origPixel = new Point(x1 + 10, y1 + 10);
        Point copiedPixel = new Point(x1 + dx + 10, y1 + dy + 10);
        
        int greenPixel = bi.getRGB(origPixel.x, origPixel.y);
        int copiedGreenPixel = bi.getRGB(copiedPixel.x, copiedPixel.y);
        
        // human readable messages
        String c1msg = "@(" + origPixel.x + "," + origPixel.y + ") : " + BITestUtils.Hex(greenPixel);
        String c2msg = "@(" + copiedPixel.x + "," + copiedPixel.y + ") : " + BITestUtils.Hex(copiedGreenPixel);
        assertEquals( "Colors " + c1msg + " and " + c2msg + " should match", greenPixel, copiedGreenPixel);         
    }

    public static Test suite() {
        return new TestSuite(CopyAreaOffScreen.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void setUp() {
        new MyFrame("CopyArea");
    }

    protected void tearDown() {
        frame.dispose();
    }

    MyFrame frame;

    class MyFrame extends Frame {
        Robot robot;

        public MyFrame(String str) {
            super(str);

            frame = this;

            init();
        }

        public void init() {
            setSize(400, 400);
            setVisible(true);
        }

        public void paint(Graphics g) {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

            GraphicsDevice device = environment.getDefaultScreenDevice();

            GraphicsConfiguration config = device.getDefaultConfiguration();

            // Create an image that does not support transparency (Opaque)
            BufferedImage bi = config.createCompatibleImage(500, 500, Transparency.OPAQUE);

            Graphics2D big = bi.createGraphics();

            big.setColor(Color.red);
            big.fillRect(0, 0, 1000, 1000);

            big.setColor(Color.green);
            big.fillRect(x1, y1, w, h);

            big.setColor(Color.blue);
            big.fillRect(x1+dx, y1+dy, w, h);

            big.copyArea(x1, y1, w, h, dx, dy);

            g.drawImage(bi, 0, 0, null);
        }
    }
}