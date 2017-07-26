package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class CodeHider extends Transformer {
	public CodeHider(MethodNode _mn) {
		super(_mn, null);
	}

	FieldNode fn;

	public CodeHider(FieldNode _fn) {
		super(null, null);
		fn = _fn;
	}

	public CodeHider() {
		super(null, null);
	}

	@Override
	public void run() {
		if (fn == null && mn == null)
			return;

		if (fn != null) {
			if ((fn.access & ACC_SYNTHETIC) == 0)
				fn.access |= ACC_SYNTHETIC;
		} else
			if (mn != null) {
				if ((mn.access & ACC_SYNTHETIC) == 0)
					mn.access |= ACC_SYNTHETIC;
			}
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			ExecutorService service = Executors.newCachedThreadPool();
			for(MethodNode mn : (List<MethodNode>) cn.methods)
				service.submit(new CodeHider(mn));
			for(FieldNode fn : (List<FieldNode>) cn.fields)
				service.submit(new CodeHider(fn));

			service.shutdown();
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

			classes.set(i, cn);
		}

		return classes;
	}
}
