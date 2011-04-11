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

package com.apple.laf;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboPopup;

class AquaComboBoxPopup extends BasicComboPopup {
    protected Component topStrut;
    protected Component bottomStrut;
    protected boolean fIsScrolling = false;
    
    public AquaComboBoxPopup(final JComboBox cBox) {
        super(cBox);
    }
    
    protected void configurePopup() {
        super.configurePopup();
        
        setBorderPainted(false);
        setBorder(null);
        updateContents(false);
    }
    
    public void updateContents(final boolean remove) {
        // for more background on this issue, see
        // AquaMenuBorder.getBorderInsets()
    
        fIsScrolling = shouldScroll();
        if (fIsScrolling) {
            if (remove) {
                if (topStrut != null) {
                    this.remove(topStrut);
                }
                if (bottomStrut != null) {
                    this.remove(bottomStrut);
                }
            } else {
                add(scroller);
            }
        } else {
            if (topStrut == null) {
                topStrut = Box.createVerticalStrut(4);
                bottomStrut = Box.createVerticalStrut(4);
            }
            
            if (remove) {
                remove(scroller);
            }
            
            this.add(topStrut);
            this.add(scroller);
            this.add(bottomStrut);
        }
    }
    
    protected Dimension getBestPopupSizeForRowCount(final int maxRowCount) {
        final int currentElementCount = comboBox.getModel().getSize();
        final int rowCount = Math.min(maxRowCount, currentElementCount);
        int height = 0;
        int width = 0;
        final ListCellRenderer renderer = list.getCellRenderer();
        Object value = null;
        
        for (int i = 0; i < rowCount; i++) {
            value = list.getModel().getElementAt(i);
            final Component c =
                renderer.getListCellRendererComponent(list, value, i,
                                                      false, false);
            final Dimension prefSize = c.getPreferredSize();
            height += prefSize.height;
            width = Math.max(prefSize.width, width);
        }
        
        width += 10;
        
        return new Dimension(width, height);
    }
    
    public boolean shouldScroll() {
        return
            comboBox.getItemCount() > comboBox.getMaximumRowCount() ||
            comboBox.isEditable();
    }
    
    public void show() {
        final int startItemCount = comboBox.getItemCount();
    
        final Rectangle popupBounds = adjustPopupAndGetBounds();
        show(comboBox, popupBounds.x, popupBounds.y);
    
        // hack for <rdar://4905531>
        //    JComboBox does not fire popupWillBecomeVisible if item count is 0
        final int afterShowItemCount = comboBox.getItemCount();
        if (afterShowItemCount == 0) {
            hide();
            return;
        }
    
        if (startItemCount != afterShowItemCount) {
            final Rectangle newBounds = adjustPopupAndGetBounds();
            list.setSize(newBounds.width, newBounds.height);
            pack();
            final Point newLoc = comboBox.getLocationOnScreen();
            setLocation(newLoc.x + newBounds.x, newLoc.y + newBounds.y);
        }
        // end hack
    
        list.requestFocus();
    }
    
    protected Rectangle adjustPopupAndGetBounds() {
        if (fIsScrolling != shouldScroll()) {
            updateContents(true);
        }
        
        final Dimension popupSize = getBestPopupSizeForRowCount(comboBox.getMaximumRowCount());
        final Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width, popupSize.height);
    
        // Returns null if the combobox is totally off screen -
        // showing the popup would be confusing
        if (popupBounds == null) {
            return null;
        }
        
        scroller.setMaximumSize(popupBounds.getSize());
        scroller.setPreferredSize(popupBounds.getSize());
        scroller.setMinimumSize(popupBounds.getSize());
        list.invalidate();
        final int selectedIndex = comboBox.getSelectedIndex();
        
        if (selectedIndex == -1) {
            list.clearSelection();
        } else {
            list.setSelectedIndex(selectedIndex);
        }
        list.ensureIndexIsVisible(list.getSelectedIndex());
    
