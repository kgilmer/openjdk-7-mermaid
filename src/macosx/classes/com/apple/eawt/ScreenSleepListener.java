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

package com.apple.eawt;

import com.apple.eawt.AppEvent.ScreenSleepEvent;

/**
 * Implementors receive notification when the displays attached to the system have entered power save sleep.
 * 
 * This notification is useful for discontinuing a costly animation, or indicating that the user is no longer present on a network service.
 * 
 * This message is not sent on Mac OS X versions prior to 10.6.
 * 
 * @see Application#addAppEventListener(AppEventListener)
 * 
 * @since Java for Mac OS X 10.6 Update 3
 * @since Java for Mac OS X 10.5 Update 8
 */
public interface ScreenSleepListener extends AppEventListener {
    /**
     * Called when the system displays have entered power save sleep.
     * @param e the screen sleep event
     */
    public void screenAboutToSleep(final ScreenSleepEvent e);
    
    /**
     * Called when the system displays have awoke from power save sleep.
     * @param e the screen sleep event
     */
    public void screenAwoke(final ScreenSleepEvent e);
}
