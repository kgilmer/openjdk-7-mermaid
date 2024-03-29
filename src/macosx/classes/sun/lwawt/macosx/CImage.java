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

package sun.lwawt.macosx;

import java.awt.*;
import java.awt.geom.Dimension2D;
import java.awt.image.*;

import sun.awt.image.SunWritableRaster;

public class CImage extends CFRetainedResource {
    private static native long nativeCreateNSImageFromArray(int[] buffer, int w, int h);
    private static native long nativeCreateNSImageFromFileContents(String file);
    private static native long nativeCreateNSImageOfFileFromLaunchServices(String file);
    private static native long nativeCreateNSImageFromImageName(String name);
    private static native long nativeCreateNSImageFromIconSelector(int selector);
    private static native void nativeCopyNSImageIntoArray(long image, int[] buffer, int w, int h);
    private static native Dimension2D nativeGetNSImageSize(long image);
    private static native void nativeSetNSImageSize(long image, double w, double h);
    
    static Creator creator = new Creator();
    static Creator getCreator() {
        return creator;
    }
    
    public static class Creator {
        Creator() { }
        
        // This is used to create a CImage with an NSImage pointer. It MUST be a CFRetained
        // NSImage, and the CImage takes ownership of the non-GC retain. If callers need the
        // NSImage themselves, they MUST call retain on the NSImage themselves.
        public BufferedImage createImageUsingNativeSize(final long image) {
            if (image == 0) return null;
            final Dimension2D size = nativeGetNSImageSize(image);
            return createBufferedImage(image, size.getWidth(), size.getHeight());
        }
        
        // the width and height passed in as a parameter could differ than the width and the height of the NSImage (image), in that case, the image will be scaled
        BufferedImage createBufferedImage(long image, double width, double height) {
            if (image == 0) throw new Error("Unable to instantiate CImage with null native image reference.");
            return createImageWithSize(image, width, height);
        }
        
        public BufferedImage createImageWithSize(final long image, final double width, final double height) {
            final CImage img = new CImage(image);
            img.resize(width, height);
            return img.toImage();
        }
        
        // This is used to create a CImage that represents the icon of the given file.
        public BufferedImage createImageOfFile(final String file, final int width, final int height) {
            return createBufferedImage(nativeCreateNSImageOfFileFromLaunchServices(file), width, height);
        }
        
        public BufferedImage createImageFromFile(final String file, final double width, final double height) {
            final long image = nativeCreateNSImageFromFileContents(file);
            nativeSetNSImageSize(image, width, height);    
            return createBufferedImage(image, width, height);
        }
    
        public BufferedImage createSystemImageFromSelector(final String iconSelector, final int width, final int height) {
            return createBufferedImage(nativeCreateNSImageFromIconSelector(getSelectorAsInt(iconSelector)), width, height);
        }
        
        public Image createImageFromName(final String name, final int width, final int height) {
            return createBufferedImage(nativeCreateNSImageFromImageName(name), width, height);
        }
        
        public Image createImageFromName(final String name) {
            return createImageUsingNativeSize(nativeCreateNSImageFromImageName(name));
        }
    
        // This is used to create a CImage from a Image
        public CImage createFromImage(final Image image) {
            if (image == null) return null;
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics2D g2 = bimg.createGraphics();
            g2.setComposite(AlphaComposite.Src);
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
            int[] buffer = ((DataBufferInt)bimg.getRaster().getDataBuffer()).getData();
            return new CImage(nativeCreateNSImageFromArray(buffer, w, h));
        }
        
        static int getSelectorAsInt(final String fromString) {
            final byte[] b = fromString.getBytes();
            final int len = Math.min(b.length, 4);
            int result = 0;
            for (int i = 0; i < len; i++) {
                if (i > 0) result <<= 8;
                result |= (b[i] & 0xff);
            }
            return result;
        }
    }
    
    CImage(long nsImagePtr) {
        super(nsImagePtr, true);
    }
    
    /** @return A BufferedImage created from nsImagePtr, or null. */
    public BufferedImage toImage() {
        if (ptr == 0) return null;
        
        final Dimension2D size = nativeGetNSImageSize(ptr);
        final int w = (int)size.getWidth();
        final int h = (int)size.getHeight();
        
        final BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        final DataBufferInt dbi = (DataBufferInt)bimg.getRaster().getDataBuffer();
        final int[] buffer = SunWritableRaster.stealData(dbi, 0);
        nativeCopyNSImageIntoArray(ptr, buffer, w, h);
        SunWritableRaster.markDirty(dbi);
        return bimg;
    }
    
    /** If nsImagePtr != 0 then scale this NSImage. @return *this* */
    CImage resize(final double w, final double h) {
        if (ptr != 0) nativeSetNSImageSize(ptr, w, h);
        return this;
    }
}
