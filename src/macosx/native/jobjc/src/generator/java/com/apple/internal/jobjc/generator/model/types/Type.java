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
package com.apple.internal.jobjc.generator.model.types;

import com.apple.internal.jobjc.generator.model.types.NType.NPointer;
import com.apple.internal.jobjc.generator.model.types.NType.NVoid;
import com.apple.internal.jobjc.generator.utils.NTypeMerger;
import com.apple.internal.jobjc.generator.utils.QA;
import com.apple.internal.jobjc.generator.utils.Fp.Pair;
import com.apple.internal.jobjc.generator.utils.NTypeMerger.MergeFailed;

public class Type implements Comparable<Type>{
	public static Type VOID = Type.getType("void", NVoid.inst(), null);
	public static Type VOID_PTR = Type.getType("void*", new NPointer(NVoid.inst()), null);

	final public String name;
	final public NType type32;
	final public NType type64;
	
	public static Type getType(final String name, final NType t32, final NType t64){
		return TypeCache.inst().pingType(new Type(name, t32, t64));
	}

	private Type(final String name, final NType t32, final NType t64) {
		QA.nonNull(t32);
		this.name = cleanName(name);
		this.type32 = t32;
		this.type64 = t64 == null || t32.equals(t64) ? t32 : t64;
	}

	private JType _getJType;
	public JType getJType() {
		return _getJType!=null ? _getJType : (_getJType = TypeToJType.inst().getJTypeFor(TypeCache.inst().pingType(this)));
	}

	private String _toString;
	@Override public String toString() {
		return _toString != null ? _toString : (_toString = name + " " + new Pair(type32, type64).toString());
	}

	@Override public boolean equals(Object o){
		if(o==null || !(o instanceof Type)) return false;
		Type t = (Type) o;
		return QA.bothNullOrEquals(t.name, this.name)
		&& t.type32.equals(this.type32)
		&& t.type64.equals(this.type64);
	}

	@Override public int hashCode(){
		return (name == null ? 0 : name.hashCode())
		+ type32.hashCode() + type64.hashCode();
	}

	public int compareTo(Type o) { return toString().compareTo(o.toString()); }

	public static Type merge(Type a, Type b) throws MergeFailed{
		if(a!=null && b==null) return a;
		if(a==null && b!=null) return b;
		if(QA.bothNullOrEquals(a, b)) return a;
		// HACK BS bug where OSStatus is (l / i) in some spots and just (l) (and thus (l / l)) in others
		if("OSStatus".equals(a.name)) return a;
		if(a.name != null && b.name != null && !a.name.equals(b.name)){
			System.out.println("Merging:");
			System.out.println("\ta.....: " + a.toString());
			System.out.println("\tb.....: " + b.toString());
		}
		final Type merged = new Type(NTypeMerger.inst().mergeName(a.name, b.name),
				NTypeMerger.inst().merge(a.type32, b.type32),
				NTypeMerger.inst().merge(a.type64, b.type64));
		if(a.name != null && b.name != null && !a.name.equals(b.name)){
			System.out.println("\tmerged: " + merged.toString());
		}
		return merged;
	}

	// HACK BS bug where sometimes the name is declared as "id <A, B..." and sometimes it's "id<A,B..."
	public static String cleanName(String name){ return name == null ? null : name.replaceAll("\\s+", ""); }
}
