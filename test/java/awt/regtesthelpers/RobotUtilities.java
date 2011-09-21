/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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

/*
 * @summary Utility routines that wait for a window to be displayed or for colors to be visible
 * @summary com.apple.junit.utils
 */

package test.java.awt.regtesthelpers;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.TimerTask;
import javax.swing.JButton;

public class RobotUtilities {
    public static final int CLICK_DELAY = 20;        // interval between mouse down and mouse up events
    public static final int DOUBLE_CLICK_DELAY = 100;    // interval between clicks of a double click
    public static final int POST_MOVE_DELAY = 250;    // allow some time for visual verification
    public static final int PRE_TEST_DELAY = 250;    // allow some time for frame to draw
    public static final int POST_TEST_DELAY = 750;    // allow some time for event processing/visual verification
    
    public RobotUtilities() {
    
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void pressKey(int num) {
        Robot robot = getRobot();
        robot.keyPress(num);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void releaseKey(int num) {
        Robot robot = getRobot();
        robot.keyRelease(num);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void typeKey(int num) {
        Robot robot = getRobot();
        robot.keyPress(num);
        robot.delay(CLICK_DELAY);
        robot.keyRelease(num);
    }
    
    static Robot _robot;
    private static Robot getRobot() {
        if (_robot == null) {
            try { _robot = new Robot(); } catch (AWTException e) { throw new RuntimeException(e); }
        }
        return _robot;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // moveMouse - move the cusor to the specified location
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void moveMouseTo(int posX, int posY) {
        Robot robot = getRobot();
        robot.mouseMove(posX , posY);
        robot.delay(POST_MOVE_DELAY);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // moveMouse - move the cusor to the specified location of the component
    //    positive values specify the offset from the top-left of the component
    //    negative values specify the offset from the bottom-right of the component
    /////////////////////////////////////////////////////////////////////////////////////////////////
        public static void moveMouseTo(Component c, int posX, int posY) {
        Point pt;
        int x;
        int y;
        
        if(c == null)
            return;
            
        // Get the position of the component
        pt = c.getLocationOnScreen();
    
        x = pt.x + posX;
        if(posX < 0)
            x += c.getWidth();
        
        y = pt.y + posY;
        if(posY < 0)
            y += c.getHeight();
    
        moveMouseTo(x, y);
//        robot.mouseMove(x , y);
//        robot.delay(POST_MOVE_DELAY);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // moveMouse - move the cusor to the specified location of the component
    //    positive values specify the offset from the top-left of the component
    //    negative values specify the offset from the bottom-right of the component
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void moveMouseFromCenter(Component c, int offsetX, int offsetY) {
        Point pt;
        int x;
        int y;
        
        if(c == null)
            return;
            
        // Get the position of the component
        pt = c.getLocationOnScreen();
    
        x = pt.x + offsetX + c.getWidth()/2;
        
        y = pt.y + offsetY + c.getHeight()/2;
    
        moveMouseTo(x, y);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // moveMouseToCenter - move the cusor to the center of the component
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void moveMouseToCenter(Component c) {
        Point pt;
        int x;
        int y;
        
        if(c == null)
            return;
        
        // Get the position of the component
        pt = c.getLocationOnScreen();
        
        x = pt.x + c.getWidth()/2;
        
        y = pt.y + c.getHeight()/2;
        
        moveMouseTo(x, y);
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void pressMouseButton() {
        Robot robot = getRobot();
        robot.mousePress(InputEvent.BUTTON1_MASK);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void releaseMouseButton() {
        Robot robot = getRobot();
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void click() {
        Robot robot = getRobot();
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(CLICK_DELAY);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void click(Component c) {
    Point pt;
    int x;
    int y;
        
    if(c == null)
        return;
            
        // Get the position of the component
    pt = c.getLocationOnScreen();
    
    x = pt.x + c.getWidth()/2;
    y = pt.y + c.getHeight()/2;
    
        moveMouseTo(x , y);
        
        click();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void doubleClick() {
        Robot robot = getRobot();
        click();
        robot.delay(DOUBLE_CLICK_DELAY);
        click();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void doubleClick(Component c) {
    Point pt;
    int x;
    int y;
        
    if(c == null)
        return;
            
        // Get the position of the component
    pt = c.getLocationOnScreen();
    
    x = pt.x + c.getWidth()/2;
    y = pt.y + c.getHeight()/2;
    
        moveMouseTo(x , y);
        
        doubleClick();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void tripleClick() {
        Robot robot = getRobot();
        click();
        robot.delay(DOUBLE_CLICK_DELAY);
        click();
        robot.delay(DOUBLE_CLICK_DELAY);
        click();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void tripleClick(Component c) {
    Point pt;
    int x;
    int y;
        
    if(c == null)
        return;
            
        // Get the position of the component
    pt = c.getLocationOnScreen();
    
    x = pt.x + c.getWidth()/2;
    y = pt.y + c.getHeight()/2;
    
        moveMouseTo(x , y);
        
        tripleClick();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // clickAt - click at specified point
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void clickAt(int posX, int posY) {
        moveMouseTo(posX , posY);
        click();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // clickAt - click at specified point in the component
    //    positive values specify the offset from the top-left of the component
    //    negative values specify the offset from the bottom-right of the component
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void clickAt(Component c, int posX, int posY) {
        moveMouseTo(c, posX, posY);
        click();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void delay(int interval) {
        Robot robot = getRobot();
        robot.delay(interval);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    // clickAndDrag - click and drag within the component
    //    positive values specify the offset from the top-left of the component
    //    negative values specify the offset from the bottom-right of the component
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static void clickAndDrag(Component c, int startX, int startY, int endX, int endY) {
    Point pt;
    int x;
    int y;
        
    if(c == null)
        return;
            
        // Get the position of the component
    pt = c.getLocationOnScreen();
    
    // Make adjustments for negative values of offset
        x = pt.x + startX;
        if(startX < 0)
            x += c.getWidth();
        
    y = pt.y + startY;
        if(startY < 0)
            y += c.getHeight();
    
    
        moveMouseTo(x , y);
        pressMouseButton();
        
        x = pt.x + endX;
        if(endX < 0)
            x += c.getWidth();
        
    y = pt.y + endY;
        if(endY < 0)
            y += c.getHeight();
    
        moveMouseTo(x , y);
        releaseMouseButton();
    }

    
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // screenshot - take a screenshot and save to disk as a jpg
    //  basename - base name of the file. the file will be named <basename>.<timestamp>.jpg
    //  location - parent directory. If null, uses the current working directory.
    //  return value - the File for the newly created image.
    //  failure cases - if the basename is null, the file already exists, or location isn't a directory, returns null. 
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public static java.io.File screenshot(String basename, java.io.File location) throws java.io.IOException, java.awt.AWTException {
        if (basename == null) return null;
        if (location != null) {
            if (!location.exists() || !location.isDirectory()) {
                return null;
            }
        }
        
        Robot robot = getRobot();
        java.awt.image.BufferedImage screenshot = robot.createScreenCapture(new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize()));
        
        java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
        StringBuffer sb = new StringBuffer();
        sb.append(basename);
        sb.append(".");
        sb.append(calendar.get(java.util.Calendar.YEAR));
        sb.append(calendar.get(java.util.Calendar.MONTH));
        sb.append(calendar.get(java.util.Calendar.DAY_OF_MONTH));
        sb.append(calendar.get(java.util.Calendar.HOUR_OF_DAY));
        sb.append(calendar.get(java.util.Calendar.MINUTE));
        sb.append(".png");
        
        java.io.File outputFile = new java.io.File(location, sb.toString());
        javax.imageio.ImageIO.write(screenshot, "png", outputFile);
        
        return outputFile;
    }
    
    
    /*
     * Utility function to walk the container heirarchy looking for a JButton 
     * matching a specific text string.  For example, this is used to extract
     * the "Cancel" button from a JDialog.
     */
    
    public static JButton extractJButton( Container parent, String text ) {
        JButton cancel = null;
        Component[] c = parent.getComponents();
        for (int i = 0; i < c.length; i += 1) {
            if (c[i]instanceof JButton) {
                JButton b = (JButton) c[i];
                if (b.getText().equals( text )) {
                    cancel = (JButton) c[i];
                    break;
                }
            }
            else if (c[i]instanceof Container) {
                cancel = extractJButton( (Container) c[i], text );
            }
        }
        return cancel;
    }

    
    /*
     *    Utility class that takes a JButton and, later on, clicks it
     */
    
    public static class Clicker extends TimerTask {
        JButton target = null;
        private Exception cachedException;

        // We need to know what to click        
        public Clicker( JButton target) {
            super();
            this.target = target;
        }
        
        // This does the clicking
        public void run() {
            try {
                Robot r = new Robot();
                Point pt = target.getLocationOnScreen();
                int x = pt.x + target.getWidth() / 2;
                int y = pt.y + target.getHeight() / 2;
                r.mouseMove( x, y );
                r.delay( 30 );
                r.mousePress( InputEvent.BUTTON1_MASK );
                r.delay( 101 );
                r.mouseRelease( InputEvent.BUTTON1_MASK );
            }
            catch(Exception x) {
                setCachedException(x);
            }
        }

        void setCachedException(Exception cachedException) {
            this.cachedException = cachedException;
        }

        public Exception getCachedException() {
            return cachedException;
        }
        
        
    }
    
}
