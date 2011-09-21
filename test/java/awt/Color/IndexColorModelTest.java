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
 * @summary Tests the behavior of IndexColorModel via two rectangular images on the screen with different Color models.
 * @summary com.apple.junit.java.graphics.color
 * @library ../regtesthelpers
 * @build VisibilityValidator
 * @run main IndexColorModelTest
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

public class IndexColorModelTest extends TestCase {
    private static BufferedImage image1;
    private static BufferedImage image2;

    private static byte[] reds = {(byte)0, (byte)0};
    private static byte[] greens = {(byte)0, (byte)0};
    private static byte[] blues = {(byte)255, (byte)255}; 

    private JFrame frame;

    public void testColor() throws Exception {
        VisibilityValidator.setVisibleAndConfirm( frame);

        Thread.sleep(125);   // let humans see it too...
        
        assertTrue("Timed out without seeing our blue image", VisibilityValidator.waitForColor(frame ,50,50, Color.blue));
        assertTrue("Timed out without seeing our IndexColorModel image", VisibilityValidator.waitForColor(frame ,150,50, new Color(204, 204, 255)));
        
    }
    
    public static Test suite() {
        return new TestSuite(IndexColorModelTest.class);
    }

    protected void tearDown() {
           frame.dispose();
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void setUp() {
        frame = new JFrame();
        frame.setSize(new Dimension(200, 100));
        JPanel panel = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                paintOntoPanel((Graphics2D)g);
            }
        }; 
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(200, 100));
        frame.getContentPane().add(panel);

        IndexColorModel solidFirstModel = createColorModelWithSolidBlueFirst(); 
        IndexColorModel alphaFirstModel = createColorModelWithAlphaBlueFirst(); 
        image1 = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_INDEXED, solidFirstModel);
        image2 = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_INDEXED, alphaFirstModel);
        paintWithColorMap(image1);
        paintWithColorMap(image2);

        frame.pack();
    }

    private static void paintWithColorMap(BufferedImage image) {
        Graphics2D indexGraphics = image.createGraphics();
        paintAlphaRect(indexGraphics); 
    }

    // solid blue comes before alpha blue in color map
    private static IndexColorModel createColorModelWithSolidBlueFirst() {
        // alpha value of 51 corresponds to .2f as set in alpha composite below
        byte[] alphas = {(byte)255, (byte)51};
        return new IndexColorModel(1, 2, reds, greens, blues, alphas);
    }

    // alpha blue comes before solid blue in color map
    private static IndexColorModel createColorModelWithAlphaBlueFirst() {
        byte[] alphas = {(byte)51, (byte)255};
        return new IndexColorModel(1, 2, reds, greens, blues, alphas);
    } 

    private static void paintAlphaRect(Graphics2D graphics) {
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f);
        graphics.setComposite(alpha);
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, 100, 100); 
    }

    private static void paintOntoPanel(Graphics2D graphics) {
        graphics.drawImage(image1, 0, 0, null);
        graphics.drawImage(image2, 100, 0, null);
    }
}