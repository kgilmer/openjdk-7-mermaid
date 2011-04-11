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
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import apple.laf.JRSUIConstants.*;
import com.apple.laf.AquaUtilControlSize.*;
import com.apple.laf.AquaUtils.LazySingleton;

public abstract class AquaButtonLabeledUI
    extends AquaButtonToggleUI
    implements Sizeable
{
    protected static LazySizingIcon regularIcon = new LazySizingIcon(18);
    protected static LazySizingIcon smallIcon = new LazySizingIcon(16);
    protected static LazySizingIcon miniIcon = new LazySizingIcon(14);
    
    protected static class LazySizingIcon extends LazySingleton<Icon> {
        final int iconSize;
        public LazySizingIcon(final int iconSize) {
            this.iconSize = iconSize;
        }
    
        protected Icon getInstance() {
            return new ImageIcon(new BufferedImage(iconSize, iconSize,
                                             BufferedImage.TYPE_INT_ARGB_PRE));
        }
    }
    
    protected AquaButtonBorder widgetBorder;
    
    public AquaButtonLabeledUI() {
        widgetBorder = getPainter();
    }
    
    public void applySizeFor(final JComponent c, final Size newSize) {
        super.applySizeFor(c, newSize);
        widgetBorder = (AquaButtonBorder)
            widgetBorder.deriveBorderForSize(newSize);
    }
    
    public Icon getDefaultIcon(final JComponent c) {
        final Size componentSize = AquaUtilControlSize.getUserSizeFrom(c);
        if (componentSize == Size.REGULAR) return regularIcon.get();
        if (componentSize == Size.SMALL)   return smallIcon.get();
        if (componentSize == Size.MINI)    return miniIcon.get();
        return regularIcon.get();
    }
    
    protected void setThemeBorder(final AbstractButton b) {
        super.setThemeBorder(b);
    
        // Set the correct border
        b.setBorder(AquaButtonBorder.getBevelButtonBorder());
    }
    
    protected abstract AquaButtonBorder getPainter();
    
    /*
     * These Dimensions/Rectangles are allocated once for all
     * RadioButtonUI.paint() calls. Re-using rectangles rather
     * than allocating them in each paint call substantially
     * reduced the time it took paint to run. Obviously, this
     * method can't be re-entered.
     */
    private static Dimension size = new Dimension();
    
    public synchronized void paint(final Graphics g, final JComponent c) {
        final Graphics2D fontg2d = (Graphics2D)g;
        final Object savedAntiAliasingHint = AquaUtils.beginFont(fontg2d);
        
        final AbstractButton b = (AbstractButton)c;
        final ButtonModel model = b.getModel();
        
        final Font f = c.getFont();
        g.setFont(f);
        final FontMetrics fm = g.getFontMetrics();
        
        size = b.getSize(size);
        
        final Insets i = c.getInsets();
        
        viewRect.x = 0;
        viewRect.y = 0;
        viewRect.width = b.getWidth();
        viewRect.height = b.getHeight();
        
        iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;
        textRect.x = textRect.y = textRect.width = textRect.height = 0;
        
        Icon altIcon = b.getIcon();
    
        final boolean isCellEditor = c.getParent() instanceof CellRendererPane;
    
        // This was erroneously removed to fix 3155996 (JTableTest0011
        // does not show selection for checkbox column), but really we
        // wanted the controls to just be opaque. So we put this back
        // in to fix 3179839 (radio buttons not being translucent)
        if (b.isOpaque() || isCellEditor) {
            g.setColor(b.getBackground());
            g.fillRect(0, 0, size.width, size.height);
        }
    
        // only do this if borders are on!
        if (((AbstractButton)c).isBorderPainted() && !isCellEditor) {
            final Border border = c.getBorder();
            if (border instanceof AquaButtonBorder) {
                ((AquaButtonBorder)border).paintButton(c, g,
                                                       viewRect.x,
                                                       viewRect.y,
                                                       viewRect.width,
                                                       viewRect.height);
            }
        }
        
        viewRect.x = i.left;
        viewRect.y = i.top;
        viewRect.width = b.getWidth() - (i.right + viewRect.x);
        viewRect.height = b.getHeight() - (i.bottom + viewRect.y);
        
        // normal size ??
        // at some point we substitute the small icon instead of the
        // normal icon we should base this on height. Use normal unless
        // we are under a certain size; see our button code!

        final String text =
            SwingUtilities.layoutCompoundLabel(c, fm, b.getText(),
                                               altIcon != null ?
                                               altIcon : getDefaultIcon(b),
                                               b.getVerticalAlignment(),
                                               b.getHorizontalAlignment(),
                                               b.getVerticalTextPosition(),
                                               b.getHorizontalTextPosition(), 
                                               viewRect, iconRect, textRect,
                                               b.getText() == null ?
                                               0 : b.getIconTextGap());
        
        // fill background
        
        // draw the native radio button stuff here.
        if (altIcon == null) {
            widgetBorder.paintButton(c, g, iconRect.x, iconRect.y,
                                     iconRect.width, iconRect.height);
        } else {
            // Paint the button
            if (!model.isEnabled()) {
                if (model.isSelected()) {
                    altIcon = b.getDisabledSelectedIcon();
                } else {
                    altIcon = b.getDisabledIcon();
                }
            } else if (model.isPressed() && model.isArmed()) {
                altIcon = b.getPressedIcon();
                if (altIcon == null) {
                    // Use selected icon
                    altIcon = b.getSelectedIcon();
                }
            } else if (model.isSelected()) {
                if (b.isRolloverEnabled() && model.isRollover()) {
                    altIcon = b.getRolloverSelectedIcon();
                    if (altIcon == null) {
                        altIcon = b.getSelectedIcon();
                    }
                } else {
                    altIcon = b.getSelectedIcon();
                }
            } else if (b.isRolloverEnabled() && model.isRollover()) {
                altIcon = b.getRolloverIcon();
            }
            
            if (altIcon == null) {
                altIcon = b.getIcon();
            }
            
            altIcon.paintIcon(c, g, iconRect.x, iconRect.y);
        }
        
        // Draw the Text
        if (text != null) {
            final View v = (View)c.getClientProperty(BasicHTML.propertyKey);
            if (v != null) {
                v.paint(g, textRect);
            } else {
                paintText(g, b, textRect, text);
            }
        }
        
        AquaUtils.endFont(fontg2d, savedAntiAliasingHint);
    }
    
    /*
     * These Insets/Rectangles are allocated once for all
     * RadioButtonUI.getPreferredSize() calls. Re-using rectangles
     * rather than allocating them in each call substantially reduced
     * the time it took getPreferredSize() to run.
     * Obviously, this method can't be re-entered.
     */
    private static final Rectangle prefViewRect = new Rectangle();
    private static final Rectangle prefIconRect = new Rectangle();
    private static final Rectangle prefTextRect = new Rectangle();
    private static Insets prefInsets = new Insets(0, 0, 0, 0);
    
    /**
     * The preferred size of the button
     */
    public Dimension getPreferredSize(final JComponent c) {
        if (c.getComponentCount() > 0) {
            return null;
        }
        
        final AbstractButton b = (AbstractButton)c;
        
        final String text = b.getText();
        
        Icon buttonIcon = b.getIcon();
        if (buttonIcon == null) {
            buttonIcon = getDefaultIcon(b);
        }
        
        final Font font = b.getFont();
        final FontMetrics fm = b.getFontMetrics(font);
        
        prefViewRect.x = prefViewRect.y = 0;
        prefViewRect.width = Short.MAX_VALUE;
        prefViewRect.height = Short.MAX_VALUE;
        prefIconRect.x = prefIconRect.y =
            prefIconRect.width = prefIconRect.height = 0;
        prefTextRect.x = prefTextRect.y =
            prefTextRect.width = prefTextRect.height = 0;
        
        SwingUtilities.layoutCompoundLabel(c, fm, text, buttonIcon,
                                           b.getVerticalAlignment(),
                                           b.getHorizontalAlignment(),
                                           b.getVerticalTextPosition(),
                                           b.getHorizontalTextPosition(),
                                           prefViewRect, prefIconRect,
                                           prefTextRect,
                                           text == null ? 0 : b.getIconTextGap());
        
        // find the union of the icon and text rects (from Rectangle.java)
        final int x1 = Math.min(prefIconRect.x, prefTextRect.x);
        final int x2 = Math.max(prefIconRect.x + prefIconRect.width,
                                prefTextRect.x + prefTextRect.width);
        final int y1 = Math.min(prefIconRect.y, prefTextRect.y);
        final int y2 = Math.max(prefIconRect.y + prefIconRect.height,
                                prefTextRect.y + prefTextRect.height);
        int width = x2 - x1;
        int height = y2 - y1;
        
        prefInsets = b.getInsets(prefInsets);
        width += prefInsets.left + prefInsets.right;
        height += prefInsets.top + prefInsets.bottom;
        return new Dimension(width, height);
    }
    
    public static abstract class LabeledButtonBorder extends AquaButtonBorder {
        public LabeledButtonBorder(final SizeDescriptor sizeDescriptor) {
            super(sizeDescriptor);
        }
    
        public LabeledButtonBorder(final LabeledButtonBorder other) {
            super(other);
        }
    
        protected void doButtonPaint(final AbstractButton b,
                                     final ButtonModel model,
                                     final Graphics g,
                                     final int x, final int y,
                                     final int width, final int height)
        {
            painter.state.set(AquaUtilControlSize.getUserSizeFrom(b));
            painter.state.set(model.isSelected() ?
                            BooleanValue.YES : BooleanValue.NO);
            super.doButtonPaint(b, model, g, x, y, width, height);
        }
    
        protected State getButtonState(final AbstractButton b,
                                       final ButtonModel model)
        {
            final State state = super.getButtonState(b, model);
            
            if (state == State.INACTIVE) return State.INACTIVE;
            if (state == State.DISABLED) return State.DISABLED;
            if (model.isArmed() && model.isPressed()) return State.PRESSED;
            if (model.isSelected()) return State.ACTIVE;
            
            return state;
        }
    }
}
