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
import java.awt.image.BufferedImage;
import java.security.PrivilegedAction;
import javax.swing.*;
import javax.swing.plaf.UIResource;

import sun.lwawt.macosx.CImage;

import sun.java2d.*;

import apple.laf.*;
import apple.laf.JRSUIConstants.*;
import com.apple.laf.AquaIcon.*;
import com.apple.laf.AquaUtils.LazySingleton;

public class AquaImageFactory {
    public static Icon getConfirmImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        
        return new AquaIcon.CachingScalingIcon(kAlertIconSize, kAlertIconSize) {
            Image createImage() {
                return getThisApplicationsIcon(kAlertIconSize, kAlertIconSize);
            }
        };
    }
    
    public static ImageIcon getCautionImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return getAppIconCompositedOn(AquaIcon.SystemIcon.cautionIcon);
    }
    
    public static ImageIcon getStopImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return getAppIconCompositedOn(AquaIcon.SystemIcon.stopIcon);
    }
    
    static Image getThisApplicationsIcon(final int width, final int height) {
        final String path = getPathToThisApplication();
        
        if (path == null) {
            return getGenericJavaIcon();
        }
        
        if (path.startsWith("/System/Library/Frameworks/JavaVM.framework/")) {
            return getGenericJavaIcon();
        }
        
        if (path.startsWith("/usr/bin")) {
            return getGenericJavaIcon();
        }
        
        int w = (int)(width * SCALE_FACTOR);
        int h = (int)(height * SCALE_FACTOR);
        return CImage.fromFile(path).resize(w, h).toImage();
    }
    
    static Image getGenericJavaIcon() {
        // TODO: use blank image for now
        /*return java.security.AccessController.doPrivileged(new PrivilegedAction<Image>() {
            public Image run() {
                return com.apple.eawt.Application.getApplication().getDockIconImage();
            }
        });*/
        return new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB_PRE);
    }
    
    static String getPathToThisApplication() {
        // TODO: just return null for now
        /*return java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return com.apple.eio.FileManager.getPathToApplicationBundle();
            }
        });*/
        return null;
    }
    
    private static final float SCALE_FACTOR = 1.0f; //RuntimeOptions.getScaleFactor();
    private static final int kAlertIconSize = 64;
    static ImageIcon getAppIconCompositedOn(final SystemIcon systemIcon) {
        final int kAlertSubIconSize = (int)(kAlertIconSize * SCALE_FACTOR / 2.0);
        final int kAlertSubIconInset = (int)((kAlertIconSize * SCALE_FACTOR) - kAlertSubIconSize);
        
        final Icon smallAppIconScaled = new AquaIcon.CachingScalingIcon(kAlertSubIconSize, kAlertSubIconSize) {
            Image createImage() {
                return getThisApplicationsIcon(kAlertSubIconSize, kAlertSubIconSize);
            }
        };
        systemIcon.setSize(kAlertIconSize, kAlertIconSize);
        final Image systemImage = systemIcon.createImage();
        
        final Graphics g = systemImage.getGraphics();
        if (g instanceof Graphics2D) {
            // improves icon rendering quality in Quartz
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        smallAppIconScaled.paintIcon(null, g, kAlertSubIconInset, kAlertSubIconInset);
        g.dispose();
        
        return new ImageIcon(systemImage);
    }

    public static Icon getTreeFolderIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.folderIcon;
    }

    public static Icon getTreeOpenFolderIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.openFolderIcon;
    }

    public static Icon getTreeDocumentIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.documentIcon;
    }

    public static Icon getTreeExpandedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new CoreUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.DOWN);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }

    public static Icon getTreeCollapsedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new CoreUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.RIGHT);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }
    
    public static Icon getTreeRightToLeftCollapsedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new CoreUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.LEFT);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }
    
    static class NamedImageSingleton extends LazySingleton<ImageIcon> {
        final String namedImage;
        
        NamedImageSingleton(final String namedImage) {
            this.namedImage = namedImage;
        }
        
        protected ImageIcon getInstance() {
            return new ImageIcon(Toolkit.getDefaultToolkit().getImage("NSImage://" + namedImage));
        }
    }
    
    protected static NamedImageSingleton northArrow = new NamedImageSingleton("NSMenuScrollUp");
    protected static NamedImageSingleton southArrow = new NamedImageSingleton("NSMenuScrollDown");
    protected static NamedImageSingleton westArrow = new NamedImageSingleton("NSMenuSubmenuLeft");
    protected static NamedImageSingleton eastArrow = new NamedImageSingleton("NSMenuSubmenu");
    
    public static ImageIcon getArrowIconForDirection(final int direction) {
        switch(direction) {
            case SwingConstants.NORTH: return northArrow.get();
            case SwingConstants.SOUTH: return southArrow.get();
            case SwingConstants.EAST: return eastArrow.get();
            case SwingConstants.WEST: return westArrow.get();
        }
        return null;
    }

    /*
     * A "paintable" which holds nine images, which represent a sliced up initial 
     * image that can be streched from it's middles.
     */
    public static class SlicedImageControl {
        final BufferedImage NW, N, NE;
        final BufferedImage W, C, E;
        final BufferedImage SW, S, SE;
        
        final int wCut, eCut;
        final int nCut, sCut;
        
        final int totalWidth, totalHeight;
        final int centerColWidth, centerRowHeight;
        
        public SlicedImageControl(final Image img,
                                  final int westCut, final int eastCut,
                                  final int northCut, final int southCut,
                                  final boolean useMiddle) {
            this.wCut = westCut; this.eCut = eastCut;
            this.nCut = northCut; this.sCut = southCut;
            
            totalWidth = img.getWidth(null);
            totalHeight = img.getHeight(null);
            centerColWidth = totalWidth - westCut - eastCut;
            centerRowHeight = totalHeight - northCut - southCut;
            
            NW = createSlice(img, 0, 0, westCut, northCut);
            N = createSlice(img, westCut, 0, centerColWidth, northCut);
            NE = createSlice(img, totalWidth - eastCut, 0, eastCut, northCut);
            W = createSlice(img, 0, northCut, westCut, centerRowHeight);
            C = useMiddle ? createSlice(img, westCut, northCut, centerColWidth, centerRowHeight) : null;
            E = createSlice(img, totalWidth - eastCut, northCut, eastCut, centerRowHeight);
            SW = createSlice(img, 0, totalHeight - southCut, westCut, southCut);
            S = createSlice(img, westCut, totalHeight - southCut, centerColWidth, southCut);
            SE = createSlice(img, totalWidth - eastCut, totalHeight - southCut, eastCut, southCut);
        }
        
        static BufferedImage createSlice(final Image img, final int x, final int y, final int w, final int h) {
            if (w == 0 || h == 0) return null;
            
            final BufferedImage slice = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            final Graphics2D g2d = slice.createGraphics();
            g2d.drawImage(img, -x, -y, null);
            g2d.dispose();
            
            return slice;
        }
        
        public void paint(final Graphics g, final int x, final int y, final int w, final int h) {
            g.translate(x, y);
            
            if (w < totalWidth || h < totalHeight) {
                paintCompressed(g, w, h);
            } else {
                paintStretchedMiddles(g, w, h);
            }
            
            g.translate(-x, -y);
        }
        
        void paintStretchedMiddles(final Graphics g, final int w, final int h) {
            if (NW != null) g.drawImage(NW, 0, 0, null);
            if (N != null) g.drawImage(N, wCut, 0, w - eCut - wCut, nCut, null);
            if (NE != null) g.drawImage(NE, w - eCut, 0, null);
            if (W != null) g.drawImage(W, 0, nCut, wCut, h - nCut - sCut, null);
            if (C != null) g.drawImage(C, wCut, nCut, w - eCut - wCut, h - nCut - sCut, null);
            if (E != null) g.drawImage(E, w - eCut, nCut, eCut, h - nCut - sCut, null);
            if (SW != null) g.drawImage(SW, 0, h - sCut, null);
            if (S != null) g.drawImage(S, wCut, h - sCut, w - eCut - wCut, sCut, null);
            if (SE != null) g.drawImage(SE, w - eCut, h - sCut, null);
        }
        
        void paintCompressed(final Graphics g, final int w, final int h) {
            final double heightRatio = h > totalHeight ? 1.0 : (double)h / (double)totalHeight;
            final double widthRatio = w > totalWidth ? 1.0 : (double)w / (double)totalWidth;
            
            final int northHeight = (int)(nCut * heightRatio);
            final int southHeight = (int)(sCut * heightRatio);
            final int centerHeight = h - northHeight - southHeight;
            
            final int westWidth = (int)(wCut * widthRatio);
            final int eastWidth = (int)(eCut * widthRatio);
            final int centerWidth = w - westWidth - eastWidth;
            
            if (NW != null) g.drawImage(NW, 0, 0, westWidth, northHeight, null);
            if (N != null) g.drawImage(N, westWidth, 0, centerWidth, northHeight, null);
            if (NE != null) g.drawImage(NE, w - eastWidth, 0, eastWidth, northHeight, null);
            if (W != null) g.drawImage(W, 0, northHeight, westWidth, centerHeight, null);
            if (C != null) g.drawImage(C, westWidth, northHeight, centerWidth, centerHeight, null);
            if (E != null) g.drawImage(E, w - eastWidth, northHeight, eastWidth, centerHeight, null);
            if (SW != null) g.drawImage(SW, 0, h - southHeight, westWidth, southHeight, null);
            if (S != null) g.drawImage(S, westWidth, h - southHeight, centerWidth, southHeight, null);
            if (SE != null) g.drawImage(SE, w - eastWidth, h - southHeight, eastWidth, southHeight, null);
        }
    }
    
    // TODO: Can we port this?
