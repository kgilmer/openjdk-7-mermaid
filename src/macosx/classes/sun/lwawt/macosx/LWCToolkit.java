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
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.peer.*;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;

import sun.awt.*;
import sun.lwawt.*;
import sun.lwawt.LWWindowPeer.PeerType;

/**
 * Mac OS X Cocoa-based AWT Toolkit.
 */
public class LWCToolkit extends LWToolkit {
    private static final Integer BUTTONS = 3; // number of available mouse buttons
    
    private static native void initIDs();
    
    static {
        System.err.flush();
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {
            public Object run() {
                System.loadLibrary("awt");
                System.loadLibrary("fontmanager");
                return null;
            }
        });
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    public LWCToolkit() {
    	SunToolkit.setDataTransfererClassName("sun.lwawt.macosx.CDataTransferer");
    }
    
    @Override
    protected PlatformWindow createPlatformWindow(PeerType peerType) {
        // TODO: window type
        return new CPlatformWindow(peerType);
    }

    @Override
    protected FileDialogPeer createFileDialogPeer(FileDialog target) {
        return new CFileDialog(target);
    }
    
    @Override
    public MenuPeer createMenu(Menu target) {
        MenuPeer peer = new CMenu(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuBarPeer createMenuBar(MenuBar target) {
         MenuBarPeer peer = new CMenuBar(target);
         targetCreatedPeer(target, peer);
             return peer;
    }

    @Override
    public MenuItemPeer createMenuItem(MenuItem target) {
        MenuItemPeer peer = new CMenuItem(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        CheckboxMenuItemPeer peer = new CCheckboxMenuItem(target);
        targetCreatedPeer(target, peer);
        return peer;
    }
    
    @Override
    public PanelPeer createPanel(Panel target) {
        LWPanelPeer peer = new LWPanelPeer(target);
        targetCreatedPeer(target, peer);
        peer.initialize();
        return peer;
    }

    @Override
    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        PopupMenuPeer peer = new CPopupMenu(target);
        targetCreatedPeer(target, peer);
        return peer;

    }

    @Override
    public SystemTrayPeer createSystemTray(SystemTray target) {
        SystemTrayPeer peer = new CSystemTray();
    	targetCreatedPeer(target, peer);
    	return peer;
    }

    @Override
    public TrayIconPeer createTrayIcon(TrayIcon target) {
        TrayIconPeer peer = new CTrayIcon(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public LWCursorManager getCursorManager() {
        return CCursorManager.getInstance(); 
    }

    @Override
    protected void platformCleanup() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void platformInit() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void platformRunMessage() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void platformShutdown() {
        // TODO Auto-generated method stub
        
    }

    class OSXPlatformFont extends sun.awt.PlatformFont
    {
        public OSXPlatformFont(String name, int style)
        {
            super(name, style);
        }
        protected char getMissingGlyphCharacter()
        {
            // Follow up for real implementation
            return (char)0xfff8; // see http://developer.apple.com/fonts/LastResortFont/
        }
    }
    public FontPeer getFontPeer(String name, int style) {
        return new OSXPlatformFont(name, style);
    }

    @Override
    protected MouseInfoPeer createMouseInfoPeerImpl() {
        return new CMouseInfoPeer();
    }


    @Override
    protected int getScreenHeight() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds().height;
    }

    @Override
    protected int getScreenWidth() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration().getBounds().width;
    }

    @Override
    protected void initializeDesktopProperties() {
        super.initializeDesktopProperties();
        Map <Object, Object> fontHints = new HashMap<Object, Object>();
        fontHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fontHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        desktopProperties.put(SunToolkit.DESKTOPFONTHINTS, fontHints);
        desktopProperties.put("awt.mouse.numButtons", BUTTONS);
    }

    
/*
 * The method returns true if some events were processed during that timeout.
 * @see sun.awt.SunToolkit#syncNativeQueue(long)
 */
    @Override
    protected boolean syncNativeQueue(long timeout) {
        return nativeSyncQueue(timeout);
    }

    @Override
    public native void beep();

    @Override
    public int getScreenResolution() throws HeadlessException {
        return ((CGraphicsDevice) GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()).getScreenResolution();
    }

    @Override
    public Insets getScreenInsets(final GraphicsConfiguration gc) {
        final CGraphicsConfig cgc = (CGraphicsConfig) gc;
        final int displayId = cgc.getDevice().getCoreGraphicsScreen();
        final long screen = CWrapper.NSScreen.screenByDisplayId(displayId);
        Rectangle fullScreen = CWrapper.NSScreen.frame(screen).getBounds();
        Rectangle workArea = CWrapper.NSScreen.visibleFrame(screen).getBounds();
        // Convert between Cocoa's coordinate system and Java.
        return new Insets(fullScreen.height - workArea.height - workArea.y,
                          workArea.x, workArea.y,
                          fullScreen.width - workArea.width - workArea.x);
    }

    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(
            InputMethodHighlight highlight) throws HeadlessException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sync() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void grab(Window w) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void ungrab(Window w) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
    	return new CRobot(target, (CGraphicsDevice)screen);
    }

    /*
     * TODO: take a look at the Cocoa API for this method, return false for now.
     */
    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
        return false;
    }

