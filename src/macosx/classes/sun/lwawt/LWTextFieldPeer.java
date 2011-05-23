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

import javax.swing.JTextField;

import java.awt.FontMetrics;
import java.awt.TextField;
import java.awt.Dimension;
import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextFieldPeer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.DocumentListener;

class LWTextFieldPeer
    extends LWTextComponentPeer<TextField, LWTextFieldPeer.JTextFieldDelegate>
    implements TextFieldPeer, SelectionClearListener, ActionListener
{
    private static List <SelectionClearListener> clearListeners =
	new ArrayList <SelectionClearListener>();
    
    private final static int DEFAULT_COLUMNS = 9;
	
    LWTextFieldPeer(TextField target) {
        super(target);
        installSelectionClearListener(this);
        getDelegate().getDocument().addDocumentListener(
                new SwingTextComponentDocumentListener());
    }

    private static void installSelectionClearListener(SelectionClearListener listener) {
	clearListeners.add(listener);
    }

    @Override //SelectionClearListener
    public void clearSelection(){
        synchronized (getDelegateLock()) {
            getDelegate().select(0, 0);
        }
    }
    
    protected JTextFieldDelegate createDelegate() {
        JTextFieldDelegate delegate = new JTextFieldDelegate();
        delegate.setText(getTarget().getText());
        delegate.addActionListener(this);
        return delegate;
    }

    @Override
    public void setEchoChar(char echoChar) {
        // TODO: use JPasswordField?
    }

    @Override
    public Dimension getPreferredSize(int columns) {
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d;
        if( fm != null ) {
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
            for (SelectionClearListener l : clearListeners){
                l.clearSelection();
            }
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
        return getTarget().isFocusable();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        postEvent(new ActionEvent(getTarget(),  ActionEvent.ACTION_PERFORMED,
                "", e.getWhen(), e.getModifiers()));
    }

    @SuppressWarnings("serial")
    class JTextFieldDelegate extends JTextField implements ComponentDelegate {

        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }
    }
}
