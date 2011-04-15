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

import java.awt.BufferCapabilities.FlipContents;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;

import sun.java2d.SurfaceData;
import sun.lwawt.*;
import sun.util.logging.PlatformLogger;

public class CPlatformWindow implements PlatformWindow {
	
	// TODO: deal with these properties
    public static final String DOCUMENT_MODIFIED = "Window.documentModified";
    
    public static final String DRAGGABLE_WINDOW_BACKGROUND = "apple.awt.draggableWindowBackground";
    
    public static final String WINDOW_ALPHA = "Window.alpha";
    public static final String WINDOW_SHADOW = "Window.shadow";

    public static final String WINDOW_SHADOW_REVALIDATE_NOW = "apple.awt.windowShadow.revalidateNow";
    
	public static final String WINDOW_FADE_OUT = "";
	public static final String WINDOW_FADE_DELEGATE = "";
	
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.lwawt.macosx.CPlatformWindow");

    private CPlatformView contentView = null;
    
    // Bounds of the native widget but in the Java coordinate system.
	// In order to keep it up-to-date we will update them on
	// 1) setting native bounds via nativeSetBounds() call
	// 2) getting notification from the native level via deliverMoveResizeEvent()
    private Rectangle nativeBounds;
    
    private static final Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);

    protected Window target;
    private CPlatformWindow ownerPlatformWindow;

    private long awtWindowPtr;

    protected LWWindowPeer peer;

    private boolean undecorated;

    private volatile boolean isFocusable;
    
    private volatile boolean isFullScreenMode = false;

    public CPlatformWindow(boolean undecorated) {
        this.undecorated = undecorated;
    }

