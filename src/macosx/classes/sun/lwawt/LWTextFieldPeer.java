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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextFieldPeer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentListener;

class LWTextFieldPeer
        extends LWTextComponentPeer<TextField, JPasswordField>
        implements TextFieldPeer, ActionListener {
    private final static int DEFAULT_COLUMNS = 9;

    LWTextFieldPeer(TextField target) {
        super(target);
    }

    protected JPasswordField createDelegate() {
        JPasswordField delegate = new JPasswordField() {
            public Point getLocationOnScreen() {
                return getTarget().getLocationOnScreen();
            }
        };
        return delegate;
    }

    public void initialize() {
        super.initialize();
        synchronized (getDelegateLock()) {
            getDelegate().setText(getTarget().getText());
            getDelegate().addActionListener(this);
            getDelegate().setEchoChar(getTarget().getEchoChar());
        }
    }

    @Override
    public Document getDocument() {
        return getDelegate().getDocument();
    }


    @Override
    public void setEchoChar(char echoChar) {
        synchronized (getDelegateLock()) {
            getDelegate().setEchoChar(echoChar);
        }
    }

    @Override
    public Dimension getPreferredSize(int columns) {
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d;
        if (fm != null) {
            d = new Dimension(columns * fm.stringWidth("x"),
                    getItemHeight(fm) + (2 * MARGIN));
        } else {
            d = new Dimension(columns * 10, 10 + (2 * MARGIN));
        }
        return d;
    }

    @Override
    public Dimension getMinimumSize(int columns) {
        return getPreferredSize(DEFAULT_COLUMNS);
    }

    @Override
    public void setEditable(boolean editable) {
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
    public void setText(String l) {
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
    public void select(int selStart, int selEnd) {
        synchronized (getDelegateLock()) {
            getDelegate().select(selStart, selEnd);
        }
    }

    @Override
    public void setCaretPosition(int pos) {
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
        synchronized (getDelegateLock()) {
            return getTarget().isFocusable();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        postEvent(new ActionEvent(getTarget(), ActionEvent.ACTION_PERFORMED,
                "", e.getWhen(), e.getModifiers()));
    }
}
