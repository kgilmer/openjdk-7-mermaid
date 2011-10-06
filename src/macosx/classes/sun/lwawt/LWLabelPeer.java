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

import java.awt.Label;
import java.awt.peer.LabelPeer;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

final class LWLabelPeer extends LWComponentPeer<Label, JLabel>
        implements LabelPeer {

    LWLabelPeer(final Label target, PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    @Override
    protected JLabel createDelegate() {
        final JLabel label = new JLabel();
        label.setOpaque(true);
        label.setVerticalAlignment(SwingConstants.TOP);
        return label;
    }

    @Override
    public void initialize() {
        super.initialize();
        setText(getTarget().getText());
        setAlignment(getTarget().getAlignment());
    }

    @Override
    public void setText(final String label) {
        synchronized (getDelegateLock()) {
            getDelegate().setText(label);
        }
    }

    @Override
    public void setAlignment(final int alignment) {
        synchronized (getDelegateLock()) {
            getDelegate().setHorizontalAlignment(convertAlignment(alignment));
        }
    }

    private static int convertAlignment(final int alignment) {
        switch (alignment) {
            case Label.CENTER:
                return SwingConstants.CENTER;
            case Label.RIGHT:
                return SwingConstants.RIGHT;
            default:
                return SwingConstants.LEFT;
        }
    }
}
