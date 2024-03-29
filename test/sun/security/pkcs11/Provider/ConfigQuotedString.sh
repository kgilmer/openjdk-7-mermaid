#
# Copyright (c) 2004, 2005, Oracle and/or its affiliates. All rights reserved.
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
# @bug 5070773
# @summary SunPKCS11 provider does not support spaces config's provider name
# @run shell ConfigQuotedString.sh

# set a few environment variables so that the shell-script can run stand-alone
# in the source directory

# if running by hand on windows, change TESTSRC and TESTCLASSES to "."
if [ "${TESTSRC}" = "" ] ; then
    TESTSRC=`pwd`
fi
if [ "${TESTCLASSES}" = "" ] ; then
    TESTCLASSES=`pwd`
fi

# if running by hand on windows, change this to appropriate value
if [ "${TESTJAVA}" = "" ] ; then
    TESTJAVA="/net/radiant/export1/charlie/mustang/build/solaris-sparc"
fi
echo TESTSRC=${TESTSRC}
echo TESTCLASSES=${TESTCLASSES}
echo TESTJAVA=${TESTJAVA}
echo ""

# let java test exit if platform unsupported

OS=`uname -s`
case "$OS" in
  SunOS | Linux | *BSD | Darwin )
    FS="/"
    PS=":"
    CP="${FS}bin${FS}cp"
    CHMOD="${FS}bin${FS}chmod"
    ;;
  Windows* )
    FS="\\"
    PS=";"
    CP="cp"
    CHMOD="chmod"
    ;;
  CYGWIN* )
    FS="/"
    PS=";"
    CP="cp"
    CHMOD="chmod"
    #
    # javac does not like /cygdrive produced by `pwd`
    #
    TESTSRC=`cygpath -d ${TESTSRC}`
    ;;
  * )
    echo "Unrecognized system!"
    exit 1;
    ;;
esac

# compile test

${TESTJAVA}${FS}bin${FS}javac \
	-classpath ${TESTSRC}${FS}.. \
	-d ${TESTCLASSES} \
	${TESTSRC}${FS}ConfigQuotedString.java

# run test

${TESTJAVA}${FS}bin${FS}java \
	-classpath ${TESTCLASSES} \
	-DCUSTOM_P11_CONFIG=${TESTSRC}${FS}ConfigQuotedString-nss.txt \
	-Dtest.src=${TESTSRC} \
	-Dtest.classes=${TESTCLASSES} \
	ConfigQuotedString

# save error status
status=$?

# return
exit $status
