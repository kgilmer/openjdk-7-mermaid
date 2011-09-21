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
 * @test
 * @summary <rdar://problem/4475972> [JavaJDK16] Hidden windows still receive mouse events
 * @summary com.apple.junit.java.awt.Window
 * @library ../../regtesthelpers
 * @build RobotUtilities VisibilityValidator Waypoint
 * @run main HiddenWindowSentEvents
 */

import test.java.awt.regtesthelpers.RobotUtilities;
import test.java.awt.regtesthelpers.VisibilityValidator;
import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;
import java.awt.*;
import java.awt.event.*;

public class HiddenWindowSentEvents extends TestCase {
    private volatile boolean hiddenMouseEntered = false;
    private Waypoint waypoint = new Waypoint();
    private static final int TEH_SIZE = 200;
    private static final int TEH_LOC = 50;
    private static final int TEH_LOC_F2 = TEH_LOC + TEH_SIZE + 45; // location of f1 + size of f1 + random space
    
    public void testHiddenEvents() throws Exception {
        Frame f1 = new Frame("Hidden");
        Frame f2 = new Frame("Not Hidden");
        
        try {
            f1.setLocation(TEH_LOC, TEH_LOC);
            f1.setSize(TEH_SIZE, TEH_SIZE);
            f1.addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) {
                hiddenMouseEntered = true;
            }});
            
            VisibilityValidator.setVisibleAndConfirm(f1);
            f1.setVisible(false);
            
            f2.setLocation(TEH_LOC_F2, TEH_LOC);
            f2.setSize(TEH_SIZE, TEH_SIZE);
            f2.addMouseListener(new MouseAdapter() { public void mouseEntered(MouseEvent e) {
                waypoint.clear();
            }});

            VisibilityValidator.setVisibleAndConfirm(f2);
            
            RobotUtilities.moveMouseTo(TEH_LOC_F2 + TEH_SIZE/2, TEH_LOC + TEH_SIZE/2); // f2
            hiddenMouseEntered = false;
            waypoint.reset();
            RobotUtilities.moveMouseTo(TEH_LOC + TEH_SIZE/2, TEH_LOC + TEH_SIZE/2); // f1
            RobotUtilities.moveMouseTo(TEH_LOC_F2 + TEH_SIZE/2, TEH_LOC + TEH_SIZE/2); // f2

            waypoint.requireClear("Frame 2 didn't get a mouseEntered as expected");
            assertFalse("Frame 1 got unexpected mouseEntered ", hiddenMouseEntered);
        
        } finally {
            f1.setVisible(false);
            f1.dispose();
            f1 = null;
            f2.setVisible(false);
            f2.dispose();
            f2 = null;
        }
    }
    
    
    public static Test suite() {
        return new TestSuite(HiddenWindowSentEvents.class);
    }
    
    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
}
