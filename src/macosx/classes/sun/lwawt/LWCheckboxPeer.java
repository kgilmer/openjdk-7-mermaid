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

import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.AWTEvent;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.peer.CheckboxPeer;

import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

class LWCheckboxPeer
    extends LWComponentPeer<Checkbox, LWCheckboxPeer.JCheckBoxDelegate> 
    implements CheckboxPeer, ItemListener
{
    LWCheckboxPeer(Checkbox target) {
        super(target);
    }

    @Override
    protected JCheckBoxDelegate createDelegate() {
	// TODO: not implemented, need delegate impl for radio
	// buttons (that is, if target group is not null)
        JCheckBoxDelegate delegate = new JCheckBoxDelegate();
        delegate.setOpaque(false);
        delegate.setText(getTarget().getLabel());
        delegate.setSelected(getTarget().getState());
        delegate.addItemListener(this);
        return delegate;
    }

    public void itemStateChanged(ItemEvent e) {
        postEvent(new ItemEvent(getTarget(),  ItemEvent.ITEM_STATE_CHANGED,
                getTarget().getLabel(), e.getStateChange()));
    }

    public void setCheckboxGroup(CheckboxGroup g) {
        synchronized (getDelegateLock()) {
            // TODO: not implemented
        }
    }

    public void setLabel(String label) {
        synchronized (getDelegateLock()) {
            getDelegate().setText(label);
        }
        repaintPeer();
    }

    public void setState(boolean state) {
        synchronized (getDelegateLock()) {
            getDelegate().setSelected(state);
        }
        repaintPeer();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    class JCheckBoxDelegate
	extends JCheckBox
	implements ComponentDelegate
    {
	@Override
        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }
    }

    class JRadioButtonDelegate
	extends JRadioButton
	implements ComponentDelegate
    {
	@Override
        public void processAWTEvent(AWTEvent e) {
            processEvent(e);
        }
    }
}
       