        return popupBounds;
    }
    
    // Get the bounds of the screen where the menu should appear
    // p is the origin of the combo box in screen bounds
    Rectangle getBestScreenBounds(final Point p) {
        //System.err.println("GetBestScreenBounds p: "+ p.x + ", " + p.y);
        final GraphicsEnvironment ge =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] gs = ge.getScreenDevices();
        //System.err.println("  gs.length = " + gs.length);
        if (gs.length == 1) {
            final Dimension scrSize =
                Toolkit.getDefaultToolkit().getScreenSize();
            
            //System.err.println("  scrSize: "+ scrSize);
            
            // If the combo box is totally off screen, don't show a popup
            if ((p.x + comboBox.getBounds().width < 0) ||
                (p.y + comboBox.getBounds().height < 0) ||
                (p.x > scrSize.width) || (p.y > scrSize.height))
            {
                return null;
            }
            return new Rectangle(0, 22, scrSize.width, scrSize.height - 22);
        }
        
        for (final GraphicsDevice gd : gs) {
            final GraphicsConfiguration[] gc = gd.getConfigurations();
            for (final GraphicsConfiguration element0 : gc) {
                final Rectangle gcBounds = element0.getBounds();
                if (gcBounds.contains(p)) {
                    return gcBounds;
                }
            }
        }
        
        // Hmm.  Origin's off screen, but is any part on?
        final Rectangle cbBounds =
            new Rectangle(p.x, p.y,
                          comboBox.getBounds().width,
                          comboBox.getBounds().height);
        for (final GraphicsDevice gd : gs) {
            final GraphicsConfiguration[] gc = gd.getConfigurations();
            for (final GraphicsConfiguration element0 : gc) {
                final Rectangle gcBounds = element0.getBounds();
                if (gcBounds.intersects(cbBounds)) {
                    return gcBounds;
                }
            }
        }
        return null;
    }
    
    // no gap, aligns on the left with scrollbar aligned with the button
    protected Rectangle computePopupBounds(int px, int py,
                                           int pw, final int ph)
    {
        final int itemCount = comboBox.getModel().getSize();
        if (comboBox.isEditable() || itemCount > comboBox.getMaximumRowCount()) {
            final int newY = (py / 2) + 9;
            // place it just below our graphic since we 
            // have insets and draw in the center of a large
            // combo box.
            
            // if py is less than new y we have a clipped combo
            py = Math.min(newY, py);
            // so leave it alone.
        }
        
        // px & py are relative to the combo box
        
        // ** Common calculation - applies to the scrolling and menu-style **
        final Point p = new Point(0, 0);
        SwingUtilities.convertPointToScreen(p, comboBox);
        //System.err.println("First Converting from point to screen: 0,0 is now " + p.x + ", " + p.y);
        final Rectangle scrBounds = getBestScreenBounds(p);
        //System.err.println("BestScreenBounds is " + scrBounds);
        
        // If the combo box is totally off screen, don't show a popup
        if (scrBounds == null) {
            return null;
        }
        
        // If editable, the minimum has the scrollbar lined up with
        // right edge of text.
        // If non-editable, the button minimum is kButtonWidth pix wide
        // (plus shadow) the popup should be wide enough for the items but
        // not wider than the screen it's on
        int minWidth;
        //if (comboBox.isEditable())
        //    minWidth = _editor().getBounds().width - kInset; // adjust for inset
        //else
        minWidth = comboBox.getBounds().width - 24;
        
        pw = Math.max(minWidth, pw);
        
        // line up with the bottom of the text field/button (or top,
        // if we have to go above it) and left edge if left-to-right,
        // right edge if right-to-left
        Insets insets;
        if (comboBox.isEditable()) {
            insets = comboBox.getInsets();
        } else {
            // Adjust for the "border"'s shadow space - bufferInsets
            insets = comboBox.getInsets();
        }
    
        boolean leftToRight = AquaUtils.isLeftToRight(comboBox);
        if (leftToRight) {
            px += insets.left;
        } else {
            px = comboBox.getBounds().width - pw - insets.right;
        }
        py -= (insets.bottom + 0); //sja fix was +kInset
        
        // Make sure it's all on the screen - shift it by the amount it's off
        p.x += px;
        p.y += py; // Screen location of px & py
        if (p.x < scrBounds.x) px -= (p.x + scrBounds.x);
        if (p.y < scrBounds.y) py -= (p.y + scrBounds.y);
        
        final Point top = new Point(0, 0);
        SwingUtilities.convertPointFromScreen(top, comboBox);
        //System.err.println("Converting from point to screen: 0,0 is now " + top.x + ", " + top.y);
        
        // Since the popup is at zero in this coord space, the
        // maxWidth == the X coord of the screen right edge
        // (it might be wider than the screen, if the combo is
        // off the left edge)
        // (subtract some buffer space here)
        final int maxWidth = Math.min(scrBounds.width,
                                      top.x + scrBounds.x + scrBounds.width) - 2;
        pw = Math.min(maxWidth, pw);
        if (pw < minWidth) {
            px -= (minWidth - pw);
            pw = minWidth;
        }
        
        // **** Common calculation ends ****
        if (!comboBox.isEditable() &&
            itemCount <= comboBox.getMaximumRowCount())
        {
            return computePopupBoundsForMenu(px, py, pw, ph, itemCount, scrBounds);
        }
        
        // Scrollbar space, make sure it's on screen too
        pw += 16;
        if (!leftToRight || px + pw > top.x + scrBounds.width) {
            px -= 16;
        }
        
        final Rectangle r = new Rectangle(px, py, pw, ph);
        // Check whether it goes below the bottom of the screen, if so flip it
        if (r.y + r.height < top.y + scrBounds.y + scrBounds.height) return r;
        
        return new Rectangle(px, -r.height + insets.top, r.width, r.height);
    }
    
    // The one to use when itemCount <= maxRowCount.  Size never
    // adjusts for arrows.  We want it positioned so the selected item is
    // right above the combo box.
    protected Rectangle computePopupBoundsForMenu(final int px, final int py,
                                                  final int pw, final int ph,
                                                  final int itemCount,
                                                  final Rectangle scrBounds)
    {
        //System.err.println("computePopupBoundsForMenu: " + px + "," + py + " " +  pw + "," + ph);
        //System.err.println("itemCount: " +itemCount +" src: "+ scrBounds);
        int elementSize = 0; //kDefaultItemSize;
        if (list != null && itemCount > 0) {
            final Rectangle cellBounds = list.getCellBounds(0, 0);
            if (cellBounds != null) elementSize = cellBounds.height;
        }
        
        int offsetIndex = comboBox.getSelectedIndex();
        if (offsetIndex < 0) {
            offsetIndex = 0;
        }
        list.setSelectedIndex(offsetIndex);
        
        int selectedLocation = elementSize * offsetIndex;
        
        final Point top = new Point(0, scrBounds.y);
        final Point bottom = new Point(0, scrBounds.y + scrBounds.height - 20); // Allow some slack
        SwingUtilities.convertPointFromScreen(top, comboBox);
        SwingUtilities.convertPointFromScreen(bottom, comboBox);
        
        final Rectangle popupBounds = new Rectangle(px, py, pw, ph);// Relative to comboBox
        
        final int theRest = ph - selectedLocation;
        
        // If the popup fits on the screen and the selection appears
        // under the mouse w/o scrolling, cool!
        // If the popup won't fit on the screen, adjust its position but
        // not its size and rewrite this to support arrows -
        // JLists always move the contents so they all show 
        
        // Test to see if it extends off the screen
        final boolean extendsOffscreenAtTop = selectedLocation > -top.y;
        final boolean extendsOffscreenAtBottom = theRest > bottom.y;
        
        if (extendsOffscreenAtTop) {
            popupBounds.y = top.y + 1;
            // Round it so the selection lines up with the combobox 
            popupBounds.y = (popupBounds.y / elementSize) * elementSize;
        } else if (extendsOffscreenAtBottom) {
            // Provide blank space at top for off-screen stuff to scroll into
            // (popupBounds.height has already been adjusted to fit)
            popupBounds.y = bottom.y - popupBounds.height;
        } else {
            // fits - position it so the selectedLocation is under the mouse
            popupBounds.y = -selectedLocation;
        }
        
        // Center the selected item on the combobox
        final int height = comboBox.getHeight();
        final Insets insets = comboBox.getInsets();
        final int buttonSize = height - (insets.top + insets.bottom);
        final int diff = (buttonSize - elementSize) / 2 + insets.top;
        popupBounds.y += diff;
        
        return popupBounds;
    }
}
