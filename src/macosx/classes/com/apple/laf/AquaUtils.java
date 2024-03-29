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
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;

import sun.awt.AppContext;

import sun.lwawt.macosx.CImage;
import sun.lwawt.macosx.CImage.Creator;
import sun.swing.SwingUtilities2;

import com.apple.laf.AquaImageFactory.SlicedImageControl;

public class AquaUtils {
    final static String ANIMATIONS_SYSTEM_PROPERTY = "swing.enableAnimations";
    
    /*
     * Convenience function for determining ComponentOrientation.  Helps us
     * avoid having Munge directives throughout the code.
     */
    public static boolean isLeftToRight(final Component c) {
        return c.getComponentOrientation().isLeftToRight();
    }
    
    public static void enforceComponentOrientation(Component c, ComponentOrientation orientation) {
        c.setComponentOrientation(orientation);
        if (c instanceof Container) {
            for (Component child : ((Container)c).getComponents()) {
                enforceComponentOrientation(child, orientation);
            }
        }
    }
    
    private static CImage.Creator getCImageCreatorInternal() {
        return java.security.AccessController.doPrivileged(new PrivilegedAction<CImage.Creator>() {
            public Creator run() {
                try {
                    final Method getCreatorMethod = CImage.class.getDeclaredMethod("getCreator", new Class[] {});
                    getCreatorMethod.setAccessible(true);
                    return (CImage.Creator)getCreatorMethod.invoke(null, new Object[] {});
                } catch (final Exception e) {
                    return null;
                }
            }
        });
    }
    
    private static final RecyclableSingleton<CImage.Creator> cImageCreator = new RecyclableSingleton<CImage.Creator>() {
        @Override
        protected Creator getInstance() {
            return getCImageCreatorInternal();
        }
    };
    static CImage.Creator getCImageCreator() {
        return cImageCreator.get();
    }
    
    protected static Image generateSelectedDarkImage(final Image image) {
        final ImageProducer prod = new FilteredImageSource(image.getSource(), new IconImageFilter() {
            int getGreyFor(final int gray) {
                return gray * 75 / 100;
            }
        });
        return Toolkit.getDefaultToolkit().createImage(prod);
    }
    
    protected static Image generateDisabledImage(final Image image) {
        final ImageProducer prod = new FilteredImageSource(image.getSource(), new IconImageFilter() {
            int getGreyFor(final int gray) {
                return 255 - ((255 - gray) * 65 / 100);
            }
        });
        return Toolkit.getDefaultToolkit().createImage(prod);
    }
    
    protected static Image generateLightenedImage(final Image image, final int percent) {
        final GrayFilter filter = new GrayFilter(true, percent);
        final ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(prod);
    }
    
    static abstract class IconImageFilter extends RGBImageFilter {
        public IconImageFilter() {
            super();
            canFilterIndexColorModel = true;
        }
        
        public int filterRGB(final int x, final int y, final int rgb) {
            final int red = (rgb >> 16) & 0xff;
            final int green = (rgb >> 8) & 0xff;
            final int blue = rgb & 0xff;
            final int gray = getGreyFor((int)((0.30 * red + 0.59 * green + 0.11 * blue) / 3));

            return (rgb & 0xff000000) | (grayTransform(red, gray) << 16) | (grayTransform(green, gray) << 8) | (grayTransform(blue, gray) << 0);
        }

        private static int grayTransform(final int color, final int gray) {
            int result = color - gray;
            if (result < 0) result = 0;
            if (result > 255) result = 255;
            return result;
        }
        
        abstract int getGreyFor(final int gray);
    }
    
    public abstract static class RecyclableObject<T> {
        protected SoftReference<T> objectRef = null;
        
        public T get() {
            T referent = null;
            if (objectRef != null && (referent = objectRef.get()) != null) return referent;
            referent = create();
            objectRef = new SoftReference<T>(referent);
            return referent;
        }
        
        protected abstract T create();
    }
    
    public abstract static class RecyclableSingleton<T> {
        public T get() {
            final AppContext appContext = AppContext.getAppContext();
            SoftReference<T> ref = (SoftReference<T>) appContext.get(this);
            if (ref != null) {
                final T object = ref.get();
                if (object != null) return object;
            }
            final T object = getInstance();
            ref = new SoftReference<T>(object);
            appContext.put(this, ref);
            return object;
        }
        
