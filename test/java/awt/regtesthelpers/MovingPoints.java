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
 * @summary A utility class for animations -- bounces a bunch of points around in a box
 * @summary com.apple.junit.utils
 */

package test.java.awt.regtesthelpers;

import java.awt.*;
import java.util.Random;

public class MovingPoints {
    private static final Random rand = new Random( 6251966 );
    static final int kDefaultSpeed = 15;
    private Rectangle box = null;

    int numVertices = 0;
    private int[] dx = null;
    private int[] dy = null;
    private int[] x = null;
    private int[] y = null;
    
    public MovingPoints(Rectangle r, int count) {
        this(r, count, kDefaultSpeed);
    }

    public MovingPoints(Rectangle r, int count, int speed) {
        box = r;
        numVertices = count;

        dx = new int[numVertices];
        dy = new int[numVertices];
        x = new int[numVertices];
        y = new int[numVertices];

        for (int i = 0; i < numVertices; i += 1) {
            x[i] = rand.nextInt(box.width);
            y[i] = rand.nextInt(box.height);
            while (dx[i] == 0) {
                dx[i] = rand.nextInt(1 + speed * 2) - speed;
            }
            while (dy[i] == 0) {
                dy[i] = rand.nextInt(1 + speed * 2) - speed;
            }
        }
    }
    
    // Bounce the points around
    public void move() {
        Dimension dim = box.getSize();
        for (int i = 0; i < x.length; i += 1) {
            x[i] += dx[i];
            y[i] += dy[i];
            if (x[i] < box.x) {
                x[i] = box.x;
                dx[i] = -dx[i];
            }
            if (x[i] > dim.width) {
                x[i] = dim.width;
                dx[i] = -dx[i];
            }
            if (y[i] < box.y) {
                y[i] = box.y;
                dy[i] = -dy[i];
            }
            if (y[i] > dim.height) {
                y[i] = dim.height;
                dy[i] = -dy[i];
            }
        }
    }

    public int[] getXs() {
        return x;
    }

    public int[] getYs() {
        return y;
    }

    public int getNumVertices() {
        return numVertices;
    }   
}
