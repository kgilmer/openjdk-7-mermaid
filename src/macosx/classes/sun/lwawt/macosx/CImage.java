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
import java.awt.image.*;

import sun.awt.image.SunWritableRaster;
import sun.java2d.*;

public class CImage {
    private long nsImagePtr;
    private Object disposerReferent;
    
    private CImage(long nsImagePtr, boolean releaseOnGC) {
        this.nsImagePtr = nsImagePtr;
        if (releaseOnGC && nsImagePtr != 0) {
            this.disposerReferent = new Object();
            Disposer.addRecord(disposerReferent, new CImageDisposerRecord(nsImagePtr));
        }
    }
    
    public long getNSImagePtr() {
        return nsImagePtr;
    }
    
    public static CImage wrapNSImage(long ptr, boolean releaseOnGC) {
        return new CImage(ptr, releaseOnGC);
    }
    
    public static CImage fromImage(Image img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2 = bimg.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        int[] buffer = ((DataBufferInt)bimg.getRaster().getDataBuffer()).getData();
        return new CImage(createNSImage(buffer, w, h), true);
    }
    
    public static CImage fromFile(String s) {
        return new CImage(createNSImageFromFile(s), true);
    }
    
    public static CImage fromIcon(String s) {
        return new CImage(createNSImageFromIcon(getSelectorAsInt(s)), true);
    }
    
    public static CImage fromName(String s) {
        return new CImage(createNSImageFromName(s), true);
    }
    
    /** @return A BufferedImage created from nsImagePtr, or null. */
    public Image toImage() {
        if (nsImagePtr == 0)
            return null;
        final int w = getNSImageWidth(nsImagePtr);
        final int h = getNSImageHeight(nsImagePtr);
        final BufferedImage bimg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
        final DataBufferInt dbi = (DataBufferInt)bimg.getRaster().getDataBuffer();
        final int[] buffer = SunWritableRaster.stealData(dbi, 0);
        copyNSImageIntoArray(nsImagePtr, buffer, w, h);
        SunWritableRaster.markDirty(dbi);
        return bimg;
    }
    
    /** If nsImagePtr != 0 then scale this NSImage. @return *this* */
    public CImage resize(int w, int h) {
        if (nsImagePtr != 0)
            setNSImageSize(nsImagePtr, w, h);
        return this;
    }
    
    private static int getSelectorAsInt(final String fromString) {
        final byte[] b = fromString.getBytes();
        final int len = Math.min(b.length, 4);
        int result = 0;
        for (int i = 0; i < len; i++) {
            if (i > 0)
                result <<= 8;
            result |= (b[i] & 0xff);
        }
        return result;
    }
    
    private static native long createNSImage(int[] buffer, int w, int h);
    private static native void disposeNSImage(long nsImagePtr);
    private static native long createNSImageFromFile(String file);
    private static native long createNSImageFromIcon(int selector);
    private static native long createNSImageFromName(String name);
    private static native void copyNSImageIntoArray(long image, int[] buffer, int w, int h);
    private static native int getNSImageWidth(long image);
    private static native int getNSImageHeight(long image);
    private static native void setNSImageSize(long image, int w, int h);
    
    private static class CImageDisposerRecord implements DisposerRecord {
        private long nsImagePtr;
        
        public CImageDisposerRecord(long nsImagePtr) {
            this.nsImagePtr = nsImagePtr;
        }
        
        public synchronized void dispose() {
            if (nsImagePtr != 0L) {
                disposeNSImage(nsImagePtr);
                nsImagePtr = 0L;
            }
        }
    }
}
