package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FinalRemover extends Transformer {
	public FinalRemover() {
		super(null, null);
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		classes.parallelStream().forEach(cn -> ((List<FieldNode>) cn.fields).parallelStream().forEach((fn) -> {
			if ((fn.access | ACC_FINAL) != 0)
				fn.access &= ~ACC_FINAL;
		}));

		return classes;
	}
}
