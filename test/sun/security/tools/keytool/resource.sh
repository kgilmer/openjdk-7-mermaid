#
# Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
# @bug 6239297
# @summary keytool usage is broken after changing Resources.java
# @author Max Wang
#
# @run shell resource.sh

if [ "${TESTSRC}" = "" ] ; then
  TESTSRC="."
fi
if [ "${TESTCLASSES}" = "" ] ; then
  TESTCLASSES="."
fi
if [ "${TESTJAVA}" = "" ] ; then
  echo "TESTJAVA not set.  Test cannot execute."
  echo "FAILED!!!"
  exit 1
fi

# set platform-dependent variables
OS=`uname -s`
case "$OS" in
  SunOS | Linux | *BSD | Darwin )
    NULL=/dev/null
    FS="/"
    TMP=/tmp
    ;;
  CYGWIN* )
    NULL=/dev/null
    FS="/"
    TMP=/tmp
    ;;
  Windows_* )
    NULL=NUL
    FS="\\"
    TMP="c:/temp"
    ;;
  * )
    echo "Unrecognized operating system!"
    exit 1;
    ;;
esac

# the test code
${TESTJAVA}${FS}bin${FS}keytool > ${TMP}${FS}temp_file_40875602475 2> ${NULL}
grep MissingResourceException ${TMP}${FS}temp_file_40875602475

if [ $? -eq 0 ]; then 
    rm ${TMP}${FS}temp_file_40875602475
    exit 1
fi

rm ${TMP}${FS}temp_file_40875602475
exit 0
