package com.neonObf;


import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class CustomClassWriter extends ClassWriter {
	private static Map<String, ClassTree> hierachy = new HashMap<>();

	public static void loadHierachy() {
		Set<String> processed = new HashSet<>();
		LinkedList<ClassNode> toLoad = new LinkedList<>();
		toLoad.addAll(Main.getInstance().hmSCN.values());
		while (!toLoad.isEmpty()) {
			ClassNode poll = toLoad.poll();

			for(ClassNode toProcess : loadHierachy(poll)) {
				if (processed.add(toProcess.name)) {
					toLoad.add(toProcess);
				}
			}
		}
	}

	public static List<ClassNode> loadHierachy(ClassNode specificNode) {
		try {
			if (specificNode.name.equals("java/lang/Object")) {
				return Collections.emptyList();
			}
			if ((specificNode.access & Opcodes.ACC_INTERFACE) != 0) {
				getClassTree(specificNode.name).parentClasses.add("java/lang/Object");
				return Collections.emptyList();
			}
			List<ClassNode> toProcess = new ArrayList<>();

			ClassTree thisTree = getClassTree(specificNode.name);
			ClassNode superClass = null;
			superClass = assureLoaded(specificNode.superName);
			if (superClass == null) {
				throw new IllegalArgumentException("Could not load " + specificNode.name);
			}
			ClassTree superTree = getClassTree(superClass.name);
			superTree.subClasses.add(specificNode.name);
			thisTree.parentClasses.add(superClass.name);
			toProcess.add(superClass);

			for(String interfaceReference : specificNode.interfaces) {
				ClassNode interfaceNode = assureLoaded(interfaceReference);
				if (interfaceNode == null) {
					throw new IllegalArgumentException("Could not load "
							+ interfaceReference);
				}
				ClassTree interfaceTree = getClassTree(interfaceReference);
				interfaceTree.subClasses.add(specificNode.name);
				thisTree.parentClasses.add(interfaceReference);
				toProcess.add(interfaceNode);
			}
			return toProcess;
		} catch(Throwable t) {
			System.err.println(specificNode.name);
			throw t;
		}
	}

	public CustomClassWriter(int flags) {
		super(flags);
	}

	public static ClassNode assureLoaded(String ref) {
		ClassNode clazz = Main.getInstance().hmSCN.get(ref);
		if (clazz == null) {
			throw new NoClassInPathException(ref);
		}
		return clazz;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {
		String a = getCommonSuperClass1(type1, type2);
		return a;
	}

	private String getCommonSuperClass0(String type1, String type2) {
		ClassNode first = assureLoaded(type1);
		ClassNode second = assureLoaded(type2);
		if (isAssignableFrom(type1, type2))
			return type1;
		else
			if (isAssignableFrom(type2, type1))
				return type2;
			else
				if (Modifier.isInterface(first.access)
						|| Modifier.isInterface(second.access))
					return "java/lang/Object";
				else {
					do {
						type1 = first.superName;
						first = assureLoaded(type1);
					} while (!isAssignableFrom(type1, type2));
					return type1;
				}
	}

	private String getCommonSuperClass1(String type1, String type2) {
		if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object"))
			return "java/lang/Object";
		String a = getCommonSuperClass0(type1, type2);
		String b = getCommonSuperClass0(type2, type1);
		if (!a.equals("java/lang/Object"))
			return a;
		if (!b.equals("java/lang/Object"))
			return b;
		ClassNode first = assureLoaded(type1);
		ClassNode second = assureLoaded(type2);
		return getCommonSuperClass(first.superName, second.superName);
	}

	private boolean isAssignableFrom(String type1, String type2) {
		if (type1.equals("java/lang/Object"))
			return true;
		if (type1.equals(type2))
			return true;
		assureLoaded(type1);
		assureLoaded(type2);
		ClassTree firstTree = getClassTree(type1);
		Set<String> allChilds1 = new HashSet<>();
		LinkedList<String> toProcess = new LinkedList<>();
		toProcess.addAll(firstTree.subClasses);
		while (!toProcess.isEmpty()) {
			String s = toProcess.poll();
			if (allChilds1.add(s)) {
				assureLoaded(s);
				ClassTree tempTree = getClassTree(s);
				toProcess.addAll(tempTree.subClasses);
			}
		}
		if (allChilds1.contains(type2))
			return true;
		return false;
	}

	public static ClassTree getClassTree(String classNode) {
		ClassTree tree = hierachy.get(classNode);
		if (tree == null) {
			tree = new ClassTree();
			tree.thisClass = classNode;
			hierachy.put(classNode, tree);
		}
		return tree;
	}
}
