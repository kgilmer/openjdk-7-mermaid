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

import java.awt.AWTEvent;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.List;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.peer.ListPeer;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class LWListPeer
    extends LWComponentPeer<List, LWListPeer.ScrollableJListDelegate>
    implements ListPeer
{

    public LWListPeer(List target) {
        super(target);
    }

    @Override
    public ScrollableJListDelegate createDelegate() {
        return new ScrollableJListDelegate();
    }

    @Override
    public void add(String item, int index) {
        synchronized (getDelegateLock()) {
            getDelegate().add(item, index);
        }
    }

    @Override
    public void delItems(int startIndex, int endIndex) {
        synchronized (getDelegateLock()) {
            getDelegate().delItems(startIndex, endIndex);
        }
    }

    @Override
    public void deselect(int index) {
        synchronized (getDelegateLock()) {
            getDelegate().deselect(index);
        }
    }

    /*
     * Returns minimum size based on JList content and a JScrollPane decorations
     * if any. We ask LWList class about size.
     */
    @Override
    public Dimension getMinimumSize(int s) {
        synchronized (getDelegateLock()) {
            return getDelegate().getMinimumSize(s);
        }
    }

    @Override
    public Dimension getPreferredSize(int s) {
        synchronized (getDelegateLock()) {
            return getDelegate().getPreferredSize(s);
        }
    }

    @Override
    public int[] getSelectedIndexes() {
        synchronized (getDelegateLock()) {
            return getDelegate().getSelectedIndexes();
        }
    }

    @Override
    public void makeVisible(int index) {
        synchronized (getDelegateLock()) {
            getDelegate().makeVisible(index);
        }
    }

    @Override
    public void removeAll() {
        synchronized (getDelegateLock()) {
            getDelegate().removeAll();
        }
    }

    @Override
    public void select(int index) {
        synchronized (getDelegateLock()) {
            getDelegate().select(index);
        }
    }

    @Override
    public void setMultipleMode(boolean multi) {
        synchronized (getDelegateLock()) {
            getDelegate().setMultipleMode(multi);
        }
    }

    /*
     * A container class for JList.
     */
    @SuppressWarnings("serial")
    public class ScrollableJListDelegate
	extends JScrollPane
	implements ComponentDelegate
    {
        public final static int MARGIN = 2;
        public final static int SPACE = 1;

        private DefaultListModel listModel;

        private JListDelegate internalDelegate;

        public ScrollableJListDelegate() {
            internalDelegate = new JListDelegate();

            internalDelegate
                    .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // TODO: if FG/BG needed?
            internalDelegate.setBackground(getTarget().getBackground());
            internalDelegate.setForeground(getTarget().getForeground());

            // Swing machinery depends on MOUSE_MOVED events.
            // Because our swing components are not fair enough, we need to
            // suppress any activity wrt to tooltips as it causes exception.
            ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
            toolTipManager.unregisterComponent(internalDelegate);

            internalDelegate
                    .addListSelectionListener(new ListSelectionListener() {

                        /*
                         * This is only required for the conflict with the
                         * ListSelectionEvents: 1) when adjusting==true event
                         * may come from mouse and we need to convert it to
                         * ItemEvent. 2) when adjusting==false event may come
                         * from keyboard and we need to convert it to ItemEvent
                         * too. 3) when adjusting==false event may come from
                         * mouse and we don't need to convert it to ItemEvent.
                         * For now I just keep the type of the previous event
                         * and if only we have NotAsjusting-NotAdjusting events
                         * sequentially, we generate ItemEvent. Better solution
                         * may to track if the mouse is in the dragged state or
                         * not. Variable should be false initially for the case
                         * when the very first event is of notAdjusting type so
                         * we may pass the if-statement.
                         */
                        private boolean isPrevEventIsAdjusting = false;

                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            // Two ItemEvents occur: the first one have the
                            // flag (isAdjusting == true) which basically
                            // means that the mouse button is in the pressed
                            // state so
                            // the dragging occur. Second event has that flag
                            // (isAdjusting == false) and that
                            // means the dragging is finished.
                            // We should notify listeners every time we get the
                            // first kind of event because AWT List naturally
                            // send
                            // ItemEvent on every drag.
                            if (e.getValueIsAdjusting() ||
                                   (!e.getValueIsAdjusting() && !isPrevEventIsAdjusting)) {
                                ItemEvent itemEvent = new ItemEvent(
                                        getTarget(),
                                        ItemEvent.ITEM_STATE_CHANGED,
                                        internalDelegate.getSelectedIndex(),
                                        ItemEvent.SELECTED);
                                LWListPeer.this.postEvent(itemEvent);

                                isPrevEventIsAdjusting = e.getValueIsAdjusting();
                            }
                        }
                    });

            listModel = new DefaultListModel();
            internalDelegate.setModel(listModel);

            getViewport().setView(internalDelegate);
            internalDelegate.setPreferredSize(LWListPeer.this.getMinimumSize());
            getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            // Pull the items from the target.
            String[] items = getTarget().getItems();
            for (int i = 0; i < items.length; i++) {
                add(items[i], i);
            }
        }

        @Override
        public void processAWTEvent(AWTEvent e) {
            if (e instanceof KeyEvent || e instanceof FocusEvent) {
                //Event should be addressed to the actual JList, not its wrapper
            	e.setSource(getView());
            	getView().processAWTEvent(e);
            } else {
                processEvent(e);
            }
        }

        public JListDelegate getView() {
            return (JListDelegate) getViewport().getView();
        }

        // Duplication of ListPeer interface
        public void add(String item, int index) {
            if (index == -1) {
                listModel.addElement(item);
            } else {
                listModel.add(index, item);
            }
        }

        public void delItems(int startIndex, int endIndex) {
            listModel.removeRange(startIndex, endIndex);
        }

        public void deselect(int index) {
            internalDelegate.getSelectionModel().removeSelectionInterval(index,
                    index);
        }

        public Dimension getMinimumSize(int s) {
            // TODO: count ScrollPane's scrolling elements if any.
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(20 + (fm == null ? 10*15 : fm.stringWidth("0123456789abcde")),
                    (fm == null ? 10 : getItemHeight(fm)) * s + (2 * MARGIN));
        }

        public Dimension getPreferredSize(int s) {
            return getMinimumSize(s);
        }

        public int[] getSelectedIndexes() {
            return internalDelegate.getSelectedIndices();
        }

        public void makeVisible(int index) {
            internalDelegate.ensureIndexIsVisible(index);
        }

        public void removeAll() {
            internalDelegate.removeAll();
        }

        public void select(int index) {
            internalDelegate.setSelectedIndex(index);
        }

        public void setMultipleMode(boolean multi) {
            internalDelegate.setSelectionMode(
                      multi ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                            : ListSelectionModel.SINGLE_SELECTION);
        }

        /* Returns height of an item in the list */
        private int getItemHeight(FontMetrics metrics) {
            return (metrics.getHeight() - metrics.getLeading()) + (2 * SPACE);
        }

        /*
         * JList itself
         */
        class JListDelegate
	    extends JList
	    implements ComponentDelegate
	{
	    @Override
	    public void processAWTEvent(AWTEvent e) {
		processEvent(e);
	    }
	    @Override
            public void revalidate() {
                ScrollableJListDelegate.this.validate();
		super.revalidate();
            }
        }
    }
}
