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
 @summary Tests for the 1.5 GregorianCalendar class
 @summary com.apple.junit.java.util;
 @run main GregorianCalendarTest
 */

import junit.framework.*;

import java.util.*;

public class GregorianCalendarTest extends TestCase {

    public static Test suite() {
        return new TestSuite(GregorianCalendarTest.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    GregorianCalendar g1;
    GregorianCalendar g2;
    GregorianCalendar g3;

    GregorianCalendar taxDay2006;

    static GregorianCalendar epoch;
    static {
        epoch = new GregorianCalendar();
        epoch.setTimeZone(TimeZone.getTimeZone("GMT+00"));
        epoch.setTimeInMillis(0L);
    }

    public void testBasicConstructor() {
        g1 = new GregorianCalendar();
        assertNotNull(g1);
    }

    public void testEpoch() {
        assertEquals(GregorianCalendar.AD, epoch.get(Calendar.ERA));
        assertEquals(1970,                 epoch.get(Calendar.YEAR));
        assertEquals(Calendar.JANUARY,     epoch.get(Calendar.MONTH));
        assertEquals(1,                    epoch.get(Calendar.WEEK_OF_YEAR), 1);
        assertEquals(1,                    epoch.get(Calendar.WEEK_OF_MONTH), 1);
        assertEquals(1,                    epoch.get(Calendar.DATE), 1);
        assertEquals(1,                    epoch.get(Calendar.DAY_OF_MONTH), 1);
        assertEquals(1,                    epoch.get(Calendar.DAY_OF_YEAR), 1);
        assertEquals(Calendar.THURSDAY,    epoch.get(Calendar.DAY_OF_WEEK), Calendar.THURSDAY);
        assertEquals(1,                    epoch.get(Calendar.DAY_OF_WEEK_IN_MONTH), 1);
        assertEquals(Calendar.AM,          epoch.get(Calendar.AM_PM), Calendar.AM);
        assertEquals(0,                    epoch.get(Calendar.HOUR), 0);
        assertEquals(0,                    epoch.get(Calendar.HOUR_OF_DAY), 0);
        assertEquals(0,                    epoch.get(Calendar.MINUTE), 0);
        assertEquals(0,                    epoch.get(Calendar.SECOND), 0);
        assertEquals(0,                    epoch.get(Calendar.MILLISECOND), 0);
        assertEquals(0,                    epoch.get(Calendar.ZONE_OFFSET), 0);
        assertEquals(0,                    epoch.get(Calendar.DST_OFFSET), 0);
    }

    public void assertEquals(int expected[], GregorianCalendar calendar) {
        assertEquals(expected[0], calendar.get(Calendar.YEAR));
        assertEquals(expected[1], calendar.get(Calendar.MONTH));
        assertEquals(expected[2], calendar.get(Calendar.DAY_OF_MONTH));
        assertEquals(expected[3], calendar.get(Calendar.DAY_OF_WEEK));
        assertEquals(expected[4], calendar.get(Calendar.AM_PM));
        assertEquals(expected[5], calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(expected[6], calendar.get(Calendar.MINUTE));
        assertEquals(expected[7], calendar.get(Calendar.SECOND));
        assertEquals(expected[8], calendar.get(Calendar.MILLISECOND));
    }

    public void testAddPastDST_NOT() {
        // start at Jan 1, 1970, 00:00:00 GMT.
        GregorianCalendar gc = (GregorianCalendar)epoch.clone();

        // go forward 28 years, 3 months and 4 days, to April 5, 1970.
        gc.add(Calendar.YEAR, 28);
        gc.add(Calendar.MONTH, 3);
        gc.add(Calendar.DAY_OF_MONTH, 4);
        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0 }, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0 }, gc);

        // Should this be happening? Dont they have DST in DMT? No?
    }

    public void testAddPastDST() {
        GregorianCalendar gc = (GregorianCalendar)epoch.clone();
        gc.setTimeZone(TimeZone.getTimeZone("GMT-0800"));
        gc.set(Calendar.YEAR, 1998);
        gc.set(Calendar.MONTH, Calendar.APRIL);
        gc.set(Calendar.DAY_OF_MONTH, 5);
        gc.set(Calendar.HOUR, 1);
        gc.set(Calendar.MINUTE, 0);
        gc.set(Calendar.SECOND, 0);
        gc.set(Calendar.MILLISECOND, 0);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0 }, gc);

        gc.add(Calendar.HOUR, 2);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 3, 0, 0, 0 }, gc);
    }

    public void testAdd() {
        // start at Jan 1, 1970, 00:00:00 GMT.
        //
        GregorianCalendar gc = (GregorianCalendar)epoch.clone();
        assertEquals(new int[] { 1970, Calendar.JANUARY, 1, Calendar.THURSDAY, Calendar.AM, 0, 0, 0, 0 }, gc);

        // go forward 3 months and 5 days, to April 5, 1970.
        //
        gc.add(Calendar.MONTH, 3);
        gc.add(Calendar.DAY_OF_MONTH, 4);
        assertEquals(new int[] { 1970, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 0, 0, 0, 0 }, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1970, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0 }, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1970, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1970

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1970, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1971, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1971, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1971

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1971, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -2);
        assertEquals(new int[] { 1972, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1972, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1972?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1972, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1973, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1973, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1973?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1973, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, 6);
        assertEquals(new int[] { 1974, Calendar.APRIL, 7, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1974, Calendar.APRIL, 7, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1974?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1974, Calendar.APRIL, 7, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1975, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1975, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1975?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1975, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -2);
        assertEquals(new int[] { 1976, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1976, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1976?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1976, Calendar.APRIL, 4, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1977, Calendar.APRIL, 3, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1977, Calendar.APRIL, 3, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1977?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1977, Calendar.APRIL, 3, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1978, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1978, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1978?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1978, Calendar.APRIL, 2, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1979, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1979, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1979?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1979, Calendar.APRIL, 1, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, 5);
        assertEquals(new int[] { 1980, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1980, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1980?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1980, Calendar.APRIL, 6, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 1);
        gc.add(Calendar.DAY_OF_MONTH, -1);
        assertEquals(new int[] { 1981, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1981, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);

        // No push for DST in 1982?

        gc.add(Calendar.HOUR, -1);
        assertEquals(new int[] { 1981, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.YEAR, 17);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 1, 0, 0, 0}, gc);

        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 2, 0, 0, 0}, gc);
        gc.add(Calendar.HOUR, 1);
        assertEquals(new int[] { 1998, Calendar.APRIL, 5, Calendar.SUNDAY, Calendar.AM, 3, 0, 0, 0}, gc);

        // No DST push for DST in 1998?
    }
}
