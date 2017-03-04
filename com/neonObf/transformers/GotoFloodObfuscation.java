package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.ListIterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

public class GotoFloodObfuscation extends Transformer {
	public GotoFloodObfuscation(MethodNode _mn) {
		super(_mn, null);
	}

	public GotoFloodObfuscation() {
		super(null, null);
	}

	@Override
	public void run() { // TODO: Randomly throw pieces of code and join it with
						// gotos
		ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
		AbstractInsnNode next;
		while (iterator.hasNext()) {
			next = iterator.next();

			LabelNode l;
			if (next.getOpcode() != GOTO && !(next instanceof LabelNode)) {
				l = new LabelNode();
				iterator.add(new JumpInsnNode(GOTO, l));
				iterator.add(l);
			}
		}
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			for(MethodNode mn : cn.methods)
				new GotoFloodObfuscation(mn).start();

			classes.set(i, cn);
		}

		return classes;
	}
}
