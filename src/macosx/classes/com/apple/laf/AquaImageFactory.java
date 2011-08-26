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
import java.security.PrivilegedAction;

import javax.swing.*;
import javax.swing.plaf.*;

import apple.laf.JRSUIState;
import apple.laf.JRSUIConstants.*;
import com.apple.laf.AquaIcon.*;
import com.apple.laf.AquaUtils.LazySingleton;
import sun.lwawt.macosx.CImage;

public class AquaImageFactory {
    public static IconUIResource getConfirmImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        
        return new IconUIResource(new AquaIcon.CachingScalingIcon(kAlertIconSize, kAlertIconSize) {
            Image createImage() {
                return getThisApplicationsIcon(kAlertIconSize, kAlertIconSize);
            }
        });
    }
    
    public static IconUIResource getCautionImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return getAppIconCompositedOn(AquaIcon.SystemIcon.getCautionIcon());
    }
    
    public static IconUIResource getStopImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return getAppIconCompositedOn(AquaIcon.SystemIcon.getStopIcon());
    }
    
    public static IconUIResource getLockImageIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        final Image lockIcon = AquaUtils.getCImageCreator().createImageFromFile("/System/Library/CoreServices/SecurityAgent.app/Contents/Resources/Security.icns", kAlertIconSize, kAlertIconSize);
        return getAppIconCompositedOn(lockIcon);
    }
    
    static Image getThisApplicationsIcon(final int width, final int height) {
        final String path = getPathToThisApplication();
        
        if (path == null) {
            return getGenericJavaIcon();
        }
        
        if (path.endsWith("/Home/bin")) {
            return getGenericJavaIcon();
        }
        
        if (path.startsWith("/usr/bin")) {
            return getGenericJavaIcon();
        }
        
        return AquaUtils.getCImageCreator().createImageOfFile(path, height, width);
    }
    
    static Image getGenericJavaIcon() {
        // TODO: port eAWT classes
//        return java.security.AccessController.doPrivileged(new PrivilegedAction<Image>() {
//            public Image run() {
//                return com.apple.eawt.Application.getApplication().getDockIconImage();
//            }
//        });
        return new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB_PRE);
    }
    
    static String getPathToThisApplication() {
        // TODO: port eIO classes
//        return java.security.AccessController.doPrivileged(new PrivilegedAction<String>() {
//            public String run() {
//                return FileManager.getPathToApplicationBundle();
//            }
//        });
        return "/tmp";
    }
    
    static IconUIResource getAppIconCompositedOn(final SystemIcon systemIcon) {
        systemIcon.setSize(kAlertIconSize, kAlertIconSize);
        return getAppIconCompositedOn(systemIcon.createImage());
    }
    
    private static final int kAlertIconSize = 64;
    static IconUIResource getAppIconCompositedOn(final Image image) {
        final int kAlertSubIconSize = (int)(kAlertIconSize * 0.5);
        final int kAlertSubIconInset = kAlertIconSize - kAlertSubIconSize;
        final Icon smallAppIconScaled = new AquaIcon.CachingScalingIcon(kAlertSubIconSize, kAlertSubIconSize) {
            Image createImage() {
                return getThisApplicationsIcon(kAlertSubIconSize, kAlertSubIconSize);
            }
        };
        final Graphics g = image.getGraphics();
        if (g instanceof Graphics2D) {
            // improves icon rendering quality in Quartz
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        smallAppIconScaled.paintIcon(null, g, kAlertSubIconInset, kAlertSubIconInset);
        g.dispose();
        
        return new IconUIResource(new ImageIcon(image));
    }

    public static IconUIResource getTreeFolderIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.getFolderIconUIResource();
    }

    public static IconUIResource getTreeOpenFolderIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.getOpenFolderIconUIResource();
    }

    public static IconUIResource getTreeDocumentIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.SystemIcon.getDocumentIconUIResource();
    }

    public static UIResource getTreeExpandedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new JRSUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.DOWN);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }

    public static UIResource getTreeCollapsedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new JRSUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.RIGHT);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }
    
    public static UIResource getTreeRightToLeftCollapsedIcon() {
        // public, because UIDefaults.ProxyLazyValue uses reflection to get this value
        return AquaIcon.getIconFor(new JRSUIControlSpec() {
            public void initIconPainter(final AquaPainter<? extends JRSUIState> painter) {
                painter.state.set(Widget.DISCLOSURE_TRIANGLE);
                painter.state.set(State.ACTIVE);
                painter.state.set(Direction.LEFT);
                painter.state.set(AlignmentHorizontal.CENTER);
                painter.state.set(AlignmentVertical.CENTER);
            }
        }, 20, 20);
    }
    
    static class NamedImageSingleton extends LazySingleton<Image> {
        final String namedImage;
        
        NamedImageSingleton(final String namedImage) {
            this.namedImage = namedImage;
        }
        
        @Override
        protected Image getInstance() {
            return Toolkit.getDefaultToolkit().getImage("NSImage://" + namedImage);
        }
    }
    
    static class IconUIResourceSingleton extends LazySingleton<IconUIResource> {
        final NamedImageSingleton holder;
        
        public IconUIResourceSingleton(final NamedImageSingleton holder) {
            this.holder = holder;
        }
        
        @Override
        protected IconUIResource getInstance() {
            return new IconUIResource(new ImageIcon(holder.get()));
        }
    }
    
    static class InvertableImageIcon extends ImageIcon implements InvertableIcon, UIResource {
        Icon invertedImage;
        public InvertableImageIcon(final Image image) {
            super(image);
        }

        @Override
        public Icon getInvertedIcon() {
            if (invertedImage != null) return invertedImage;
            return invertedImage = new IconUIResource(new ImageIcon(AquaUtils.generateLightenedImage(getImage(), 100)));
        }
    }
    
    protected static final NamedImageSingleton northArrow = new NamedImageSingleton("NSMenuScrollUp");
    protected static final IconUIResourceSingleton northArrowIcon = new IconUIResourceSingleton(northArrow);
    protected static final NamedImageSingleton southArrow = new NamedImageSingleton("NSMenuScrollDown");
    protected static final IconUIResourceSingleton southArrowIcon = new IconUIResourceSingleton(southArrow);
    protected static final NamedImageSingleton westArrow = new NamedImageSingleton("NSMenuSubmenuLeft");
    protected static final IconUIResourceSingleton westArrowIcon = new IconUIResourceSingleton(westArrow);
    protected static final NamedImageSingleton eastArrow = new NamedImageSingleton("NSMenuSubmenu");
    protected static final IconUIResourceSingleton eastArrowIcon = new IconUIResourceSingleton(eastArrow);
    
    static Image getArrowImageForDirection(final int direction) {
        switch(direction) {
            case SwingConstants.NORTH: return northArrow.get();
            case SwingConstants.SOUTH: return southArrow.get();
            case SwingConstants.EAST: return eastArrow.get();
            case SwingConstants.WEST: return westArrow.get();
        }
        return null;
    }
    
    static Icon getArrowIconForDirection(int direction) {
        switch(direction) {
            case SwingConstants.NORTH: return northArrowIcon.get();
            case SwingConstants.SOUTH: return southArrowIcon.get();
            case SwingConstants.EAST: return eastArrowIcon.get();
            case SwingConstants.WEST: return westArrowIcon.get();
        }
        return null;
    }
    
    public static Icon getMenuArrowIcon() {
        return new InvertableImageIcon(AquaUtils.generateLightenedImage(eastArrow.get(), 25));
    }
    
    public static Icon getMenuItemCheckIcon() {
        return new InvertableImageIcon(AquaUtils.generateLightenedImage(Toolkit.getDefaultToolkit().getImage("NSImage://NSMenuItemSelection"), 25));
    }
    
    public static Icon getMenuItemDashIcon() {
        return new InvertableImageIcon(AquaUtils.generateLightenedImage(Toolkit.getDefaultToolkit().getImage("NSImage://NSMenuMixedState"), 25));
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
        
        public SlicedImageControl(final Image img, final int westCut, final int eastCut, final int northCut, final int southCut, final boolean useMiddle) {
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
    
    public static Color getFocusRingColorUIResource() {
        // TODO: un-hardcode Color value
        return new Color(75, 137, 208);//SystemColorProxy(CToolkit.getAppleColor(CToolkit.KEYBOARD_FOCUS_COLOR));
    }
    
    public static Color getSelectionInactiveBackgroundColorUIResource() {
        // TODO: un-hardcode Color value
        return new Color(202, 202, 202);//SystemColorProxy(CToolkit.getAppleColor(CToolkit.INACTIVE_SELECTION_BACKGROUND_COLOR));
    }
    
    public static Color getSelectionInactiveForegroundColorUIResource() {
        // TODO: un-hardcode Color value
        return new Color(0, 0, 0);//SystemColorProxy(CToolkit.getAppleColor(CToolkit.INACTIVE_SELECTION_FOREGROUND_COLOR));
    }
}
