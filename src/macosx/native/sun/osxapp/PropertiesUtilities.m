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

#import "PropertiesUtilities.h"

@implementation PropertiesUtilities

+ (NSString *) javaSystemPropertyForKey:(NSString *)key withEnv:(JNIEnv *)env {
    static JNF_CLASS_CACHE(jc_System, "java/lang/System");
    static JNF_STATIC_MEMBER_CACHE(jm_getProperty, jc_System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
    
    jstring jKey = JNFNSToJavaString(env, key);
    jstring jValue = JNFCallStaticObjectMethod(env, jm_getProperty, jKey);
    (*env)->DeleteLocalRef(env, jKey);
    
    NSString *value = JNFJavaToNSString(env, jValue);
    (*env)->DeleteLocalRef(env, jValue);
    return value;
}

@end
