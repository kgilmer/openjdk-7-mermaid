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
 @summary com.apple.junit.java.awt.Frame
 @run main R2226724Insets
 */
import java.awt.Frame;
import java.awt.Insets;

public class R2226724Insets {

    public static void main(String argv[]) throws RuntimeException {
        String propertyString = System.getProperties().getProperty("os.name");
        if (propertyString == null) {
            throw new RuntimeException("UNEXPECTED ERROR: Unable to getProperty(\"os.name\").");
        }

        Frame windowf = new Frame("WindowFrame");
        windowf.setVisible(true);
        try {
            Insets insets = windowf.getInsets();
            
            if( propertyString.equals("Mac OS X") || propertyString.equals("Darwin") ) {
                if(insets.top == 0) {throw new RuntimeException("FAILURE: Frame.getInsets().top == 0.  Should return height of title bar.");}
                if(insets.left != 0) {throw new RuntimeException("FAILURE: Frame.getInsets().left != 0.  Mac OS X windows don't have side borders.");}
                if(insets.right != 0) {throw new RuntimeException("FAILURE: Frame.getInsets().right != 0.  Mac OS X windows don't have side borders.");}
                if(insets.bottom != 0) {throw new RuntimeException("FAILURE: Frame.getInsets().bottom != 0.  Mac OS X windows don't have bottom borders.");}
            }
            else {
                if(insets.left == 0) {throw new RuntimeException("FAILURE: Frame.getInsets().left == 0.  Classic windows have side borders.");}
                if(insets.right == 0) {throw new RuntimeException("FAILURE: Frame.getInsets().right == 0.  Classic windows have side borders.");}
                if(insets.bottom == 0) {throw new RuntimeException("FAILURE: Frame.getInsets().bottom == 0.  Classic windows have bottom borders.");}
            }
        } finally {
            windowf.setVisible(false);
            windowf.dispose();
        }
    }

}


