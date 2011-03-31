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

package sun.lwawt.macosx;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.VolatileImage;

import sun.java2d.SurfaceData;

import sun.lwawt.LWWindowPeer;
import sun.lwawt.macosx.event.NSEvent;

import sun.awt.CGraphicsConfig;

public class CPlatformView {

    private long awtViewPtr; // TODO: isValid check
    private LWWindowPeer peer;
    
    // the pointer to the enveloping window
    // this might be a foreign window if it's an embedded view
    private volatile long windowPtr = 0L;

    private SurfaceData surfaceData;
    
    public CPlatformView() {
    }

    public void initialize(LWWindowPeer peer) {
        this.peer = peer;
        
        awtViewPtr = nativeCreateView(0, 0, 0, 0);
    }

    /*
     * All coordinates passed to the method should be based
     * on the origin being in the bottom-left corner
     * (standard Cocoa coordinates).
     */
    public void setBounds(int x, int y, int width, int height) {
        CWrapper.NSView.setFrame(getAWTView(), x, y, width, height);
    }

    // REMIND: CGLSurfaceData expects top-level's size
    public Rectangle getBounds() {
        return peer.getBounds();
    }    

    public void dispose() {
        CWrapper.NSObject.release(awtViewPtr);
        awtViewPtr = 0L;
    }

    private native long nativeCreateView(int x, int y, int width, int height);

    public long getAWTView() {
        return awtViewPtr;
    }

    public Object getDestination() {
        return peer;
    }

    public long getWindow() {
        return windowPtr;
    }
    
    public void setWindow(long windowPtr) {
        this.windowPtr = windowPtr;
    }
    
    public void enterFullScreenMode() {
        CWrapper.NSView.enterFullScreenMode(awtViewPtr);
        
        // REMIND: CGLSurfaceData expects top-level's size
        // and therefore we need to account insets before
        // recreating the surface data
        Insets insets = peer.getInsets();

        long screenPtr = CWrapper.NSWindow.screen(getWindow());
        Rectangle screenBounds = CWrapper.NSScreen.frame(screenPtr);

        // the move/size notification from the underlying system comes
        // but it contains a bounds smaller than the whole screen
        // and therefore we need to create the synthetic notifications
        peer.notifyReshape(screenBounds.x - insets.left,
                           screenBounds.y - insets.bottom,
                           screenBounds.width + insets.left + insets.right,
                           screenBounds.height + insets.top + insets.bottom);
    }

    public void exitFullScreenMode() {
        CWrapper.NSView.exitFullScreenMode(awtViewPtr);
    }

    // ----------------------------------------------------------------------
    //                          PAINTING METHODS
    // ----------------------------------------------------------------------    
    
    public void drawImageOnPeer(VolatileImage xBackBuffer,
                                int x1, int y1, int x2, int y2) {
        Graphics g = peer.getGraphics();
        try {
            g.drawImage(xBackBuffer,
                        x1, y1, x2, y2,
                        x1, y1, x2, y2,
                        null);
        } finally {
            g.dispose();
        }
    }    
    
    public Image createBackBuffer() {
        Rectangle r = peer.getBounds();
        Image im = null;
        if (!r.isEmpty()) {
            im = peer.getGraphicsConfiguration().createCompatibleImage(r.width, r.height);
        }
        return im;
    }    

    public SurfaceData replaceSurfaceData() {
        if (surfaceData == null) {
            CGraphicsConfig graphicsConfig = (CGraphicsConfig)peer.getGraphicsConfiguration();
            surfaceData = graphicsConfig.createSurfaceData(this);
        } else {
            validateSurface();
        }
        return surfaceData;
    }    

    private void validateSurface() {
        // on other platforms we create a new SurfaceData for every
        // live resize step, but on Mac OS X we just resize the onscreen
        // surface directly
        //TODO: by the time we come here first time we don't have surface yet
        if (surfaceData != null) {
            ((sun.java2d.opengl.CGLSurfaceData)surfaceData).setBounds();
        }
    }

    public GraphicsConfiguration getGraphicsConfiguration() {
        return peer.getGraphicsConfiguration();
    }

    public SurfaceData getSurfaceData() {
        return surfaceData;
    }    

    // ----------------------------------------------------------------------
    //                          NATIVE CALLBACKS
    // ----------------------------------------------------------------------

