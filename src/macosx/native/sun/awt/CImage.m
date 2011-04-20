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

#import <AppKit/NSImage.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "sun_lwawt_macosx_CImage.h"

static void
CImage_CopyArrayIntoNSImageRep(jint *srcPixels, jint *dstPixels,
                               int width, int height)
{
    int x, y;
    // TODO: test this on big endian systems (not sure if its correct)...
    for (y = 0; y < height; y++) {
        for (x = 0; x < width; x++) {
            jint pix = srcPixels[x];
            jint a = (pix >> 24) & 0xff;
            jint r = (pix >> 16) & 0xff;
            jint g = (pix >>  8) & 0xff;
            jint b = (pix      ) & 0xff;
            dstPixels[x] = (b << 24) | (g << 16) | (r << 8) | a;
        }
        srcPixels += width; // TODO: use explicit scanStride
        dstPixels += width;
    }
}

static void
CImage_CopyNSImageIntoArray(NSImage *srcImage, jint *dstPixels,
                            int width, int height)
{
    NSBitmapImageRep *srcRep =
        [NSBitmapImageRep imageRepWithData: [srcImage TIFFRepresentation]];
    jint *srcPixels = (jint *)[srcRep bitmapData];
    int x, y;
    // TODO: test this on big endian systems (not sure if its correct)...
    for (y = 0; y < height; y++) {
        for (x = 0; x < width; x++) {
            jint pix = srcPixels[x];
            jint a = (pix >> 24) & 0xff;
            jint b = (pix >> 16) & 0xff;
            jint g = (pix >>  8) & 0xff;
            jint r = (pix      ) & 0xff;
            dstPixels[x] = (a << 24) | (r << 16) | (g << 8) | b;
        }
        srcPixels += width; // TODO: use explicit scanStride
        dstPixels += width;
    }
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    createNSImage
 * Signature: ([III)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CImage_createNSImage
    (JNIEnv *env, jclass klass,
     jintArray buffer, jint width, jint height)
{
    jlong result = 0L;

    JNF_COCOA_ENTER(env);

    NSBitmapImageRep* imageRep =
        [[NSBitmapImageRep alloc]
            initWithBitmapDataPlanes:NULL
                pixelsWide:width
                pixelsHigh:height
                bitsPerSample:8
                samplesPerPixel:4
                hasAlpha:YES
                isPlanar:NO
                colorSpaceName:NSDeviceRGBColorSpace
                bitmapFormat:NSAlphaFirstBitmapFormat
                bytesPerRow:width*4 // TODO: use explicit scanStride
                bitsPerPixel:32];

    jint *imgData = (jint *)[imageRep bitmapData];
    if (imgData != NULL) {
        jint *src = (*env)->GetPrimitiveArrayCritical(env, buffer, NULL);
        if (src != NULL) {
            CImage_CopyArrayIntoNSImageRep(src, imgData, width, height);

            (*env)->ReleasePrimitiveArrayCritical(env, buffer, src, JNI_ABORT);

            NSImage *nsImage =
                [[NSImage alloc] initWithSize:NSMakeSize(width, height)];
            [nsImage addRepresentation:imageRep];
            [imageRep release];

            if (nsImage != nil) {
                CFRetain(nsImage);
            }

            result = ptr_to_jlong(nsImage);
        }
    }

    JNF_COCOA_EXIT(env);

    return result;
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    createNSImageFromIcon
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CImage_createNSImageFromIcon
    (JNIEnv *env, jclass klass, jint selector)
{
    NSImage *image = NULL;
    JNF_COCOA_ENTER(env);

    IconRef iconRef;
    if (noErr == GetIconRef(kOnSystemDisk, kSystemIconsCreator, selector, &iconRef)) {
        image = [[NSImage alloc] initWithIconRef:iconRef];
        if (image) CFRetain(image); // GC
        ReleaseIconRef(iconRef);
    }

    JNF_COCOA_EXIT(env);
    return ptr_to_jlong(image);
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    createNSImageFromFile
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CImage_createNSImageFromFile
    (JNIEnv *env, jclass klass, jstring file)
{
    NSImage *image = NULL;
    JNF_COCOA_ENTER(env);

    NSString *s = JNFJavaToNSString(env, file);
    image = [[NSImage alloc] initByReferencingFile:s];
    if(image) CFRetain(image); // GC

    JNF_COCOA_EXIT(env);
    return ptr_to_jlong(image);
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    createNSImageFromName
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CImage_createNSImageFromName
    (JNIEnv *env, jclass klass, jstring name)
{
    NSImage *image = NULL;
    JNF_COCOA_ENTER(env);

    image = [NSImage imageNamed:JNFJavaToNSString(env, name)];
    if (image) CFRetain(image); // GC

    JNF_COCOA_EXIT(env);
    return ptr_to_jlong(image);
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    disposeNSImage
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CImage_disposeNSImage
    (JNIEnv *env , jclass klass, jlong nsImagePtr)
{
   CFRelease(jlong_to_ptr(nsImagePtr));
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    copyNSImageIntoArray
 * Signature: (J[III)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CImage_copyNSImageIntoArray
    (JNIEnv *env, jclass klass, jlong jnsImage, jintArray buffer, jint w, jint h)
{
    jint *dst = (*env)->GetPrimitiveArrayCritical(env, buffer, NULL);
    if (dst) {
        CImage_CopyNSImageIntoArray(jlong_to_ptr(jnsImage), dst, w, h);
        (*env)->ReleasePrimitiveArrayCritical(env, buffer, dst, JNI_ABORT);
    }
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    getNSImageWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_sun_lwawt_macosx_CImage_getNSImageWidth
    (JNIEnv *env, jclass clazz, jlong image)
{
    if (!image) return 0;
    jint width;
    JNF_COCOA_ENTER(env);
    width = [(NSImage *)jlong_to_ptr(image) size].width;
    JNF_COCOA_EXIT(env);
    return width;
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    getNSImageHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_sun_lwawt_macosx_CImage_getNSImageHeight
    (JNIEnv *env, jclass clazz, jlong image)
{
    if (!image) return 0;
    jint height;
    JNF_COCOA_ENTER(env);
    height = [(NSImage *)jlong_to_ptr(image) size].height;
    JNF_COCOA_EXIT(env);
    return height;
}

/*
 * Class:     sun_lwawt_macosx_CImage
 * Method:    setNSImageSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_CImage_setNSImageSize
    (JNIEnv *env, jclass clazz, jlong image, jint w, jint h)
{
    if (!image) return;
    NSImage *i = (NSImage *)jlong_to_ptr(image);
    JNF_COCOA_ENTER(env);
    [i setScalesWhenResized:TRUE];
    [i setSize:NSMakeSize(w, h)];
    JNF_COCOA_EXIT(env);
}
