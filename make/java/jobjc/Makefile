#
# Copyright (c) 1998, 2005, Oracle and/or its affiliates. All rights reserved.
# ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#
#

#
# Makefile for building jobjc

BUILDDIR = ../..
include $(BUILDDIR)/common/Defs.gmk
SRCDIR = $(JDK_TOPDIR)/src/macosx/native/jobjc

ifeq ($(PLATFORM),macosx)

all:
	echo running ant with java_home set to ${ALT_BOOTDIR}
	(cd $(SRCDIR); OBJROOT="$(OUTPUTDIR)/JObjC.build" DSTROOT="$(OUTPUTDIR)/JObjC.dst" JAVA_HOME=${ALT_BOOTDIR} /usr/bin/ant -verbose all)
	$(CP) $(OUTPUTDIR)/JObjC.dst/Debug/libJObjC.dylib $(LIB_LOCATION)/libJObjC.dylib

clean clobber::
	(cd $(SRCDIR); export OBJROOT=$(OUTPUTDIR)/JObjC.build; export DSTROOT=$(OUTPUTDIR)/JObjC.dst; /usr/bin/ant clean)

endif
