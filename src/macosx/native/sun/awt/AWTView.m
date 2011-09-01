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
#import "JavaComponentAccessibility.h"
#import "JavaTextAccessibility.h"
#import "CGLLayer.h"


@interface AWTView()
@property (retain) CDropTarget *_dropTarget;
@property (retain) CDragSource *_dragSource;
@end


@implementation AWTView

@synthesize _dropTarget;
@synthesize _dragSource;
@synthesize cglLayer;

// Note: Must be called on main (AppKit) thread only
- (id) initWithRect: (NSRect) rect
       platformView: (jobject) cPlatformView
{
AWT_ASSERT_APPKIT_THREAD;
    // Initialize ourselves
    self = [super initWithFrame: rect];
    if (self == nil) return self;

    m_cPlatformView = cPlatformView;

    cglLayer = [CGLLayer layer];
    
    [self setWantsLayer: YES];
    [self.layer addSublayer: (CALayer *)cglLayer];

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
AWT_ASSERT_APPKIT_THREAD;
    
    [AWTToolkit eventCountPlusPlus];
    
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
    mouseDownButtonMask |= NSLeftMouseDownMask;
}

- (void) mouseUp: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
    mouseDownButtonMask &= ~NSLeftMouseDownMask;
}

- (void) rightMouseDown: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
    mouseDownButtonMask |= NSRightMouseDownMask;
}

- (void) rightMouseUp: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
    mouseDownButtonMask &= ~NSRightMouseDownMask;
}

- (void) otherMouseDown: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
    mouseDownButtonMask |= NSOtherMouseDownMask;
}