//TODO: use Volatile image instead of buffered.
//    private BufferCapabilities backBufferCaps;
//    private VolatileImage backBuffer = null;
//    private long backBufferPtr;

    //TODO: add opacity
    //private float opacity;

    /*
     * Delegate initialization (create native window and all the
     * related resources).
     */
    @Override // PlatformWindow
    public void initialize(Window target, LWWindowPeer peer, PlatformWindow owner) {
        this.peer = peer;
        this.target = target;
        this.ownerPlatformWindow = (CPlatformWindow)owner;

        Font f = target.getFont();
        if (f == null) {
            target.setFont(defaultFont);
        }
        Color c = target.getBackground();
        if (c == null) {
            target.setBackground(SystemColor.window);
        } else {
            // first we check if user provided alpha for background. This is
            // similar to what Apple's Java do. 
            // Since JDK7 we should rely on setOpacity() only.
            // this.opacity = c.getAlpha();
            // System.out.println("Delegate assigns alpha (we ignore setOpacity()):"
            // +this.opacity);
        }
        c = target.getForeground();
        if (c == null) {
            target.setForeground(SystemColor.windowText);
            // we should not call setForeground because it will call a repaint
            // which the peer may not be ready to do yet.
        }

        isFocusable = isNativelyFocusableWindow();
        
        contentView = new CPlatformView();
        contentView.initialize(peer);

        awtWindowPtr = nativeCreateWindow(contentView.getAWTView(),
                                          !undecorated, //hasBorder 
                                          0, 0, //x, y, width, height
                                          0, 0
                                          );
        
        contentView.setWindow(awtWindowPtr);

        // Since JDK7 we have standard way to set opacity, so we should not pick
        // background's alpha.
        // TODO: set appropriate opacity value
        //        this.opacity = target.getOpacity();
        //        this.setOpacity(this.opacity);
    }

    private native void nativeSetMenuBar(long nsWindow, long menuBarPtr);

    @Override // PlatformWindow
    public void setMenuBar(MenuBar mb) {
        if (!isValidPtr()) { return; }
        CMenuBar mbPeer = (CMenuBar)LWCToolkit.targetToPeer(mb);
        if (mbPeer != null) {
            nativeSetMenuBar(awtWindowPtr, mbPeer.getModel());
        }
    }

    @Override // PlatformWindow
    public Image createBackBuffer() {
        return contentView.createBackBuffer();
    }

    @Override // PlatformWindow
    public void dispose() {
        if (!isValidPtr()) {
            return;
        }
        if (ownerPlatformWindow != null) {
            CWrapper.NSWindow.removeChildWindow(ownerPlatformWindow.getAWTWindow(), getAWTWindow());
        }
        contentView.dispose();

        CWrapper.NSObject.release(awtWindowPtr);
        awtWindowPtr = 0L;
    }

    @Override // PlatformWindow
    public void flip(int x1, int y1, int x2, int y2, FlipContents flipAction) {
        // TODO: not implemented
        
    }

    @Override // PlatformWindow
    public FontMetrics getFontMetrics(Font f) {
        // TODO: not implemented
        return null;
    }

    @Override // PlatformWindow
    public Insets getInsets() {
        if (!isValidPtr()) {
            return null;
        }
        return nativeGetInsets(awtWindowPtr);
    }

    @Override // PlatformWindow
    public Point getLocationOnScreen() {
    	return new Point(nativeBounds.x, nativeBounds.y);
    }

    @Override // PlatformWindow
    public int getScreenImOn() {
        if (!isValidPtr()) {
            return 0;
        }
	// REMIND: we could also acquire screenID from the
	// graphicsConfig.getDevice().getCoreGraphicsScreen()
	// which might look a bit less natural but don't
	// require new native accessor.
        return nativeScreenOn(awtWindowPtr);
    }

    @Override // PlatformWindow
    public SurfaceData getScreenSurface() {
        // TODO: not implemented
        return null;
    }

    @Override // PlatformWindow
    public SurfaceData replaceSurfaceData() {
        return contentView.replaceSurfaceData();
    }

    @Override // PlatformWindow
    public void setBounds(int x, int y, int w, int h) {
        if (!isValidPtr()) {
            return;
        }

    	Rectangle newBounds = new Rectangle(x, y, w, h);
        Rectangle cocoaBounds = convertCoreCoordinatesToNSWindow(newBounds);
        nativeSetBounds(awtWindowPtr, cocoaBounds.x, cocoaBounds.y, cocoaBounds.width, cocoaBounds.height);
    }

    @Override // PlatformWindow
    public void setVisible(boolean visible) {
        if (ownerPlatformWindow != null) {
            if (!visible) {
                CWrapper.NSWindow.removeChildWindow(ownerPlatformWindow.getAWTWindow(),
                         getAWTWindow());
            }
        }

        updateTitleIconImages();
        updateFocusabilityForAutoRequestFocus(false);

        LWWindowPeer blocker = peer.getBlocker();
        if (blocker == null || !visible) {
            // If it ain't blocked, or is being hidden, go regular way
            setVisibleHelper(visible);
        } else {
            // otherwise, put it in a proper z-order
            CWrapper.NSWindow.orderWindow(getAWTWindow(), CWrapper.NSWindow.NSWindowBelow,
                    ((CPlatformWindow)blocker.getPlatformWindow()).getAWTWindow());
        }
        updateFocusabilityForAutoRequestFocus(true);

        if (ownerPlatformWindow != null) {
            if (visible) {
                CWrapper.NSWindow.addChildWindow(ownerPlatformWindow.getAWTWindow(),
                         getAWTWindow(), CWrapper.NSWindow.NSWindowAbove);
            }
        }
        if (blocker != null && visible) {
            // Make sure the blocker is above its siblings
            ((CPlatformWindow)blocker.getPlatformWindow()).orderAboveSiblings();
        }

    }

    @Override // PlatformWindow
    public void setTitle(String title) {
        nativeSetTitle(awtWindowPtr, title);
    }

