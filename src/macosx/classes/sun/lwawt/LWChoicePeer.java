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

import java.awt.Choice;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.peer.ChoicePeer;

import javax.swing.JComboBox;

class LWChoicePeer
    extends LWComponentPeer<Choice, LWChoicePeer.JComboBoxDelegate>
    implements ChoicePeer
{
    LWChoicePeer(Choice target) {
        super(target);
    }

    @Override
    protected JComboBoxDelegate createDelegate() {
        final Choice ch = (Choice)getTarget();

        final JComboBoxDelegate combo = new JComboBoxDelegate();

        for (int i = 0; i < ch.getItemCount(); i++) {
            combo.addItem(ch.getItem(i));
        }

        // NOTE: the listener must be added at the very end, otherwise it fires
        // events upon initialization of the combo box.
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // AWT Choice sends SELECTED event only whereas JComboBox
                // sends both SELECTED and DESELECTED.
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    Choice target = (Choice)LWChoicePeer.this.getTarget();
                    ItemEvent itemEvent = new ItemEvent(target,
                            ItemEvent.ITEM_STATE_CHANGED, combo.getSelectedItem(),
                            ItemEvent.SELECTED);
                    postEvent(itemEvent);
                }
            }
        });

        return combo;
    }

    @Override
    public void add(String item, int index) {
        synchronized (getDelegateLock()) {
            getDelegate().insertItemAt(item, index);
        }
    }

    @Override
    public void remove(int index) {
        synchronized (getDelegateLock()) {
            getDelegate().removeItemAt(index);
        }
    }

    @Override
    public void removeAll() {
        synchronized (getDelegateLock()) {
            getDelegate().removeAllItems();
        }
    }

    @Override
    public void select(int index) {
        synchronized (getDelegateLock()) {
            getDelegate().setSelectedIndex(index);
        }
    }

    public boolean isFocusable() {
        return true;
    }

    @Override
    protected void processDelegateEvent(AWTEvent e) {
        // TODO: We receive LOST-GAIN sequence on popup opening.
        // TODO: KeyEvents should have altered target. May relate to CR 6945376.
        super.processDelegateEvent(e);
     }

    class JComboBoxDelegate
	extends JComboBox
	implements ComponentDelegate
    {
	//TODO: JComboBox is deaf to mouse events over the button which opens the menu.
        @Override
        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }

        //Needed for proper popup menu location
        @Override
        public Point getLocationOnScreen() {
            return getTarget().getLocationOnScreen();
        }
    }
}
