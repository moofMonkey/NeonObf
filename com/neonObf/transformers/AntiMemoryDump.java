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
 + At moment (22.02.2017) this is abandoned module. I don't have enough time
 *
 * @author moofMonkey
 */
@Deprecated
public class AntiMemoryDump extends Transformer {
	@Deprecated
	public AntiMemoryDump(MethodNode _mn) {
		super(_mn, null);
	}

	@Deprecated
	public AntiMemoryDump() {
		super(null, null);
	}

	@Deprecated
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

	@Deprecated
	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		classes.parallelStream().forEach((cn) -> {
			ExecutorService service = Executors.newCachedThreadPool();
			((List<MethodNode>) cn.methods).parallelStream().forEach(mn -> service.execute(new AntiMemoryDump(mn)));

			service.shutdown();
			try {
				service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
			} catch(Throwable t) {
				t.printStackTrace();
			}
		});

		return classes;
	}
}
