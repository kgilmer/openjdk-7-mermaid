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

/**
 @test
 @summary 
 @summary com.apple.junit.java.util;
 @run main CollectionStress01
 */

/*

    This test generates a field of boolean values, then uses recursive checks to
    attempt to find a minimal set of rectangles that contain only the false values.
    
    Puts a fair bit of stress on the collection classes.

*/

import junit.framework.*;

import java.awt.*;
import java.util.*;


public class CollectionStress01 extends TestCase {
    
    static final boolean kDebug = false;
    static final int kSeed  = 12225;
    static final int kPaintThreshold = 50000;
    static final int kPauseTime = 5;
    static final int kBooleanDensity  = 25;
    static final int kWidth  = 32;
    static final int kHeight = 36;
    static final int kScale = 18;
    static final Rectangle kBoolRect = new Rectangle( 0, 0, kWidth, kHeight);
    static final Rectangle kDisplayRect = new Rectangle( 0, 0, kWidth*kScale, kHeight*kScale);

    boolean[][] boolSpace = new boolean[kWidth][kHeight];
    
    Frame displayFrame = null;
    DisplayArea displayArea = new DisplayArea();
    Random rand = new Random(kSeed);
    
    HashMap<Rectangle,Vector<Rectangle>> cache = new HashMap<Rectangle,Vector<Rectangle>> ();
    
    class DisplayArea extends Panel {
        private static final long serialVersionUID = 1L;
        Vector<Rectangle> currEmpties;
        Vector<Rectangle> debugCases;
        String debugInfo;
        Rectangle currRect;
        Point    currPoint;
        
        public Dimension getPreferredSize() {
            return ( new Dimension(kDisplayRect.width, kDisplayRect.height) );
        }
        
        public void paint( Graphics g){
            super.paint(g);
            g.clearRect(0,0, kDisplayRect.width, kDisplayRect.height);
            
            // representation of boolean field
            g.setColor( Color.RED );
            for(int i = 0; i < boolSpace.length; i+=1) {
                for(int ii = 0; ii < boolSpace[i].length; ii+=1) {
                    if (boolSpace[i][ii]) {
                        g.fillRect(i*kScale, ii*kScale, kScale, kScale);
                    }
                }
            }
            
            // draw the currPoint
            g.setColor( Color.BLUE );
            Point p = null;
            synchronized (this) {
                p = currPoint;
            }
            if (p != null) {
                g.fillRect(p.x*kScale, p.y*kScale, kScale, kScale);
            }
            
            // draw the debugCases
            g.setColor( new Color( 0x40, 0xFF, 0x40, 0x80 ));
            Vector<Rectangle> dVect = null;
            synchronized (this) {
                dVect = debugCases; 
            }
            if (dVect != null) {
                for (Rectangle e : dVect) {
                    g.fillRoundRect((e.x*kScale)+1, (e.y*kScale)+1, (e.width*kScale)-2, (e.height*kScale) -2 , 15, 15);
                }                
            }
            
            // draw the currEmpties
            g.setColor( Color.CYAN);
            Vector<Rectangle> eVect = null;
            synchronized (this) {
                eVect = currEmpties; 
            }
            if (eVect != null) {
                for (Rectangle e : eVect) {
                    g.fillRoundRect((e.x*kScale)+1, (e.y*kScale)+1, (e.width*kScale)-2, (e.height*kScale) -2 , 15, 15);
                }
            }
            
            // draw the currRect
            g.setColor( Color.BLACK );
            Rectangle r = null;
            synchronized (this) {
                r = currRect;
            }
            if (r != null) {
                g.drawRect(r.x*kScale+2, r.y*kScale+2, r.width*kScale-4, r.height*kScale-4);
            }
            
            // draw debugInfo at currPoint
            g.setColor(Color.BLACK);
            String d = null;
            synchronized (this) {
                d = debugInfo;
            }
            if (d != null) {
                if ( p != null ) {
                    g.drawString(d, p.x*kScale, p.y*kScale+10);
                }
                else {
                    g.drawString(d, 5, 15);            
                }
            }
        }
        
