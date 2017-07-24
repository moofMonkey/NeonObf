package com.neonObf.transformers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import com.neonObf.Main;
import com.neonObf.SmartNameGen;

public class LocalVariableNameObfuscator extends Transformer {
	public SmartNameGen nameGen = Main.getInstance().nameGen;
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
					nameGen.get(i),
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
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) {
		for(int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);

			for(MethodNode mn : (List<MethodNode>) cn.methods)
				new LocalVariableNameObfuscator(mn).start();

			classes.set(i, cn);
		}

		return classes;
	}
}
