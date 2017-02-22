package com.neonObf.transformers;

import java.util.ArrayList;

import org.objectweb.asm.tree.ClassNode;

public class SourceFileRemover extends Transformer {
	public SourceFileRemover(ClassNode _parent) {
		super(null, _parent);
	}
	
	public SourceFileRemover() { super(null, null); }
	
	@Override
	public void run() {
		parent.sourceFile = "SourceFile";
	}
	
	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for (int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);
			
			new SourceFileRemover(cn).start();
			
			classes.set(i, cn);
		}
		
		return classes;
	}
}
