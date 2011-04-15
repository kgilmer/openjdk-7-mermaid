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

#import <dlfcn.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "CMenuBar.h"
#import "InitIDs.h"
#import "LWCToolkit.h"
#import "ThreadUtilities.h"


const char *kInternalError = "java/lang/InternalError";

static long eventCount;

@implementation AWTToolkit

+ (long) getEventCount{
    return eventCount;
}

+ (void) eventCountPlusPlus{
    eventCount++;
}

@end

JavaVM *jvm = NULL;

//used to check if some event has processed by the main loop in syncNativeQueue.

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    // Cache the JavaVM when this library is loaded; it never changes (only one JVM per process)
    jvm = vm;
    
    void *handle = dlopen(0, RTLD_LAZY | RTLD_GLOBAL);
    if (handle != NULL) {
        void (*fptr)() = dlsym(handle, "JLI_NotifyAWTLoaded");
        if (fptr != NULL) {
            fptr();
        } else {
            fprintf(stderr, "dlsym failed\n");
        }
    } else {
        fprintf(stderr, "dlopen failed\n");
    }
    

    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    
    [ThreadUtilities performOnMainThread:@selector(clearMenuBarExcludingAppleMenu_OnAppKitThread:)
                                onObject:[CMenuBar class]
                              withObject:NO
                           waitUntilDone:NO
                                 awtMode:YES];

    // It's recommended to prepare the system for setting the dock image.
    // Although seem it works on available scenarios even without that.
    // Left for some further case when we discover an app not showing the
    // custom icon.
    /*
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    NSDictionary *dict = [NSDictionary
                           dictionaryWithObject:@"YES"
                           forKey:@"AppleDockIconEnabled"];
    [userDefaults registerDefaults:dict];
    */

    [pool drain];

    return JNI_VERSION_1_2;
}

/*
 * Returns a reference to the class java.awt.Component.
 */
static jclass
GetComponentClass(JNIEnv *env)
{
    static jclass componentCls = NULL;

    // get global reference of java/awt/Component class (run only once)
    if (componentCls == NULL) {
        jclass componentClsLocal = (*env)->FindClass(env, "java/awt/Component");
        //DASSERT(componentClsLocal != NULL);
        if (componentClsLocal == NULL) {
            /* exception already thrown */
            return NULL;
        }
        componentCls = (jclass)(*env)->NewGlobalRef(env, componentClsLocal);
        (*env)->DeleteLocalRef(env, componentClsLocal);
    }
    return componentCls;
}


/*
 * Returns a reference to the class java.awt.MenuComponent.
 */
static jclass
GetMenuComponentClass(JNIEnv *env)
{
    static jclass menuComponentCls = NULL;

    // get global reference of java/awt/MenuComponent class (run only once)
    if (menuComponentCls == NULL) {
        jclass menuComponentClsLocal =
            (*env)->FindClass(env, "java/awt/MenuComponent");
        //DASSERT(menuComponentClsLocal != NULL);
        if (menuComponentClsLocal == NULL) {
            /* exception already thrown */
            return NULL;
        }
        menuComponentCls = (jclass)
            (*env)->NewGlobalRef(env, menuComponentClsLocal);
        (*env)->DeleteLocalRef(env, menuComponentClsLocal);
    }
    return menuComponentCls;
}

/*
 * Class:     sun_lwawt_macosx_LWCToolkit
 * Method:    nativeSyncQueue
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_sun_lwawt_macosx_LWCToolkit_nativeSyncQueue
(JNIEnv *env, jobject self, jlong timeout)
{
    int currentEventNum = [AWTToolkit getEventCount];
    
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){}];
    
    if (([AWTToolkit getEventCount] - currentEventNum) != 0) {
        return JNI_TRUE;
    }
    
    return JNI_FALSE;
}


JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_LWCToolkit_nativeSetApplicationIconImage
(JNIEnv *env, jobject obj, jlong nsImagePtr)
{
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        [NSApp setApplicationIconImage:(NSImage *)jlong_to_ptr(nsImagePtr)];
    }];
}

/*
 * Class:     sun_awt_SunToolkit
 * Method:    getAppContext
 * Signature: (Ljava/awt/Object;)Lsun/awt/AppContext;
 */
JNIEXPORT jobject JNICALL
Java_sun_awt_SunToolkit_getAppContext
    (JNIEnv *env, jclass cls, jobject obj)
{
    jobject appContext = NULL;

    if ((*env)->IsInstanceOf(env, obj, GetComponentClass(env))) {
        appContext = (*env)->GetObjectField(env, obj, componentIDs.appContext);
    } else if ((*env)->IsInstanceOf(env, obj, GetMenuComponentClass(env))) {
        appContext = (*env)->GetObjectField(env, obj,
                                            menuComponentIDs.appContext);
    }
    return appContext;
}

