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
 * @summary  <rdar://problem/4122177> [JavaJDK15] Font bounds returned by TextLayout significantly off.
 * @library ../../../regtesthelpers
 * @build VisibilityValidator
 * @summary com.apple.junit.java.text.TextBounds;
 */

/**
 *
 * Paints a series of red strings, overwrites them with a 1/2 alpha blue wash, then looks for bright red 
 * uncovered pixels.
 *
 **/

// classes necessary for this test

import junit.framework.*;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class Bounds01 extends TestCase {
    static final int WIDTH  = 600;
    static final int HEIGHT = 200;

    static final int slop = 1;    
    static final int sizes[] = { 9, 12, 18, 37 };    // Use reduced set to make the test run fast

    static final int styles[] = { Font.PLAIN, Font.BOLD  };
    static final String names[] = { "Serif", "Helvetica", "DIALOG"};

    protected    Vector<Font> fonts;
    protected    BufferedImage bi;
    protected    Waypoint painted = new Waypoint();
    protected    Robot robot;
    
    class TestFrame extends Frame
    {
        static final long serialVersionUID = 0;
        Image img = null;        

        public TestFrame(Image img) {
            this.img = img;
            setUndecorated(true);
            setBounds( 50, 50, Bounds01.WIDTH, Bounds01.HEIGHT);
        }

        public void paint( Graphics g ) {
            super.paint( g );
            g.setColor( Color.black );
            if (img != null) {
                g.drawImage( img, 0, 0, this );
            }
            else {
                g.drawString( "null image", 30, 30);
            }
            painted.clear();            
        }
    }

    protected void setUp() throws Exception{
        // this is to wake the screen out of sleep
        robot = new Robot();
        robot.mouseMove(10, 10);
        robot.mouseMove(0, 0);
        Thread.sleep(1000);
        
        fonts = new Vector<Font>();
        for (String name : names) {
            for (int size : sizes) {
                for (int style : styles) {
                    Font font = new Font(name, style, size);
                    assertNotNull(font);
                    fonts.add(font);
                }
            }
        }

        // Get the default (or compatible) buffered image
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        bi = gc.createCompatibleImage( WIDTH,HEIGHT );
    }

    public void testFontBounds() throws Exception {

        for (Font font : fonts) {

            Graphics2D ig = (Graphics2D) bi.getGraphics();

            // Use the most percise mode available
            RenderingHints hints = ig.getRenderingHints();
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            ig.setRenderingHints(hints);

            ig.setColor(Color.black);
            ig.fillRect(0, 0, Bounds01.WIDTH, Bounds01.HEIGHT);
            
            String testString = "Covered by bounds rect +1?";

            // Draw centered and a little in from the left
            Point2D.Float loc = new Point2D.Float(20, Bounds01.HEIGHT / 2);

            FontRenderContext frc = ig.getFontRenderContext();
            assertNotNull(frc);

            TextLayout layout = new TextLayout(testString, font, frc);
            assertNotNull(layout);

            Rectangle2D cover1 = layout.getBounds();
            assertNotNull(cover1);

            // cutting the cover rect a little slack, since anti-aliasing and fractional text
            // can account for off-by-one errors which over/under shoot the bounding box

            cover1.setRect(cover1.getX() + loc.getX() - slop,
                    cover1.getY() + loc.getY() - slop,
                    cover1.getWidth() + (slop * 2),
                    cover1.getHeight() + (slop * 2));

            // Draw test string in red
            ig.setColor(Color.red);
            layout.draw(ig, (float) loc.getX(), (float) loc.getY());

            // cover1 with blue bounding box
            ig.setColor(new Color(0, 0, 0xFF, 0x80));
            ig.fill(cover1);
            ig.dispose();

            TestFrame frame = new TestFrame(bi);
            painted.reset();
            try {
                // Bring the window up and make sure at least one paint has occured
                VisibilityValidator.setVisibleAndConfirm(frame);
                painted.requireClear();
                assertTrue("Did not get a paint call", painted.isValid());

                // Slow machines have a slight delay before the window is truely visible
                // We need time for the tick-flusher to go
                Thread.sleep(150); 

                // Grab the area where text should be and look for bright red pixels.
                Rectangle rect = new Rectangle((int) cover1.getX() - 10 + frame.getX(), (int) cover1.getY() - 10 + frame.getY(), (int) cover1.getWidth() + 20, (int) cover1.getHeight() + 20);
                BufferedImage image = (new Robot()).createScreenCapture(rect);

                boolean someRed = false;
                boolean pixelsOK = true;
                int sizeX = image.getWidth();
                int sizeY = image.getHeight();

                File f = null;
                String failurefile = "Bounds01_" + System.currentTimeMillis() + font;
                for (int x = 0; x < sizeX; x++) {
                    for (int y = 0; y < sizeY; y++) {
                        Color c = new Color(image.getRGB(x, y));
                        boolean pixelOK = c.getRed() < 255;

                        if (!pixelOK) {
                            pixelsOK = false;
                        }

                        if (c.getRed() > 0) {
                            someRed = true;
                        }
                    }
                }
                
                if (!pixelsOK) {
                    // Dump the screen
                    f = RobotUtilities.screenshot(failurefile, new File(System.getProperty("java.io.tmpdir")));
                    assertNotNull("Problem creating error file", f);

                    // Dump just the smaller (original) image we checked
                    java.io.File outputFile1 = new File(System.getProperty("java.io.tmpdir"), failurefile + "Image.png");
                    javax.imageio.ImageIO.write(image, "png", outputFile1);

                    // Dump the image we checked in Hex, too
                    BITestUtils.pixelLogfileHex(image, failurefile + "Hex");                    
                }

                assertTrue(font + "apparently did not paint at all.  Probable error in testcase.", someRed);
                assertTrue(font + "not covered.  See file " + failurefile, pixelsOK);

            }
            finally {
                frame.dispose();
            }
        }

    }

    // Boilerplate
    public static Test suite() {
        return new TestSuite(Bounds01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        String name = System.getProperty("os.name");
        if (name.equals("Mac OS X")) {
            // This test uses a font that may not exist on other platforms
            TestResult tr = junit.textui.TestRunner.run(suite());
            if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
                throw new RuntimeException("### Unexpected JUnit errors or failures.");
            }
        }
    }
}

