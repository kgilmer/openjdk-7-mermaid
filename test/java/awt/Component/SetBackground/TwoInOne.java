/*
 * Copyright (c) 2006, 2007, Oracle and/or its affiliates. All rights reserved.
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
 @summary Verify that component's background matches wat was set using setBackground
 @summary com.apple.junit.java.awt.Component
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main TwoInOne
 */

import junit.framework.*;

import java.awt.*;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class TwoInOne {
    static int width = 400, height = 100;
    
    static Color color0 = Color.red;
    static Color color1 = Color.green;
    static Color color2 = Color.blue;
    static Frame frame;
    static Robot robot;

    public static final void main(String[] args) throws Exception {
        try {
            init();
            VisibilityValidator.setVisibleAndConfirm(frame);
            Thread.sleep(200); // Let the user see things.  Not really needed, but original test was up for 2 secs...
            
            Color c1 = getPixel((width / 2), (height / 2));            
            VisibilityValidator.assertColorEquals("North canvas should have gotten background color", c1 , color1);
            
            Color c2 = getPixel((width / 2), height + (height / 2));        
            VisibilityValidator.assertColorEquals("North canvas should have gotten background color", c2, color2);            
        }
        finally {
            frame.dispose();
        }
    }

    static private void init() throws Exception {

        frame = new Frame("CopyArea");
        frame.setBackground(color0);
        frame.add(createColoredCanvas(color1), "North");
        frame.add(createColoredCanvas(color2), "South");
        frame.pack();
        robot = new Robot();
        robot.setAutoWaitForIdle(true);
    }


    static public Color getPixel(int x, int y) {
        int locX = (int) (frame.getLocationOnScreen().getX()) + x;
        int locY = (int) (frame.getLocationOnScreen().getY()) + y;
        return robot.getPixelColor(locX, locY);
    }

    private static Canvas createColoredCanvas( Color color) {
        Canvas cCanvas = new Canvas();
        
        cCanvas.setPreferredSize(new Dimension(width, height));
        cCanvas.setMinimumSize(new Dimension(width, height));
        cCanvas.setBackground(color);
        return cCanvas;
    }
}