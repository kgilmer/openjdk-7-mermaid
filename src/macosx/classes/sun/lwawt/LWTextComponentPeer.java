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

import java.awt.FontMetrics;
import java.awt.SystemColor;
import java.awt.TextComponent;
import java.awt.event.TextEvent;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

abstract class LWTextComponentPeer<T extends TextComponent, D extends JComponent>
        extends LWComponentPeer<T, D> {
    protected static final int MARGIN = 2;
    protected static final int SPACE = 1;
    protected static final char WIDE_CHAR = 'W';

    LWTextComponentPeer(final T target,
                        final PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    /**
     * Returns height of the line in textarea or textfield.
     */
    protected static int getItemHeight(final FontMetrics metrics) {
        return (metrics.getHeight() - metrics.getLeading()) + (2 * SPACE);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!getTarget().isBackgroundSet()) {
            getTarget().setBackground(SystemColor.text);
        }
        synchronized (getDelegateLock()) {
            getDocument().addDocumentListener(new SwingTextComponentDocumentListener());
        }
    }

    abstract Document getDocument();

    private final class SwingTextComponentDocumentListener
            implements DocumentListener {

        void sendTextEvent(DocumentEvent de) {
            postEvent(new TextEvent(getTarget(), TextEvent.TEXT_VALUE_CHANGED));
            synchronized (getDelegateLock()) {
                getDelegate().invalidate();
                getDelegate().validate();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }
    }
}
