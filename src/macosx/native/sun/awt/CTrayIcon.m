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

#import <AppKit/AppKit.h>
#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "CTrayIcon.h"
#import "ThreadUtilities.h"
#include "GeomUtilities.h"

#define kImageInset 4.0

/**
 * If the image of the specified size won't fit into the status bar,
 * then scale it down proprtionally. Otherwise, leave it as is.
 */
static NSSize ScaledImageSizeForStatusBar(NSSize imageSize) {
    NSRect imageRect = NSMakeRect(0.0, 0.0, imageSize.width, imageSize.height);
    
    // There is a black line at the bottom of the status bar  
    // that we don't want to cover with image pixels.
    CGFloat desiredHeight = [[NSStatusBar systemStatusBar] thickness] - 1.0;
    CGFloat scaleFactor = MIN(1.0, desiredHeight/imageSize.height);
    
    imageRect.size.width *= scaleFactor;
    imageRect.size.height *= scaleFactor;
    imageRect = NSIntegralRect(imageRect);
    
    return imageRect.size;
}

@implementation AWTTrayIcon

- (id) initWithPeer:(jobject)thePeer {
    if (!(self = [super init])) return nil;
    
    peer = thePeer;
    
    theItem = [[NSStatusBar systemStatusBar] statusItemWithLength:NSVariableStatusItemLength];  
    [theItem retain];    

    view = [[AWTTrayIconView alloc] initWithTrayIcon:self];
    [theItem setView:view];
    
    return self;
}

-(void) dealloc {
    JNIEnv *env = [ThreadUtilities getJNIEnvUncached];
    JNFDeleteGlobalRef(env, peer); 

    [view release];    
    [theItem release];
    
    [super dealloc];
}

- (void) setTooltip:(NSString *) tooltip{
    [view setToolTip:tooltip];
}

-(NSStatusItem *) theItem{
    return theItem;
}

- (jobject) peer{
    return peer;
}

- (void) setImage:(NSImage *) imagePtr sizing:(BOOL)autosize{
    NSSize imageSize = [imagePtr size];
    NSSize scaledSize = ScaledImageSizeForStatusBar(imageSize);
    if (imageSize.width != scaledSize.width || 
        imageSize.height != scaledSize.height) {
        [imagePtr setSize: scaledSize];
    }
    
    [view setImage:imagePtr];
}

- (NSPoint) getLocationOnScreen {
    return [[view window] convertBaseToScreen: NSZeroPoint];
}

@end //AWTTrayIcon
//================================================

@implementation AWTTrayIconView

-(id)initWithTrayIcon:(AWTTrayIcon *)theTrayIcon {    
    self = [super initWithFrame:NSMakeRect(0, 0, 1, 1)];
    
    trayIcon = theTrayIcon;
    isHighlighted = NO;
    image = nil;    
        
    return self;
}

-(void) dealloc {
    [image release];
    [super dealloc];
}

- (void)setHighlighted:(BOOL)aFlag 
{
    if (isHighlighted != aFlag) {
        isHighlighted = aFlag;
        [self setNeedsDisplay:YES];
    }
}

- (void)setImage:(NSImage*)anImage {
    [anImage retain];
    [image release];   
    image = anImage;

    CGFloat itemLength = [anImage size].width + 2.0*kImageInset;   
    [trayIcon.theItem setLength:itemLength];
    
    [self setNeedsDisplay:YES];
}

- (void)menuWillOpen:(NSMenu *)menu 
{
    [self setHighlighted:YES];
}

- (void)menuDidClose:(NSMenu *)menu 
{
    [menu setDelegate:nil];
    [self setHighlighted:NO];
}

- (void)drawRect:(NSRect)dirtyRect
{
    NSRect bounds = [self bounds];
    NSSize imageSize = [image size];
    
    NSRect drawRect = {{ (bounds.size.width - imageSize.width) / 2.0, 
        (bounds.size.height - imageSize.height) / 2.0 }, imageSize};

    // don't cover bottom pixels of the status bar with the image 
    if (drawRect.origin.y < 1.0) {
        drawRect.origin.y = 1.0;
    }
    drawRect = NSIntegralRect(drawRect);    
    
    [trayIcon.theItem drawStatusBarBackgroundInRect:bounds
                                withHighlight:isHighlighted];       
    [image drawInRect:drawRect 
             fromRect:NSZeroRect 
            operation:NSCompositeSourceOver 
             fraction:1.0
     ];
}

