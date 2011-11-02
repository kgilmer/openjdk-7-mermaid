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
import javax.swing.text.Document;
import java.awt.*;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextAreaPeer;

final class LWTextAreaPeer
        extends LWTextComponentPeer<TextArea, LWTextAreaPeer.ScrollableJTextArea>
        implements TextAreaPeer {

    private static final int DEFAULT_COLUMNS = 60;
    private static final int DEFAULT_ROWS = 10;

    LWTextAreaPeer(final TextArea target,
                   final PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    @Override
    protected ScrollableJTextArea createDelegate() {
        return new ScrollableJTextArea();
    }

    @Override
    public void initialize() {
        super.initialize();
        setText(getTarget().getText());
    }

    @Override
    public Document getDocument() {
        return getDelegate().getView().getDocument();
    }

    @Override
    protected Component getDelegateFocusOwner() {
        return getDelegate().getView();
    }

    @Override
    public Dimension getMinimumSize() {
        return getMinimumSize(DEFAULT_ROWS, DEFAULT_COLUMNS);
    }

    @Override
    public Dimension getMinimumSize(final int rows, final int columns) {
        return getPreferredSize(rows, columns);
    }

    @Override
    public Dimension getPreferredSize(final int rows, final int columns) {
        final Dimension size = super.getPreferredSize(rows, columns);
        synchronized (getDelegateLock()) {
            int scrollbarW = getDelegate().getVerticalScrollBar().getWidth();
            int scrollbarH = getDelegate().getHorizontalScrollBar().getHeight();
            return new Dimension(size.width + scrollbarW,
                                 size.height + scrollbarH);
        }
    }

    @Override
    public void setText(final String label) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().setText(label);
        }
        repaintPeer();
    }

    @Override
    public String getText() {
        synchronized (getDelegateLock()) {
            return getDelegate().getView().getText();
        }
    }

    @Override
    public boolean isFocusable() {
        return getTarget().isFocusable();
    }

    @Override
    public void insert(final String text, final int pos) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().insert(text, pos);

        }
        repaintPeer();
    }

    @Override
    public void replaceRange(final String text, final int start,
                             final int end) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().replaceRange(text, start, end);
        }
        repaintPeer();
    }


    @Override
    public void setEditable(final boolean editable) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().setEditable(editable);
        }
    }

    @Override
    public int getSelectionStart() {
        synchronized (getDelegateLock()) {
            return getDelegate().getView().getSelectionStart();
        }
    }

    @Override
    public int getSelectionEnd() {
        synchronized (getDelegateLock()) {
            return getDelegate().getView().getSelectionEnd();
        }
    }

    @Override
    public void select(final int selStart, final int selEnd) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().select(selStart, selEnd);
        }
    }

    @Override
    public void setCaretPosition(final int pos) {
        synchronized (getDelegateLock()) {
            getDelegate().getView().setCaretPosition(pos);
        }
    }

    @Override
    public int getCaretPosition() {
        synchronized (getDelegateLock()) {
            return getDelegate().getView().getCaretPosition();
        }
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        return null;
    }


    @SuppressWarnings("serial")
    final class ScrollableJTextArea extends JScrollPane {

        ScrollableJTextArea() {
            getViewport().setView(new JTextAreaDelegate());
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }

        public JTextArea getView() {
            return (JTextArea) getViewport().getView();
        }

        @Override
        public void setEnabled(final boolean enabled) {
            getViewport().getView().setEnabled(enabled);
            super.setEnabled(enabled);
        }

        @SuppressWarnings("serial")
        private final class JTextAreaDelegate extends JTextArea {

            // Empty non private constructor was added because access to this
            // class shouldn't be emulated by a synthetic accessor method.
            JTextAreaDelegate() {
                super();
            }

            @Override
            public boolean hasFocus() {
                return getTarget().hasFocus();
            }

            @Override
            public Point getLocationOnScreen() {
                return LWTextAreaPeer.this.getLocationOnScreen();
            }
        }
    }
}
