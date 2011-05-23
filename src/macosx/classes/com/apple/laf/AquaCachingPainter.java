/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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

package com.apple.laf;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.HashMap;

import sun.java2d.SunGraphics2D;
import apple.laf.JRSUIControl;
import apple.laf.JRSUIState;

import com.apple.laf.AquaImageFactory.SlicedImageControl;

public class AquaCachingPainter<T extends JRSUIState> extends AquaPainter<T> {
    final HashMap <JRSUIState, SlicedImageControl> map = new HashMap<JRSUIState, SlicedImageControl>();
    
    final int sliceWidth;
    final int sliceHeight;
    final int westCut;
    final int eastCut;
    final int northCut;
    final int southCut;
    
    public AquaCachingPainter(final JRSUIControl control, final T state, int sliceWidth, int sliceHeight, final int westCut, final int eastCut, final int northCut, final int southCut ) {
        super(control, state);
        this.sliceHeight = sliceHeight;
        this.sliceWidth = sliceWidth;
        this.westCut = westCut;
        this.eastCut = eastCut;
        this.northCut = northCut;
        this.southCut = southCut;
    }
    
    @Override
    void paint(final Graphics g, final Component c, final int x, final int y, final int w, final int h) {
        state = (T)state.derive();
        final SlicedImageControl slices = map.get(state);
        if (slices != null) {
            slices.paint(g, x, y, w, h);
            return;
        }
        
        final BufferedImage image = new BufferedImage(sliceWidth, sliceHeight, BufferedImage.TYPE_INT_ARGB_PRE);
        final JRSUIControl control = new JRSUIControl(false);
        control.set(state);
        
        final WritableRaster raster = image.getRaster();
        final DataBufferInt buffer = (DataBufferInt)raster.getDataBuffer();
        final int[] data = buffer.getData();
        control.paint(data, sliceWidth, sliceHeight, 0.0, 0.0, sliceWidth, sliceHeight);
        
        final SlicedImageControl newSlices = new SlicedImageControl(image, westCut, eastCut, northCut, southCut, false);
        map.put(state, newSlices);
        newSlices.paint(g, x, y, w, h);
    }

    @Override
    void paint(SunGraphics2D g, T stateToPaint, Component c) {
        
    }
}
