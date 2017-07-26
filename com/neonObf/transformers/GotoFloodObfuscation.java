package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			ExecutorService service = Executors.newCachedThreadPool();
			for(MethodNode mn : (List<MethodNode>) cn.methods)
				service.execute(new GotoFloodObfuscation(mn));

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			classes.set(i, cn);
		}

		return classes;
	}
}
