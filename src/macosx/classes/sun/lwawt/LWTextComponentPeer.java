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
import java.awt.SystemColor;
import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextComponentPeer;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

abstract class LWTextComponentPeer<T extends TextComponent, D extends JComponent>
        extends LWComponentPeer<T, D>
        implements DocumentListener, TextComponentPeer {

    /**
     * Character with reasonable value between the minimum width and maximum.
     */
    protected static final char WIDE_CHAR = 'w';
    private volatile boolean firstChangeSkipped;

    LWTextComponentPeer(final T target,
                        final PlatformComponent platformComponent) {
        super(target, platformComponent);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!getTarget().isBackgroundSet()) {
            getTarget().setBackground(SystemColor.text);
        }
        synchronized (getDelegateLock()) {
            // This listener should be added before setText().
            getTextComponent().getDocument().addDocumentListener(this);
        }
        setEditable(getTarget().isEditable());
        setText(getTarget().getText());
        final int start = getTarget().getSelectionStart();
        final int end = getTarget().getSelectionEnd();
        if (end > start) {
            select(start, end);
        }
        setCaretPosition(getTarget().getCaretPosition());
        firstChangeSkipped = true;
    }

    abstract JTextComponent getTextComponent();

    public Dimension getPreferredSize(final int rows, final int columns) {
        final Insets insets;
        synchronized (getDelegateLock()) {
            insets = getDelegate().getInsets();
        }
        final int borderHeight = insets.top + insets.bottom;
        final int borderWidth = insets.left + insets.right;
        final FontMetrics fm = getFontMetrics(getFont());
        final int charWidth = (fm != null) ? fm.charWidth(WIDE_CHAR) : 10;
        final int itemHeight = (fm != null) ? fm.getHeight() : 10;
        return new Dimension(columns * charWidth + borderWidth,
                             rows * itemHeight + borderHeight);
    }

    @Override
    public final void setEditable(final boolean editable) {
        synchronized (getDelegateLock()) {
            getTextComponent().setEditable(editable);
        }
    }

    @Override
    public final String getText() {
        synchronized (getDelegateLock()) {
            return getTextComponent().getText();
        }
    }

    @Override
    public void setText(final String l) {
        synchronized (getDelegateLock()) {
            // JTextArea.setText() posts two different events (remove & insert).
            // Since we make no differences between text events,
            // the document listener has to be disabled while
            // JTextArea.setText() is called.
            final Document document = getTextComponent().getDocument();
            document.removeDocumentListener(this);
            getTextComponent().setText(l);
            if (firstChangeSkipped) {
                postEvent(new TextEvent(getTarget(),
                                        TextEvent.TEXT_VALUE_CHANGED));
            }
            document.addDocumentListener(this);
        }
        repaintPeer();
    }

    @Override
    public final int getSelectionStart() {
        synchronized (getDelegateLock()) {
            return getTextComponent().getSelectionStart();
        }
    }

    @Override
    public final int getSelectionEnd() {
        synchronized (getDelegateLock()) {
            return getTextComponent().getSelectionEnd();
        }
    }

    @Override
    public final void select(final int selStart, final int selEnd) {
        synchronized (getDelegateLock()) {
            getTextComponent().select(selStart, selEnd);
        }
        repaintPeer();
    }

    @Override
    public final void setCaretPosition(final int pos) {
        synchronized (getDelegateLock()) {
            getTextComponent().setCaretPosition(pos);
        }
        repaintPeer();
    }

    @Override
    public final int getCaretPosition() {
        synchronized (getDelegateLock()) {
            return getTextComponent().getCaretPosition();
        }
    }

    @Override
    public final InputMethodRequests getInputMethodRequests() {
        return null;
    }

    @Override
    public final boolean isFocusable() {
        return getTarget().isFocusable();
    }

    private void sendTextEvent(final DocumentEvent e) {
        postEvent(new TextEvent(getTarget(), TextEvent.TEXT_VALUE_CHANGED));
        synchronized (getDelegateLock()) {
            getDelegate().invalidate();
            getDelegate().validate();
        }
    }

    @Override
    public final void changedUpdate(final DocumentEvent e) {
        sendTextEvent(e);
    }

    @Override
    public final void insertUpdate(final DocumentEvent e) {
        sendTextEvent(e);
    }

    @Override
    public final void removeUpdate(final DocumentEvent e) {
        sendTextEvent(e);
    }
}
