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

/*
 @test
 @summary This is for <rdar://problem/3966067> jre1.4.2_05: Zoomed JFrame reports wrong location
 @summary com.apple.junit.java.awt.Frame
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main ZoomedFrame
 */

// classes necessary for this test
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class ZoomedFrame {
    private static final int mX = 100;
    private static final int mY = 100;
    private static final int mWidth = 100;
    private static final int mHeight = 100;
    private static final Point mOrigin = new Point(mX, mY);
    private static final Rectangle mNormalRect = new Rectangle(mX, mY, mWidth, mHeight);
    
    public static void main( String[] args ) throws Exception, RuntimeException {
        Point loc;
        Rectangle r;
        Frame f = new Frame("Hi");
        try {
            f.setBounds(mX, mY, mWidth, mHeight);
            VisibilityValidator.setVisibleAndConfirm(f);
            
            // Verify the frame's location and size
            loc = f.getLocation();
            if (!mOrigin.equals(loc)) {
                throw new RuntimeException("Frame is at the wrong location.  Expected: " + mOrigin + "; Actual: " + loc);
            }
            
            f.setExtendedState(Frame.MAXIMIZED_BOTH);
            Thread.sleep(100);
            
            loc = f.getLocation();
            if (mOrigin.equals(loc)) {
                throw new RuntimeException("Frame is at it's original location after frame was maximized (MAXIMIZED_BOTH).");
            }
            if (f.getWidth() == mWidth) {
                throw new RuntimeException( "Frame is at it's original width after frame was maximized (MAXIMIZED_BOTH).");
            }
            if (f.getHeight() == mHeight) {
                throw new RuntimeException( "Frame is at it's original height after frame was maximized (MAXIMIZED_BOTH).");
            }
            
            f.setExtendedState(Frame.NORMAL);
            Thread.sleep(100);
            
            r = f.getBounds();
            if (!r.equals(mNormalRect)) {
                throw new RuntimeException( "Frame was not restored to it's original size and location.");
            }
            
        } finally {
            f.dispose();
            f = null;
        }
    }
}

