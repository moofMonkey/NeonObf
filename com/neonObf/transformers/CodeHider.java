package com.neonObf.transformers;

import java.util.ArrayList;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class CodeHider extends Transformer {
	public CodeHider(MethodNode _mn) {
		super(_mn, null);
	}
	
	FieldNode fn;
	
	public CodeHider(FieldNode _fn) {
		super(null, null);
		fn = _fn;
	}
	
	public CodeHider() { super(null, null); }
	
	@Override
	public void run() {
		if(fn == null && mn == null)
			return;
		
		if(mn == null) {
			if((fn.access & ACC_SYNTHETIC) == 0)
				fn.access |= ACC_SYNTHETIC;
		} else {
			if((mn.access & ACC_SYNTHETIC) == 0)
				mn.access |= ACC_SYNTHETIC;
		}
	}
	
	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for (int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);
			
			for (MethodNode mn : cn.methods)
				new CodeHider(mn).start();
			for (FieldNode fn : cn.fields)
				new CodeHider(fn).start();
			
			classes.set(i, cn);
		}
		
		return classes;
	}
}
