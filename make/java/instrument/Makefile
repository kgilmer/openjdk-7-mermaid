#
# Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
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

# Makefile for building the Java Programming Language Instrumentation Services
# agent, supporting java.lang.instrument

BUILDDIR = ../..
PACKAGE = sun.instrument
LIBRARY = instrument
PRODUCT = sun

# Configure the CFLAGS for this library.  Use static binding so that there
# are not dependencies on modules not on the search patch when invoked from
# the Windows system directory (or elsewhere).
MS_RUNTIME_STATIC=true

FILES_m = mapfile-vers

include $(BUILDDIR)/common/Defs.gmk

# Use the mapfile-vers (See the mapfile located with this Makefile)
ifdef FILES_m
include $(BUILDDIR)/common/Mapfile-vers.gmk
endif

#
# Files to compile.
#
FILES_c = \
	EncodingSupport.c \
	EncodingSupport_md.c \
	FileSystemSupport_md.c \
	InstrumentationImplNativeMethods.c \
	InvocationAdapter.c \
	JarFacade.c \
	JPLISAgent.c \
	JPLISAssert.c \
	JavaExceptions.c \
	PathCharsValidator.c \
	Reentrancy.c \
	Utilities.c

#
# -jaragent support requires zip and jar manfiest parser to be compiled
# into the instrument library.
#
IO_PLATFORM_SRC = $(PLATFORM_SRC)/native/java/io
LAUNCHER_SHARE_SRC = $(SHARE_SRC)/bin
LAUNCHER_PLATFORM_SRC = $(PLATFORM_SRC)/bin

FILES_c += \
	canonicalize_md.c

FILES_export = \
	sun/instrument/InstrumentationImpl.java

#
# This controls the ability to do logging in the library.
#
CPPFLAGS_DBG += -DJPLIS_LOGGING
CPPFLAGS_OPT += -DNO_JPLIS_LOGGING

OTHER_INCLUDES = -I$(SHARE_SRC)/instrument
OTHER_INCLUDES += -I$(PLATFORM_SRC)/instrument
OTHER_INCLUDES += -I$(IO_PLATFORM_SRC)
OTHER_INCLUDES += -I$(LAUNCHER_SHARE_SRC) -I$(LAUNCHER_PLATFORM_SRC)

#
# Create a dependency on libjli (Java Launcher Infrastructure)
#
# On UNIX, this is a relative dependency using $ORIGIN. Unfortunately, to
# do this reliably on Linux takes a different syntax than Solaris.
#
# On Windows, this is done by using the same directory as the executable
# itself, as with all the Windows libraries.
#
ifeq ($(PLATFORM), windows)
  OTHER_LDLIBS += $(OUTPUTDIR)/tmp/java/jli/$(OBJDIRNAME)/static/jli.lib
  OTHER_LCF += -export:Agent_OnAttach
  # equivalent of strcasecmp is stricmp on Windows
  CPPFLAGS_COMMON += -Dstrcasecmp=stricmp
else
ifneq (,$(findstring $(PLATFORM), bsd macosx))
  ifeq ($(OS_VENDOR), Apple)
    ifneq ($(ARCH), universal)
    LDFLAGS += -Wl,-all_load
    endif
    LDFLAGS += $(OUTPUTDIR)/tmp/java/jli/$(OBJDIRNAME)/static/libjli.a
    OTHER_LDLIBS += -liconv
  else
    LDFLAGS += -Wl,--whole-archive
    LDFLAGS += $(OUTPUTDIR)/tmp/java/jli/$(OBJDIRNAME)/static/libjli.a
    LDFLAGS += -Wl,--no-whole-archive
    ifneq ($(OS_NAME), netbsd)
# Use CPPFLAGS instead of OTHER_INCLUDES to force this last
      CPPFLAGS += -I$(PACKAGE_PATH)/include
      OTHER_LDLIBS += -L$(PACKAGE_PATH)/lib -liconv
    endif
  endif

  ifeq ($(SYSTEM_ZLIB), true)
    OTHER_LDLIBS += -lz
  endif
else
  LDFLAGS += -L $(LIBDIR)/$(LIBARCH)/jli
  OTHER_LDLIBS += -ljli
  OTHER_LDLIBS += $(LIBDL)
  ifeq ($(PLATFORM), solaris)
    LDFLAGS += -R \$$ORIGIN/jli
  endif
  ifeq ($(PLATFORM), linux)
    LDFLAGS += $(LDFLAG_Z_ORIGIN)
    LDFLAGS += -Wl,--allow-shlib-undefined
    LDFLAGS += -Wl,-rpath -Wl,\$$ORIGIN/jli
  endif
endif
endif

ifeq ($(PLATFORM), macosx)
  LDFLAGS += -framework Cocoa -framework Security -framework ApplicationServices
endif

#
# Library to compile.
#
include $(BUILDDIR)/common/Library.gmk

# We don't want to link against -ljava
JAVALIB=

#
# Add to ambient vpath so we pick up the library files
#
vpath %.c $(SHARE_SRC)/instrument $(PLATFORM_SRC)/instrument
vpath %.c $(IO_PLATFORM_SRC)

