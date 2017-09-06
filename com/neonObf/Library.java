package com.neonObf;

import org.objectweb.asm.tree.ClassNode;

public interface Library {
	ClassNode getClassNode(String name);

	boolean isLibrary();
}
