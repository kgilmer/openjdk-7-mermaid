/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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

#include <stdio.h>
#include <dlfcn.h>
#include <string.h>
#include <stdlib.h>
#include <jni.h>
#include <jni_util.h>
#include <jvm.h>
#include "gdefs.h"

#include <sys/param.h>
#include <sys/utsname.h>

#include "awt_Plugin.h"

#ifdef DEBUG
#define VERBOSE_AWT_DEBUG
#endif

static void *awtHandle = NULL;

typedef JNIEXPORT jint JNICALL JNI_OnLoad_type(JavaVM *vm, void *reserved);

/* Initialize the Java VM instance variable when the library is
   first loaded */
JavaVM *jvm;

JNIEXPORT jboolean JNICALL AWTIsHeadless() {
    static JNIEnv *env = NULL;
    static jboolean isHeadless;
    jmethodID headlessFn;
    jclass graphicsEnvClass;

    if (env == NULL) {
        env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
        graphicsEnvClass = (*env)->FindClass(env,
                                             "java/awt/GraphicsEnvironment");
        if (graphicsEnvClass == NULL) {
            return JNI_TRUE;
        }
        headlessFn = (*env)->GetStaticMethodID(env,
                                               graphicsEnvClass, "isHeadless", "()Z");
        if (headlessFn == NULL) {
            return JNI_TRUE;
        }
        isHeadless = (*env)->CallStaticBooleanMethod(env, graphicsEnvClass,
                                                     headlessFn);
    }
    return isHeadless;
}

jint
AWT_OnLoad(JavaVM *vm, void *reserved)
{
    Dl_info dlinfo;
    char buf[MAXPATHLEN];
    int32_t len;
    char *p, *tk = 0;
    JNI_OnLoad_type *JNI_OnLoad_ptr;
    struct utsname name;
    JNIEnv *env = (JNIEnv *)JNU_GetEnv(vm, JNI_VERSION_1_2);
    void *v;
    char *envvar;
    jstring toolkit = NULL, grenv = NULL, fmanager = NULL;
    jstring tkProp = NULL, geProp = NULL, fmProp = NULL;

    if (awtHandle != NULL) {
        /* Avoid several loading attempts */
        return JNI_VERSION_1_2;
    }

    jvm = vm;

    /* Get address of this library and the directory containing it. */
    dladdr((void *)JNI_OnLoad, &dlinfo);
    realpath((char *)dlinfo.dli_fname, buf);
    len = strlen(buf);
    p = strrchr(buf, '/');

    /*
     * The code below is responsible for:
     * 1. Loading appropriate awt library, i.e. xawt/libmawt or headless/libwawt
     * 2. Setting "awt.toolkit" system property to use the appropriate Java toolkit class,
     *    (if user has specified the toolkit in env varialble)
     */

    tkProp = (*env)->NewStringUTF(env, "awt.toolkit");
    geProp = (*env)->NewStringUTF(env, "java.awt.graphicsenv");
    fmProp = (*env)->NewStringUTF(env, "sun.font.fontmanager");
    /* Check if toolkit is specified in env variable */
#ifdef MACOSX
    envvar = getenv("AWT_TOOLKIT");
    if (envvar && strstr(envvar, "XToolkit")) {
#endif
    toolkit = (*env)->NewStringUTF(env, "sun.awt.X11.XToolkit");
	grenv = (*env)->NewStringUTF(env, "sun.awt.X11GraphicsEnvironment");
	fmanager = (*env)->NewStringUTF(env, "sun.awt.X11FontManager");
	tk = "/xawt/libmawt";
#ifdef MACOSX
    } else {
    toolkit = (*env)->NewStringUTF(env, "sun.lwawt.macosx.LWCToolkit");
	grenv = (*env)->NewStringUTF(env, "sun.awt.CGraphicsEnvironment");
	fmanager = (*env)->NewStringUTF(env, "sun.font.CFontManager");
	tk = "/lwawt/liblwawt";
    }
#endif
    if (toolkit && tkProp) {
        JNU_CallStaticMethodByName(env, NULL, "java/lang/System", "setProperty",
	                           "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                   tkProp, toolkit);
    }
    if (grenv && geProp) {
        JNU_CallStaticMethodByName(env, NULL, "java/lang/System", "setProperty",
                                   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                   geProp, grenv);
    }
    if (fmanager && fmProp) {
        JNU_CallStaticMethodByName(env, NULL, "java/lang/System", "setProperty",
                                   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                   fmProp, fmanager);
    }

    /* Calculate library name to load */
#ifndef MACOSX
    if (AWTIsHeadless()) {
        strcpy(p, "/headless/libmawt");
    } else if (tk) {
#endif
        strcpy(p, tk);
#ifndef MACOSX
    }
#endif

    if (toolkit) {
        (*env)->DeleteLocalRef(env, toolkit);
    }
    if (tkProp) {
        (*env)->DeleteLocalRef(env, tkProp);
    }
    if (grenv) {
        (*env)->DeleteLocalRef(env, grenv);
    }
    if (geProp) {
        (*env)->DeleteLocalRef(env, geProp);
    }

#ifdef MACOSX
    strcat(p, ".dylib");
#else
    strcat(p, ".so");
#endif

    if (tk) {
        JNU_CallStaticMethodByName(env, NULL, "java/lang/System", "load",
				   "(Ljava/lang/String;)V",
				   JNU_NewStringPlatform(env, buf));
	    awtHandle = dlopen(buf, RTLD_LAZY | RTLD_GLOBAL);
    }

    return JNI_VERSION_1_2;
}

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    return AWT_OnLoad(vm, reserved);
}

