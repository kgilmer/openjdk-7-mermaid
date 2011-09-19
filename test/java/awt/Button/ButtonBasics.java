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
 * @summary Quicklook test ButtonBasics -- Test some basic functionality of Buttons
 * @summary com.apple.junit.java.awt.button
 */

import java.awt.Button;
import java.awt.Frame;

public class ButtonBasics extends Frame  {
    Button button1;

    public static void main (String[] args) throws Exception {
        ButtonBasics ql = null;
        try {
            ql = new ButtonBasics();
            ql.quickLook();
            ql.manipulate();
            if (ql.result() == false) {
                throw new Exception("Couldn't set the label of the button.");
            }
        }
        finally {
            ql.dispose();
        }
    }

     // constructor 
    public ButtonBasics() {
        super("Button Example");
    }

    void quickLook() throws Exception {
        button1  = new Button("FAILED");
        add("Center",button1);
        pack();
        setVisible(true);
    }

    void manipulate() throws Exception {
        String a = "PASSED";
        int i = 0;
        while (i++ < a.length()) {
            button1.setLabel(a.substring(0,i));
            Thread.sleep(200);
        }
    }

    public boolean result() {
        if (button1.getLabel().equals("PASSED")) {
            return true;
        }
        else {
            return false;
        }
    }
}