//     public static Icon createFocusedImage(final Image image, final Component c, final int slack) {
//         final int w = image.getWidth(c);
//         final int h = image.getHeight(c);
        
//         final AImage focusedImage = AquaUtils.getAImageCreator().createImage(w + slack + slack, h + slack + slack);
        
//         final Graphics g = focusedImage.getGraphics();
//         if (!(g instanceof SunGraphics2D)) return null;
        
//         final SunGraphics2D sg2d = (SunGraphics2D)g;
//         final SurfaceData surfaceData = sg2d.getSurfaceData();
//         if (!(surfaceData instanceof OSXSurfaceData)) return null;
        
//         try {
//             ((OSXSurfaceData)surfaceData).performCocoaDrawing(sg2d, new OSXSurfaceData.CGContextDrawable() {
//                 @Override
//                 public void drawIntoCGContext(final long cgContext) {
//                     final JRSUIFocus focus = new JRSUIFocus(cgContext);
//                     focus.beginFocus(JRSUIFocus.RING_BELOW);
//                     sg2d.drawImage(image, slack, slack, c);
//                     focus.endFocus();
//                 }
//             });
//         } finally {
//             g.dispose();
//         }
        
//         return new ImageIcon(focusedImage);
//     }
    
//     public static Icon createFocusedIcon(final Icon tmpIcon, final Component c, final int slack) {
//         return new FocusedIcon(tmpIcon, slack);
//     }
    
