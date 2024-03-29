#
# Copyright (c) 2005, 2011, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
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

BUILDDIR = ../../../..
PACKAGE = sun.security.jgss.wrapper
PRODUCT = sun

LIBRARY = j2gss
# Force name of temp directory (needed due to LIBRARY change below)
LIBRARY_OUTPUT = j2gss

include $(BUILDDIR)/common/Defs.gmk

#
# C and Java Files
#
include FILES_c.gmk

AUTO_FILES_JAVA_DIRS = sun/security/jgss/wrapper

#
# Java files that define native methods
#
FILES_export = \
    sun/security/jgss/wrapper/GSSLibStub.java

#
# Find native code
#
vpath %.c \
  $(SHARE_SRC)/native/sun/security/jgss/wrapper \
  $(PLATFORM_SRC)/native/sun/security/jgss/wrapper

#
# Find include files
#
OTHER_INCLUDES += \
  -I$(SHARE_SRC)/native/sun/security/jgss/wrapper \
  -I$(PLATFORM_SRC)/native/sun/security/jgss/wrapper

#
# Rules
#
#FILES_class = $(FILES_java:%.java=$(CLASSDESTDIR)/%.class)

include $(BUILDDIR)/common/Mapfile-vers.gmk
include $(BUILDDIR)/common/Library.gmk

#
# Libraries to link
#
ifeq ($(PLATFORM), windows)
OTHER_LDLIBS = $(LIBDL) $(JVMLIB)
endif
