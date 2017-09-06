package com.neonObf;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.util.ArrayList;
import static com.neonObf.CustomClassWriter.loadClass;

public class ZIPLibrary implements Library {
    public final File file;
    public final ArrayList<String> classNames;
    public final boolean isLibrary;

    public ZIPLibrary(File file, ArrayList<String> classNames, boolean isLibrary) {
        this.file = file;
        this.classNames = classNames;
        this.isLibrary = isLibrary;
    }

	@Override
	public boolean isLibrary() {
		return isLibrary;
	}

	@Override
    public ClassNode getClassNode(String name) {
		if(!classNames.contains(name))
			return null;
    	return loadClass (
    			CustomClassWriter.getClass(file, name),
				isLibrary
						? ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
						: ClassReader.SKIP_FRAMES
		);
	}
}