- (void) otherMouseUp: (NSEvent *)event {
    [self deliverJavaMouseEvent: event];
    mouseDownButtonMask &= ~NSOtherMouseDownMask;
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

    jint modifiers = GetJavaMouseModifiers(event, mouseDownButtonMask);
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
    rolloverTrackingRectTag = [self addTrackingRect:[self visibleRect]
                                              owner:self
                                           userData:NULL
                                       assumeInside:NO];
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

// NSAccessibility support
- (jobject)awtComponent:(JNIEnv*)env
{
    static JNF_CLASS_CACHE(jc_CPlatformView, "sun/lwawt/macosx/CPlatformView");
    static JNF_MEMBER_CACHE(jf_Peer, jc_CPlatformView, "peer", "Lsun/lwawt/LWWindowPeer;");
    if ((env == NULL) || (m_cPlatformView == NULL)) {
        NSLog(@"Apple AWT : Error AWTView:awtComponent given bad parameters.");
        if (env != NULL)
        {
            JNFDumpJavaStack(env);
        }
        return NULL;
    }
    jobject peer = JNFGetObjectField(env, m_cPlatformView, jf_Peer);
    static JNF_CLASS_CACHE(jc_LWWindowPeer, "sun/lwawt/LWWindowPeer");
    static JNF_MEMBER_CACHE(jf_Target, jc_LWWindowPeer, "target", "Ljava/awt/Component;");
    if (peer == NULL) {
        NSLog(@"Apple AWT : Error AWTView:awtComponent got null peer from CPlatformView");
        JNFDumpJavaStack(env);
        return NULL;
    }
    return JNFGetObjectField(env, peer, jf_Target);
}

- (id)getAxData:(JNIEnv*)env
{
    return [[[JavaComponentAccessibility alloc] initWithParent:self withEnv:env withAccessible:[self awtComponent:env] withIndex:-1 withView:self withJavaRole:nil] autorelease];
}

- (NSArray *)accessibilityAttributeNames
{
    return [[super accessibilityAttributeNames] arrayByAddingObject:NSAccessibilityChildrenAttribute];
}

// NSAccessibility messages
// attribute methods
- (id)accessibilityAttributeValue:(NSString *)attribute
{
    AWT_ASSERT_APPKIT_THREAD;
    
    if ([attribute isEqualToString:NSAccessibilityChildrenAttribute])
    {
        JNIEnv *env = [ThreadUtilities getJNIEnv];
        
        (*env)->PushLocalFrame(env, 4);
        
        id result = NSAccessibilityUnignoredChildrenForOnlyChild([self getAxData:env]);
        
        (*env)->PopLocalFrame(env, NULL);
        
        return result;
    }
    else
    {
        return [super accessibilityAttributeValue:attribute];
    }
}
- (BOOL)accessibilityIsIgnored
{
    return YES;
}

- (id)accessibilityHitTest:(NSPoint)point
{
    AWT_ASSERT_APPKIT_THREAD;
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    
    (*env)->PushLocalFrame(env, 4);
    
    id result = [[self getAxData:env] accessibilityHitTest:point withEnv:env];
    
    (*env)->PopLocalFrame(env, NULL);

    return result;
}

- (id)accessibilityFocusedUIElement
{
    AWT_ASSERT_APPKIT_THREAD;
    
    JNIEnv *env = [ThreadUtilities getJNIEnv];
    
    (*env)->PushLocalFrame(env, 4);
    
    id result = [[self getAxData:env] accessibilityFocusedUIElement];
    
    (*env)->PopLocalFrame(env, NULL);
    
    return result;
}

// --- Services menu support for lightweights ---

// finds the focused accessable element, and if it's a text element, obtains the text from it
- (NSString *)accessibleSelectedText
{
    id focused = [self accessibilityFocusedUIElement];
    if (![focused isKindOfClass:[JavaTextAccessibility class]]) return nil;
    return [(JavaTextAccessibility *)focused accessibilitySelectedTextAttribute];
}

// same as above, but converts to RTFD
- (NSData *)accessibleSelectedTextAsRTFD
{
    NSString *selectedText = [self accessibleSelectedText];
    NSAttributedString *styledText = [[NSAttributedString alloc] initWithString:selectedText];
    NSData *rtfdData = [styledText RTFDFromRange:NSMakeRange(0, [styledText length]) documentAttributes:nil];
    [styledText release];
    return rtfdData;
}

// finds the focused accessable element, and if it's a text element, sets the text in it
- (BOOL)replaceAccessibleTextSelection:(NSString *)text
{
    id focused = [self accessibilityFocusedUIElement];
    if (![focused isKindOfClass:[JavaTextAccessibility class]]) return NO;
    [(JavaTextAccessibility *)focused accessibilitySetSelectedTextAttribute:text];
    return YES;
}

// called for each service in the Services menu - only handle text for now
- (id)validRequestorForSendType:(NSString *)sendType returnType:(NSString *)returnType
{
    if ([[self window] firstResponder] != self) return nil; // let AWT components handle themselves
    
    if ([sendType isEqual:NSStringPboardType] || [returnType isEqual:NSStringPboardType]) {
        NSString *selectedText = [self accessibleSelectedText];
        if (selectedText) return self;
    }
    
    return nil;
}

// fetch text from Java and hand off to the service
- (BOOL)writeSelectionToPasteboard:(NSPasteboard *)pboard types:(NSArray *)types
{
    if ([types containsObject:NSStringPboardType])
    {
        [pboard declareTypes:[NSArray arrayWithObject:NSStringPboardType] owner:nil];
        return [pboard setString:[self accessibleSelectedText] forType:NSStringPboardType];
    }
    
    if ([types containsObject:NSRTFDPboardType])
    {
        [pboard declareTypes:[NSArray arrayWithObject:NSRTFDPboardType] owner:nil];
        return [pboard setData:[self accessibleSelectedTextAsRTFD] forType:NSRTFDPboardType];
    }
    
    return NO;
}

// write text back to Java from the service
- (BOOL)readSelectionFromPasteboard:(NSPasteboard *)pboard
{
    if ([[pboard types] containsObject:NSStringPboardType])
    {
        NSString *text = [pboard stringForType:NSStringPboardType];
        return [self replaceAccessibleTextSelection:text];
    }
    
    if ([[pboard types] containsObject:NSRTFDPboardType])
    {
        NSData *rtfdData = [pboard dataForType:NSRTFDPboardType];
        NSAttributedString *styledText = [[NSAttributedString alloc] initWithRTFD:rtfdData documentAttributes:nil];
        NSString *text = [styledText string];
        [styledText release];
        
        return [self replaceAccessibleTextSelection:text];
    }
    
    return NO;
}


-(void) setDragSource:(CDragSource *)source {
	self._dragSource = source;
}
	

- (void) setDropTarget:(CDropTarget *)target {
	self._dropTarget = target;
	[ThreadUtilities performOnMainThread:@selector(controlModelControlValid) onObject:self._dropTarget withObject:nil waitUntilDone:YES awtMode:YES];
}

/********************************  BEGIN NSDraggingSource Interface  ********************************/

- (NSDragOperation)draggingSourceOperationMaskForLocal:(BOOL)flag
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
    NSDragOperation dragOp = NSDragOperationNone;
	
    if (dragSource != nil)
        dragOp = [dragSource draggingSourceOperationMaskForLocal:flag];
    else if ([super respondsToSelector:@selector(draggingSourceOperationMaskForLocal:)])
        dragOp = [super draggingSourceOperationMaskForLocal:flag];
	
    return dragOp;
}

