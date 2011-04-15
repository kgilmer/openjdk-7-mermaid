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

#import <JavaNativeFoundation/JavaNativeFoundation.h>

#import "ThreadUtilities.h"
#import "AWTView.h"
#import "AWTEvent.h"
#import "AWTWindow.h"
#import "LWCToolkit.h"


@implementation AWTView

// Note: Must be called on main (AppKit) thread only
- (id) initWithRect: (NSRect) rect
       platformView: (jobject) cPlatformView
{
    AWT_ASSERT_APPKIT_THREAD;
    // Initialize ourselves
    self = [super initWithFrame: rect];
    
    if (self == nil) {
        // TODO: not implemented
    }

    m_cPlatformView = cPlatformView;

    return self;
}

- (void) dealloc {
    AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    (*env)->DeleteGlobalRef(env, m_cPlatformView);
    m_cPlatformView = NULL;
    
    [super dealloc];
}

- (void) viewDidMoveToWindow {
    [AWTToolkit eventCountPlusPlus];
    
    AWT_ASSERT_APPKIT_THREAD;
    [[self window] makeFirstResponder: self];
    if ([self window] != NULL) {
        [self resetTrackingRect];
    }    
}

- (BOOL) acceptsFirstMouse: (NSEvent *)event {
  return YES;
}

- (BOOL) acceptsFirstResponder {
  return YES;
}

- (BOOL) becomeFirstResponder {
  return YES;
}

- (BOOL) preservesContentDuringLiveResize {
  return YES;
}

/*
 * Automatically triggered functions.
 */

/*
 * MouseEvents support
 */

- (void) mouseDown: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) mouseUp: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) rightMouseDown: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) rightMouseUp: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) otherMouseDown: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) otherMouseUp: (NSEvent *)event {
  [self deliverJavaMouseEvent: event];
}

- (void) mouseMoved: (NSEvent *)event {
    // TODO: better way to redirect move events to the "under" view

    NSPoint eventLocation = [event locationInWindow];
    NSPoint localPoint = [self convertPoint: eventLocation fromView: nil];

    if  ([self mouse: localPoint inRect: [self bounds]]) {
        [self deliverJavaMouseEvent: event];
    } else {
        [[self nextResponder] mouseDown:event];
    }
}

- (void) mouseDragged: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
}

- (void) rightMouseDragged: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
}

- (void) otherMouseDragged: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
}

- (void) mouseEntered: (NSEvent *)event {
    [[self window] setAcceptsMouseMovedEvents:YES];
    //[[self window] makeFirstResponder:self];
    [self deliverJavaMouseEvent: event];
}

- (void) mouseExited: (NSEvent *)event {
    [[self window] setAcceptsMouseMovedEvents:NO];
    [self deliverJavaMouseEvent: event];
    //Restore the cursor back.
    //[CCursorManager _setCursor: [NSCursor arrowCursor]];
}

- (void) scrollWheel: (NSEvent*) event {
    [self deliverJavaMouseEvent: event];
}

/*
 * KeyEvents support
 */

- (void) keyDown: (NSEvent *)event {
    [self deliverJavaKeyEventHelper: event];
}

- (void) keyUp: (NSEvent *)event {
    [self deliverJavaKeyEventHelper: event];}

- (void) flagsChanged: (NSEvent *)event {
    [self deliverJavaKeyEventHelper: event];
}

/**
 * Utility methods and accessors
 */

-(void) deliverJavaMouseEvent: (NSEvent *) event {
    [AWTToolkit eventCountPlusPlus];

    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    
    NSPoint eventLocation = [event locationInWindow];
    NSPoint localPoint = [self convertPoint: eventLocation fromView: nil];

    // TODO: sometimes "illegal" events are coming to NSView
    // it results in NPE on Java level
    NSRect bounds = [self bounds];
    if (localPoint.x < 0 || localPoint.x > bounds.size.width ||
        localPoint.y < 0 || localPoint.y > bounds.size.height)
    {
        return;
    }    
    
    NSPoint absP = [NSEvent mouseLocation];
    
    // Convert global numbers between Cocoa's coordinate system and Java.
    // TODO: need consitent way for doing that both with global as well as with local coordinates.
    // The reason to do it here is one more native method for getting screen dimension otherwise.
    
    NSRect screenRect = [[NSScreen mainScreen] frame];
    absP.y = screenRect.size.height - absP.y;    
    jint clickCount;
    
    if ([event type] == NSMouseEntered ||
	[event type] == NSMouseExited ||
	[event type] == NSScrollWheel) {
        clickCount = 0;
    } else {
        clickCount = [event clickCount];
    }

    jint modifiers = GetJavaMouseModifiers(event);
    static JNF_CLASS_CACHE(jc_NSEvent, "sun/lwawt/macosx/event/NSEvent");
    static JNF_CTOR_CACHE(jctor_NSEvent, jc_NSEvent, "(IIIIIIIIDD)V");
    jobject jEvent = JNFNewObject(env, jctor_NSEvent,
                                  [event type], 
                                  modifiers,
                                  clickCount,
                                  [event buttonNumber],
                                  (jint)localPoint.x, (jint)localPoint.y,
                                  (jint)absP.x, (jint)absP.y,
                                  [event deltaY],
                                  [event deltaX]);
    if (jEvent == nil) {
        // Unable to create event by some reason.
        return;
    }
    
    static JNF_CLASS_CACHE(jc_PlatformView, "sun/lwawt/macosx/CPlatformView");
    static JNF_MEMBER_CACHE(jm_deliverMouseEvent, jc_PlatformView, "deliverMouseEvent", "(Lsun/lwawt/macosx/event/NSEvent;)V");
    JNFCallVoidMethod(env, m_cPlatformView, jm_deliverMouseEvent, jEvent);
}


