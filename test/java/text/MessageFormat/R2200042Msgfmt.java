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
 * @summary <rdar://problem/2200042 MessageFormat.format() doesn't work if msg string include ' mark
 * @summary com.apple.junit.java.text.MessageFormat
 */

import java.text.MessageFormat;

public class R2200042Msgfmt
{
    public static void main (String[] args) throws Exception {
        String message;
        String result;
        String expectedMsg;
                
        message = "One Quote doesn't {0}";
        Object[] args1 = { "work!" };
        
        result = MessageFormat.format(message, args1) ;
        // System.out.println(result);
        expectedMsg = "One Quote doesnt {0}";
        if (!result.equals(expectedMsg)) {
            throw new RuntimeException("MessageFormat.format error: expected result = " + expectedMsg +"; actual result = " + result);
        }
    
        message = "Two Quotes don''t {0}";
        Object[] args2 = { "fail!" };
        
        result = MessageFormat.format(message, args2) ;
        // System.out.println(result);
        expectedMsg = "Two Quotes don't fail!";            
        if (!result.equals(expectedMsg)) {
            throw new RuntimeException("MessageFormat.format error: expected result = " + expectedMsg +"; actual result = " + result);
        }
    }
}