- (NSArray *)namesOfPromisedFilesDroppedAtDestination:(NSURL *)dropDestination
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
    NSArray* array = nil;
	
    if (dragSource != nil)
        array = [dragSource namesOfPromisedFilesDroppedAtDestination:dropDestination];
    else if ([super respondsToSelector:@selector(namesOfPromisedFilesDroppedAtDestination:)])
        array = [super namesOfPromisedFilesDroppedAtDestination:dropDestination];
	
    return array;
}

- (void)draggedImage:(NSImage *)image beganAt:(NSPoint)screenPoint
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
	
    if (dragSource != nil)
        [dragSource draggedImage:image beganAt:screenPoint];
    else if ([super respondsToSelector:@selector(draggedImage::)])
        [super draggedImage:image beganAt:screenPoint];
}

- (void)draggedImage:(NSImage *)image endedAt:(NSPoint)screenPoint operation:(NSDragOperation)operation
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
	
    if (dragSource != nil)
        [dragSource draggedImage:image endedAt:screenPoint operation:operation];
    else if ([super respondsToSelector:@selector(draggedImage:::)])
        [super draggedImage:image endedAt:screenPoint operation:operation];
}

- (void)draggedImage:(NSImage *)image movedTo:(NSPoint)screenPoint
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
	
    if (dragSource != nil)
        [dragSource draggedImage:image movedTo:screenPoint];
    else if ([super respondsToSelector:@selector(draggedImage::)])
        [super draggedImage:image movedTo:screenPoint];
}

- (BOOL)ignoreModifierKeysWhileDragging
{
    // If draggingSource is nil route the message to the superclass (if responding to the selector):
    CDragSource *dragSource = self._dragSource;
    BOOL result = FALSE;
	
    if (dragSource != nil)
        result = [dragSource ignoreModifierKeysWhileDragging];
    else if ([super respondsToSelector:@selector(ignoreModifierKeysWhileDragging)])
        result = [super ignoreModifierKeysWhileDragging];
	
    return result;
}

/********************************  END NSDraggingSource Interface  ********************************/

/********************************  BEGIN NSDraggingDestination Interface  ********************************/

- (NSDragOperation)draggingEntered:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
    NSDragOperation dragOp = NSDragOperationNone;
	
    if (dropTarget != nil)
        dragOp = [dropTarget draggingEntered:sender];
    else if ([super respondsToSelector:@selector(draggingEntered:)])
        dragOp = [super draggingEntered:sender];
	
    return dragOp;
}

- (NSDragOperation)draggingUpdated:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
    NSDragOperation dragOp = NSDragOperationNone;
	
    if (dropTarget != nil)
        dragOp = [dropTarget draggingUpdated:sender];
    else if ([super respondsToSelector:@selector(draggingUpdated:)])
        dragOp = [super draggingUpdated:sender];
	
    return dragOp;
}

- (void)draggingExited:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
	
    if (dropTarget != nil)
        [dropTarget draggingExited:sender];
    else if ([super respondsToSelector:@selector(draggingExited:)])
        [super draggingExited:sender];
}

- (BOOL)prepareForDragOperation:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
    BOOL result = FALSE;
	
    if (dropTarget != nil)
        result = [dropTarget prepareForDragOperation:sender];
    else if ([super respondsToSelector:@selector(prepareForDragOperation:)])
        result = [super prepareForDragOperation:sender];
	
    return result;
}

- (BOOL)performDragOperation:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
    BOOL result = FALSE;
	
    if (dropTarget != nil)
        result = [dropTarget performDragOperation:sender];
    else if ([super respondsToSelector:@selector(performDragOperation:)])
        result = [super performDragOperation:sender];
	
    return result;
}

- (void)concludeDragOperation:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
	
    if (dropTarget != nil)
        [dropTarget concludeDragOperation:sender];
    else if ([super respondsToSelector:@selector(concludeDragOperation:)])
        [super concludeDragOperation:sender];
}

- (void)draggingEnded:(id <NSDraggingInfo>)sender
{
    // If draggingDestination is nil route the message to the superclass:
    CDropTarget *dropTarget = self._dropTarget;
	
    if (dropTarget != nil)
        [dropTarget draggingEnded:sender];
    else if ([super respondsToSelector:@selector(draggingEnded:)])
        [super draggingEnded:sender];
}

/********************************  END NSDraggingDestination Interface  ********************************/

@end // AWTView

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
