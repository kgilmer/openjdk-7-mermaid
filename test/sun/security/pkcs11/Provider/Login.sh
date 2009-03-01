#
# Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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
# Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
# CA 95054 USA or visit www.sun.com if you need additional information or
# have any questions.
#

# @test
# @bug 4850423
# @summary login facilities for hardware tokens
#
# @run shell Login.sh

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
esac

# first make cert/key DBs writable

${CP} ${TESTSRC}${FS}..${FS}nss${FS}db${FS}cert8.db ${TESTCLASSES}
${CHMOD} +w ${TESTCLASSES}${FS}cert8.db

${CP} ${TESTSRC}${FS}..${FS}nss${FS}db${FS}key3.db ${TESTCLASSES}
${CHMOD} +w ${TESTCLASSES}${FS}key3.db

# compile test

${TESTJAVA}${FS}bin${FS}javac \
	-classpath ${TESTSRC}${FS}.. \
	-d ${TESTCLASSES} \
	${TESTSRC}${FS}Login.java

# run test

${TESTJAVA}${FS}bin${FS}java \
	-classpath ${TESTCLASSES} \
	-DCUSTOM_DB_DIR=${TESTCLASSES} \
	-DCUSTOM_P11_CONFIG=${TESTSRC}${FS}Login-nss.txt \
	-DNO_DEFAULT=true \
	-DNO_DEIMOS=true \
	-Dtest.src=${TESTSRC} \
	-Dtest.classes=${TESTCLASSES} \
	-Djava.security.manager \
	-Djava.security.policy=${TESTSRC}${FS}Login.policy \
	-Djava.security.debug=${DEBUG} \
	Login

# save error status
status=$?

# return
exit $status
