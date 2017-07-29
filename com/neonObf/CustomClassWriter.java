package com.neonObf;


import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CustomClassWriter extends ClassWriter {
	private static Map<String, ClassTree> hierachy = new HashMap<>();

	public static void loadHierachy() {
		Set<String> processed = new HashSet<>();
		LinkedList<ClassNode> toLoad = new LinkedList<>();
		toLoad.addAll(Main.getInstance().nameToNode.values());
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

			for(String interfaceReference : (List<String>) specificNode.interfaces) {
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
		ClassNode clazz = Main.getInstance().nameToNode.get(ref);
		if (clazz == null) {
			for(Library lib : Main.getInstance().loadedAPI)
				if(lib.classNames.contains(ref)) {
					clazz = loadClass(getClass(lib.file, ref), lib.isLibrary ? ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES : ClassReader.SKIP_FRAMES);
					if(clazz != null) {
						if(!lib.isLibrary)
							Main.getInstance().classes.add(clazz);
						return clazz;
					}
				}
			throw new NoClassInPathException(ref);
		}
		return clazz;
	}

	public static InputStream getClass(File f, String className) {
		try {
			final ZipFile zipIn = new ZipFile(f);
			final Enumeration<? extends ZipEntry> e = zipIn.entries();
			ArrayList<String> classList = new ArrayList<>();
			while (e.hasMoreElements()) {
				final ZipEntry next = e.nextElement();
				String nextClassName = next.getName().replaceAll("(.*)\\.class", "$1");
				if (nextClassName.equals(className))
					return zipIn.getInputStream(next);
			}
			zipIn.close();
		} catch(Throwable t) {  }
		return null;
	}

	public static ClassNode loadClass(InputStream is, int mode) {
		try {
			final ClassReader reader = new ClassReader(is);
			final ClassNode node = new ClassNode();

			reader.accept(node, mode);
			for(int i = 0; i < node.methods.size(); ++i) {
				final MethodNode methodNode2 = (MethodNode) node.methods.get(i);
				final JSRInlinerAdapter adapter = new JSRInlinerAdapter(methodNode2, methodNode2.access, methodNode2.name, methodNode2.desc, methodNode2.signature, (String[]) methodNode2.exceptions.toArray(new String[0]));
				methodNode2.accept(adapter);
				node.methods.set(i, adapter);
			}

			Main.getInstance().nameToNode.put(node.name, node);
			Main.getInstance().nodeToName.put(node, node.name);

			return node;
		} catch(Throwable t) {
			t.printStackTrace();
			return null;
		}
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
