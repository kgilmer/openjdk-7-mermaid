/*
 * Copyright 1999 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

#ifndef _JAVASOFT_BSD_LARGEFILE_SUPPORT_H_
#define _JAVASOFT_BSD_LARGEFILE_SUPPORT_H_

#include <sys/types.h>
#include <sys/stat.h>

/* define compatibility macros */
#define                off64_t         off_t
#define                stat64          stat

#define                F_SETLK64       F_SETLK
#define                F_SETLKW64      F_SETLKW

#define                lseek64         lseek
#define                ftruncate64     ftruncate
#define                open64          open
#define                fstat64         fstat
#define                flock64         flock
#define                mmap64          mmap
#define                fdatasync       fsync

#define                pread64         pread
#define                pwrite64        pwrite

#endif /* _JAVASOFT_BSD_LARGEFILE_SUPPORT_H_ */
