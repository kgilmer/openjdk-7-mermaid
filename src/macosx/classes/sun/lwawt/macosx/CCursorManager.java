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

import java.awt.Cursor;
import java.awt.Point;
import sun.lwawt.LWCursorManager;

public class CCursorManager extends LWCursorManager {

    private final static CCursorManager theInstance = new CCursorManager();

    private Cursor currentCursor = null;

    public synchronized static CCursorManager getInstance() {
        return theInstance;
    }

    private CCursorManager() {
        super();
    }

    @Override
    protected Point getCursorPosition() {
        return nativeGetCursorPosition();
    }

    @Override
    protected void setCursor(Cursor cursor) {
        if (cursor == null) {
            setArrowCursor();
            return;
        }

        // TODO: lock required
        switch (cursor.getType()) {
        case Cursor.HAND_CURSOR:
            setHandCursor();
            break;
        case Cursor.CROSSHAIR_CURSOR:
            setCrosshairCursor();
            break;
        case Cursor.E_RESIZE_CURSOR:
            setEResizeCursor();
            break;
        case Cursor.MOVE_CURSOR:
            setMoveCursor();
            break;
        case Cursor.N_RESIZE_CURSOR:
            setNResizeCursor();
            break;
        case Cursor.S_RESIZE_CURSOR:
            setSResizeCursor();
            break;
        case Cursor.TEXT_CURSOR:
            setTextCursor();
            break;
        case Cursor.W_RESIZE_CURSOR:
            setWResizeCursor();
            break;
        case Cursor.WAIT_CURSOR:
            setWaitCursor();
            break;
        case Cursor.DEFAULT_CURSOR:
            setArrowCursor();
            break;
        }

        // currentCursor = cursor;
    }

    /*************************************************/

    private native void setNResizeCursor();
    private native void setSResizeCursor();
    private native void setWResizeCursor();
    private native void setEResizeCursor();

    private native void setWaitCursor();
    private native void setMoveCursor();
    private native void setCrosshairCursor();
    private native void setHandCursor();
    private native void setArrowCursor();
    private native void setTextCursor();

    private native Point nativeGetCursorPosition();

}
