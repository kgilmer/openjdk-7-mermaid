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

#include "LWCToolkit.h"

/*
 * Class:     sun_awt_CGraphicsEnvironment
 * Method:    initCocoa
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_awt_CGraphicsEnvironment_initCocoa
  (JNIEnv *env, jclass self)
{
    AR_POOL(pool);

    /* Inform Cocoa that we're multi-threaded. Creating a short-lived NSThread is the
     * recommended way of doing so. */
    [NSThread detachNewThreadSelector: @selector(sharedApplication) toTarget: [NSApplication class] withObject: nil];

    [pool drain];
}

/*
 * Class:     sun_awt_CGraphicsEnvironment
 * Method:    getDisplayIDs
 * Signature: ()[I
 */
JNIEXPORT jintArray JNICALL
Java_sun_awt_CGraphicsEnvironment_getDisplayIDs
  (JNIEnv *env, jclass class)
{
    CGDisplayCount displayCount;
    CGDirectDisplayID *displays = NULL;
    jintArray ret = NULL;

    /* Get the count */
    if (CGGetOnlineDisplayList(0, NULL, &displayCount) != kCGErrorSuccess) {
        JNU_ThrowInternalError(env, "CGGetOnlineDisplayList() failed to get display count");
        return NULL;
    }

    /* Allocate an array and get the size list of display Ids */
    displays = malloc(sizeof(CGDirectDisplayID) * displayCount);
    if (displays == NULL)
        return NULL;

    if (CGGetOnlineDisplayList(displayCount, displays, &displayCount) != kCGErrorSuccess) {
        JNU_ThrowInternalError(env, "CGGetOnlineDisplayList() failed to get display list");
        goto cleanup;
    }

    /* Allocate a java array for display identifiers */
    ret = (*env)->NewIntArray(env, displayCount);
    if (ret == NULL)
        goto cleanup;

    /* Initialize and return the backing int array */
    assert(sizeof(jint) >= sizeof(CGDirectDisplayID));
    {
        jint *elems = (*env)->GetIntArrayElements(env, ret, 0);

        CGDisplayCount i;
        for (i = 0; i < displayCount; i++) {
            elems[i] = displays[i];
        }

        (*env)->ReleaseIntArrayElements(env, ret, elems, 0);
    }

cleanup:
    if (displays != NULL)
        free(displays);

    return ret;
}

/*
 * Class:     sun_awt_CGraphicsEnvironment
 * Method:    getMainDisplayID
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_sun_awt_CGraphicsEnvironment_getMainDisplayID
  (JNIEnv *env, jclass class)
{
    return CGMainDisplayID();
}

/*
 * Context data for handling CoreGraphics display changed callbacks.
 */
struct DisplayCallback {
    JavaVM *vm;
    jobject instance;
};

/*
 * Post the display reconfiguration event.
 */
static void displaycb_handle
  (CGDirectDisplayID display, CGDisplayChangeSummaryFlags flags, void *userInfo)
{
    JNIEnv *env;
    struct DisplayCallback *cb = userInfo;
    jboolean unbind = JNI_FALSE;

    /* Skip pre-change notification */
    if (flags == kCGDisplayBeginConfigurationFlag)
        return;

    /* Is the thread already attached? Try getting the environment. */
    env = JNU_GetEnv(cb->vm, JNI_VERSION_1_2);
    if (env == NULL || env == (void *) JNI_ERR) {
        /* Not bound, bind this thread to the VM */
        unbind = JNI_TRUE;
        if ((*(cb->vm))->AttachCurrentThread(cb->vm, (void **)&env, (void *)NULL) != 0) {
            /* If this fails, we can not do anything else here */
            return;
        }
    }

    /* If the weak reference is not valid, return */
    if ((*env)->IsSameObject(env, cb->instance, NULL) == JNI_TRUE)
        return;

    /* Reference is valid, environment is live, send the message */
    JNU_CallMethodByName(env, NULL, cb->instance, "_displayReconfiguration", "(J)V", (jlong) display);

    /* Unbind this thread, if necessary */
    if (unbind)
        (*(cb->vm))->DetachCurrentThread(cb->vm);
}

/*
 * Free any resources held by the DisplayCallback instance.
 */
static void displaycb_free (JNIEnv *env, struct DisplayCallback *cb) {
    if (cb->instance != NULL)
        (*env)->DeleteWeakGlobalRef(env, cb->instance);
    free(cb);
}

/*
 * Class:     sun_awt_CGraphicsEnvironment
 * Method:    registerDisplayReconfiguration
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_sun_awt_CGraphicsEnvironment_registerDisplayReconfiguration
  (JNIEnv *env, jobject this)
{
    struct DisplayCallback *cb;

    /* Allocate our new DisplayCallback */
    cb = calloc(1, sizeof(*cb));
    if (cb == NULL) {
        JNU_ThrowOutOfMemoryError(env, "Could not malloc() DisplayCallback struct");
        return 0L;
    }

    /* Save a reference to the VM */
    if ((*env)->GetJavaVM(env, &(cb->vm)) != 0) {
        JNU_ThrowInternalError(env, "Could not get Java VM reference");
        goto error;
    }

    /* Save a weak reference to our instance */
    cb->instance = (*env)->NewWeakGlobalRef(env, this);
    if (cb->instance == NULL) {
        goto error;
    }

    /* Register the callback */
    if (CGDisplayRegisterReconfigurationCallback(&displaycb_handle, cb) != kCGErrorSuccess) {
        JNU_ThrowInternalError(env, "CGDisplayRegisterReconfigurationCallback() failed");
        goto error;
    }

    /* All done, return the handle */
    return PTRLONG(cb);

error:
    displaycb_free(env, cb);
    return 0L;
}

/*
 * Class:     sun_awt_CGraphicsEnvironment
 * Method:    deregisterDisplayReconfiguration
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_sun_awt_CGraphicsEnvironment_deregisterDisplayReconfiguration
  (JNIEnv *env, jobject this, jlong p)
{
    struct DisplayCallback *cb = PTR(p);

    /* Remove the registration */
    if (CGDisplayRemoveReconfigurationCallback(&displaycb_handle, cb) != kCGErrorSuccess) {
        JNU_ThrowInternalError(env, "CGDisplayRemoveReconfigurationCallback() failed, leaking the callback context!");
        return;
    }

    /* Free the context */
    displaycb_free(env, cb);
}
