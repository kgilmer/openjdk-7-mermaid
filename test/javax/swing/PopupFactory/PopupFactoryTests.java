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
 @summary com.apple.junit.javax.swing.PopupFactoryTests.java
 @run main PopupFactoryTests
 */

import junit.framework.*;

import javax.swing.*;
import java.awt.*;

public class PopupFactoryTests extends TestCase {
    public static Test suite() {
        return new TestSuite(PopupFactoryTests.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

    public void testPopupFactory2001() {
        PopupFactory c = new PopupFactory();

        if (!(c != null && c instanceof PopupFactory)) {
            fail("Failed to create PopupFactory object");
        }
    }

    public void testPopupFactory2002() {
        Popup c = PopupFactory.getSharedInstance().getPopup(new Canvas(), new Canvas(), 10, 10);

        if (!(c != null && c instanceof Popup)) {
            fail("Test for method getPopup failed.");
        }
    }

    public void testPopupFactory2003() {
        try {
            Popup c = PopupFactory.getSharedInstance().getPopup(new Canvas(), null, 10, 10);
            fail("Failed. IllegalArgumentException should be thrown, but " + c +" was created.");
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    public void testPopupFactory2004() {
        PopupFactory c = new PopupFactory();
        PopupFactory before = PopupFactory.getSharedInstance();
        PopupFactory.setSharedInstance(c);
        PopupFactory after = PopupFactory.getSharedInstance();

        if (!(c == after && c != before)) {
            fail("Test for method setSharedInstance failed.");
        }
    }

    public void testPopupFactory2005() {
        try {
            PopupFactory.setSharedInstance(null);
        } catch (IllegalArgumentException e) {
            return;
        }

        fail("Failed.  IllegalArgumentException should be thrown.");
    }

    public void testPopupFactory2006() {
        PopupFactory c = PopupFactory.getSharedInstance();

        if (!(c != null && c instanceof PopupFactory)) {
            fail("Test for method getSharedInstance failed.");
        }
    }
}

