package com.neonObf.transformers;

import java.util.ArrayList;
import java.util.ListIterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

public class LineNumberObfuscation extends Transformer {
	public LineNumberObfuscation(MethodNode _mn) {
		super(_mn, null);
	}
	
	public LineNumberObfuscation() { super(null, null); }
	
	@Override
	public void run() {
		try {
			ListIterator<AbstractInsnNode> iterator;
			AbstractInsnNode next;
			iterator = mn.instructions.iterator(0);
			while(iterator.hasNext()) {
				next = iterator.next();
				
				if(next instanceof LineNumberNode)
					iterator.set(new LineNumberNode(rand.nextInt(), ((LineNumberNode) next).start));
			}
		} catch(Throwable t) {  }
	}
	
	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for (int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);
			
			for (MethodNode mn : cn.methods)
				new LineNumberObfuscation(mn).start();
			
			classes.set(i, cn);
		}
		
		return classes;
	}
}