//     static class FocusedIcon implements Icon {
//         final Icon icon;
//         final int slack;
        
//         public FocusedIcon(final Icon icon, final int slack) {
//             this.icon = icon;
//             this.slack = slack;
//         }

//         @Override
//         public int getIconHeight() {
//             return icon.getIconHeight() + slack + slack;
//         }

//         @Override
//         public int getIconWidth() {
//             return icon.getIconWidth() + slack + slack;
//         }

//         @Override
//         public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
//             if (!(g instanceof SunGraphics2D)) return;
            
//             final SunGraphics2D sg2d = (SunGraphics2D)g;
//             final SurfaceData surfaceData = sg2d.getSurfaceData();
//             if (!(surfaceData instanceof OSXSurfaceData)) return;
            
//             ((OSXSurfaceData)surfaceData).performCocoaDrawing(sg2d, new OSXSurfaceData.CGContextDrawable() {
//                 @Override
//                 public void drawIntoCGContext(final long cgContext) {
//                     final JRSUIFocus focus = new JRSUIFocus(cgContext);
//                     focus.beginFocus(JRSUIFocus.RING_BELOW);
//                     icon.paintIcon(c, sg2d, x + slack, y + slack);
//                     focus.endFocus();
//                 }
//             });
//         }
//     }
    
    // when we use SystemColors, we need to proxy the color with something that implements UIResource,
    // so that it will be uninstalled when the look and feel is changed.
    private static class SystemColorProxy extends Color implements UIResource {
        final Color color;
        public SystemColorProxy(final Color color) {
            super(color.getRGB());
            this.color = color;
        }
        
        public int getRGB() {
            return color.getRGB();
        }
    }
    
    public static Color getWindowBackgroundColorUIResource() {
        return AquaNativeResources.getWindowBackgroundColorUIResource();
    }

    public static Color getTextSelectionBackgroundColorUIResource() {
        return new SystemColorProxy(SystemColor.textHighlight);
    }

    public static Color getTextSelectionForegroundColorUIResource() {
        return new SystemColorProxy(SystemColor.textHighlightText);
    }
    
    public static Color getSelectionBackgroundColorUIResource() {
        return new SystemColorProxy(SystemColor.controlHighlight);
    }
    
    public static Color getSelectionForegroundColorUIResource() {
        return new SystemColorProxy(SystemColor.controlLtHighlight);
    }
    
    // TODO: Un-hardcode the returned color
    public static Color getFocusRingColorUIResource() {
        //return new SystemColorProxy(AToolkit.getAppleColor(AToolkit.KEYBOARD_FOCUS_COLOR));
        return new SystemColorProxy(new Color(75, 137, 208));
    }
}
