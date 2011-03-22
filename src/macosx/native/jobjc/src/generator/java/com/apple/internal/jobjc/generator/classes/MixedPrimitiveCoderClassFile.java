/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */
package com.apple.internal.jobjc.generator.classes;

import java.io.PrintStream;
import java.util.Collection;

import com.apple.internal.jobjc.generator.ClassGenerator;
import com.apple.internal.jobjc.generator.model.coders.ComplexCoderDescriptor.MixedEncodingDescriptor;
import com.apple.jobjc.PrimitiveCoder;

public class MixedPrimitiveCoderClassFile extends GeneratedClassFile {
	private static final String MULTI_CODER_CLASSNAME = "MixedPrimitiveCoder";
	public static final String FULL_MULTI_CODER_CLASSNAME = ClassGenerator.JOBJC_PACKAGE + "." + MULTI_CODER_CLASSNAME;

	final Collection<MixedEncodingDescriptor> coderDescs;

	public MixedPrimitiveCoderClassFile(final Collection<MixedEncodingDescriptor> coderDescs) {
		super(ClassGenerator.JOBJC_PACKAGE, MULTI_CODER_CLASSNAME, "java.lang.Object");
		this.coderDescs = coderDescs;
	}

	@Override
	public void writeBody(final PrintStream out) {
		for (final MixedEncodingDescriptor desc : coderDescs) {
			out.println("\tpublic static final " + PrimitiveCoder.class.getCanonicalName() + " " + desc.getMixedName() + " = " + desc.getDefinition() + ";");
		}
	}
}
