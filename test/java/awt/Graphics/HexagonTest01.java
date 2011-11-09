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
 * @summary Test to insure a crash case does not regress.
 * @summary com.apple.junit.java.graphics.Polygons
 */

import junit.framework.*;
import java.awt.*;

public class HexagonTest01 extends TestCase {

    static final int kTimeOutMillis = 400;

    class Hexagon extends Polygon {
        private void init(final int x, final int y, final int size) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI/3) {
                int px = (int) (x + size * Math.cos(theta));
                int py = (int) (y + size * Math.sin(theta));
                addPoint(px, py);
            }
        }
    
        public Hexagon(final int x, final int y, final int size) {
            super();
            init(x,y,size);
        }
    
        public Hexagon(final Point origin, final int size) {
            super();
            init((int) origin.getX(), (int) origin.getY(), size);
        }
    }


    class HexGrid {
        Hexagon[][] grid;
    
        public HexGrid(int width, int height, int radius) {
            grid = new Hexagon[width][height];
            double apothem = radius * Math.sqrt(3) / 2;
    
            for (int xi = 0; xi < width; xi++) {
                for (int yi = 0; yi < height; yi++) {
                    double xoff = (xi * (3 * radius));
                    double yoff = (yi * (apothem));         
                    if (yi % 2 == 1) { 
                        xoff += 1.5 * radius;
                    }
                    grid[xi][yi] = new Hexagon((int)xoff , (int) yoff, radius);
                }
            }
        }
    
        public void draw( Graphics2D g) {
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    g.setColor( new Color( 255, (i * 20)  % 255, (j * 15) % 255) );
                    g.fill( grid[i][j]);
                    g.setColor(Color.black);
                    g.draw( grid[i][j]);
                }       
            }       
        }
    }

    class DisplayFrame extends Frame {
        public DisplayFrame() {
            super("Test");
            setSize(700, 600);
        }
    
        public void paint( Graphics g) {
            Graphics2D
            g2d = (Graphics2D) g;           
            g.setColor(Color.white);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.translate(75, 75);
            g2d.setColor(Color.black);
            g2d.setStroke(stroke);
            grid.draw(g2d); }
    }

    final BasicStroke stroke = new BasicStroke( 1.2f);
    final HexGrid grid = new HexGrid(4,12,50);
    private DisplayFrame testFrame;

    public void testThatIDontCrash() throws Exception {
        testFrame = new DisplayFrame();
        testFrame.setVisible(true);
        Thread.sleep(kTimeOutMillis);
        testFrame.dispose();
    }


    // boilerplate below
    public static Test suite() {
        return new TestSuite(HexagonTest01.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