    //This code uses peer's API which is a no-no on the AppKit thread.
    //TODO: post onto the EDT.
    private void deliverMouseEvent(NSEvent event) {
        int jModifiers = event.getModifiers();
        int jX = event.getX();
        //a difference in coordinate systems
        int jY = getBounds().height - event.getY();
        int jAbsX = event.getAbsX();
        int jAbsY = event.getAbsY();
        int jButton = NSEvent.nsButtonToJavaButton(event.getButton());
        int jClickCount = event.getClickCount();
        double wheelDeltaY = event.getScrollDeltaY();
        double wheelDeltaX = event.getScrollDeltaX();
        boolean isPopupTrigger = event.isPopupTrigger();
        
        int jEventType;
        switch (event.getType()) {
            case CocoaConstants.NSLeftMouseDown:
            case CocoaConstants.NSRightMouseDown:
                //TODO:          case CocoaConstants.NSOtherMouseDown:
                jEventType = MouseEvent.MOUSE_PRESSED;
                break;
            case CocoaConstants.NSLeftMouseUp:
            case CocoaConstants.NSRightMouseUp:
                //TODO:          case CocoaConstants.NSOtherMouseUp:
                jEventType = MouseEvent.MOUSE_RELEASED;
                break;
            case CocoaConstants.NSMouseMoved:
                jEventType = MouseEvent.MOUSE_MOVED;
                break;
            case CocoaConstants.NSLeftMouseDragged:
            case CocoaConstants.NSRightMouseDragged:
                //TODO:        case CocoaConstants.NSOtherMouseDragged:
                jEventType = MouseEvent.MOUSE_DRAGGED;
                break;
                //Intentionally left Entered without any action as the peer should keep track
                //on the components' state
            case CocoaConstants.NSMouseExited:
                jEventType = MouseEvent.MOUSE_EXITED;
                break;
            case CocoaConstants.NSScrollWheel:
                jEventType = MouseEvent.MOUSE_WHEEL;
                double wheelDelta = wheelDeltaY;
                
                // shift+vertical wheel scroll produces horizontal scroll
                // we convert it to vertical
                if ((jModifiers & KeyEvent.SHIFT_DOWN_MASK) != 0) {
                    wheelDelta = wheelDeltaX;
                }
                //Wheel amount "oriented" inside out
                wheelDelta = -wheelDelta;
                peer.dispatchMouseWheelEvent(System.currentTimeMillis(), 
                                             jX, jY,
                                             jModifiers,
                                             MouseWheelEvent.WHEEL_UNIT_SCROLL, 
                                             3, // WHEEL_SCROLL_AMOUNT
                                             (int)wheelDelta, wheelDelta,
                                             null);
                return;
            default:
                return;
        }
        peer.dispatchMouseEvent(jEventType, 
                                System.currentTimeMillis(), 
                                jButton, 
                                jX, jY, 
                                jAbsX, jAbsY,
                                jModifiers, 
                                jClickCount, 
                                isPopupTrigger,
                                null);
    }    

    private void deliverKeyEvent(final int javaKeyType, final int javaModifiers,
                                 final char testChar, final int javaKeyCode, final int javaKeyLocation) {
        //TODO: there is no focus owner installed now, get back to this once we assign it properly.
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                peer.dispatchKeyEvent(javaKeyType,
                                      System.currentTimeMillis(),
                                      javaModifiers,
                                      javaKeyCode,
                                      testChar,
                                      javaKeyLocation);
                //That's the reaction on the PRESSED (not RELEASED) event as it comes to
                //appear in MacOSX.
                //Modifier keys (shift, etc) don't want to send TYPED events.
                if (javaKeyType == KeyEvent.KEY_PRESSED && 
                    testChar != KeyEvent.CHAR_UNDEFINED)
                {
                    peer.dispatchKeyEvent(KeyEvent.KEY_TYPED, 
                                          System.currentTimeMillis(), 
                                          javaModifiers,
                                          KeyEvent.VK_UNDEFINED,
                                          testChar,
                                          KeyEvent.KEY_LOCATION_UNKNOWN);
                }
            }
        });
    }    

    private void deliverWindowDidExposeEvent() {
        Rectangle r = peer.getBounds();
        peer.notifyExpose(0, 0, r.width, r.height);
    }

    private void deliverWindowDidExposeEvent(float x, float y, float w, float h) {
        peer.notifyExpose((int)x, (int)y, (int)w, (int)h);
    }
}
