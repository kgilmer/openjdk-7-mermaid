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
 @test
 @summary Simple Dock icon test as well as calling other eawt.Application methods
 @summary com.apple.junit.apple.eawt;
 @library ../../../java/awt/regtesthelpers
 @build VisibilityValidator
 @run main DockIconTest
 */

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import junit.framework.*;

import com.apple.eawt.Application;
import test.java.awt.regtesthelpers.VisibilityValidator;

public class DockIconTest extends TestCase {

    private Application app;
    private JFrame f;
    private PopupMenu menu;
    private Image defaultImg;

    public void setUp() throws Exception {
    
    app = Application.getApplication();    

    f = new JFrame("testing");
        
    f.getContentPane().add(new JLabel(new ImageIcon(app.getDockIconImage())));
    f.setSize(520, 520);
    f.setVisible(true);
    
    menu = new PopupMenu("Dock Menu");
    menu.add(new MenuItem("Alpha"));
    menu.add(new MenuItem("Beta"));
    menu.addSeparator();
    menu.add(new MenuItem("Gamma"));
    menu.add(new MenuItem("Delta"));
    f.add(menu);

    app.setDockMenu(menu);

    // this requires a bundled help bundle
    // something to think about for later
    //app.openHelpViewer();    
    }

    public void testDockIcon() throws Exception {
        Thread.sleep(3000);
        
        defaultImg = app.getDockIconImage();
        Image toolkitImg = Toolkit.getDefaultToolkit().getImage("NSImage://NSComputer");
            
        assertNotNull("Toolkit.getDefaultToolkit().getImage(\"NSImage://NSComputer\") returned null", toolkitImg);

        // assert default != toolkit
        assertTrue("Default icon image and toolkit image are the same???", !imagesAreIdentical(defaultImg, toolkitImg));

        app.setDockIconImage(toolkitImg);
        Image dockiconImg = app.getDockIconImage();
        
        // assert dockicon == toolkit
        assertTrue("Dock icon image and toolkit image are not the same!", imagesAreIdentical(dockiconImg, toolkitImg));

        // assert dockicon != default 
        assertTrue("Dock icon image and default icon image are the same???", !imagesAreIdentical(dockiconImg, defaultImg));

        // we can set the dock icon badge, but there is no 
        // easy way to verify it graphically
        app.setDockIconBadge("42");
        Thread.sleep(3000);
        }

        protected void tearDown() {
        app.setDockIconBadge("");
        app.setDockIconImage(defaultImg);
        f.dispose();
    }

    public static Test suite() {
    return new TestSuite(DockIconTest.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected junit errors or failures.");
        }
    }
    
    private BufferedImage getBufferedImageFromImage(Image img) {
    // convert Image to BufferedImage
    BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = bimg.createGraphics();
    graphics.drawImage(img, 0, 0, null);
    return bimg;
    }

    private boolean imagesAreIdentical(Image a, Image b) {
    BufferedImage imgA = getBufferedImageFromImage(a);
    BufferedImage imgB = getBufferedImageFromImage(b);

    // first check dimensions
    if (imgA.getHeight() != imgB.getHeight()) {
        return false;
    }
    if (imgA.getWidth() != imgB.getWidth()) {
        return false;
    }
    // then check contents
    for (int y = 0; y < imgA.getHeight(); y++) {
        for (int x = 0; x < imgA.getWidth(); x++) {
        int rgbA = imgA.getRGB(x, y);
        int rgbB = imgB.getRGB(x, y);
            if (!VisibilityValidator.colorMatch(new Color(rgbA), new Color(rgbB))) {
                return false;
            }
        }
    }
    return true;
    }

}
