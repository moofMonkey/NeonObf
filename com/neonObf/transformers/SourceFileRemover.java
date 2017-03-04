package com.neonObf.transformers;


import java.util.ArrayList;

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
	public void run() {
		parent.sourceFile = nameGen.get(rand.nextInt());
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			new SourceFileRemover(cn).start();

			classes.set(i, cn);
		}

		return classes;
	}
}