        int paintCount = 0;
        void tickle() {
            if (paintCount > kPaintThreshold) {
                paintCount = 0;
                repaint();
                try {
                    Thread.sleep(kPauseTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                paintCount +=1;
            }
            
        }
        
        // setters which also (occasionally) redisplay
        public void setCurrEmpties(Vector<Rectangle> currEmpties) {
            synchronized (this) {
                this.currEmpties = currEmpties;
            }
            tickle();
        }
        
        public void setCurrPoint(Point currPoint) {
            synchronized (this) {
                this.currPoint = currPoint;
            }
            tickle();
        }
        
        public void setCurrRect(Rectangle currRect) {
            synchronized (this) {
                this.currRect = currRect;
            }
            tickle();
        }
        
        public void setDebugCases(Vector<Rectangle> debugCases) {
            synchronized (this) {
                this.debugCases = debugCases;
            }
            tickle();
        }
        
        public void setDebugInfo(String debugInfo) {
            synchronized (this) {
                this.debugInfo = debugInfo;
            }
            tickle();
        }
        
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        displayFrame = new Frame();
        displayFrame.setLocation(10,10);
        displayFrame.add( displayArea );
        displayFrame.pack();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        displayFrame.dispose();
    }

    public void xxtestSplit() throws Exception {
        init();
        
        assertNotNull(displayFrame);
        displayFrame.setVisible(true);
        
        
        for (int i = 0; i < 200; i+=1) {
            int ww  = rand.nextInt(1+kBoolRect.width);
            int hh = rand.nextInt(1+kBoolRect.height);
            int xx = rand.nextInt(1+kBoolRect.width-ww);
            int yy = rand.nextInt(1+kBoolRect.height-hh);
            
            Rectangle probe = new Rectangle(xx, yy, ww, hh);
            displayArea.setCurrRect(probe);

            Point p = split(probe);
            displayArea.setCurrPoint(p);
            displayArea.repaint();
            // Thread.sleep(1250);
    }

        Thread.sleep(125);
    }
    
    public void testSubRects() throws Exception {
        init();
        
        assertNotNull(displayFrame);
        // displayFrame.setVisible(true);
        
        for (int i = 0; i < 10; i+=1) {
            int x = kBoolRect.x + rand.nextInt( kBoolRect.width);
            int y = kBoolRect.y + rand.nextInt( kBoolRect.height);

            Point p = new Point(x,y);
            displayArea.setCurrPoint(p);
            for (int kind =0; kind< 8; kind++) {
                Vector<Rectangle> caseRects = divideRectForSubcase(kBoolRect, p, kind);
                displayArea.setDebugInfo( kind + " " + caseRects.size() );
                displayArea.setDebugCases(caseRects);
            }
        }
        
        displayArea.setCurrRect(kBoolRect);
        displayArea.setDebugCases(null);

        Thread.sleep(125);
    }
    
    public void testRecursiveStress() throws Exception {
        init();

        assertNotNull(displayFrame);
        displayFrame.setVisible(true);
        
        Vector<Rectangle> answer = findEmptyRects(kBoolRect, 0);
        assertTrue( answer.size() < (kBoolRect.width * kBoolRect.height) );
    
        //System.out.println(answer.size());
        
        displayArea.setCurrPoint(null);
        displayArea.setCurrRect(kBoolRect);
        displayArea.setDebugCases(null);
        displayArea.setCurrEmpties(answer);
        displayArea.setDebugInfo( ""+ answer.size() );
        displayArea.repaint();

        Thread.sleep(125);
    }
    
    // initialize the boolean field to random values, and load 1,1 empties into cache
    void init() {
        for(int i = 0; i < boolSpace.length; i+=1) {
            for(int ii = 0; ii < boolSpace[i].length; ii+=1) {
                boolean val = (rand.nextInt(100)<= kBooleanDensity);
                boolSpace[i][ii] = val;
                
                if (!val) {
                    Rectangle r = new Rectangle(i,ii,1,1);
                    Vector<Rectangle> v = new Vector<Rectangle>();
                    v.add(r);
                    cache.put(r, v);
                }
            }
        }
    }
    
    // text display of boolean field
    void displayRaw() {
        System.out.println();
        for(int i = 0; i < boolSpace.length; i+=1) {
            for(int ii = 0; ii < boolSpace[i].length; ii+=1) {
                System.out.print( boolSpace[i][ii] ? " " : "X" );
            }
            System.out.println();
        }
    }
    
    
    // return an OK fracture point, or null if rectangle is empty
    // algorithm is correct for any fracture point, but will converge faster for central points
    Point split(Rectangle r) {
        Point result = null;
        
        int midx = r.x + r.width/2;
        int midy = r.y + r.height/2;
        int score = Integer.MAX_VALUE;
    
        for(int xoff = 0; (xoff < r.width); xoff+=1) {
            for(int yoff = 0; (yoff < r.height); yoff+=1) {
                int x = r.x + xoff;
                int y = r.y + yoff;
                displayArea.setCurrPoint( new Point(x,y));

                if (boolSpace[x][y] ) {    
                    int newscore = (x-midx)*(x-midx)+(y-midy)*(y-midy);
                    if (newscore < score) {    
                        score = newscore;
                        result = new Point(x,y);
                    }
                }
            }
        }
        
        return result;
    }
    
    Vector<Rectangle> findEmptyRects( Rectangle r, int depth) throws Exception {
        debug( depth, "findEmptyRects " + show(r));
        displayArea.setCurrEmpties(null);
        displayArea.setCurrRect(r);
    
        if (r.width <= 0 || r.height <= 0) {
            debug( depth, "Unexpected rectagle :" + r);
            return null ;
        }
        
        if ( cache.containsKey(r) ) {
            debug( depth, "C :" + show(r) + "--" + show( cache.get(r)));
            return cache.get(r);
        }
        
        if (r.width == 1 && r.height == 1) {
            debug( depth, "F :" + show(r) + "--");
            assertTrue( boolSpace[r.x][r.y]);
            return null;
        }

        // figure it out recursively
        Vector<Rectangle> empties = new Vector<Rectangle>(); 
        
        Point p = split( r );
        displayArea.setCurrPoint(p);
        
        if (p == null) {
            debug( depth, "S :" + show(r) + "--" + show(r));
            empties.add(r);
        }
        else {
            debug( depth, "point :" + p.x + " "+ p.y);

            Vector<Rectangle> bestEmpties = null;
            // iterate through the 8 possible cases
            for (int kind =0; kind< 8; kind++) {
                debug( depth, "case :" + kind);
                Vector<Rectangle> caseRects = divideRectForSubcase(r, p, kind);
                displayArea.setCurrRect(r);
                displayArea.setCurrEmpties(null);
                displayArea.setDebugInfo(""+caseRects.size());
                displayArea.setDebugCases(caseRects);
            
                Vector<Rectangle> caseEmpties = new Vector<Rectangle>();
                for ( Rectangle caseRect : caseRects) { 
                    Vector<Rectangle> subEmpties = findEmptyRects( caseRect, depth+1 );
                    if (subEmpties != null) {
                        caseEmpties.addAll(subEmpties);
                    }
                }
                displayArea.setCurrEmpties(caseEmpties);
                debug( depth, "totals :" + caseEmpties.size());
                
                
                if ( (bestEmpties == null) || (bestEmpties.size() > caseEmpties.size())) {
                    debug( depth, "new best! :" + caseEmpties.size());
                    displayArea.setCurrEmpties(caseEmpties);
                    bestEmpties = caseEmpties;
                }
                
                if (caseRects.size() <= 2) {
                    break; // simple case, so exit early
                }
            }

            debug(depth, "R : " + r + "--"+show(bestEmpties));
            empties = bestEmpties;

        }
        
        cache.put(r, empties);
        return empties;    
    }
    
    
    Vector<Rectangle> divideRectForSubcase(Rectangle r, Point p, int i ) {
        Vector<Rectangle> fullCase = new Vector<Rectangle>();
        
        int topHeight = p.y - r.y; 
        int botHeight = (r.height - topHeight) -1; 
        int leftWidth = p.x - r.x; 
        int rightWidth = (r.width - leftWidth) -1;
        
        switch(i) {
        case 0:
            fullCase.add(new Rectangle( r.x, r.y, r.width, topHeight));
            fullCase.add(new Rectangle( r.x, p.y+1, r.width, botHeight));
            fullCase.add(new Rectangle( r.x, p.y, leftWidth, 1));
            fullCase.add(new Rectangle( p.x+1, p.y, rightWidth, 1));
            break;
        case 1:             
            fullCase.add(new Rectangle( r.x, r.y, leftWidth, r.height));
            fullCase.add(new Rectangle( p.x, r.y, 1, topHeight));
            fullCase.add(new Rectangle( p.x+1, r.y, rightWidth, r.height));
            fullCase.add(new Rectangle( p.x, p.y+1, 1, botHeight));
            break;
        case 2:             
            fullCase.add(new Rectangle( r.x, r.y, leftWidth, topHeight+1));
            fullCase.add(new Rectangle( p.x, r.y, 1, topHeight));
            fullCase.add(new Rectangle( r.x, p.y+1, r.width, botHeight));
            fullCase.add(new Rectangle( p.x+1, r.y, rightWidth, topHeight+1));
            break;
        case 3:
            fullCase.add(new Rectangle( r.x, r.y, leftWidth, r.height));
            fullCase.add(new Rectangle( p.x, r.y, rightWidth+1, topHeight));
            fullCase.add(new Rectangle( p.x, p.y+1, rightWidth+1, botHeight));
            fullCase.add(new Rectangle( p.x+1, p.y, rightWidth, 1));
            break;
        case 4: 
            fullCase.add(new Rectangle( r.x, r.y, r.width, topHeight));
            fullCase.add(new Rectangle( r.x, p.y, leftWidth, botHeight+1));
            fullCase.add(new Rectangle( p.x, p.y+1, 1, botHeight));
            fullCase.add(new Rectangle( p.x+1, p.y, rightWidth, botHeight+1));
            break;
        case 5:             
            fullCase.add(new Rectangle( r.x, r.y, leftWidth+1, topHeight));
            fullCase.add(new Rectangle( r.x, p.y, leftWidth, 1));
            fullCase.add(new Rectangle( r.x, p.y+1, leftWidth+1, botHeight));
            fullCase.add(new Rectangle( p.x+1, r.y, rightWidth, r.height));
            break;
        case 6:             
            fullCase.add(new Rectangle( r.x, r.y, leftWidth+1, topHeight));
            fullCase.add(new Rectangle( r.x, p.y, leftWidth, botHeight+1));
            fullCase.add(new Rectangle( p.x, p.y+1, rightWidth+1, botHeight));
            fullCase.add(new Rectangle( p.x+1, r.y, rightWidth, topHeight+1));
            break;
        case 7:             
            fullCase.add(new Rectangle( r.x, r.y, leftWidth, topHeight+1));
            fullCase.add(new Rectangle( r.x, p.y+1, leftWidth+1, botHeight));
            fullCase.add(new Rectangle( p.x, r.y, rightWidth+1, topHeight));
            fullCase.add(new Rectangle( p.x+1, p.y, rightWidth, botHeight+1));
            break;
        /* ### note that there are 8! more cases.... ### */    
            
        default:
            System.out.println("Unexpected case!"); break;
        } 
        
        
        Vector<Rectangle> result = new Vector<Rectangle>();
        for (Rectangle rr : fullCase ) {
            if (rr.width > 0 && rr.height > 0) {
                result.add(rr);
            }
        }
        return result;
        
    }
    
    // debugging utilities
    static void debug( int depth, String s) {
        if (kDebug) {
            for(int i= 0; i<depth; i+=1) {
                System.out.print("\t");
                System.out.println(s);
            }
        }
    }
    
    static String show( Rectangle r) {
        return "["+r.x+","+r.y+" "+r.width+"x"+r.height+"]";
    }

    static String show( Vector<Rectangle> rects) {
        String result = "" + rects.size() + " ";
        for( Rectangle r : rects) {
            result = result + " " +show(r);
        }
        return result;
    }


    // boilerplate for running as a JUnit test
    public static Test suite() {
        return new TestSuite(CollectionStress01.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

