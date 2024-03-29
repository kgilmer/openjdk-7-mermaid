#! /bin/sh

#
# Copyright (c) 2004, 2010, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

# @test
# @author Yingxian Wang
# @bug 4957669 5017871
# @library ../../../sun/net/www/httptest/
# @build HttpCallback HttpServer ClosedChannelList HttpTransaction
# @run shell/timeout=300 ClassnameCharTest.sh
# @summary ; cannot load class names containing some JSR 202 characters;
#          plugin does not escape unicode character in http request
#
# set platform-dependent variables

OS=`uname -s`
case "$OS" in
  SunOS | Linux | *BSD | Darwin )
    PS=":"
    FS="/"
    ;;
  Windows* | CYGWIN* )
    PS=";"
    FS="\\"
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac

cp ${TESTSRC}${FS}testclasses.jar ${TESTCLASSES}
cd ${TESTCLASSES}
${TESTJAVA}${FS}bin${FS}jar xvf testclasses.jar "fo o.class"
${TESTJAVA}${FS}bin${FS}javac -d ${TESTCLASSES} ${TESTSRC}${FS}ClassnameCharTest.java

${TESTJAVA}${FS}bin${FS}java -classpath "${TESTCLASSES}${PS}${TESTCLASSES}${FS}sun${FS}misc${FS}URLClassPath" ClassnameCharTest

rm -rf "fo o.class" testclasses.jar
