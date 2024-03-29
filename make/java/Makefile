#
# Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
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

#
# Makefile for building all of java
#

PRODUCT=java
BUILDDIR=..
include $(BUILDDIR)/common/Defs.gmk

#
# The order of subdirs here is important
#
SUBDIRS += version jvm redist verify fdlibm java sun_nio jli main zip

# Others
#    Note: java_crw_demo java_hprof_demo are demos but must be delivered built in sdk

SUBDIRS += security math util text net nio jar

SUBDIRS_desktop    = awt applet beans
SUBDIRS_management = management
SUBDIRS_misc       = npt java_crw_demo java_hprof_demo \
                     logging instrument invoke sql rmi


ifeq ($(PLATFORM), solaris)
  ifeq ($(ARCH_DATA_MODEL), 32)
    SUBDIRS += jexec
  endif
endif # PLATFORM

ifneq (,$(findstring $(PLATFORM), linux bsd))
  SUBDIRS += jexec
endif # PLATFORM

ifeq ($(PLATFORM), macosx)
  SUBDIRS += jobjc
endif # PLATFORM

include $(BUILDDIR)/common/Subdirs.gmk

all build clean clobber::
	$(SUBDIRS-loop)

