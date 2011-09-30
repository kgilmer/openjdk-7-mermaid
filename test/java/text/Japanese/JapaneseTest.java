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
 * @summary
 * @summary com.apple.junit.java.text;
 */

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import junit.framework.*;

public class JapaneseTest extends TestCase {

    public void testJapaneseText() {
        boolean failed = true;
        JFrame f = new JFrame("Japanese Test");
        f.setBounds(0, 0, 300, 200);
        TextPanel textPanel = new TextPanel();
        f.getContentPane().add(textPanel, BorderLayout.CENTER);
        f.setVisible(true);
        f.toFront();
        Robot robot;
        try {
            robot = new Robot();
            Thread.sleep(1000);
            Point p = textPanel.getLocationOfText();
            SwingUtilities.convertPointToScreen(p, textPanel);
            Rectangle r = new Rectangle(p.x - 20, p.y - 20, 100, 60);
            BufferedImage screenshot = robot.createScreenCapture(r);
            int pixels[] = (int[]) screenshot.getData().getDataElements(0, 0,
                    100, 60, null);
            int black = Color.BLACK.getRGB();
            for (int i = 0; pixels != null && i < pixels.length; i++) {
                if (pixels[i] != black) {
                    failed = false;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        f.dispose();
        assertFalse("Japanese Text did not render", failed);
    }

    @SuppressWarnings("serial")
    class TextPanel extends JPanel {
        public TextPanel() {
            super();
            setOpaque(true);
        }

        private Point locationOfText = new Point(20, 60);

        public Point getLocationOfText() {
            return locationOfText;
        }

        public void paintComponent(Graphics g) {
            Rectangle r = getBounds();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, r.width, r.height);
            g.setColor(Color.WHITE);
            g.drawString("\u30a4\u30e1\u30fc\u30b8\u304a\u3088\u3073\u30cb\u30e5\u30fc\u30b9\u691c\u7d22", locationOfText.x, locationOfText.y);
        }

    }

    public static Test suite() {
        return new TestSuite(JapaneseTest.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
}
