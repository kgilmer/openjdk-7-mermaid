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

package sun.lwawt;

import java.awt.*;

import sun.java2d.SurfaceData;

// TODO Is it worth to generify this interface, like that:
//
// public interface PlatformWindow<WindowType extends Window>
//
// ?

public interface PlatformWindow {

    /*
     * Delegate initialization (create native window and all the
     * related resources).
     */
    public void initialize(Window target, LWWindowPeer peer, PlatformWindow owner);

    /*
     * Delegate shutdown (dispose native window and all the
     * related resources).
     */
    public void dispose();

    /*
     * Shows or hides the window.
     */
    public void setVisible(boolean visible);

    /*
     * Sets the window title
     */
    public void setTitle(String title);

    /*
     * Sets the window bounds. Called when user changes window bounds
     * with setSize/setLocation/setBounds/reshape methods.
     */
    public void setBounds(int x, int y, int w, int h);

    /*
     * Returns the screen number where the window is.
     */
    public int getScreenImOn();

    /*
     * Returns the location of the window.
     */
    public Point getLocationOnScreen();

    /*
     * Returns the window insets.
     */
    public Insets getInsets();

    /*
     * Returns the metrics for a given font.
     */
    public FontMetrics getFontMetrics(Font f);

    /*
     * Get the SurfaceData for the window.
     */
    public SurfaceData getScreenSurface();

    /*
     * Revalidates the window's current SurfaceData and returns
     * the newly created one.
     */
    public SurfaceData replaceSurfaceData();

    /*
     * Creates a new image to serve as a back buffer.
     */
    public Image createBackBuffer();

    /*
     * Move the given part of the back buffer to the front buffer.
     */
    public void flip(int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction);

    public void toFront();

    public void toBack();

    public void setMenuBar(MenuBar mb);

    public void setAlwaysOnTop(boolean value);

    public void updateFocusableWindowState();

    public boolean requestWindowFocus(boolean isMouseEventCause);

    public void setResizable(boolean resizable);

    public void setMinimumSize(int width, int height);

    /**
     * Transforms the given Graphics object according to the native
     * implementation traits (insets, etc.).
     */
    public Graphics transformGraphics(Graphics g);

    /*
     * Installs the images for particular window.
     */
    public void updateIconImages();

    public void setOpacity(float opacity);
    
    public void setOpaque(boolean isOpaque);
    
    public void enterFullScreenMode();
    
    public void exitFullScreenMode();

    public void setWindowState(int windowState);
}
