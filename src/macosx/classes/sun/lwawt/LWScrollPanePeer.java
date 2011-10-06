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

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.peer.ScrollPanePeer;
import java.util.List;

public class LWScrollPanePeer
        extends LWContainerPeer<ScrollPane, JScrollPane>
        implements ScrollPanePeer, ChangeListener {


    public LWScrollPanePeer(ScrollPane target, PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    protected JScrollPane createDelegate() {
        final JScrollPane sp = new JScrollPane();
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        sp.getViewport().setView(panel);
        sp.getViewport().addChangeListener(this);
        return sp;
    }

    public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (getDelegateLock()) {
                    LWComponentPeer viewPeer = getViewPeer();
                    if (viewPeer != null) {
                        viewPeer.setBounds(getDelegate().getViewport().getView().getBounds());
                    }
                }
            }
        });
    }

    public void initialize() {
        super.initialize();
        synchronized (getDelegateLock()) {
            getDelegate().getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            getDelegate().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            getDelegate().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        }
    }

    private LWComponentPeer getViewPeer() {
        List<LWComponentPeer> peerList = getChildren();
        return peerList.isEmpty() ? null : peerList.get(0);
    }


    protected Component getDelegateFocusOwner() {
        return getDelegate().getViewport().getView();
    }


    protected void peerPaintChildren(Graphics g, Rectangle r) {
        synchronized (getDelegateLock()) {
            Rectangle viewRect = getDelegate().getViewport().getViewRect();
            g.clipRect(0, 0, viewRect.width, viewRect.height);
        }
        super.peerPaintChildren(g, r);
    }

    public void layout() {
        super.layout();
        synchronized (getDelegateLock()) {
            LWComponentPeer viewPeer = getViewPeer();
            if (viewPeer != null) {
                Component view = getDelegate().getViewport().getView();
                view.setBounds(viewPeer.getBounds());
                view.setPreferredSize(viewPeer.getPreferredSize());
                view.setMinimumSize(viewPeer.getMinimumSize());
                getDelegate().invalidate();
                getDelegate().validate();
                viewPeer.setBounds(view.getBounds());
            }
        }
    }

    public void setScrollPosition(int x, int y) {
    }

    public int getHScrollbarHeight() {
        return 0;
    }

    public int getVScrollbarWidth() {
        return 0;
    }

    public void childResized(int w, int h) {
        synchronized (getDelegateLock()) {
            getDelegate().invalidate();
            getDelegate().validate();
        }
    }

    public void setUnitIncrement(Adjustable adj, int u) {
    }

    public void setValue(Adjustable adj, int v) {
    }
}
