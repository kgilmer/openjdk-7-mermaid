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
 * @summary Paints a couple of animated polygons using repaint.
 * @summary <rdar://problem/4939642> [JavaJDK15] g.fillPolygon crashes JVM under XORMode
 * @summary com.apple.junit.java.graphics.images
 * @library ../../regtesthelpers
 * @build MovingPoints
 * @run main Polys02
 */

import test.java.awt.regtesthelpers.MovingPoints;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Timer;


public class Polys02 extends TestCase {
    static boolean TESTXOR = false;
    static boolean RUNLONG = false;
    static final Random rand = new Random( 0x20406 );
    Timer ticktock = new Timer();
    
    //
    //  Constants controlling the scene
    //
    static Rectangle rect = new Rectangle( 40, 40, 350, 400 );
    static final int SLOP = 120;
    static final int kPolys = 5;
 
    //
    // An animator class so the scene is vaguely interesting
    //
    abstract protected class ActivePolygon extends MovingPoints {
        protected Color color = null;
        // Some initial conditions

        public ActivePolygon(Rectangle r) {
            super(r, Math.max(3, rand.nextInt(5) + rand.nextInt(5) + rand.nextInt(5)));
            color = new Color(rand.nextInt(0xFF), rand.nextInt(0xFF), rand.nextInt(0xFF));
        }

        abstract public void render(Graphics g);
    }

    //
    // A polygon that draws via setColor
    //
    class NormalPolygon extends ActivePolygon {

        public NormalPolygon(Rectangle r) {
            super(r);
        }

        public void render(Graphics g) {
            g.setColor(color);
            g.fillPolygon(new Polygon(getXs(), getYs(), getNumVertices()));
        }
    }

    //
    // A polygon that draws via setXORMode
    //
    class XorPolygon extends ActivePolygon {

        public XorPolygon(Rectangle r) {
            super(r);
        }

        public void render(Graphics g) {
            g.setXORMode(color);
            g.fillPolygon(new Polygon(getXs(), getYs(), getNumVertices()));
        }
    }

    /*
    */
 
    class AnimatedWindow extends Frame {
        volatile   ActivePolygon[] polys = new ActivePolygon[kPolys];
        Rectangle rect = null;
        TimerTask renderer;

        public AnimatedWindow( Rectangle rect ) throws Exception {
            super( "AnimatingFrame" );
            this.rect = rect;
            setBounds( rect );

            int peX = -SLOP;
            int peY = -SLOP;
            int peW = rect.width+SLOP;
            int peH = rect.height+SLOP;
            Rectangle polyedges = new Rectangle(  peX, peY, peW, peH);
            for (int i = 0; i<polys.length; i++ ) {
                if (TESTXOR && i % 2 == 0) {
                    polys[i] = new XorPolygon( polyedges );
                }
                else {
                    polys[i] = new NormalPolygon( polyedges );
                }
            }

            setVisible(true);

            // Simple animation into a cached graphics object
            renderer = new TimerTask() {
                public void run() {
                    AnimatedWindow.this.animateOnce();
                    AnimatedWindow.this.repaint();
                }
            };
            ticktock.schedule(renderer, 0L, 50L);
        }

        public void animateOnce() {
            for (ActivePolygon item : polys) {
                item.move();
            }           
        }

        public void paint( Graphics gg ) {
            super.paint(gg );
            gg.setColor(Color.cyan);
            gg.fillRect(0, 0, rect.width, rect.height);
            for (ActivePolygon item : polys) {
                item.render(gg);
            }           

        }
    }
    
     volatile boolean done = false;

    public void testPoly() throws Exception {
        final AnimatedWindow testWindow = new AnimatedWindow(rect);

        TimerTask stopper = new TimerTask() {
            public void run() {
                done = true;
            }
        };
        
        if (!RUNLONG) {
            ticktock.schedule(stopper, 1250L);
        }
        else {
            ticktock.schedule(stopper, 30000L);
        }
        
        while( done == false) {
            Thread.sleep(250);
        }

        testWindow.renderer.cancel();
        ticktock.cancel();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                testWindow.dispose();
            }
        });
    }

    public static Test suite() {
        return new TestSuite( Polys02.class);
    }

    public static void main( String[] args ) {
        for (String arg : args) {
            if (arg.toLowerCase().equals("-testxor")) {
                TESTXOR=true;
            }
            if (arg.toLowerCase().equals("-runlong")) {
                RUNLONG=true;
            }                               
        }
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
}
        


