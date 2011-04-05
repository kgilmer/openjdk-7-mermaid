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
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.peer.*;
import java.util.*;

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
        
        initIDs();
        CWrapper.init();
    }

    @Override
    protected PlatformWindow createPlatformWindow(boolean undecorated, PeerType peerType) {
        // TODO: window type
        return new CPlatformWindow(undecorated);
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

    @Override
    public FontPeer getFontPeer(String name, int style) {
        // TODO Auto-generated method stub
        return null;
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
    public void beep() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int getScreenResolution() throws HeadlessException {
        return ((CGraphicsDevice) GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice()).getScreenResolution();
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
    private static void installToolkitThreadNameInJava(){
      Thread.currentThread().setName("CToolkit thread");
    }

    void setApplicationIconImage(CImage cImage){
        nativeSetApplicationIconImage(cImage.getNSImagePtr());
    }

    @Override
    public boolean isWindowOpacitySupported() {
        return true;
    }

    @Override
    public boolean isFrameStateSupported(int state) throws HeadlessException
    {
        switch (state) {
            case Frame.NORMAL:
            case Frame.ICONIFIED:
            case Frame.MAXIMIZED_BOTH:
                return true;
            default:
                return false;
        }
    }    
    
    /************************
     * Native methods section
     ************************/
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
