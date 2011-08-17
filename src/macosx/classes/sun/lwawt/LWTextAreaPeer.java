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
import java.awt.event.*;
import java.awt.im.InputMethodRequests;
import java.awt.peer.TextAreaPeer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.xml.bind.SchemaOutputResolver;

class LWTextAreaPeer
        extends LWTextComponentPeer<TextArea, LWTextAreaPeer.JTextAreaDelegate>
        implements TextAreaPeer, SelectionClearListener, ActionListener
{
    private final static int DEFAULT_COLUMNS = 9;
    private final static int DEFAULT_ROWS=3;
    private final static int BORDERMARGIN = 5;

    private static java.util.List<SelectionClearListener> clearListeners =
            new ArrayList <SelectionClearListener>();

    public LWTextAreaPeer(TextArea target) {
        super(target);
        installSelectionClearListener(this);
        getDelegate().getDocument().addDocumentListener(
                new SwingTextComponentDocumentListener());
        getDelegate().getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Window w = getTopLevelContainer(getTarget());
                if(w != null) {
                    synchronized (getDelegateLock()) {
                        w.repaint();
                    }
                }
            }
        });
    }

    private static void installSelectionClearListener(SelectionClearListener listener) {
        clearListeners.add(listener);
    }


    @Override
    protected JTextAreaDelegate createDelegate() {
        JTextAreaDelegate delegate = new JTextAreaDelegate(new InternalTextArea());
        delegate.setText(getTarget().getText());
        delegate.setBorder(BorderFactory.createLoweredBevelBorder());
        return delegate;
    }

    @Override
    public Dimension getPreferredSize(int rows, int columns) {
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d;
        if( fm != null ) {
            d = new Dimension(columns * fm.stringWidth("W"),
                    (getItemHeight(fm) * rows + ((1+rows) * MARGIN)) + 2 * BORDERMARGIN);
        } else {
            d = new Dimension(columns * 10, 12 * rows + ((rows+1) * MARGIN) + 2 * BORDERMARGIN);
        }
        return d;
    }

    @Override
    public Dimension getMinimumSize(int rows, int columns) {
        return getPreferredSize(DEFAULT_ROWS, DEFAULT_COLUMNS);
    }

    @Override
    public void setText(String label) {
        synchronized (getDelegateLock()) {
            getDelegate().setText(label);
        }
        repaintPeer();
    }

    @Override
    public String getText() {
        synchronized (getDelegateLock()) {
                return getDelegate().getText();
        }
    }

    @Override
    public boolean isFocusable() {
        return getTarget().isFocusable();
    }

    @Override
    public void insert(String text, int pos) {
        synchronized (getDelegateLock()) {
            getDelegate().insert(text, pos);

        }
        repaintPeer();
    }

    @Override
    public void replaceRange(String text, int start, int end) {
        synchronized (getDelegateLock()) {
            getDelegate().replaceRange(text, start, end);
        }
        repaintPeer();
    }


    @Override
    public void setEditable(boolean editable) {
        synchronized (getDelegateLock()) {
            getDelegate().setEditable(editable);
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
    public void clearSelection() {
        synchronized (getDelegateLock()) {
            getDelegate().select(0, 0);
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
    public void actionPerformed(ActionEvent e) {
        postEvent(new ActionEvent(getTarget(),  ActionEvent.ACTION_PERFORMED,
                "", e.getWhen(), e.getModifiers()));
    }

    @SuppressWarnings("serial")
    class JTextAreaDelegate extends JScrollPane implements ComponentDelegate
    {
        private InternalTextArea contents;

        public JTextAreaDelegate(Component view) {
            super(view);
            if(view instanceof InternalTextArea) contents = (InternalTextArea) view;
            setHorizontalScrollBar(new InternalScrollBar(JScrollBar.HORIZONTAL));
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            setVerticalScrollBar(new InternalScrollBar(JScrollBar.VERTICAL));
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        }

        public JTextAreaDelegate() {
            super();
        }

        /*
         * Passing all the delegate calls into the enclosed JTextArea
         */
        public Document getDocument() {
            return contents.getDocument();
        }

        public void setText(String t) {
            contents.setText(t);
        }

        public String getText() {
            return contents.getText();
        }

        public void insert(String str, int pos) {
            contents.insert(str, pos);
        }

        public void replaceRange(String str, int start, int end) {
            contents.replaceRange(str, start, end);
        }

        public void setEditable(boolean b) {
            contents.setEditable(b);
        }

        public int getSelectionStart() {
            return contents.getSelectionStart();
        }

        public int getSelectionEnd() {
            return contents.getSelectionEnd();
        }

        public void select(int selectionStart, int selectionEnd) {
            contents.select(selectionStart, selectionEnd);
        }

        public void setCaretPosition(int position) {
            contents.setCaretPosition(position);
        }

        public int getCaretPosition() {
            return contents.getCaretPosition();
        }

        /*
         * Dispatch and process an AWT event - depending on the area it happened we must
         * forward it to the corresponding part of JScrollPane
         */
        @Override
        public void processAWTEvent(AWTEvent e) {
            if (e instanceof ContainerEvent) {
                processContainerEvent((ContainerEvent)e);
                return;
            }
            if(e instanceof FocusEvent) {
                e.setSource(contents);
                contents.processAWTEvent(e);
            } else if(e instanceof MouseEvent) {
                switch(e.getID()) {
                  case MouseEvent.MOUSE_PRESSED:
                  case MouseEvent.MOUSE_RELEASED:
                  case MouseEvent.MOUSE_CLICKED:
                  case MouseEvent.MOUSE_MOVED:
                  case MouseEvent.MOUSE_DRAGGED:
                      if(getViewportBorderBounds().contains(((MouseEvent) e).getPoint())) {
                          MouseEvent newEvent = translateEventToComponent((MouseEvent)e, (int)getViewport().getViewRect().getX(),
                                  (int)getViewport().getViewRect().getY());
                          newEvent.setSource(contents);
                          contents.processAWTEvent(newEvent);
                      } else {
                          if(getVerticalScrollBar().getBounds().contains(((MouseEvent) e).getPoint())) {
                              MouseEvent newEvent = translateEventToComponent((MouseEvent)e,
                                      -getVerticalScrollBar().getBounds().x,
                                      -getVerticalScrollBar().getBounds().y);
                              newEvent.setSource(getVerticalScrollBar());
                              ((InternalScrollBar)getVerticalScrollBar()).processAWTEvent(newEvent);
                          } else if(getHorizontalScrollBar().getBounds().contains(((MouseEvent) e).getPoint())) {
                              MouseEvent newEvent = translateEventToComponent((MouseEvent)e,
                                      -getHorizontalScrollBar().getBounds().x,
                                      -getHorizontalScrollBar().getBounds().y);
                              newEvent.setSource(getHorizontalScrollBar());
                              ((InternalScrollBar)getHorizontalScrollBar()).processAWTEvent(newEvent);
                          } else {
                              super.processEvent(e);
                          }
                      }
                      break;
                  case MouseEvent.MOUSE_ENTERED:
                  case MouseEvent.MOUSE_EXITED:
                  case MouseEvent.MOUSE_WHEEL:
                      e.setSource(contents);
                      contents.processAWTEvent(e);
                      break;
                }
            } else if (e instanceof KeyEvent) {
                e.setSource(contents);
                contents.processAWTEvent(e);
            } else {
                super.processEvent(e);
            }
        }
    }

    /*
     * Create the new event shifted by the provided offsets inside the target component
     */
    private MouseEvent translateEventToComponent(MouseEvent e, int offsetX, int offsetY) {
        MouseEvent event = new MouseEvent(getTarget(), e.getID(),
                                  ((MouseEvent) e).getWhen(),
                                  ((MouseEvent) e).getModifiers(),
                                  ((MouseEvent) e).getX()+offsetX,
                                  ((MouseEvent) e).getY()+offsetY,
                                  ((MouseEvent) e).getClickCount(),
                                  ((MouseEvent) e).isPopupTrigger());
        return event;
    }

    /*
     * Get the top-level container if any. If there is no Window - return null
     */
    private Window getTopLevelContainer(Component c) {
        Component top = c;
        while (top != null && !(top instanceof Window)) {
            top = top.getParent();
        }
        return (Window) top;
    }

    /*
     * Enclosed text area - need this inner class to correctly deliver AWT event into it
     */
    @SuppressWarnings("serial")
    class InternalTextArea extends JTextArea {
        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }
    }

    /*
     * Enclosed scrollbar - need this inner class to correctly deliver AWT event into it
     */
    @SuppressWarnings("serial")
    class InternalScrollBar extends JScrollBar {
        public InternalScrollBar(int orientation) {
            super(orientation);
        }

        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }
    }
}
