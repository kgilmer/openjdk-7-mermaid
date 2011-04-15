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

#ifndef __LWCTOOLKIT_H
#define __LWCTOOLKIT_H

#import <jni.h>
#import <pthread.h>
#import <assert.h>

#import <Cocoa/Cocoa.h>
#import <CoreServices/CoreServices.h>
#import <AudioToolbox/AudioToolbox.h>
#import <AvailabilityMacros.h>

#define DEBUG 1


const char *kInternalError;

@interface AWTToolkit : NSObject { }

+ (long) getEventCount;
+ (void) eventCountPlusPlus;

@end

CGDirectDisplayID FindCGDirectDisplayIDForScreenIndex(jint screenIndex);

typedef struct tagLWCJavaIDs {
    struct {
        jmethodID canBecomeKeyWindow;
        jmethodID windowDidBecomeMain;
        jmethodID windowShouldClose;
    } CPlatformWindow;
    struct {
        jmethodID deliverMouseEvent;
    } CPlatformView;
} LWCJavaIDs;

extern LWCJavaIDs javaIDs;

/**
 * A simple C representation of a Java Rectangle.
 */
typedef struct AWTRect {
    jint x;
    jint y;
    jint width;
    jint height;
} AWTRect;

/*
 * Utility Macros
 */

#ifdef DEBUG
#define CDEBUG(str, args...) NSLog(str, args)
#define ASSERT(args...) assert(args)
#else
#define CDEBUG(str, args...)
#define ASSERT(args...)
#endif

/** Macro to cast a jlong to a void pointer. Casts to long on 32-bit systems to quiesce the compiler. */
#ifdef _LP64
#define PTR(jl) ((void *) jl)
#else
#define PTR(jl) ((void *) (long) jl)
#endif

/** Macro to cast a void pointer to jlong. Casts to long on 32-bit systems to quiesce the compiler. */
#ifdef _LP64
#define PTRLONG(ptr) ((jlong) ptr)
#else
#define PTRLONG(ptr) ((jlong) (long) ptr)
#endif

/** Macro to cast a jlong to an Objective-C object (id). Casts to long on 32-bit systems to quiesce the compiler. */
#ifdef _LP64
#define OBJC(jl) ((id) jl)
#else
#define OBJC(jl) ((id) (long) jl)
#endif

/** Macro to cast an Objective-C object (id) to jlong. Casts to long on 32-bit systems to quiesce the compiler. */
#ifdef _LP64
#define OBJCLONG(ptr) ((jlong) ptr)
#else
#define OBJCLONG(ptr) ((jlong) (long) ptr)
#endif

/** Macro to create an NSAutoreleasePool. */
#define AR_POOL(name) NSAutoreleasePool *name = [[NSAutoreleasePool alloc] init]

/** Lock a pthread mutex. */
#define LOCK(v) do { pthread_mutex_lock(v); } while (0)

/** Unlock a pthread mutex. */
#define UNLOCK(v) do { pthread_mutex_unlock(v); } while (0)

#endif /* __LWCTOOLKIT_H */
