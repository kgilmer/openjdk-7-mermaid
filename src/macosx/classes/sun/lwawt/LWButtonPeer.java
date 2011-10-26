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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.ButtonPeer;

import javax.swing.*;

final class LWButtonPeer extends LWComponentPeer<Button, JButton>
        implements ButtonPeer, ActionListener {

    // We don't support background and foreground colors for button
    // since AquaButtonUI doesn't do it
    private Color background, foreground;

    LWButtonPeer(final Button target, PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    @Override
    protected JButton createDelegate() {
        return new JButton() {
            public boolean hasFocus() {
                return getTarget().hasFocus();
            }
        };
    }

    @Override
    public void initialize() {
        super.initialize();
        setLabel(getTarget().getLabel());
        synchronized (getDelegateLock()) {
            getDelegate().addActionListener(this);
        }
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getForeground() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        postEvent(new ActionEvent(getTarget(), ActionEvent.ACTION_PERFORMED,
                getTarget().getActionCommand(), e.getWhen(),
                e.getModifiers()));
    }

    @Override
    public void setLabel(final String label) {
        synchronized (getDelegateLock()) {
            getDelegate().setText(label);
        }
    }

    @Override
    public boolean isFocusable() {
        return true;
    }
}
