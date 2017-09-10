package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

public class LineNumberObfuscation extends Transformer {
	public LineNumberObfuscation(MethodNode _mn) {
		super(_mn, null);
	}

	public LineNumberObfuscation() {
		super(null, null);
	}

	@Override
	public void run() {
		try {
			ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
			AbstractInsnNode next;
			//TODO: implement stream
			while (iterator.hasNext()) {
				next = iterator.next();

				if (next instanceof LineNumberNode)
					iterator.set (
						new LineNumberNode (
							rand.nextInt(),
							((LineNumberNode) next).start
						)
					);
			}
		} catch(Throwable t) {
		}
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		classes.parallelStream().forEach((cn) -> {
			ExecutorService service = Executors.newCachedThreadPool();
			((List<MethodNode>) cn.methods).parallelStream().forEach(mn -> service.execute(new LineNumberObfuscation(mn)));

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
