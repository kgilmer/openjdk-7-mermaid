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
 * @summary <rdar://problem/4904332> GradientPaint causes Java hang/crash
 * @summary com.apple.junit.java.graphics.GradientPaint
 * @library ../../regtesthelpers
 * @build VisibilityValidator Waypoint
 * @run main R4904332GradientHang
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;
import java.util.Vector;

public class R4904332GradientHang extends TestCase {

    private JFrame frame = null;
    
    @Override
    protected void setUp() throws Exception {
        frame = new JFrame(System.getProperty("vm.version"));
    }
        
    @Override
    protected void tearDown() throws Exception {
        assertNotNull(frame);
        frame.dispose();
    }

    public void testR4904332() throws Exception {
        assertNotNull(frame);
        Container contentPane = frame.getContentPane();
        assertNotNull(contentPane);
        contentPane.setLayout(new BorderLayout());
        GradientJPanel gradientPanel = new GradientJPanel(5);
        assertNotNull(gradientPanel);
        contentPane.add(gradientPanel, BorderLayout.CENTER);        
        frame.pack();
        VisibilityValidator.setVisibleAndConfirm(frame);
        gradientPanel.requirePaint("Panel with gradient paint failed to paint");        
        frame.repaint();        
        gradientPanel.requirePaint("Panel with gradient paint did not get 2nd paint");
    }

    public void testVarients() throws Exception {
        assertNotNull(frame);
        Container contentPane = frame.getContentPane();
        assertNotNull(contentPane);
        contentPane.setLayout(new FlowLayout());
        
        Vector<GradientJPanel> panels = new Vector<GradientJPanel>(); 
        
        // Add a bunch of different panels with different sized gradients
        for (int i = 1; i < 100; i+=10) {
            GradientJPanel panel =new GradientJPanel(i); 
            panels.add(panel);
            contentPane.add(panel);
        }
        
        // Show the whole thing     
        frame.pack();
        VisibilityValidator.setVisibleAndConfirm(frame);

        // check that everything paints once
        for ( GradientJPanel panel : panels) {
            panel.requirePaint("Panel with gradient paint failed to paint");
        }

        frame.repaint();        

        // check that everything paints again
        for ( GradientJPanel panel : panels) {
            panel.requirePaint("Panel with gradient paint failed to paint");
        }
    }

    // A panel painted with a gradient
    class GradientJPanel extends JPanel {
        private int size = 5;
        private GradientPaint gp = null;
        public final Waypoint painted = new Waypoint();
        
        public GradientJPanel(int size) {
            super();
            this.size = size;
            gp =new GradientPaint(5, 5, Color.white, size, 5+size, Color.black);
        }
        
        @Override
        public void paint(Graphics gx) {
            Graphics2D g = (Graphics2D)gx;
            g.setStroke(new BasicStroke( (1.0f * size) - 1 ));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setPaint(gp);
            g.drawLine(5, 5, size, 5+size);
            painted.clear();
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(size+10, size+10);
        }

        public void requirePaint() throws RuntimeException {
            requirePaint("Timed out waiting for paint to compelete");
        }

        public void requirePaint(String msg) throws RuntimeException {
            painted.requireClear(msg);
            painted.reset();
        }
    }
    

// Boilerplate below

public static Test suite() {
    return new TestSuite(R4904332GradientHang.class);
}

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }


}