    /*
     * TODO: take a look on pointer API for Cocoa. For now, suppress extra-button                                 
     * mouse events.                                                                                              
     */
    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        return false;
    }

    @Override
    public boolean isNativeDoubleBufferingEnabled() {
        return true;
    }
    
    @Override
    public boolean isTraySupported() {
        return true;
    }
	
    @Override
    public boolean isAlwaysOnTopSupported() {
        return true;
    }

    // Intended to be called from the LWCToolkit.m only.
    private static void installToolkitThreadNameInJava() {
        Thread.currentThread().setName(CThreading.APPKIT_THREAD_NAME);
    }

    void setApplicationIconImage(CImage cImage){
        nativeSetApplicationIconImage(cImage.ptr);
    }

    @Override
    public boolean isWindowOpacitySupported() {
        return true;
    }

    @Override
    public boolean isFrameStateSupported(int state) throws HeadlessException {
        switch (state) {
            case Frame.NORMAL:
            case Frame.ICONIFIED:
            case Frame.MAXIMIZED_BOTH:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Image getImage(final String filename) {
        final Image nsImage = checkForNSImage(filename);
        if (nsImage != null) return nsImage;

        return super.getImage(filename);
    }

    static final String nsImagePrefix = "NSImage://";
    protected Image checkForNSImage(final String imageName) {
        if (imageName == null) return null;
        if (!imageName.startsWith(nsImagePrefix)) return null;
        return CImage.getCreator().createImageFromName(imageName.substring(nsImagePrefix.length()));
    }
    
    // Thread-safe Object.equals() called from native
    public static boolean doEquals(final Object a, final Object b, Component c) {
        if (a == b) return true;
        
        final boolean[] ret = new boolean[1];
        
        try {  invokeAndWait(new Runnable() { public void run() { synchronized(ret) {
            ret[0] = a.equals(b);
        }}}, c); } catch (Exception e) { e.printStackTrace(); }
        
        synchronized(ret) { return ret[0]; }
    }
    
    // Kicks an event over to the appropriate eventqueue and waits for it to finish
    // To avoid deadlocking, we manually run the NSRunLoop while waiting
    // Any selector invoked using ThreadUtilities performOnMainThread will be processed in doAWTRunLoop
    // The CInvocationEvent will call LWCToolkit.stopAWTRunLoop() when finished, which will stop our manual runloop
    public static void invokeAndWait(Runnable event, Component component) throws InterruptedException, InvocationTargetException {
        invokeAndWait(event, component, true);
    }
    
    public static <T> T invokeAndWait(final Callable<T> callable, Component component) throws Exception {
        final CallableWrapper<T> wrapper = new CallableWrapper<T>(callable);
        invokeAndWait(wrapper, component);
        return wrapper.getResult();
    }
    
    static final class CallableWrapper<T> implements Runnable {
        final Callable<T> callable;
        T object;
        Exception e;
        
        public CallableWrapper(final Callable<T> callable) {
            this.callable = callable;
        }
        
        public void run() {
            try {
                object = callable.call();
            } catch (final Exception e) {
                this.e = e;
            }
        }
        
        public T getResult() throws Exception {
            if (e != null) throw e;
            return object;
        }
    }
    
    public static void invokeAndWait(Runnable event, Component component, boolean detectDeadlocks) throws InterruptedException, InvocationTargetException {
        long mediator = createAWTRunLoopMediator();
        
        InvocationEvent invocationEvent = new CPeerEvent(event, mediator);
        
        if (component != null) {
            AppContext appContext = SunToolkit.targetToAppContext(component);
            SunToolkit.postEvent(appContext, invocationEvent);
            
            // 3746956 - flush events from PostEventQueue to prevent them from getting stuck and causing a deadlock
            sun.awt.SunToolkitSubclass.flushPendingEvents(appContext);
        } else {
            // This should be the equivalent to EventQueue.invokeAndWait
            ((LWCToolkit)Toolkit.getDefaultToolkit()).getSystemEventQueueForInvokeAndWait().postEvent(invocationEvent);
        }
        
        doAWTRunLoop(mediator, true, detectDeadlocks);
        
        Throwable eventException = invocationEvent.getException();
        if (eventException != null) {
            if (eventException instanceof UndeclaredThrowableException) {
                eventException = ((UndeclaredThrowableException)eventException).getUndeclaredThrowable();
            }
            throw new InvocationTargetException(eventException);
        }
    }
    
    public static void invokeLater(Runnable event, Component component) throws InvocationTargetException {
        final InvocationEvent invocationEvent = new CPeerEvent(event, 0);
        
        if (component != null) {
            final AppContext appContext = SunToolkit.targetToAppContext(component);
            SunToolkit.postEvent(appContext, invocationEvent);
            
            // 3746956 - flush events from PostEventQueue to prevent them from getting stuck and causing a deadlock
            sun.awt.SunToolkitSubclass.flushPendingEvents(appContext);
        } else {
            // This should be the equivalent to EventQueue.invokeAndWait
            ((LWCToolkit)Toolkit.getDefaultToolkit()).getSystemEventQueueForInvokeAndWait().postEvent(invocationEvent);
        }
        
        final Throwable eventException = invocationEvent.getException();
        if (eventException == null) return;
        
        if (eventException instanceof UndeclaredThrowableException) {
            throw new InvocationTargetException(((UndeclaredThrowableException)eventException).getUndeclaredThrowable());
        }
        throw new InvocationTargetException(eventException);
    }
    
    // This exists purely to get around permissions issues with getSystemEventQueueImpl
    EventQueue getSystemEventQueueForInvokeAndWait() {
        return getSystemEventQueueImpl();
    }
    
    
// DnD support 
    
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        DragSourceContextPeer dscp = CDragSourceContextPeer.createDragSourceContextPeer(dge);

        return dscp;
    }
    
    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        DragGestureRecognizer dgr = null;

        // Create a new mouse drag gesture recognizer if we have a class match:
        if (MouseDragGestureRecognizer.class.equals(abstractRecognizerClass))
            dgr = new CMouseDragGestureRecognizer(ds, c, srcActions, dgl);

        return (T)dgr;
    }
    
    
    
    // Extends PeerEvent because we want to pass long an ObjC mediator object and because we want these events to be posted early
    // Typically, rather than relying on the notifier to call notifyAll(), we use the mediator to stop the runloop
    public static class CPeerEvent extends PeerEvent {
        private long _mediator = 0;
        
        public CPeerEvent(Runnable runnable, long mediator) {
            super(Toolkit.getDefaultToolkit(), runnable, null, true, 0);
            _mediator = mediator;
        }
        
        public void dispatch() {
            try {
                super.dispatch();
            } finally {
                if (_mediator != 0) {
                    LWCToolkit.stopAWTRunLoop(_mediator);
                }
            }
        }
    }
    
    // Call through to native methods
    public static void doAWTRunLoop(long mediator, boolean awtMode) { doAWTRunLoop(mediator, awtMode, true); }
    public static void doAWTRunLoop(long mediator) { doAWTRunLoop(mediator, true); }
    
    /************************
     * Native methods section
     ************************/
    
    // These are public because they are accessed from WebKitPluginObject in JavaDeploy
    // Basic usage: 
    // createAWTRunLoopMediator. Start client code on another thread. doAWTRunLoop. When client code is finished, stopAWTRunLoop.
    public static native long createAWTRunLoopMediator();
    public static native void doAWTRunLoop(long mediator, boolean awtMode, boolean detectDeadlocks);
    public static native void stopAWTRunLoop(long mediator);
    
    private native boolean nativeSyncQueue(long timeout);

    private native void nativeSetApplicationIconImage(long imagePtr);

    @Override
    public Clipboard createPlatformClipboard() {
        return new CClipboard("System");
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == null) ||
            (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE) ||
            (exclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE) ||
            (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        //TODO: FileDialog blocks excluded windows...
        //TODO: Test: 2 file dialogs, separate AppContexts: a) Dialog 1 blocked, shouldn't be. Frame 4 blocked (shouldn't be).
        return (modalityType == null) ||
            (modalityType == Dialog.ModalityType.MODELESS) ||
            (modalityType == Dialog.ModalityType.DOCUMENT_MODAL) ||
            (modalityType == Dialog.ModalityType.APPLICATION_MODAL) ||
            (modalityType == Dialog.ModalityType.TOOLKIT_MODAL);
    }

}
