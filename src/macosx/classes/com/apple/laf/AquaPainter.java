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

import java.awt.*;
import java.awt.image.*;
import sun.awt.image.*;
import sun.java2d.*;
import sun.print.*;
import apple.laf.*;

abstract class AquaPainter <T extends JRSUIState> {
    static <T extends JRSUIState> AquaPainter<T> create(final T state) {
        // TODO : requires OSXSurfaceData
//         return new DirectOSXSurfacePainter<T>(state);
        return new CachedImagePainter(state);
    }
    
    abstract void paint(final SunGraphics2D g, final T stateToPaint, final Component c);
    
    final Rectangle boundsRect = new Rectangle();
    final JRSUIControl control;
    T state;
    public AquaPainter(final JRSUIControl control, final T state) {
        this.control = control;
        this.state = state;
    }
    
    JRSUIControl getControl() {
        control.set(state = (T)state.derive());
        return control;
    }
    
    void paint(final Graphics g, final Component c, final int x, final int y, final int w, final int h) {
        boundsRect.setBounds(x, y, w, h);
        
        final T nextState = (T)state.derive();
        final SunGraphics2D g2d = getGraphics2D(g);
        if (g2d != null) paint(g2d, nextState, c);
        state = nextState;
    }
    
        // TODO : requires OSXSurfaceData
//     static class DirectOSXSurfacePainter<T extends JRSUIState> extends AquaPainter<T> implements OSXSurfaceData.CGContextDrawable {
//         public DirectOSXSurfacePainter(final T state) {
//             super(new JRSUIControl(true), state);
//         }

//         @Override
//         void paint(final SunGraphics2D g, final T controlState, final Component c) {
//             final SurfaceData surfaceData = g.getSurfaceData();
//             if (!(surfaceData instanceof apple.awt.OSXSurfaceData)) return;
//             control.set(controlState);
//             ((OSXSurfaceData)surfaceData).performCocoaDrawing(g, this);
//         }
        
//         @Override
//         public void drawIntoCGContext(final long cgContext) {
//             control.paint(cgContext, boundsRect.x, boundsRect.y, boundsRect.width, boundsRect.height);
//         }
//     }
    
    static class CachedImagePainter<T extends JRSUIState> extends AquaPainter<T> {
        static final ImageCache cache = ImageCache.getInstance();
        
        final Rectangle clipRect = new Rectangle();
        Rectangle intersection = new Rectangle();
        
        public CachedImagePainter(final T state) {
            super(new JRSUIControl(false), state);
        }
        
        void paint(final SunGraphics2D g, final T controlState, final Component c) {
            g.getClipBounds(clipRect);
            intersection = boundsRect.intersection(clipRect);
            if (intersection.width <= 0 || intersection.height <= 0) return;
            
            final GraphicsConfiguration config = g.getDeviceConfiguration();
            BufferedImage image = (BufferedImage)cache.getImage(config, intersection.width, intersection.height, controlState);
            if (image == null) {
                image = new BufferedImage(intersection.width, intersection.height, BufferedImage.TYPE_INT_ARGB_PRE);
                cache.setImage(image, config, intersection.width, intersection.height, controlState);
            } else {
                g.drawImage(image, boundsRect.x, boundsRect.y, c);
                return;
//              Graphics imgG = image.getGraphics();
//              ((Graphics2D)imgG).setComposite(AlphaComposite.Src);
//              imgG.setColor(new Color(0,0,0,0));
//              imgG.fillRect(0, 0, intersection.width, intersection.height);
            }
            
            final WritableRaster raster = image.getRaster();
            final DataBufferInt buffer = (DataBufferInt)raster.getDataBuffer();
            
            control.set(controlState);
            control.paint(SunWritableRaster.stealData(buffer, 0),
                          image.getWidth(), image.getHeight(),
                          boundsRect.x - intersection.x,
                          boundsRect.y - intersection.y,
                          boundsRect.width, boundsRect.height);
            SunWritableRaster.markDirty(buffer);

//          g.setColor(outerbounds);
//          g.drawRect(0, 0, w, h);
//          
//          g.setColor(innerbounds);
//          g.drawRect(x, y, w, h);
            
            g.drawImage(image, intersection.x, intersection.y, c);
        }
        
        //static Color outerbounds = new Color(0xFF, 0x0, 0x0, 0x80);
        //static Color innerbounds = new Color(0x0, 0x0, 0xFF, 0x80);
        
//      static AquaPainter create(final int w, final int h) {
//          return null;
//      }
//      
//      static AquaPainter createHorizontalThreeSlice(final int w, final int h, final int left, final int right) {
//          return null;
//      }
//      
//      static AquaPainter createNineSlice(final int w, final int h, final int left, final int top, final int right, final int bottom) {
//          return null;
//      }
    }
    
    protected SunGraphics2D getGraphics2D(final Graphics g) {
        try {
            return (SunGraphics2D)g; // doing a blind try is faster than checking instanceof
        } catch (Exception e) {
            if (g instanceof PeekGraphics) {
                // if it is a peek just dirty the region
                g.fillRect(boundsRect.x, boundsRect.y, boundsRect.width, boundsRect.height);
            } else if (g instanceof ProxyGraphics2D) {
                final ProxyGraphics2D pg = (ProxyGraphics2D)g;
                final Graphics2D g2d = pg.getDelegate();
                if (g2d instanceof SunGraphics2D) { return (SunGraphics2D)g2d; }
            }
        }

        return null;
    }
}
