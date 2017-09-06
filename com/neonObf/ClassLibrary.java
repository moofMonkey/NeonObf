package com.neonObf;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;

import static com.neonObf.CustomClassWriter.loadClass;

public class ClassLibrary implements Library {
	public final File file;
	public final ClassNode classNode;
	public final boolean isLibrary;

	public ClassLibrary(File file, boolean isLibrary) throws Throwable {
		this.file = file;
		this.isLibrary = isLibrary;
		this.classNode = loadClass (
				new FileInputStream(file),
				isLibrary
						? ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
						: ClassReader.SKIP_FRAMES
		); // There's no other way to detect class name :(
	}

	@Override
	public boolean isLibrary() {
		return isLibrary;
	}

	@Override
	public ClassNode getClassNode(String name) {
		if(!classNode.name.equals(name))
			return null;
		return classNode;
	}
}