/*
 * Class:     sun_awt_SunToolkit
 * Method:    setAppContext
 * Signature: (Ljava/lang/Object;Lsun/awt/AppContext;)Z
 */
JNIEXPORT jboolean JNICALL
Java_sun_awt_SunToolkit_setAppContext
    (JNIEnv *env, jclass cls, jobject comp, jobject appContext)
{
    jboolean isComponent;
    if ((*env)->IsInstanceOf(env, comp, GetComponentClass(env))) {
        (*env)->SetObjectField(env, comp, componentIDs.appContext, appContext);
        isComponent = JNI_TRUE;
    } else if ((*env)->IsInstanceOf(env, comp, GetMenuComponentClass(env))) {
        (*env)->SetObjectField(env, comp, menuComponentIDs.appContext,
                               appContext);
        isComponent = JNI_TRUE;
    } else {
        isComponent = JNI_FALSE;
    }
    return isComponent;
}

/*
 * Class:     sun_lwawt_macosx_LWCToolkit
 * Method:    beep
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_LWCToolkit_beep
(JNIEnv *env, jobject self)
{
    NSBeep(); // produces both sound and visual flash, if configured in System Preferences
}

CGDirectDisplayID
FindCGDirectDisplayIDForScreenIndex(jint screenIndex)
{
    // most common case - just one monitor
    CGDirectDisplayID screenID = CGMainDisplayID();
	
    CGDisplayCount displayCount = 0;
    CGGetOnlineDisplayList(0, NULL, &displayCount);
    
    if ((displayCount > 1) &&
        (screenIndex >= 0) &&
        (screenIndex < (jint)displayCount))
    {
        if (displayCount < 10) {
            // stack allocated optimization for less than 10 monitors
            CGDirectDisplayID onlineDisplays[displayCount];
            CGGetOnlineDisplayList(displayCount, onlineDisplays, &displayCount);
            screenID = (CGDirectDisplayID)onlineDisplays[screenIndex];
        } else {
            CGDirectDisplayID *onlineDisplays =
			malloc(displayCount*sizeof(CGDirectDisplayID));
            if (onlineDisplays != NULL) {
                CGGetOnlineDisplayList(displayCount, onlineDisplays,
                                       &displayCount);
                screenID = (CGDirectDisplayID)onlineDisplays[screenIndex];
                free(onlineDisplays);
            }
        }
    }
    
    return screenID;
}

/*
 * Class:     sun_lwawt_macosx_LWCToolkit
 * Method:    initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_lwawt_macosx_LWCToolkit_initIDs
(JNIEnv *env, jclass klass) {
    // set thread names
    dispatch_async(dispatch_get_main_queue(), ^(void){
        [[NSThread currentThread] setName:@"CToolkit NSThread"];
        
        JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
        static JNF_CLASS_CACHE(jc_LWCToolkit, "sun/lwawt/macosx/LWCToolkit");
        static JNF_STATIC_MEMBER_CACHE(jsm_installToolkitThreadNameInJava, jc_LWCToolkit, "installToolkitThreadNameInJava", "()V");
        JNFCallStaticVoidMethod(env, jsm_installToolkitThreadNameInJava);
    });
}

/*
 * Class:     sun_awt_SunToolkit
 * Method:    closeSplashScreen
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_sun_awt_SunToolkit_closeSplashScreen(JNIEnv *env, jclass cls)
{
    void *hSplashLib = dlopen(0, RTLD_LAZY);
    if (!hSplashLib) return;
    
    void (*splashClose)() = dlsym(hSplashLib, "SplashClose");
    if (splashClose) {
        splashClose();
    }
    dlclose(hSplashLib);
}


// TODO: definitely doesn't belong here (copied from fontpath.c in the
// solaris tree)...

JNIEXPORT jstring JNICALL
Java_sun_font_FontManager_getFontPath
(JNIEnv *env, jclass obj, jboolean noType1)
{
    return JNFNSToJavaString(env, @"/Library/Fonts");
}

// This isn't yet used on unix, the implementation is added since shared
// code calls this method in preparation for future use.
JNIEXPORT void JNICALL
Java_sun_font_FontManager_populateFontFileNameMap
(JNIEnv *env, jclass obj, jobject fontToFileMap, jobject fontToFamilyMap, jobject familyToFontListMap, jobject locale)
{
    
}