/*
 * Should be called on every window key property change. 
 */
    @Override // PlatformWindow
    public void updateTitleIconImages() {
        CImage cImage = getImageForTarget();
        if (cImage != null) {
            nativeSetTitleIconImage(awtWindowPtr, cImage.getNSImagePtr());
        }
    }

    public long getAWTWindow() {
        return awtWindowPtr;
    }    
    
    public SurfaceData getSurfaceData() {
        return contentView.getSurfaceData();
    }

    @Override  // PlatformWindow
    public void toBack() {
        if (!isValidPtr()) {
            return;
        }
        nativeToBack(awtWindowPtr);
    }

    @Override  // PlatformWindow
    public void toFront() {
        if (!isValidPtr()) {
            return;
        }

        updateFocusabilityForAutoRequestFocus(false);
        nativeToFront(awtWindowPtr);
        updateFocusabilityForAutoRequestFocus(true);
    }

    @Override
    public void setResizable(boolean resizable) {
        if (!isValidPtr()) {
            return;
        }
        nativeSetResizable(awtWindowPtr, resizable);
    }

    @Override
    public void setMinimumSize(int width, int height) {
        if (!isValidPtr()) {
            return;
        }        
        nativeSetMinSize(awtWindowPtr, width, height);
    }

    @Override
    public boolean requestWindowFocus(boolean isMouseEventCause) {
//TODO: ask if the native system is to assign focus on the NSWindow.
        return true;
    }

    @Override
    public void updateFocusableWindowState() {
        if (!isValidPtr()) {
            return;
        }
        isFocusable = isNativelyFocusableWindow();
    }

    @Override
    public Graphics transformGraphics(Graphics g) {
        return g;
    }
	
    @Override
    public void setAlwaysOnTop(boolean isAlwaysOnTop) {
        if (!isValidPtr()) {
            return;
        }        
        nativeSetAlwaysOnTop(awtWindowPtr, isAlwaysOnTop);
    }

    @Override
    public void setOpacity(float opacity) {
        CWrapper.NSWindow.setAlphaValue(getAWTWindow(), opacity);
    }

    @Override
    public void enterFullScreenMode() {
        isFullScreenMode = true;
        contentView.enterFullScreenMode();
    }
    
    @Override
    public void exitFullScreenMode() {
        contentView.exitFullScreenMode();
        isFullScreenMode = false;
    }
    
    @Override
    public void setWindowState(int windowState) {
        int prevWindowState = peer.getState();
        if (prevWindowState != windowState) {
            switch (windowState) {
                case Frame.ICONIFIED:
                    if (!peer.isVisible()) {
                        // later on the setVisible will minimize itself
                        // otherwise, orderFront will deminiaturize window
                       return;
                    }
                    if (prevWindowState == Frame.MAXIMIZED_BOTH) {
                        // let's return into the normal states first
                        // the zoom call toggles between the normal and the max states
                        CWrapper.NSWindow.zoom(getAWTWindow());
                    }
                    CWrapper.NSWindow.miniaturize(getAWTWindow());
                    break;
                case Frame.MAXIMIZED_BOTH:
                    if (prevWindowState == Frame.ICONIFIED) {
                        // let's return into the normal states first
                        CWrapper.NSWindow.deminiaturize(getAWTWindow());
                    }
                    CWrapper.NSWindow.zoom(getAWTWindow());
                    break;
                case Frame.NORMAL:
                    if (prevWindowState == Frame.ICONIFIED) {
                        CWrapper.NSWindow.deminiaturize(getAWTWindow());
                    } else if (prevWindowState == Frame.MAXIMIZED_BOTH) {
                        // the zoom call toggles between the normal and the max states
                        CWrapper.NSWindow.zoom(getAWTWindow());
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown window state: "+windowState);
            }
            
            // NOTE: the SWP.windowState field gets updated to the newWindowState
            //       value when the native notification comes to us
        }
    }

    // ----------------------------------------------------------------------
    //                          UTILITY METHODS
    // ----------------------------------------------------------------------

    private void setVisibleHelper(boolean visible) {
        if (visible) {
            CWrapper.NSWindow.makeFirstResponder(getAWTWindow(), contentView.getAWTView());
            boolean isKeyWindow = CWrapper.NSWindow.isKeyWindow(getAWTWindow());
            if (!isKeyWindow) {
                CWrapper.NSWindow.makeKeyAndOrderFront(getAWTWindow());
            } else {
                CWrapper.NSWindow.orderFront(getAWTWindow());
            }
            if (target instanceof Frame) {
                if (((Frame)target).getExtendedState() == Frame.ICONIFIED) {
                    CWrapper.NSWindow.miniaturize(getAWTWindow());
                }
            }
        } else {
            if (target instanceof Frame) {
                if (((Frame)target).getExtendedState() == Frame.ICONIFIED) {
                    CWrapper.NSWindow.deminiaturize(getAWTWindow());
                }
            }
            CWrapper.NSWindow.orderOut(getAWTWindow());
        }        
    }
    
    /*
     * Find image to install into Title or into Application icon.
     * First try icons installed for toplevel. If there is no icon
     * use default Duke image.
     * This method shouldn't return null.
     */
    private CImage getImageForTarget() {
        List<Image> icons = target.getIconImages();
        if (icons == null || icons.size() == 0) {
            return null;
        }
        
        // TODO: need a walk-through to find the best image.
        // The best mean with higher resolution. Otherwise an icon looks bad.
        Image image = icons.get(0);
        CImage cImage = null;
        try {
            cImage = CImage.fromImage(image);
        } catch (Exception ignore) {
            log.fine("Invalid image.", ignore);
        }

        return cImage;
    }

    /*
     * Returns LWWindowPeer associated with this delegate. 
     */
    public LWWindowPeer getPeer() {
        return peer;
    }
    
    public CPlatformView getContentView() {
        return contentView;
    }
    
    /*
     * Once we disposed the ptr, it becomes invalid. In our case, the 0L value means it is invalid.
     */
    private boolean isValidPtr() {
        if (getAWTWindow() == 0L) {
            log.fine("Pointer to the native NSWindow is invalid. Disposed before?");
            return false;
        }
        return true;
    }

    /**
     * Convert CoreGraphics coordinates to NSWindow coordinates, on the given
     * {@link GraphicsConfiguration}.
     * <p/>
     * CoreGraphics: 0,0 is top left corner.
     * NSWindow: 0,0 is bottom left corner.
     * 
     * @param origBounds Rectangle using standard coordinates.
     * @return Rectangle mapped to NSWindow coordinates.
     */
    protected Rectangle convertCoreCoordinatesToNSWindow(Rectangle origBounds) {
        final Rectangle bounds = peer.getGraphicsConfiguration().getBounds();
        int y = bounds.height - (origBounds.y + origBounds.height);
        final Rectangle cocoaBounds =
            new Rectangle(origBounds.x, y, origBounds.width, origBounds.height);
        return cocoaBounds; 
    }

    private void validateSurface() {
        // on other platforms we create a new SurfaceData for every
        // live resize step, but on Mac OS X we just resize the onscreen
        // surface directly
        //TODO: by the time we come here first time we don't have surface yet
        SurfaceData surfaceData = getSurfaceData();
        if (surfaceData != null) {
            ((sun.java2d.opengl.CGLSurfaceData)surfaceData).setBounds();
        }
    }
    
/******************
 * Native methods.
 ******************/
    private native long nativeCreateWindow(long viewPointer, boolean withBorder,
                                           int x, int y, int width, int height);
    
    private native Insets nativeGetInsets(long awtWindow);

    private native void nativeSetBounds(long awtWindow, int x, int y, int w, int h);

    private native int nativeScreenOn(long awtWindow);
   
    protected native void nativeToBack(long awtWindow);

    protected native void nativeToFront(long awtWindow);

    protected native void nativeSetTitle(long awtWindow, String title);

    private native void nativeSetResizable(long awtWindow, boolean resizable);
   
    private native void nativeSetMinSize(long awtWindow, int w, int h);

    private native void nativeSetAlwaysOnTop(long awtWindow, boolean isAlwaysOnTop);

    private native void nativeSetTitleIconImage(long awtWindow, long nsImage);
    
    private native long nativeGetContentViewPtr(long awtWindow);
    
    /*************************************************************
     * Callbacks from the AWTWindowDelegate, AWTWindow and AWTView objc classes.
     *************************************************************/
    private void deliverWindowFocusEvent(boolean gained){
        peer.notifyActivation(gained);

        CImage image = getImageForTarget();
        if (gained && image != null) {
            ((LWCToolkit)Toolkit.getDefaultToolkit()).setApplicationIconImage(image);
        }
    }
    
    private void deliverMoveResizeEvent(int x, int y, int width, int height) {
        // when the content view enters the full-screen mode, the native
        // move/resize notifications contain a bounds smaller than
        // the whole screen and therefore we ignore the native notifications
        // and the content view itself creates correct synthetic notifications
        if (!isFullScreenMode) {
            nativeBounds = new Rectangle(x, y, width, height);
            peer.notifyReshape(x, y, width, height);
        }
    }
    
    private void deliverWindowClosingEvent() {
        if (ownerPlatformWindow != null) {
            CWrapper.NSWindow.removeChildWindow(ownerPlatformWindow.getAWTWindow(), getAWTWindow());
        }
        peer.postEvent(new WindowEvent(target, WindowEvent.WINDOW_CLOSING));
    }
    
    private void deliverIconify(final boolean iconify) {
        peer.notifyIconify(iconify);
    }

    private void deliverZoom(final boolean isZoomed) {
        peer.notifyZoom(isZoomed);
    }

    /*
     * Our focus model is synthetic and only non-simple window
     * may become natively focusable window.
     */
    private boolean isNativelyFocusableWindow() {
        return !peer.isSimpleWindow() && target.getFocusableWindowState();
    }
    
    /*
     * An utility method for the support of the auto request focus. 
     * Updates the focusable state of the window under certain
     * circumstances.
     */
    private void updateFocusabilityForAutoRequestFocus(boolean isFocusable) {
        if (!target.isAutoRequestFocus() && isNativelyFocusableWindow()) {
            this.isFocusable = isFocusable;
        }
    }

    private boolean checkBlocking() {
        LWWindowPeer blocker = peer.getBlocker();
        if (blocker == null) {
            return false;
        }

        CPlatformWindow pWindow = (CPlatformWindow)blocker.getPlatformWindow();

        pWindow.orderAboveSiblings();

        CWrapper.NSWindow.orderFrontRegardless(pWindow.getAWTWindow());
        CWrapper.NSWindow.makeKeyAndOrderFront(pWindow.getAWTWindow());
        CWrapper.NSWindow.makeMainWindow(pWindow.getAWTWindow());

        return true;
    }

    private void orderAboveSiblings() {
        if (ownerPlatformWindow == null) {
            return;
        }

        // Recursively pop up the windows from the very bottom so that only
        // the very top-most one becomes the main window
        ownerPlatformWindow.orderAboveSiblings();

        // Order the window to front of the stack of child windows
        CWrapper.NSWindow.removeChildWindow(ownerPlatformWindow.getAWTWindow(), getAWTWindow());
        CWrapper.NSWindow.addChildWindow(ownerPlatformWindow.getAWTWindow(), getAWTWindow(),
                                         CWrapper.NSWindow.NSWindowAbove);
    }

    // ----------------------------------------------------------------------
    //                          NATIVE CALLBACKS
    // ----------------------------------------------------------------------

    private boolean canBecomeKeyWindow() {
        return isFocusable;
    }

    private void windowDidBecomeMain() {
        if (!checkBlocking()) {
            // If it's not blocked, make sure it's above its siblings
            orderAboveSiblings();
        }
    }

    private boolean windowShouldClose() {
        return peer.getBlocker() == null;
    }
}