/*
 * This entry point must remain in libawt.so as part of a contract
 * with the CDE variant of Java Media Framework. (sdtjmplay)
 * Reflect this call over to the correct libmawt.so.
 */
JNIEXPORT void JNICALL
Java_sun_awt_motif_XsessionWMcommand(JNIEnv *env, jobject this,
                                     jobject frame, jstring jcommand)
{
    /* type of the old backdoor function */
    typedef JNIEXPORT void JNICALL
        XsessionWMcommand_type(JNIEnv *env, jobject this,
                               jobject frame, jstring jcommand);

    static XsessionWMcommand_type *XsessionWMcommand = NULL;

    if (XsessionWMcommand == NULL && awtHandle == NULL) {
        return;
    }

    XsessionWMcommand = (XsessionWMcommand_type *)
        dlsym(awtHandle, "Java_sun_awt_motif_XsessionWMcommand");

    if (XsessionWMcommand == NULL)
        return;

    (*XsessionWMcommand)(env, this, frame, jcommand);
}


/*
 * This entry point must remain in libawt.so as part of a contract
 * with the CDE variant of Java Media Framework. (sdtjmplay)
 * Reflect this call over to the correct libmawt.so.
 */
JNIEXPORT void JNICALL
Java_sun_awt_motif_XsessionWMcommand_New(JNIEnv *env, jobjectArray jargv)
{
    typedef JNIEXPORT void JNICALL
        XsessionWMcommand_New_type(JNIEnv *env, jobjectArray jargv);

    static XsessionWMcommand_New_type *XsessionWMcommand = NULL;

    if (XsessionWMcommand == NULL && awtHandle == NULL) {
        return;
    }

    XsessionWMcommand = (XsessionWMcommand_New_type *)
        dlsym(awtHandle, "Java_sun_awt_motif_XsessionWMcommand_New");

    if (XsessionWMcommand == NULL)
        return;

    (*XsessionWMcommand)(env, jargv);
}


#define REFLECT_VOID_FUNCTION(name, arglist, paramlist)                 \
typedef name##_type arglist;                                            \
void name arglist                                                       \
{                                                                       \
    static name##_type *name##_ptr = NULL;                              \
    if (name##_ptr == NULL && awtHandle == NULL) {                      \
        return;                                                         \
    }                                                                   \
    name##_ptr = (name##_type *)                                        \
        dlsym(awtHandle, #name);                                        \
    if (name##_ptr == NULL) {                                           \
        return;                                                         \
    }                                                                   \
    (*name##_ptr)paramlist;                                             \
}

#define REFLECT_FUNCTION(return_type, name, arglist, paramlist)         \
typedef return_type name##_type arglist;                                \
return_type name arglist                                                \
{                                                                       \
    static name##_type *name##_ptr = NULL;                              \
    if (name##_ptr == NULL && awtHandle == NULL) {                      \
        return NULL;                                                    \
    }                                                                   \
    name##_ptr = (name##_type *)                                        \
        dlsym(awtHandle, #name);                                        \
    if (name##_ptr == NULL) {                                           \
        return NULL;                                                    \
    }                                                                   \
    return (*name##_ptr)paramlist;                                      \
}


/*
 * These entry point must remain in libawt.so ***for Java Plugin ONLY***
 * Reflect this call over to the correct libmawt.so.
 */

/*
 * TODO: implement for LWCToolkit, MacOSX
 */

REFLECT_VOID_FUNCTION(getAwtLockFunctions,
                      (void (**AwtLock)(JNIEnv *), void (**AwtUnlock)(JNIEnv *),
                       void (**AwtNoFlushUnlock)(JNIEnv *), void *reserved),
                      (AwtLock, AwtUnlock, AwtNoFlushUnlock, reserved))

REFLECT_VOID_FUNCTION(getAwtData,
                      (int32_t *awt_depth, Colormap *awt_cmap, Visual **awt_visual,
                       int32_t *awt_num_colors, void *pReserved),
                      (awt_depth, awt_cmap, awt_visual,
                       awt_num_colors, pReserved))

REFLECT_FUNCTION(Display *, getAwtDisplay, (void), ())
