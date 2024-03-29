/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.apple.internal.jobjc.generator.classes;

import java.io.*;

public class CopiedFile extends OutputFile {
    final File sourceFile;
    
    public CopiedFile(final File sourceFile, final String pkg, final String filename) {
        super(pkg, filename);
        this.sourceFile = sourceFile;
    }
    
    @Override
    public void write(final File parentDir) {
        try {
            final PrintStream out = open(parentDir);
            final InputStream in = new FileInputStream(sourceFile);
            
            copy(in, out);
            close(out);
        } catch (final IOException e) { throw new RuntimeException(e); }
    }
    
    private static void copy(final InputStream in, final PrintStream out) throws IOException {
        int bit;
        while (-1 != (bit = in.read())) {
            out.write(bit);
        }
    }
}
