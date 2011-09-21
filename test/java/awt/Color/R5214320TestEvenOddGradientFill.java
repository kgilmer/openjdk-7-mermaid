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
 * @summary Filling Java GeneralPath shapes using the even-odd winding rule doesn't work correctly when a GradientPaint or TexturePaint is used.
 * @summary com.apple.junit.java.graphics.color
 * @library ../regtesthelpers
 * @build VisibilityValidator
 * @run main R5214320TestEvenOddGradientFill
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/*
 * Filling Java GeneralPath shapes using the even-odd winding rule doesn't work correctly when a GradientPaint or TexturePaint is used.
 *
 * Steps to Reproduce
 *
 * The following code generates a Java GeneralPath consisting of a outer rectangle (clockwise) and an inner rectangle (also clockwise), using the even-odd winding rule.
 * 
 * GeneralPath gp = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
 * gp.append(new Rectangle(0, 0, 200, 200), false);
 * gp.closePath();
 * gp.append(new Rectangle(50, 50, 100, 100), false);
 * gp.closePath();
 * 
 * g2d.setPaint(new GradientPaint(0f, 0f, Color.BLUE, 200f, 200f, Color.YELLOW));
 * g2d.fill(gp);
 * 
 * gp.transform(AffineTransform.getTranslateInstance(200, 0));
 * g2d.setPaint(Color.ORANGE);
 * g2d.fill(gp);
 * 
 * Expected Result
 * 
 * The filled shapes should look like square donuts - i.e., a square shape with a square hole in the center.
 * 
 * Actual Result
 * 
 * When filled using a solid color, the outer loop is filled and the inner loop is empty (as it should be).  When filled using a GradientPaint or TexturePaint, the inner rectangle is also filled (which is incorrect).
 * 
 * Regression
 * 
 * This code works correctly under Windows.
 */
public class R5214320TestEvenOddGradientFill extends TestCase {
    private final static int INNER_LENGTH = 100;
    private final static int OUTER_LENGTH = 200;
    private final static int INNER_X = (OUTER_LENGTH - INNER_LENGTH)/2;
    private final static int INNER_Y = INNER_X;
    private final static Color FRAME_BACKGROUND = Color.white;

    private JFrame frame = null;
    private Robot robot;

    protected void setUp() throws Exception {
        if (robot == null) {
            robot = new Robot();
        }

        frame = new JFrame("TestEventOdd");
        frame.setBackground(FRAME_BACKGROUND);
        frame.getContentPane().add(new EvenOddFill());
        frame.setSize(3*OUTER_LENGTH, OUTER_LENGTH+20);
    }
    
    private static class EvenOddFill extends Panel {
        
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D)g;
            
            GeneralPath gp = new GeneralPath(Path2D.WIND_EVEN_ODD);
            gp.append(new Rectangle(0, 0, OUTER_LENGTH, OUTER_LENGTH), false);
            gp.closePath();
            gp.append(new Rectangle(INNER_X, INNER_Y, INNER_LENGTH, INNER_LENGTH), false);
            gp.closePath();

            g2d.setPaint(new GradientPaint(0f, 0f, Color.BLUE, 200f, 200f, Color.YELLOW));
            g2d.fill(gp);

            gp.transform(AffineTransform.getTranslateInstance(OUTER_LENGTH, 0));
            BufferedImage bi = new BufferedImage(2,2,BufferedImage.TYPE_INT_RGB);
            bi.setRGB(0, 0, 0xffffffff); bi.setRGB(1, 0, 0xffffffff);
            bi.setRGB(0, 1, 0xffffffff); bi.setRGB(1, 1, 0xff0000ff);
            TexturePaint bluedots = new TexturePaint(bi,new Rectangle(0,0,2,2));
            g2d.setPaint(bluedots);
            g2d.fill(gp);

            gp.transform(AffineTransform.getTranslateInstance(OUTER_LENGTH, 0));
            g2d.setPaint(Color.ORANGE);
            g2d.fill(gp);
        }
    }

    public void testEvenOddFill() throws Exception {
        VisibilityValidator.setVisibleAndConfirm(frame);
        Thread.sleep(125); // Let humans see it too...
        Point loc = frame.getLocationOnScreen();
        loc.x = loc.x + OUTER_LENGTH/2;
        loc.y = loc.y + OUTER_LENGTH/2;
        robot.mouseMove(loc.x, loc.y);
        //System.out.println("robot.getPixelColor("+loc.x+", "+loc.y+"): "+robot.getPixelColor(loc.x, loc.y));
        assertTrue("Timed out without seeing a square donut for the first even-odd fill", VisibilityValidator.waitForColor(frame, loc.x, loc.y, FRAME_BACKGROUND));
    }

    protected void tearDown() {
           frame.dispose();
        }


    public static Test suite() {
        return new TestSuite(R5214320TestEvenOddGradientFill.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
