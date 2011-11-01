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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextFieldPeer;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

final class LWTextFieldPeer
        extends LWTextComponentPeer<TextField, JPasswordField>
        implements TextFieldPeer, ActionListener {

    private static final int DEFAULT_COLUMNS = 1;

    LWTextFieldPeer(final TextField target,
                    final PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    protected JPasswordField createDelegate() {
        final JPasswordField delegate = new JTextAreaDelegate();
        delegate.setOpaque(true);
        return delegate;
    }

    @Override
    public void initialize() {
        super.initialize();
        setText(getTarget().getText());
        setEchoChar(getTarget().getEchoChar());
        synchronized (getDelegateLock()) {
            getDelegate().addActionListener(this);
        }
    }

    @Override
    public Document getDocument() {
        return getDelegate().getDocument();
    }

    @Override
    public void setEchoChar(final char echoChar) {
        synchronized (getDelegateLock()) {
            getDelegate().setEchoChar(echoChar);
        }
    }

    @Override
    public Dimension getPreferredSize(final int columns) {
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d;
        if (fm != null) {
            final Insets insets;
            synchronized (getDelegateLock()) {
                insets = getDelegate().getInsets();
            }
            d = new Dimension(columns * fm.charWidth(WIDE_CHAR) + insets.left
                              + insets.right, getItemHeight(fm) + (2 * MARGIN));
        } else {
            d = new Dimension(columns * 10, 10 + (2 * MARGIN));
        }
        return d;
    }

    @Override
    public Dimension getMinimumSize(final int columns) {
        return getPreferredSize(columns);
    }

    @Override
    public Dimension getMinimumSize() {
        return getMinimumSize(DEFAULT_COLUMNS);
    }

    @Override
    public void setEditable(final boolean editable) {
        synchronized (getDelegateLock()) {
            getDelegate().setEditable(editable);
        }
    }

    @Override
    public String getText() {
        synchronized (getDelegateLock()) {
            return getDelegate().getText();
        }
    }

    @Override
    public void setText(final String l) {
        synchronized (getDelegateLock()) {
            getDelegate().setText(l);
        }
    }

    @Override
    public int getSelectionStart() {
        synchronized (getDelegateLock()) {
            return getDelegate().getSelectionStart();
        }
    }

    @Override
    public int getSelectionEnd() {
        synchronized (getDelegateLock()) {
            return getDelegate().getSelectionEnd();
        }
    }

    @Override
    public void select(final int selStart, final int selEnd) {
        synchronized (getDelegateLock()) {
            getDelegate().select(selStart, selEnd);
        }
    }

    @Override
    public void setCaretPosition(final int pos) {
        synchronized (getDelegateLock()) {
            getDelegate().setCaretPosition(pos);
        }
    }

    @Override
    public int getCaretPosition() {
        synchronized (getDelegateLock()) {
            return getDelegate().getCaretPosition();
        }
    }

    @Override
    public InputMethodRequests getInputMethodRequests() {
        return null;
    }

    @Override
    public boolean isFocusable() {
        return getTarget().isFocusable();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        postEvent(new ActionEvent(getTarget(), ActionEvent.ACTION_PERFORMED, "",
                                  e.getWhen(), e.getModifiers()));
    }

    private final class JTextAreaDelegate extends JPasswordField {

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
            return LWTextFieldPeer.this.getLocationOnScreen();
        }
    }
}
