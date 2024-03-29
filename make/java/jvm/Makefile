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

BUILDDIR = ../..

include $(BUILDDIR)/common/Defs.gmk

#
# Make the directories of include files shipped with the JDK.
#

FILES_h = $(INCLUDEDIR)/jni.h 			\
	  $(PLATFORM_INCLUDE)/jni_md.h		\
	  $(INCLUDEDIR)/jvmti.h			\
	  $(INCLUDEDIR)/jvmticmlr.h		\
	  $(INCLUDEDIR)/classfile_constants.h

$(INCLUDEDIR)/%.h: $(SHARE_SRC)/javavm/export/%.h
	$(install-file)

$(PLATFORM_INCLUDE)/%.h: $(PLATFORM_SRC)/javavm/export/%.h
	$(install-file)

ifeq ($(PLATFORM),macosx)
  JVMCFG_DIR = $(LIBDIR)
else
  JVMCFG_DIR = $(LIBDIR)/$(LIBARCH)
endif

JVMCFG = $(JVMCFG_DIR)/jvm.cfg

#
# How to install jvm.cfg.
#
ifeq ($(ZERO_BUILD), true)
JVMCFG_ARCH = zero
else
JVMCFG_ARCH = $(ARCH)
endif

ifdef BUILD_CLIENT_ONLY
$(JVMCFG)::
	$(MKDIR) -p $(JVMCFG_DIR)
	@# Update jvm.cfg to use -client by default and alias -server to -client
	$(RM) -f $(JVMCFG)
	$(ECHO) "-client KNOWN">$(JVMCFG)
	$(ECHO) "-server ALIASED_TO -client">>$(JVMCFG)
	$(ECHO) "-hotspot ALIASED_TO -client">>$(JVMCFG)
	$(ECHO) "-classic WARN">>$(JVMCFG)
	$(ECHO) "-native ERROR">>$(JVMCFG)
	$(ECHO) "-green ERROR">>$(JVMCFG)
else
$(JVMCFG): $(PLATFORM_SRC)/bin/$(JVMCFG_ARCH)/jvm.cfg
	$(install-file)
endif

all: build

build: $(FILES_h) $(JVMCFG)

clean clobber:: 
	$(RM) $(FILES_h) $(JVMCFG)

