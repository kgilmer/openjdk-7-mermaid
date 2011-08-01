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
 * @author  Barry Langdon-Lassagne; ported to jtreg by David Durrence
 * @summary Test some basic functionality of TextFields
 * @summary com.apple.junit.java.text.TextFields
 */

import java.awt.Frame;
import java.awt.TextField;

public class TextFields01 {

    public static void main( String[] args ) throws Exception {
        TextField tf1 = new TextField("PASSED");
        TextField tf2 = new TextField("FAILED");
        Frame tfFrame = new Frame("TextField Example");
        tfFrame.add("North",tf1);
        tfFrame.add("South",tf2);
        tfFrame.pack();
        tfFrame.setVisible(true);

        String a = tf1.getText();
        int i = 0;
        while (i++ < a.length()) {
            tf2.setText(a.substring(0,i));
        }
        
        if (!tf2.getText().equals("PASSED")) {
            throw new RuntimeException("TextField manipuation failed");
        }
        
        tfFrame.setVisible(false);
        tfFrame.dispose();
    }
}


