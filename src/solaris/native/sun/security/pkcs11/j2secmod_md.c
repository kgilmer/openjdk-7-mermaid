/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
#include <stdlib.h>
#include <string.h>

#include <dlfcn.h>
#ifndef __APPLE__
#include <link.h>
#endif

#include <jni_util.h>

#include "j2secmod.h"

#ifndef RTLD_NOLOAD
/* A gross hack that will work if the NSS library is only opened once */
static void *nssLibHandle = NULL;
#endif

void *findFunction(JNIEnv *env, jlong jHandle, const char *functionName) {
    void *hModule = (void*)jHandle;
    void *fAddress = dlsym(hModule, functionName);
    if (fAddress == NULL) {
        char errorMessage[256];
        snprintf(errorMessage, sizeof(errorMessage), "Symbol not found: %s", functionName);
        JNU_ThrowNullPointerException(env, errorMessage);
        return NULL;
    }
    return fAddress;
}

JNIEXPORT jlong JNICALL Java_sun_security_pkcs11_Secmod_nssGetLibraryHandle
  (JNIEnv *env, jclass thisClass, jstring jLibName)
{
    const char *libName = (*env)->GetStringUTFChars(env, jLibName, NULL);
    // look up existing handle only, do not load
#ifdef RTLD_NOLOAD
    void *hModule = dlopen(libName, RTLD_NOLOAD);
#else
    void *hModule = nssLibHandle;
#endif
    dprintf2("-handle for %s: %u\n", libName, hModule);
    (*env)->ReleaseStringUTFChars(env, jLibName, libName);
    return (jlong)hModule;
}

JNIEXPORT jlong JNICALL Java_sun_security_pkcs11_Secmod_nssLoadLibrary
  (JNIEnv *env, jclass thisClass, jstring jLibName)
{
    void *hModule;
    const char *libName = (*env)->GetStringUTFChars(env, jLibName, NULL);

    dprintf1("-lib %s\n", libName);
    hModule = dlopen(libName, RTLD_LAZY);
#ifndef RTLD_NOLOAD
    nssLibHandle = hModule;
#endif
    (*env)->ReleaseStringUTFChars(env, jLibName, libName);
    dprintf2("-handle: %u (0X%X)\n", hModule, hModule);

    if (hModule == NULL) {
        JNU_ThrowIOException(env, dlerror());
        return 0;
    }

    return (jlong)hModule;
}
