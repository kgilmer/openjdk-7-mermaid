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
import java.awt.TextComponent;
import javax.swing.JComponent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.TextEvent;

abstract class LWTextComponentPeer<T extends TextComponent, D extends JComponent>
    extends LWComponentPeer<T, D>
{
    protected final static int MARGIN = 2;
    protected final static int SPACE = 1;
    
    LWTextComponentPeer(T target) {
        super(target);
    }
    
    /* Returns height of the line in textarea or textfield */
    protected int getItemHeight(FontMetrics metrics) {
        return (metrics.getHeight() - metrics.getLeading()) + (2 * SPACE);
    }

    class SwingTextComponentDocumentListener implements DocumentListener {
        void sendTextEvent(DocumentEvent de) {
             LWTextComponentPeer.this.postEvent(
                 new TextEvent(LWTextComponentPeer.this.getTarget(), 
                               TextEvent.TEXT_VALUE_CHANGED));
        }
        public void changedUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }
        public void insertUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }
        public void removeUpdate(DocumentEvent de) {
            sendTextEvent(de);
        }
    }
}
