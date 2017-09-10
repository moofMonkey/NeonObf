package com.neonObf.transformers;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

public class TryCatch extends Transformer {
	public TryCatch(MethodNode _mn) {
		super(_mn, null);
	}
	
	public TryCatch() { super(null, null); }
	
	LabelNode tryStart,
			tryFinalStart, // At end must jump over catch block (goto tryCatchEnd)
			tryCatchStart,
			tryCatchEnd; // When exception was throw'd, exception places into stack,
						// and finally does not calling => no jumping over catch => exception processing by catch
	@Override
	public void run() {
		if(mn.name.startsWith("<") && mn.name.endsWith(">")) // There are error if we are trying to add that try/catch in static
			return;
		
		ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
		AbstractInsnNode next;
		while(iterator.hasNext()) {
			next = iterator.next();
			
			if(next.getOpcode() != GOTO && (next instanceof MethodInsnNode)) {
				tryStart = new LabelNode();
				tryFinalStart = new LabelNode();
				tryCatchStart = new LabelNode();
				tryCatchEnd = new LabelNode();
				
				iterator.previous();
				iterator.add(tryStart); // Auto iterator.next()
				iterator.next();
				iterator.add(tryFinalStart);
				{
				iterator.add (
					new JumpInsnNode (
						GOTO,
						tryCatchEnd
					)
				);
				}
				iterator.add(tryCatchStart);
				iterator.add (
					new InsnNode (
						ATHROW
					)
				);
				iterator.add(tryCatchEnd);
				mn.tryCatchBlocks.add(new TryCatchBlockNode(tryStart, tryFinalStart, tryCatchStart, "java/lang/Exception"));
			}
		}
	}
	
	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		classes.parallelStream().forEach(cn -> {
			ExecutorService service = Executors.newCachedThreadPool();
			((List<MethodNode>) cn.methods).parallelStream().forEach(mn -> service.execute(new TryCatch(mn)));

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
