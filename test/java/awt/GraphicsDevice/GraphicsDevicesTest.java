/*
 * Copyright (c) 2006, 2007, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/**
 @test
 @summary <rdar://problem/4490646>
 @summary Test that makes sure that there is at least one GraphicsDevice (monitor) and at least one DisplayMode (screen settings)
 @summary com.apple.junit.java.awt.Device
 @run main GraphicsDevicesTest
 */
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.DisplayMode;

public class GraphicsDevicesTest 
{
    public static void main(String[] args) throws Exception
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if ( ge == null )
            throw new RuntimeException("Test failed: no graphics environment found.");

        GraphicsDevice[] gds = ge.getScreenDevices();
        if ( gds.length < 1 ) // need at least one device
            throw new RuntimeException("Test failed: no screen devices found.");
        
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        if ( gd == null )
            throw new RuntimeException("Test failed: no default screen device found.");
        
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        if ( gc == null )
            throw new RuntimeException("Test failed: no default configuration found.");
        
        DisplayMode[] dms = gd.getDisplayModes();
        if ( dms.length < 1 ) // need at least one device
            throw new RuntimeException("Test failed: no display modes found.");
    }
}