- (void) mouseDown:(NSEvent *)e {
    //find CTrayIcon.getPopupMenuModel method and call it to get popup menu ptr.
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    static JNF_CLASS_CACHE(jc_CTrayIcon, "sun/lwawt/macosx/CTrayIcon");
    static JNF_MEMBER_CACHE(jm_getPopupMenuModel, jc_CTrayIcon, "getPopupMenuModel", "()J");
    static JNF_MEMBER_CACHE(jm_performAction, jc_CTrayIcon, "performAction", "()V");
    jlong res = JNFCallLongMethod(env, trayIcon.peer, jm_getPopupMenuModel);
    if (res != 0) {
        CPopupMenu *cmenu = jlong_to_ptr(res);
        NSMenu* menu = [cmenu menu];
        [menu setDelegate:self];
        [trayIcon.theItem popUpStatusItemMenu:menu];
        [self setNeedsDisplay:YES];        
    } else {
        JNFCallVoidMethod(env, trayIcon.peer, jm_performAction);
    }
}

- (void) rightMouseDown:(NSEvent *)e {
    // Call CTrayIcon.performAction() method on right mouse press
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    static JNF_CLASS_CACHE(jc_CTrayIcon, "sun/lwawt/macosx/CTrayIcon");
    static JNF_MEMBER_CACHE(jm_performAction, jc_CTrayIcon, "performAction", "()V");
    JNFCallVoidMethod(env, trayIcon.peer, jm_performAction);
}


@end //AWTTrayIconView
//================================================

/*                                                                                                                                                                                                                                 
 * Class:     sun_lwawt_macosx_CTrayIcon
 * Method:    nativeCreate
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_sun_lwawt_macosx_CTrayIcon_nativeCreate
(JNIEnv *env, jobject peer) {
    __block AWTTrayIcon *trayIcon = nil;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    jobject thePeer = JNFNewGlobalRef(env, peer);
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        trayIcon = [[AWTTrayIcon alloc] initWithPeer:thePeer];
    }];
    
JNF_COCOA_EXIT(env);
    
    return ptr_to_jlong(trayIcon);
}


/*
 * Class: java_awt_TrayIcon
 * Method: initIDs
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_awt_TrayIcon_initIDs
(JNIEnv *env, jclass cls) {
    //Do nothing.
}

/*
 * Class:     sun_lwawt_macosx_CTrayIcon
 * Method:    nativeSetToolTip
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CTrayIcon_nativeSetToolTip
(JNIEnv *env, jobject self, jlong model, jstring jtooltip) {
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTTrayIcon *icon = jlong_to_ptr(model);
    NSString *tooltip = JNFJavaToNSString(env, jtooltip);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        [icon setTooltip:tooltip];
    }];
    
JNF_COCOA_EXIT(env);
}

/*
 * Class:     sun_lwawt_macosx_CTrayIcon
 * Method:    setNativeImage
 * Signature: (JJZ)V
 */
JNIEXPORT void JNICALL Java_sun_lwawt_macosx_CTrayIcon_setNativeImage
(JNIEnv *env, jobject self, jlong model, jlong imagePtr, jboolean autosize) {
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    AWTTrayIcon *icon = jlong_to_ptr(model);
    [JNFRunLoop performOnMainThreadWaiting:NO withBlock:^(){
        [icon setImage:jlong_to_ptr(imagePtr) sizing:autosize];
    }];
    
JNF_COCOA_EXIT(env);
}

JNIEXPORT jobject JNICALL
Java_sun_lwawt_macosx_CTrayIcon_nativeGetIconLocation
(JNIEnv *env, jobject self, jlong model) {
    jobject jpt = NULL;

JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
  
    __block NSPoint pt = NSZeroPoint;
    AWTTrayIcon *icon = jlong_to_ptr(model);
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
    
        NSPoint loc = [icon getLocationOnScreen];        
        pt = ConvertNSScreenPoint(env, loc);
    }];

    jpt = NSToJavaPoint(env, pt);
   
JNF_COCOA_EXIT(env);
    
    return jpt;
}
