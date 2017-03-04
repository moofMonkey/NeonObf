package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.ListIterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * BETA
 + At moment (22.02.2017) this is abadoned module. I don't have enough time
 *
 * @author moofMonkey
 */
public class AntiMemoryDump extends Transformer {
	public AntiMemoryDump(MethodNode _mn) {
		super(_mn, null);
	}

	public AntiMemoryDump() {
		super(null, null);
	}

	@Override
	public void run() {
		ListIterator<AbstractInsnNode> iterator;
		AbstractInsnNode next;
		iterator = mn.instructions.iterator();
		while (iterator.hasNext()) {
			next = iterator.next();

			if (next instanceof FieldInsnNode) {
				if (((FieldInsnNode) next).owner.startsWith("java/"))
					continue;
			} else
				continue;

			FieldInsnNode fn = (FieldInsnNode) next;
			if (next.getOpcode() == GETFIELD) {
				
			}
			if (next.getOpcode() == PUTFIELD) {
				
			}
		}
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			for(MethodNode mn : cn.methods)
				new AntiMemoryDump(mn).start();

			classes.set(i, cn);
		}

		return classes;
	}
}
