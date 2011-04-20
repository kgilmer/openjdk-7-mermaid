/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @test
 * @summary <rdar://problem/1610789> JCK: (DateTest) The output of some Date class commands is wrong
 * @summary com.apple.junit.java.util.Date
 */

import junit.framework.*;
import java.util.Date;
import java.util.TimeZone;

public class R1610789DateTests extends TestCase {
    public static Test suite() {
        return new TestSuite(R1610789DateTests.class);
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    @SuppressWarnings({ "deprecation", "static-access" })
    // Compatiblity test -- intentionally uses the old API

    public void testR1610789Date() {
        
        Date  y1 = new Date();
        Date  y2 = new Date(1000000);
        Date  y3 = new Date(95, 11, 21);
        Date  y4 = new Date(95, 11, 20, 8, 57);
        Date  y5 = new Date(95, 11, 19, 7, 50, 45);
        Date  y6 = new Date("6 Feb 1972 5:23:00 PST");

        // The writer of this test assumed "PST" as the current timezone, but all
        // the "getters" report back in whatever the default timezone is. So, sadly, 
        // huge corrections are needed to save this test.  Instead, just warn and skip.
  
        assertNotNull(y1);
        assertNotNull(y2);
        assertNotNull(y3);
        assertNotNull(y4);
        assertNotNull(y5);
        assertNotNull(y6);

        TimeZone tester = TimeZone.getDefault();
        TimeZone test_writer = TimeZone.getTimeZone("PST");
        if ( !tester.hasSameRules( test_writer)) {
            System.out.println("WARNING: This test is only valid when run in PST timezone!  Skipping all assertions.");
        }
        else {
            long utc = Date.UTC(1995, 11, 20, 8, 45, 45);
            assertTrue("y1.UTC(int, int, int, int, int, int) incorrect",utc == 60777679545000L);
            assertEquals("y1.after(java.util.Date) incorrect",y1.after(y2), true);
            assertEquals("y1.before(java.util.Date) incorrect",y1.before(y3), false);
            assertEquals("y1.equals(java.lang.Object) incorrect",y1.equals(y4), false);
            assertEquals("y6.getDate() incorrect",y6.getDate(), 6);
            assertEquals("y6.getDay() incorrect",y6.getDay(), 0);

            assertEquals("y6.getHours() incorrect",y6.getHours(), 5);
            assertEquals("y6.getMinutes() incorrect",y6.getMinutes(), 23);
            assertEquals("y6.getMonth() incorrect",y6.getMonth(), 1);
            assertEquals("y6.getSeconds() incorrect",y6.getSeconds(), 0);
            assertTrue("y6.getTime() incorrect",y6.getTime() == 66230580000L);
            assertEquals("y6.getTimezoneOffset() incorrect",y6.getTimezoneOffset(), 480);
            assertEquals("y6.getYear() incorrect",y6.getYear(), 72);
            assertTrue("y1.parse(java.lang.String) incorrect",Date.parse("21 Nov 1995 9:00:00 PST") == 816973200000L);

            // output void

            y3.setDate(10);
            assertEquals("y3.setDate(10) ", y3.toString(), "Sun Dec 10 00:00:00 PST 1995"); 

            y3.setHours(20);
            assertEquals("y3.setHours(int) ", y3.toString(), "Sun Dec 10 20:00:00 PST 1995"); 

            y3.setMinutes(40);
            assertEquals("y3.setMinutes(int) ", y3.toString(), "Sun Dec 10 20:40:00 PST 1995"); 

            y3.setMonth(9);
            assertEquals("y3.setMonth(int) ", y3.toString(), "Tue Oct 10 20:40:00 PDT 1995"); 

            y3.setSeconds(23);
            assertEquals("y3.setSeconds(int) ", y3.toString(), "Tue Oct 10 20:40:23 PDT 1995"); 

            y3.setTime(120000);
            assertEquals("y3.setTime(long) ", y3.toString(), "Wed Dec 31 16:02:00 PST 1969"); 

            y3.setYear(100);
            assertEquals("y3.setYear(int) ", y3.toString(), "Sun Dec 31 16:02:00 PST 2000"); 
            assertEquals("y4.toGMTString() ", y4.toGMTString(), "20 Dec 1995 16:57:00 GMT"); 
            assertEquals("y4.toLocaleString() ", y4.toLocaleString(), "Dec 20, 1995 8:57:00 AM"); 
            assertEquals("y4.toString() ", y4.toString(), "Wed Dec 20 08:57:00 PST 1995");

            assertNotNull(y5);
            
            assertEquals("y6 hours not correct", y6.getHours(),5 );
            assertEquals("y6 timezoneOffset not correct", y6.getTimezoneOffset(), 480 );
        }
    }
}


