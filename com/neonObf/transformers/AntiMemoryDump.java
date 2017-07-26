package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			ExecutorService service = Executors.newCachedThreadPool();
			for(MethodNode mn : (List<MethodNode>) cn.methods)
				service.execute(new AntiMemoryDump(mn));

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			classes.set(i, cn);
		}

		return classes;
	}
}
