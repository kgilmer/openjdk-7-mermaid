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
 * @summary Tests whether an exception is being thrown while trying to draw on AWT peer components
 * @summary com.apple.junit.java.graphics.drawable
 * @library ../regtesthelpers
 * @build VisibilityValidator
 * @run main TestDrawables
 */

import test.java.awt.regtesthelpers.VisibilityValidator;
import junit.framework.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TestDrawables extends TestCase {
    private static final int TIMEOUT = 5000;
    public static final Color YELLOW_COLOR = Color.yellow;

    public static Test suite() {
        return new TestSuite(TestDrawables.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }


    protected void setUp() {
        initBaseImage();

        fFrame = new Frame("Test Frame");
        fFrame.setLayout( new GridLayout(4, 3, 0, 0) );
        addAWTComponents();

        fFrame.pack();

        fFrame.setSize(600, 600);
        fFrame.setVisible(true);
    }


    // Intialize the base image with a gradient fill.  We could use anything here.
    void initBaseImage() {
        Graphics2D ig = (Graphics2D) bi.createGraphics();
        GradientPaint gp = new GradientPaint( 0,0, Color.red, w, h, Color.cyan, false );
        ig.setPaint( gp );
        ig.fillRect( 0, 0, w, h);
        ig.dispose();
    }


    protected void tearDown() {
        fFrame.dispose();
    }

    public void testPainting() throws Exception {
        fFrame.invalidate();
        fFrame.paint(fFrame.getGraphics());
        fButton.paint(fButton.getGraphics());
        fCanvas.paint(fCanvas.getGraphics());
        fCheckbox.paint(fCheckbox.getGraphics());
        fChoice.paint(fChoice.getGraphics());
        fList.paint(fList.getGraphics());
        fScrollbar.paint(fScrollbar.getGraphics());
        fTextArea.paint(fTextArea.getGraphics());
        fScrollPane.paint(fScrollPane.getGraphics());


        Robot r = new Robot();

        int col1Center = 100;
        int row1Center = 100;
        int row2Center = 240;
        int row3Center = 380;
        int row4Center = 525;


        Point loc = fFrame.getLocationOnScreen();
        int buttonCenterX = loc.x + col1Center;
        int buttonCenterY = loc.y + row1Center;

        // We are willing to wait several seconds for contents of the frame to be flushed
        long endtime = System.currentTimeMillis() + TIMEOUT;
        Color buttonCenterHit = null;
        Color checkboxCenterColor = null;
        Color listCenterColor = null;
        Color textAreaCenterColor = null;

        // Hang out here until we either see what we expect, or we time out
        while ( System.currentTimeMillis() < endtime) {
            buttonCenterHit     = r.getPixelColor(buttonCenterX, buttonCenterY);
            checkboxCenterColor = r.getPixelColor(loc.x + col1Center, loc.y + row2Center);
            listCenterColor     = r.getPixelColor(loc.x + col1Center, loc.y + row3Center);
            textAreaCenterColor = r.getPixelColor(loc.x + col1Center, loc.y + row4Center);

            if (buttonCenterHit.equals(YELLOW_COLOR)
                && checkboxCenterColor.equals(YELLOW_COLOR)
                && listCenterColor.equals(YELLOW_COLOR)
                && textAreaCenterColor.equals(YELLOW_COLOR)) {
                // hey, contents of the frame have been flushed and look good
                break;
            }
            else  {
                // we may have looked to quickly, wait a bit and look again
                Thread.sleep(200);
            }
        }

        VisibilityValidator.assertColorEquals(" Color should be yellow at the center of button",YELLOW_COLOR, buttonCenterHit);
        VisibilityValidator.assertColorEquals(" Color should be yellow at the center of the checkbox", YELLOW_COLOR, checkboxCenterColor);
        VisibilityValidator.assertColorEquals(" Color should be yellow at the center of the list", YELLOW_COLOR, listCenterColor);
        VisibilityValidator.assertColorEquals(" Color should be yellow at the center of the checkbox", YELLOW_COLOR, textAreaCenterColor);
    }


    private void addAWTComponents() {

        fButton  = new DrawableButton("Button");
        fButton.setBackground(Color.red);
        fButton.setForeground(Color.blue);
        fFrame.add(fButton);

        fCanvas = new DrawableCanvas();
        fCanvas.setBackground(Color.red);
        fCanvas.setForeground(Color.blue);
        fFrame.add(fCanvas);

        fCheckbox = new DrawableCheckbox("Checkbox");
        fCheckbox.setBackground(Color.red);
        fCheckbox.setForeground(Color.blue);
        fFrame.add(fCheckbox);

        CheckboxGroup radioButtons = new CheckboxGroup();
        Checkbox radiobutton = new DrawableCheckbox("CheckboxGroup 1", radioButtons, true);
        radiobutton.setBackground(Color.red);
        radiobutton.setForeground(Color.blue);
        Checkbox radiobutton2 = new DrawableCheckbox("CheckboxGroup 2", radioButtons, false);
        radiobutton2.setBackground(Color.red);
        radiobutton2.setForeground(Color.blue);
        fFrame.add(radiobutton);
        fFrame.add(radiobutton2);

        fChoice = new DrawableChoice();
        fChoice.add("Choice1");
        fChoice.add("Choice2");
        fChoice.add("Choice3");
        fChoice.add("Choice4");
        fChoice.setBackground(Color.red);
        fChoice.setForeground(Color.blue);
        fFrame.add(fChoice);

        fList = new DrawableList(4, true);
        fList.add("List1");
        fList.add("List2");
        fList.add("List3");
        fList.add("List4");
        fList.add("List5");
        fList.add("List6");
        fList.add("List7");
        fList.add("List8");
        fList.add("List9");
        fList.add("List10");
        fList.setBackground(Color.red);
        fList.setForeground(Color.blue);
        fFrame.add(fList);

        fScrollbar = new DrawableScrollbar(Scrollbar.VERTICAL, 0, 1, 0, 255);
        fScrollbar.setBackground(Color.red);
        fScrollbar.setForeground(Color.blue);
        fFrame.add(fScrollbar);

        Scrollbar scrollbar2 = new DrawableScrollbar(Scrollbar.HORIZONTAL, 0, 1, 0, 255);
        scrollbar2.setBackground(Color.red);
        scrollbar2.setForeground(Color.blue);
        fFrame.add(scrollbar2);

        String str = "Text Area\n";
        fTextArea = new DrawableTextArea(str+str+str+str+str+str+str+str+str+str+str+str+str+str+str+str+str+str+str+str);
        fTextArea.setBackground(Color.red);
        fTextArea.setForeground(Color.blue);
        fFrame.add(fTextArea);

        str = "Text Field";
        TextField textField = new DrawableTextField(str);
        textField.setBackground(Color.red);
        textField.setForeground(Color.blue);
        fFrame.add(textField);

        fScrollPane = new DrawableScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        fScrollPane.setBackground(Color.red);
        fScrollPane.setForeground(Color.blue);
        ImageThingy imageThingy = new ImageThingy();
        fScrollPane.add(imageThingy);
        fFrame.add(fScrollPane);
    }

    class ImageThingy extends Panel {
        Image image;
        public ImageThingy() {
            super();
        }
        public void paint(Graphics g) {
            g.drawImage(bi, 0, 0, null);
        }
        public Dimension getPreferredSize() {
            return new Dimension(w, h);
        }
    }


    private class DrawableButton extends Button {
        public DrawableButton(String title) {
            super(title);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableCanvas extends Canvas {
        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }


    private class DrawableChoice extends Choice {
        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableCheckbox extends Checkbox {
        public DrawableCheckbox(String title) {
            super(title);
        }

        public DrawableCheckbox(String title, CheckboxGroup group, boolean b) {
            super(title, group, b);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableList extends List {
        public DrawableList(int i, boolean b) {
            super(i, b);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableScrollbar extends Scrollbar {
        public DrawableScrollbar(int orientation, int value, int visible, int minimum, int maximum) {
            super(orientation, value, visible, minimum, maximum);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }


    private class DrawableTextArea extends TextArea {
        public DrawableTextArea(String title) {
            super(title);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableTextField extends TextField {
        public DrawableTextField(String title) {
            super(title);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class DrawableScrollPane extends ScrollPane {
        public  DrawableScrollPane(int policy) {
            super(policy);
        }

        public void paint(Graphics g) {
            (new CompDrawer(this)).paint(g);
        }
    }

    private class CompDrawer {
        public CompDrawer(Component comp) {
            comp_ = comp;
        }


        public void paint(Graphics g) {
            g.setColor(Color.black);
            int ww = comp_.getWidth();
            int hh = comp_.getHeight();
            g.drawLine(0, 0, ww, hh);
            g.drawLine(ww, 0, 0, hh);

            Graphics g2 = comp_.getGraphics();
            g2.setColor(TestDrawables.YELLOW_COLOR);
            g2.fillOval(ww / 2 - 10, hh / 2 - 10, 20, 20);
        }

        private Component comp_ = null;
    }

    static final int        w = 400;
    static final int        h = 400;
    static final int        siw = w/ 4;
    static final int        sih = h / 4;

    GraphicsEnvironment     ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice          gd = ge.getDefaultScreenDevice();
    GraphicsConfiguration   gc = gd.getDefaultConfiguration();
    BufferedImage           bi = gc.createCompatibleImage( w, h );

    private Frame fFrame = null;
    private Button fButton = null;
    private Canvas fCanvas = null;
    private Checkbox fCheckbox = null;
    private Choice fChoice = null;
    private List fList = null;
    private Scrollbar fScrollbar = null;
    private TextArea fTextArea = null;
    private ScrollPane fScrollPane = null;
}
