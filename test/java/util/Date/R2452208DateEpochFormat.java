/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * @test
 * @summary <rdar://problem/2452208> JCK: Daylight Savings Time broke java.util.Date! (timezone is always GMT)
 * @summary com.apple.junit.java.util.Date
 */

import junit.framework.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 
public class R2452208DateEpochFormat extends TestCase {
    protected String before, after;
    protected Date beforeDate, afterDate;
    protected DateFormat f1;
    protected String formattedBeforeDate, formattedAfterDate;
    
    protected void setUp() {
        before = "6 Feb 1922 05:13:50 PST";
        after = "29 Feb 2000 12:23:48 PST";
        f1 = new SimpleDateFormat("d MMM yyyy HH:mm:ss zzz");
    }

    public void testDateEpochFormat() throws Exception {
        beforeDate = f1.parse(before);
        formattedBeforeDate = f1.format(beforeDate);
        //System.out.println("\"" + before + "\" becomes: " + beforeDate);
        //System.out.println("\"" + before + "\" using format() becomes: " + formattedBeforeDate);
        assertTrue("Date before epoch was not correctly formatted.", before.equals(formattedBeforeDate));   
    
        afterDate = f1.parse(after);
        formattedAfterDate = f1.format(afterDate);
        //System.out.println("\"" + after + "\" becomes: " + afterDate);
        //System.out.println("\"" + after + "\" using format() becomes: " + formattedAfterDate);        
        assertTrue("Date after epoch was not correctly formatted.", after.equals(formattedAfterDate));  
    }

    public static Test suite() {
        return new TestSuite(R2452208DateEpochFormat.class);
    }
    
    public static void main (String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

