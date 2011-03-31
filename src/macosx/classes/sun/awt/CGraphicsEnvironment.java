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

package sun.awt;

import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.HeadlessException;
import java.util.HashMap;
import java.util.Map;

import sun.awt.FontConfiguration;
import sun.awt.SunToolkit;

import sun.lwawt.macosx.LWCToolkit;

import sun.font.FontFamily;
import sun.java2d.MacosxSurfaceManagerFactory;
import sun.java2d.SunGraphicsEnvironment;
import sun.java2d.SurfaceManagerFactory;

/**
 * This is an implementation of a GraphicsEnvironment object for the
 * default local GraphicsEnvironment used by the Java Runtime Environment
 * for Mac OS X GUI environments.
 *
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 */
public class CGraphicsEnvironment extends SunGraphicsEnvironment {

    /**
     * Noop function that just acts as an entry point for someone to force
     * a static initialization of this class.
     */
    public static void init() {
    }

    /**
     * Construct a new instance.
     */
    public CGraphicsEnvironment() {
        if (isHeadless()) {
            devices = null;
            displayReconfigContext = 0L;
        } else {
            /* Populate the device table */
            devices = new HashMap<Integer,CGraphicsDevice>();
            initDevices();

            /* Register our display reconfiguration listener */
            displayReconfigContext = registerDisplayReconfiguration();
            if (displayReconfigContext == 0L) {
                throw new RuntimeException(
                        "Could not register CoreGraphics display " +
                        "reconfiguration callback");
            }
        }
    }
    
    /**
     * Register our instance with CGDisplayRegisterReconfigurationCallback()
     * The registration uses a weak global reference -- if our instance is
     * garbage collected, the reference will be dropped.
     * 
     * @return Return the registration context (a pointer).
     */
    private native long registerDisplayReconfiguration();

    /**
     * Remove our instance's registration with
     * CGDisplayRemoveReconfigurationCallback().
     */
    private native void deregisterDisplayReconfiguration(long context);
    
    /**
     * Called by our CoreGraphics Display Reconfiguration Callback.
     * @param displayId CoreGraphics displayId
     */
    void _displayReconfiguration(long displayId) {
        displayChanged();
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            super.finalize();
        } finally {
            deregisterDisplayReconfiguration(displayReconfigContext);
        }
    }
    
    /**
     * (Re)create all CGraphicsDevices
     * @return 
     */
    private synchronized void initDevices() {
        int[] displayIDs = CGraphicsEnvironment.getDisplayIDs();

        devices.clear();
        for (int displayID : displayIDs) {
            devices.put(displayID, new CGraphicsDevice(displayID));
        }
    }
    
    /**
     * Retrieve a CGraphicsDevice for a given CoreGraphics DisplayID
     */
    private synchronized CGraphicsDevice getCoreGraphicsDevice(int displayID) {
        return devices.get(displayID);
    }
    
    /**
     * Retrieve an array of all CGraphicsDevices
     */
    private synchronized CGraphicsDevice[] getCoreGraphicsDevices() {
        return devices.values().toArray(new CGraphicsDevice[devices.values().size()]);
    }
    
    /**
     * Fetch an array of all valid CoreGraphics display identifiers.
     */
    private static native int[] getDisplayIDs();
    
    /**
     * Fetch the CoreGraphics display ID for the 'main' display.
     */
    private static native int getMainDisplayID();
    
    @Override
    public GraphicsDevice getDefaultScreenDevice() throws HeadlessException {
        return getCoreGraphicsDevice(getMainDisplayID());
    }
    
    @Override
    public GraphicsDevice[] getScreenDevices() throws HeadlessException {
        return getCoreGraphicsDevices();
    }
        
    @Override
    protected int getNumScreens() {
        return devices.size();
    }
    
    @Override
    protected GraphicsDevice makeScreenDevice(int screennum) {
        throw new UnsupportedOperationException(
            "This method is unused and " +
            "should not be called in our implementation");
    }
        
    /** Available CoreGraphics displays. */
    private final Map<Integer,CGraphicsDevice> devices;
        
    /** Reference to our display reconfiguration callback context. */
    private final long displayReconfigContext;
        
    /**
     * Global initialization of the Cocoa runtime.
     */
    private static native void initCocoa();
    static {
	System.err.println("CGE.<clinit>");
	System.err.flush();
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Object>() {
                public Object run() {
                    if (!isHeadless()) {
                        initCocoa();
                    }
                    return null;
                }
            }
        );

        // Install the correct surface manager factory.
        SurfaceManagerFactory.setInstance(new MacosxSurfaceManagerFactory());
    }
        
    @Override
    public boolean isDisplayLocal() {
	// TODO: not implemented
	return false;
    }
}

