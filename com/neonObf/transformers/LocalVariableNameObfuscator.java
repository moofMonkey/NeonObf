package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.neonObf.Main;
import com.neonObf.SmartNameGen;

public class LocalVariableNameObfuscator extends Transformer {
	HashMap<String, Boolean> hm = new HashMap<String, Boolean>();

	public LocalVariableNameObfuscator(MethodNode _mn) {
		super(_mn, null);
	}

	public LocalVariableNameObfuscator() {
		super(null, null);
	}

	@Override
	public void run() {
		if (mn.localVariables == null)
			return;
		for(int i = 0; i < mn.localVariables.size(); i++) {
			LocalVariableNode lvn = (LocalVariableNode) mn.localVariables.get(i);

			mn.localVariables.set (
				i,
				new LocalVariableNode (
					Main.getInstance().nameGen.get(i),
					lvn.desc,
					null,
					lvn.start,
					lvn.end,
					i
				)
			);
			hm.put(lvn.desc, Boolean.TRUE);
		}
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			ExecutorService service = Executors.newCachedThreadPool();
			for(MethodNode mn : (List<MethodNode>) cn.methods)
				service.execute(new LocalVariableNameObfuscator(mn));

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			classes.set(i, cn);
		}

		return classes;
	}
}