        public void reset() {
            AppContext appContext = AppContext.getAppContext();
            appContext.remove(this);
        }
        
        protected abstract T getInstance();
    }
    
    public static class RecyclableSingletonFromDefaultConstructor<T> extends RecyclableSingleton<T> {
        protected final Class<T> clazz;
        
        public RecyclableSingletonFromDefaultConstructor(final Class<T> clazz) {
            this.clazz = clazz;
        }
        
        protected T getInstance() {
            try {
                return clazz.newInstance();
            } catch (final InstantiationException e) {
                e.printStackTrace();
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    
    public abstract static class LazyKeyedSingleton<K, V> {
        protected Map<K, V> refs;
        
        public V get(final K key) {
            if (refs == null) refs = new HashMap<K, V>();
            
            final V cachedValue = refs.get(key);
            if (cachedValue != null) return cachedValue;
            
            final V value = getInstance(key);
            refs.put(key, value);
            return value;
        }
        
        protected abstract V getInstance(final K key);
    }
    
    static final RecyclableSingleton<Boolean> enableAnimations = new RecyclableSingleton<Boolean>() {
        @Override
        protected Boolean getInstance() {
            final String sizeProperty = (String)java.security.AccessController.doPrivileged((PrivilegedAction<?>)new sun.security.action.GetPropertyAction(ANIMATIONS_SYSTEM_PROPERTY));
            return new Boolean(!"false".equals(sizeProperty)); // should be true by default
        }
    };
    static boolean animationsEnabled() {
        return enableAnimations.get();
    }
    
    static final int MENU_BLINK_DELAY = 50; // 50ms == 3/60 sec, according to the spec
    protected static void blinkMenu(final Selectable selectable) {
        if (!animationsEnabled()) return;
        try {
            selectable.paintSelected(false);
            Thread.sleep(MENU_BLINK_DELAY);
            selectable.paintSelected(true);
            Thread.sleep(MENU_BLINK_DELAY);
        } catch (final InterruptedException e) { }
    }
    
    interface Selectable {
        void paintSelected(final boolean selected);
    }
    
    interface JComponentPainter {
        public void paint(JComponent c, Graphics g, int x, int y, int w, int h);
    }
    
    interface Painter {
        public void paint(final Graphics g, int x, int y, int w, int h);
    }
    
    public static void paintDropShadowText(final Graphics g, final JComponent c, final Font font, final FontMetrics metrics, final int x, final int y, final int offsetX, final int offsetY, final Color textColor, final Color shadowColor, final String text) {
        g.setFont(font);
        g.setColor(shadowColor);
        SwingUtilities2.drawString(c, g, text, x + offsetX, y + offsetY + metrics.getAscent());
        g.setColor(textColor);
        SwingUtilities2.drawString(c, g, text, x, y + metrics.getAscent());
    }
    
    public static class ShadowBorder implements Border {
        final Painter prePainter;
        final Painter postPainter;
        
        final int offsetX;
        final int offsetY;
        final float distance;
        final int blur;
        final Insets insets;
        final ConvolveOp blurOp;
        
        public ShadowBorder(final Painter prePainter, final Painter postPainter, final int offsetX, final int offsetY, final float distance, final float intensity, final int blur) {
            this.prePainter = prePainter; this.postPainter = postPainter;
            this.offsetX = offsetX; this.offsetY = offsetY; this.distance = distance; this.blur = blur;
            final int halfBlur = blur / 2;
            this.insets = new Insets(halfBlur - offsetY, halfBlur - offsetX, halfBlur + offsetY, halfBlur + offsetX);
            
            final float blurry = intensity / (blur * blur);
            final float[] blurKernel = new float[blur * blur];
            for (int i = 0; i < blurKernel.length; i++) blurKernel[i] = blurry;
            blurOp = new ConvolveOp(new Kernel(blur, blur, blurKernel));
        }
        
        public boolean isBorderOpaque() {
            return false;
        }
        
        public Insets getBorderInsets(final Component c) {
            return insets;
        }
        
        public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
            final BufferedImage img = new BufferedImage(width + blur * 2, height + blur * 2, BufferedImage.TYPE_INT_ARGB_PRE);
            paintToImage(img, x, y, width, height);
//            debugFrame("border", img);
            g.drawImage(img, -blur, -blur, null);
        }
        
        protected void paintToImage(final BufferedImage img, final int x, final int y, final int width, final int height) {
            // clear the prior image
            Graphics2D imgG = (Graphics2D)img.getGraphics();
            imgG.setComposite(AlphaComposite.Clear);
            imgG.setColor(Color.black);
            imgG.fillRect(0, 0, width + blur * 2, height + blur * 2);
            
            final int adjX = (int)(x + blur + offsetX + (insets.left * distance));
            final int adjY = (int)(y + blur + offsetY + (insets.top * distance));
            final int adjW = (int)(width - (insets.left + insets.right) * distance);
            final int adjH = (int)(height - (insets.top + insets.bottom) * distance);
            
            // let the delegate paint whatever they want to be blurred
            imgG.setComposite(AlphaComposite.DstAtop);
            if (prePainter != null) prePainter.paint(imgG, adjX, adjY, adjW, adjH);
            imgG.dispose();
            
            // blur the prior image back into the same pixels
            imgG = (Graphics2D)img.getGraphics();
            imgG.setComposite(AlphaComposite.DstAtop);
            imgG.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            imgG.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            imgG.drawImage(img, blurOp, 0, 0);
            
            if (postPainter != null) postPainter.paint(imgG, adjX, adjY, adjW, adjH);
            imgG.dispose();
        }
    }
    
    public static class SlicedShadowBorder extends ShadowBorder {
        final SlicedImageControl slices;
        
        public SlicedShadowBorder(final Painter prePainter, final Painter postPainter, final int offsetX, final int offsetY, final float distance, final float intensity, final int blur, final int templateWidth, final int templateHeight, final int leftCut, final int topCut, final int rightCut, final int bottomCut) {
            super(prePainter, postPainter, offsetX, offsetY, distance, intensity, blur);
            
            final BufferedImage i = new BufferedImage(templateWidth, templateHeight, BufferedImage.TYPE_INT_ARGB_PRE);
            super.paintBorder(null, i.getGraphics(), 0, 0, templateWidth, templateHeight);
//            debugFrame("slices", i);
            slices = new SlicedImageControl(i, leftCut, topCut, rightCut, bottomCut, false);
        }
        
        public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
            slices.paint(g, x, y, width, height);
        }
    }
    
