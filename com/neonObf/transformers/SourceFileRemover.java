package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.objectweb.asm.tree.ClassNode;

import com.neonObf.Main;
import com.neonObf.SmartNameGen;

public class SourceFileRemover extends Transformer {
	public SmartNameGen nameGen = Main.getInstance().nameGen;
	
	public SourceFileRemover(ClassNode _parent) {
		super(null, _parent);
	}

	public SourceFileRemover() {
		super(null, null);
	}

	@Override
	public void run() { parent.sourceFile = nameGen.get(rand.nextInt(100)); }

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		ExecutorService service = Executors.newCachedThreadPool();
		classes.parallelStream().forEach((cn) -> {
			service.execute(new SourceFileRemover(cn));
		});

		service.shutdown();
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch(Throwable t) {
			t.printStackTrace();
		}

		return classes;
	}
}
