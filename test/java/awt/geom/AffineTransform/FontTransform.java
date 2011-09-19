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
 @summary This tests differences between a font derived by an 0.75, 0.75
 @summary affine transform of a 16 point font and a simple 12 point font.
 @summary Basically, there should be no differences...
 @summary com.apple.junit.java.awt.Font;
 @library ../../regtesthelpers
 @build VisibilityValidator
 @build Waypoint
 @run main FontTransform
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class FontTransform extends TestCase {

    static final int PULSE = 5;
    static final int HEATBEAT = 250;

    static String[] getty = {
        "Four score and seven years ago our fathers brought forth on this continent, a new nation, conceived in Liberty, and dedicated to the proposition that all men are created equal.\n\n",
        "Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived and so dedicated, can long endure. We are met on a great battle-field of that war. We have come to dedicate a portion of that field, as a final resting place for those who here gave their lives that that nation might live. It is altogether fitting and proper that we should do this.\n\n",
        "But, in a larger sense, we can not dedicate -- we can not consecrate -- we can not hallow -- this ground. The brave men, living and dead, who struggled here, have consecrated it, far above our poor power to add or detract. The world will little note, nor long remember what we say here, but it can never forget what they did here. It is for us the living, rather, to be dedicated here to the unfinished work which they who fought here have thus far so nobly advanced. It is rather for us to be here dedicated to the great task remaining before us -- that from these honored dead we take increased devotion to that cause for which they gave the last full measure of devotion -- that we here highly resolve that these dead shall not have died in vain -- that this nation, under God, shall have a new birth of freedom -- and that government of the people, by the people, for the people, shall not perish from the earth.\n",
        "\nThis tests differences between a font derived by an 0.75, 0.75 affine transform of a 16 point font and a simple 12 point font.",
        "Basically, there should be no differences...\n"
    
    };
    
    private AffineTransform a;
    private Font base = null;
    private Font derived = null;
    private Font similar = null;


    // debugging
    static public void dump(LineMetrics lm) {
        System.out.println( "getAscent " + lm.getAscent() );
        System.out.println( "getHeight " + lm.getHeight() );
        System.out.println( "getNumChars " + lm.getNumChars() );
        System.out.println( "getBaselineIndex " + lm.getBaselineIndex() );
        System.out.println( "getDescent " + lm.getDescent() );
        System.out.println( "getLeading " + lm.getLeading() );
        System.out.println( "getStrikethroughOffset " + lm.getStrikethroughOffset() );
        System.out.println( "getStrikethroughThickness " + lm.getStrikethroughThickness() );
        System.out.println( "getUnderlineOffset " + lm.getUnderlineOffset() );
        System.out.println( "getUnderlineThickness " + lm.getUnderlineThickness() );
    }

    protected void setUp() {
        base = new Font( "Serif", Font.PLAIN, 16 );
        assertNotNull( base );

        a = AffineTransform.getScaleInstance(0.75,0.75);
        assertNotNull( a );
        derived = base.deriveFont(a);
        assertNotNull( derived );

        similar = new Font( "Serif", Font.PLAIN, 12 );
        assertNotNull( similar );
    
    }

    public void testSimpleDerivedFont() 
    {
        assertEquals( "Unexpected base size", 16, base.getSize() );
        assertFalse( "base isTransformed() should be false", base.isTransformed() );
        derived = base.deriveFont(a);
        assertNotNull( derived );
        assertEquals( "Unexpected derived size", 16, derived.getSize() );
        assertTrue( "derived isTransformed() should be true", derived.isTransformed() );
    }

    public void testDisplayDerivedFont() throws Exception
    {
        final JTextArea text = new JTextArea();
        assertNotNull( text );

        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setFont(derived);
        JScrollPane scroll = new JScrollPane(text);
        
        for (int i = 0; i< getty.length; i++) {
            text.append(getty[i]);
        }

        JFrame f = new JFrame();
        assertNotNull( f );

        try {
            f.getContentPane().add(scroll, java.awt.BorderLayout.CENTER);
            f.setSize(600,600);
    
            VisibilityValidator.setVisibleAndConfirm(f);

            text.setFont( derived );
            for( int i=0; i < PULSE; i++) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        text.selectAll();
                    }
                });
                Thread.sleep(HEATBEAT);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        text.select(0,1);
                    }
                });
                Thread.sleep(HEATBEAT);
            }
            
            text.setFont( similar );
            for( int i=0; i < PULSE; i++) {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        text.selectAll();
                    }
                });
                Thread.sleep(HEATBEAT);
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        text.select(0,1);
                    }
                });
                Thread.sleep(HEATBEAT);
            }
            
            Graphics2D g = (Graphics2D) text.getGraphics();
            assertNotNull( g );
            
            FontRenderContext frc =  g.getFontRenderContext();
            assertNotNull( frc );
            
            LineMetrics derived_lm = derived.getLineMetrics( getty[2], 0, getty[2].length(), frc);
            LineMetrics similar_lm = similar.getLineMetrics( getty[2], 0, getty[2].length(), frc);
            
            
            String descr = "12pt derived font and 12pt simple font should have similar ";
            double b_val, s_val, diff;
            
            
            //
            //    Check a few basic line metrics
            //
            
            b_val = derived_lm.getAscent();
            s_val = similar_lm.getAscent();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "ascent", diff < b_val/100);

            b_val = derived_lm.getHeight();
            s_val = similar_lm.getHeight();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "height", diff < b_val/100);

            b_val = derived_lm.getDescent();
            s_val = similar_lm.getDescent();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "descent", diff < b_val/100);


            //
            //    Check string bounds
            //
            Rectangle2D derived_b = derived.getStringBounds( getty[2], frc );
            Rectangle2D similar_b = similar.getStringBounds( getty[2], frc );
            
            
            b_val = derived_b.getX();
            s_val = similar_b.getX();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "bounds X coord", diff < 0.01);

            b_val = derived_b.getY();
            s_val = similar_b.getY();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "bounds Y coord", diff < 0.01);

            b_val = derived_b.getWidth();
            s_val = similar_b.getWidth();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "bounds width", diff < b_val/100);
            
            b_val = derived_b.getHeight();
            s_val = similar_b.getHeight();
            diff = Math.abs(b_val - s_val);
            assertTrue(descr + "bounds width", diff < b_val/100);
        }
        finally {
            f.dispose();
        }
        
    }

    public static Test suite() {
        return new TestSuite(FontTransform.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}

