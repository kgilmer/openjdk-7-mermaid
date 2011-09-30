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

/**
 @test
 @summary Tests for the 1.4 regex package
 @summary com.apple.junit.java.util.regex;
 @run main BasicMatching
 */

import junit.framework.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicMatching extends TestCase {

    public static Test suite() {
        return new TestSuite(BasicMatching.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### Unexpected JUnit errors or failures.");
        }
    }
    
    public static String kGettysburgAddress  =  
        "Four score and seven years ago our fathers brought forth on this \n" +
        "continent a new nation, conceived in liberty and dedicated to the \n" +
        "proposition that all men are created equal. Now we are engaged in \n" +
        "a great civil war, testing whether that nation or any nation so \n" +
        "conceived and so dedicated can long endure. We are met on a great \n" +
        "battlefield of that war. We have come to dedicate a portion of \n" +
        "that field as a final resting-place for those who here gave their \n" +
        "lives that that nation might live. It is altogether fitting and \n" +
        "proper that we should do this. But in a larger sense, we cannot \n" +
        "dedicate, we cannot consecrate, we cannot hallow this ground. \n" +
        "The brave men, living and dead who struggled here have consecrated \n" +
        "it far above our poor power to add or detract. The world will \n" +
        "little note nor long remember what we say here, but it can never \n" +
        "forget what they did here. It is for us the living rather to be \n" +
        "dedicated here to the unfinished work which they who fought here\n" +
        "have thus far so nobly advanced. It is rather for us to be here \n" +
        "dedicated to the great task remaining before us--that from these \n" +
        "honored dead we take increased devotion to that cause for which \n" +
        "they gave the last full measure of devotion--that we here highly \n" +
        "resolve that these dead shall not have died in vain, that this \n" +
        "nation under God shall have a new birth of freedom, and that \n" +
        "government of the people, by the people, for the people shall \n" +
        "not perish from the earth.\n";

    class rx {
         String pattern;
         boolean match;
         int flags = 0;
        
        // Getters and Setters
        public String getPattern() { return pattern; }
        public boolean getMatch() { return match; }
        public int getFlags() { return flags; }
        public void setPattern(String s) { pattern = s; }
        public void setMatch(boolean b) { match = b; }
        public void setFlags(int f) { flags = f; }
        
        public String toString() { return ("\"" + pattern + "\", " + ((match == true)?"true":"false") + ((flags==0)?"":", flags " + flags)); }
        
        public rx(String s, boolean b) {
            pattern = s;
            match = b;
        }
        
        public rx(String s, boolean b, int f) {
            pattern = s;
            match = b;
            flags = f;
        }
        
    };

    // See http://java.sun.com/j2se/1.4/docs/api/java/util/regex/Pattern.html for lots of great regular expression info

    public rx[] pattern = {
        new rx("father", true),
        new rx("con*secrate", true),
        new rx("con.+secrate", false),
        new rx("dedicate.*ground", true),
        new rx("dedicate.*brave", false),
        new rx("dedicate.*brave", true, Pattern.DOTALL),
        new rx("c.n.e.v.d", true),
        new rx("c.n.e.v.e", false),
        new rx("F[a-z][a-z]r score", true),
        new rx("F[a-z][a-z&&[^r]] score", false),
        new rx("\\d", false),
        new rx("here$", true, Pattern.MULTILINE),
        new rx("foul", false)
    };

    public boolean doRegexTest (String regexpattern, int flags, String regexmatcher) {
        Pattern p = Pattern.compile (regexpattern, flags);
        Matcher m = p.matcher(regexmatcher);
        return (m.find());
    }

    public void testBasicMatching () {
        for (int i = 0; i < pattern.length; i++ ) {
            boolean answer = doRegexTest(pattern[i].getPattern(), pattern[i].getFlags(), kGettysburgAddress);
            String error = "Unexpected result: \"" + pattern[i].getPattern() + "\" did not evaluate to " + pattern[i].getMatch() + " with flags set to " + pattern[i].getFlags();
            assertTrue( error, answer == pattern[i].getMatch() );
        }
    }
}
