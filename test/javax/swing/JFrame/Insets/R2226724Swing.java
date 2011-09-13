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
 @summary <rdar://problem/2226724>
 @summary Frame.getInsets() does not return expected/meaningful data.
 @summary com.apple.junit.javax.swing
 @run main R2226724Swing
 */
import junit.framework.*;

import javax.swing.JFrame;
import java.awt.Insets;

public class R2226724Swing extends TestCase {

    public static Test suite() {
        return new TestSuite(R2226724Swing.class);
    }

    public static void main(String argv[]) {
      junit.textui.TestRunner.run(suite());
    }

    // use "ref" for stdout info; return PASSED or FAILED
    public void testInsets() {
        String propertyString = System.getProperties().getProperty("os.name");
        assertNotNull( "UNEXPECTED ERROR: Unable to getProperty(\"os.name\").", propertyString);
        
        JFrame windowf = new JFrame("WindowFrame");
        windowf.setVisible(true);
        try {
            Insets insets = windowf.getInsets();        
            
            if( propertyString.equals("Mac OS X") || propertyString.equals("Darwin") ) {
                assertTrue("ERROR: Frame.getInsets().top == 0.  Should return height of title bar.", ( insets.top != 0 ));
                assertTrue("ERROR: Frame.getInsets().left != 0.  Mac OS X windows don't have side borders.", ( insets.left == 0 ));
                assertTrue("ERROR: Frame.getInsets().right != 0.  Mac OS X windows don't have side borders.", ( insets.right == 0 ));
                assertTrue("ERROR: Frame.getInsets().bottom != 0.  Mac OS X windows don't have bottom borders.", insets.bottom == 0);
            }
            else {
                assertTrue("ERROR: Frame.getInsets().left == 0.  Classic windows have side borders.", ( insets.left != 0 ));
                assertTrue("ERROR: Frame.getInsets().right == 0.  Classic windows have side borders.", ( insets.right != 0 ));
                assertTrue("ERROR: Frame.getInsets().bottom == 0.  Classic windows have bottom borders.", ( insets.bottom != 0 ));
            }
        } finally {
            windowf.setVisible(false);
            windowf.dispose();
        }
    }
}


