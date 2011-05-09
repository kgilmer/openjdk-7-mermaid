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

#ifndef __THREADUTILITIES_H
#define __THREADUTILITIES_H

#import <pthread.h>

// --------------------------------------------------------------------------
#ifndef PRODUCT_BUILD

// Turn on the AWT thread assert mechanism. See below for different variants.
// TODO: don't enable this for production builds...
#define AWT_THREAD_ASSERTS

#endif /* PRODUCT_BUILD */
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
#ifdef AWT_THREAD_ASSERTS

// Turn on to have awt thread asserts display a message on the console.
#define AWT_THREAD_ASSERTS_MESSAGES

// Turn on to have awt thread asserts use an environment variable switch to 
// determine if assert should really be called.
//#define AWT_THREAD_ASSERTS_ENV_ASSERT

// Define AWT_THREAD_ASSERTS_WAIT to make asserts halt the asserting thread
// for debugging purposes.
//#define AWT_THREAD_ASSERTS_WAIT

#ifdef AWT_THREAD_ASSERTS_MESSAGES

#define AWT_THREAD_ASSERTS_NOT_APPKIT_MESSAGE \
    NSLog(@"Cocoa AWT: Not running on AppKit thread 0 when expected. (%s - %s:%d)", __FUNCTION__, __FILE__, __LINE__)

#define AWT_THREAD_ASSERTS_ON_APPKIT_MESSAGE \
    NSLog(@"Cocoa AWT: Running on AppKit thread 0 when not expected. (%s - %s:%d)", __FUNCTION__, __FILE__, __LINE__)

#define AWT_THREAD_ASSERTS_BUG_REPORT_MESSAGE \
    NSLog(@"\tPlease file a bug report at http://java.net/jira/browse/MACOSX_PORT with this message and a reproducible test case.")

#ifdef AWT_THREAD_ASSERTS_ENV_ASSERT

extern int sAWTThreadAsserts;
#define AWT_THREAD_ASSERTS_ENV_ASSERT_CHECK    \
do {                                           \
    if (sAWTThreadAsserts) {                   \
        NSLog(@"\tPlease run this java program again with setenv COCOA_AWT_DISABLE_THREAD_ASSERTS to proceed with a warning."); \
        assert(NO);                            \
    }                                          \
} while (0)

#else

#define AWT_THREAD_ASSERTS_ENV_ASSERT_CHECK do {} while (0)

#endif /* AWT_THREAD_ASSERTS_ENV_ASSERT */

#define AWT_ASSERT_APPKIT_THREAD               \
do {                                           \
    if (pthread_main_np() == 0) {              \
        AWT_THREAD_ASSERTS_NOT_APPKIT_MESSAGE; \
        AWT_THREAD_ASSERTS_BUG_REPORT_MESSAGE; \
        AWT_THREAD_ASSERTS_ENV_ASSERT_CHECK;   \
    }                                          \
} while (0)

#define AWT_ASSERT_NOT_APPKIT_THREAD           \
do {                                           \
    if (pthread_main_np() != 0) {              \
        AWT_THREAD_ASSERTS_ON_APPKIT_MESSAGE; \
        AWT_THREAD_ASSERTS_BUG_REPORT_MESSAGE; \
        AWT_THREAD_ASSERTS_ENV_ASSERT_CHECK;   \
    }                                          \
} while (0)

#define AWT_ASSERT_ANY_THREAD

#endif /* AWT_THREAD_ASSERTS_MESSAGES */
    
#ifdef AWT_THREAD_ASSERTS_WAIT
    
#define AWT_ASSERT_APPKIT_THREAD      \
do {                                  \
    while (pthread_main_np() == 0) {} \
} while (0)

#define AWT_ASSERT_NOT_APPKIT_THREAD  \
do {                                  \
    while (pthread_main_np() != 0) {} \
} while (0)

#define AWT_ASSERT_ANY_THREAD

#endif /* AWT_THREAD_ASSERTS_WAIT */

#else /* AWT_THREAD_ASSERTS */

#define AWT_ASSERT_APPKIT_THREAD     do {} while (0)
#define AWT_ASSERT_NOT_APPKIT_THREAD do {} while (0)
#define AWT_ASSERT_ANY_THREAD

#endif /* AWT_THREAD_ASSERTS */
// --------------------------------------------------------------------------


// This is an empty Obj-C object just so that -performSelectorOnMainThread
// can be used, and to use the Obj-C +initialize feature.
__attribute__((visibility("default")))
@interface ThreadUtilities : NSObject { }

+ (JNIEnv*)getJNIEnv;
+ (JNIEnv*)getJNIEnvUncached;
+ (JNIEnv*)getAppKitJNIEnv;

+ (void)performOnMainThread:(SEL)aSelector onObject:(id)target withObject:(id)arg waitUntilDone:(BOOL)wait awtMode:(BOOL)inAWT;

@end

#endif /* __THREADUTILITIES_H */
