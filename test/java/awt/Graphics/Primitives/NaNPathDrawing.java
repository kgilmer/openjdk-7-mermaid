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
 * @summary <rdar://problem/4330667> [JavaJDK15] SIGBUS error during execution of Graphics2D.fill (NAN float problem)
 * @summary com.apple.junit.java.graphics.primitives
 */

import junit.framework.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.DecimalFormat;

public class NaNPathDrawing extends TestCase {
	static final boolean DEBUG = false;

    public void testCopyArea() throws Exception {
        // we just need to wait so that the frame becomes viisble for the crash in <rdar://problem/4330667> to happen
        Thread.sleep(1000);
    }

    public static Test suite() {
        return new TestSuite(NaNPathDrawing.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void setUp() {
        Problem4330667 p = new Problem4330667();
        p.init();
        frame = new Frame();
        frame.add(p);
        frame.pack();
        frame.setVisible(true);

    }

    protected void tearDown() {
        frame.dispose();
    }

    Frame frame;

    private class Problem4330667 extends JApplet {

        MyPanel myPanel;

        public Problem4330667() {
            myPanel = new MyPanel();
            myPanel.setBackground(Color.gray);
        }

        public void init() {
            // set the default look and feel
            String laf = UIManager.getSystemLookAndFeelClassName();
            try {
                UIManager.setLookAndFeel(laf);
            } catch (UnsupportedLookAndFeelException exc) {
                System.err.println ("Warning: UnsupportedLookAndFeel: " + laf);
            } catch (Exception exc) {
                System.err.println ("Error loading " + laf + ": " + exc);
            }
            getContentPane().setLayout (new BorderLayout());
            myPanel.setLayout(new BorderLayout());
            myPanel.setPreferredSize(new Dimension(600,600));
            myPanel.setMinimumSize(new Dimension(600,600));
            getContentPane().add(myPanel);
            repaint();
        }
    }

    public class MyPanel extends JPanel {

        static final int numberOfArcs = 10;
        static final boolean showBug = true;
        final DecimalFormat fmt = new DecimalFormat("###.##");

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)(g);
            // compute the affine transforms
            Dimension d = getSize(null);
            d = new Dimension(600, 600);
            double r = 1.10;
            Point2D.Double gPx = rescaler( new Point2D.Double(-r,  r                ) ,
                                           new Point2D.Double(0.0, (double)d.width  ) );
            Point2D.Double gPy = rescaler( new Point2D.Double(r,   -r               ) ,
                                           new Point2D.Double(0.0, (double)d.height ) );
            AffineTransform geometryToPanel =
                    new AffineTransform(gPx.x, 0.0, 0.0, gPy.x, gPx.y, gPy.y);
            // set affine transform in graphics
            g2.setTransform(geometryToPanel);
            //g2.setColor(Color.GREEN);
            //drawDot(g2);
            g2.setColor(Color.RED);
            for(int i=0; i<numberOfArcs;i++){
                if (DEBUG) System.out.println(i);
                drawFigure(g2);
                if (DEBUG) System.out.println(".");
            }
        }

        // This computes the tuple (m,c) such that
        // m*from.x + c = to.x
        // m*from.y + c = to.y
        private Point2D.Double rescaler(Point2D.Double from, Point2D.Double to){
            return new Point2D.Double( (to.x-to.y)/(from.x-from.y),
                                       (to.x*from.y - to.y*from.x)/(from.y - from.x));
        }

        void drawDot(Graphics2D g2){
            Shape dot = new Arc2D.Double(-1.0, -1.0, 0.5, 0.5, 0.0, 360.0, Arc2D.OPEN);
            GeneralPath f = new GeneralPath();
            f.append(dot, true);
            g2.fill(f);
        }

        // Thie "figure" is a sort of triangle; the sides are arcs of
        // circles. The circles are required to be orthogonal to the
        // unit circle.
        private void drawFigure(Graphics2D g2) {
            Vector2d
                    vA = randomPosition(),
                    vB = randomPosition(),
                    vC = randomPosition();
            Vector3d wA = toHyperboloid(vA),
                    wB = toHyperboloid(vB),
                    wC = toHyperboloid(vC);
            // To demonstrate the bug, it is necessary to distort the
            // vectors wA, wB, and wC.
            if(showBug){
                double d = 0.25;
                wA.x += d*wA.z;
                wB.x += d*wB.z;
                wC.x += d*wC.z;
            }
            GeneralPath f = new GeneralPath();
            f.append(arc(wA, wB), true);
            f.append(arc(wB, wC), true);
            f.append(arc(wC, wA), true);
            g2.fill(f);
        }

        // This function picks a random point in the unit circle.
        private Vector2d randomPosition() {
            double r2 =  Math.random();
            double r = Math.sqrt(r2);
            double theta = 2*Math.PI*Math.random();
            return new Vector2d(r*Math.cos(theta), r*Math.sin(theta));
        }

        // This function maps a point on the unit disc to a point on the
        // hyperboloid
        private Vector3d toHyperboloid(Vector2d v) {
            double s = 1.0 - v.x*v.x - v.y*v.y;
            return new Vector3d(2*v.x/s, 2*v.y/s, (1.0 + v.x*v.x + v.y*v.y)/s );
        }

        // This function determines an arc from two vectors.  It is designed to
        // give good results if AND ONLY IF the vectors v satisfy the
        // inequality v.x*v.x + v.y*v.y - v.z*v.z < 0.
        private Shape arc(Vector3d v1, Vector3d v2) {
            double nx = v1.y*v2.z - v2.y*v1.z,
                    ny = v1.z*v2.x - v2.z*v1.x,
                    nz = -v1.x*v2.y + v2.x*v1.y;
            double d = Math.sqrt(nx*nx + ny*ny - nz*nz);
            nx /= d; ny /= d; nz /= d;
            double cx = nx/nz;
            double cy = ny/nz;
            double r = 1/Math.abs(nz);
            double v1n = Math.sqrt(v1.z*v1.z - v1.x*v1.x - v1.y*v1.y);
            double v2n = Math.sqrt(v2.z*v2.z - v2.x*v2.x - v2.y*v2.y);
            double p1x = v1.x/(v1n + v1.z), p1y = v1.y/(v1n + v1.z);
            double p2x = v2.x/(v2n + v2.z), p2y = v2.y/(v2n + v2.z);
            double theta1 = Math.toDegrees(Math.atan2(p1y - cy, p1x - cx));
            double theta2 = Math.toDegrees(Math.atan2(p2y - cy, p2x - cx));
            if(theta2 > theta1 + 180.0)theta2 -= 360.0;
            else if (theta2 < theta1 - 180.0) theta2 += 360.0;
            if (DEBUG) System.out.println("cx=" + fmt.format(cx) + ", cy=" + fmt.format(cy) +
                               ", r=" + fmt.format(r) + ", theta1=" + fmt.format(theta1) +
                               ", theta2=" + fmt.format(theta2) );
            Shape s = new Arc2D.Double(cx-r, cy-r, 2*r, 2*r, -theta1, -theta2 + theta1,
                                       Arc2D.OPEN);
            return s;
        }
    }
}