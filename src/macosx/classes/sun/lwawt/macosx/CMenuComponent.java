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

import java.awt.Font;
import java.awt.MenuComponent;
import java.awt.peer.MenuComponentPeer;

public abstract class CMenuComponent implements MenuComponentPeer {

    private MenuComponent target;
    private long modelPtr;

    CMenuComponent(MenuComponent target) {
        this.target = target;
        this.modelPtr = createModel();
    }

    MenuComponent getTarget() {
        return target;
    }

    long getModel() {
        return modelPtr;
    }

    protected abstract long createModel();

    public void dispose() {
        LWCToolkit.targetDisposedPeer(target, this);
        nativeDispose(modelPtr);
        target = null;
    }

    private native void nativeDispose(long modelPtr);
    
    // 1.5 peer method
    public void setFont(Font f) {
        // no-op, as we don't currently support menu fonts
        // c.f. radar 4032912
    }
}