- (void) clearTrackingRect {
    if (rolloverTrackingRectTag > 0) {
        [self removeTrackingRect:rolloverTrackingRectTag];
        rolloverTrackingRectTag = 0;
    }
}

- (void) resetTrackingRect {
    [self clearTrackingRect];
    rolloverTrackingRectTag =
	[self addTrackingRect:[self visibleRect]
					owner:self userData:NULL assumeInside:NO];
}

- (void) resetCursorRects {
    [super resetCursorRects];
    [self resetTrackingRect];
}

-(void) deliverJavaKeyEventHelper: (NSEvent *) event {
    [AWTToolkit eventCountPlusPlus];
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    // Pulled as-is as it's highly-depend on native code.
    DeliverJavaKeyEvent(env, event, m_cPlatformView);
}

- (void) setContextMenu:(NSMenu *)aMenu{
    // TODO?: release the old reference before setting new one.
    popupMenu = aMenu;
}

// a callback invoked by Cocoa
- (NSMenu *)menuForEvent:(NSEvent *)theEvent {
    if (popupMenu != nil) {
        return popupMenu;
    }
    return nil;
}

- (void) drawRect:(NSRect)dirtyRect {
AWT_ASSERT_APPKIT_THREAD;
    
    [super drawRect:dirtyRect];
    JNIEnv *env = [ThreadUtilities getAppKitJNIEnv];
    if (env != NULL) {
/*
        if ([self inLiveResize]) {
	    NSRect rs[4];
	    NSInteger count;
	    [self getRectsExposedDuringLiveResize:rs count:&count];
	    for (int i = 0; i < count; i++) {
	        JNU_CallMethodByName(env, NULL, [m_awtWindow cPlatformView],
			     "deliverWindowDidExposeEvent", "(FFFF)V",
			     (jfloat)rs[i].origin.x, (jfloat)rs[i].origin.y,
			     (jfloat)rs[i].size.width, (jfloat)rs[i].size.height);
		if ((*env)->ExceptionOccurred(env)) {
		    (*env)->ExceptionDescribe(env);
		    (*env)->ExceptionClear(env);
		}
	    }
        } else {
*/
        static JNF_CLASS_CACHE(jc_CPlatformView, "sun/lwawt/macosx/CPlatformView");
        static JNF_MEMBER_CACHE(jm_deliverWindowDidExposeEvent, jc_CPlatformView, "deliverWindowDidExposeEvent", "()V");
        JNFCallVoidMethod(env, m_cPlatformView, jm_deliverWindowDidExposeEvent);
/*       
        }    
*/      
    }
}

@end //AWTView

/*
 * Class:     sun_lwawt_macosx_CPlatformView
 * Method:    nativeCreateView
 * Signature: (IIII)J
 */
JNIEXPORT jlong JNICALL
Java_sun_lwawt_macosx_CPlatformView_nativeCreateView
(JNIEnv *env, jobject obj, jint originX, jint originY, jint width, jint height)
{
    __block AWTView *newView = nil;
    
JNF_COCOA_ENTER(env);
AWT_ASSERT_NOT_APPKIT_THREAD;
    
    NSRect rect = NSMakeRect(originX, originY, width, height);
    jobject cPlatformView = (*env)->NewGlobalRef(env, obj);
    
    [JNFRunLoop performOnMainThreadWaiting:YES withBlock:^(){
        AWT_ASSERT_APPKIT_THREAD;
        
        AWTView *view = [[AWTView alloc] initWithRect:rect
                                         platformView:cPlatformView];
        CFRetain(view);
        [view release]; // GC
        
        newView = view;
    }];

JNF_COCOA_EXIT(env);
    
    return ptr_to_jlong(newView);
}