    public interface NineSliceMetricsProvider {
        
    }
    
//    static void debugFrame(String name, Image image) {
//        JFrame f = new JFrame(name);
//        f.setContentPane(new JLabel(new ImageIcon(image)));
//        f.pack();
//        f.setVisible(true);
//    }
    
    // special casing naughty applications, like InstallAnywhere
    // <rdar://problem/4851533> REGR: JButton: Myst IV: the buttons of 1.0.3 updater have redraw issue
    static boolean shouldUseOpaqueButtons() {
        final ClassLoader launcherClassLoader = sun.misc.Launcher.getLauncher().getClassLoader();
        if (classExists(launcherClassLoader, "com.installshield.wizard.platform.macosx.MacOSXUtils")) return true;
        return false;
    }
        
    static boolean classExists(final ClassLoader classLoader, final String clazzName) {
        try {
            return Class.forName(clazzName, false, classLoader) != null;
        } catch (final Throwable e) { }
        return false;
    }
    
    private static RecyclableSingleton<Method> getJComponentGetFlagMethod = new RecyclableSingleton<Method>() {
        protected Method getInstance() {
            return java.security.AccessController.doPrivileged(
                new PrivilegedAction<Method>() {
                    public Method run() {
                        try {
                            final Method method = JComponent.class.getDeclaredMethod("getFlag", new Class[] { int.class });
                            method.setAccessible(true);
                            return method;
                        } catch (final Throwable e) {
                            return null;
                        }
                    }
                }
            );
        }
    };
    
    private static final Integer OPAQUE_SET_FLAG = new Integer(24); // private int JComponent.OPAQUE_SET
    protected static boolean hasOpaqueBeenExplicitlySet(final JComponent c) {
        final Method method = getJComponentGetFlagMethod.get();
        if (method == null) return false;
        try {
            return Boolean.TRUE.equals(method.invoke(c, OPAQUE_SET_FLAG));
        } catch (final Throwable e) {
            return false;
        }
    }
}
