package com.neonObf;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;

public class DirWalker {
	public DirWalker(File file, boolean isLibrary) throws Throwable {
		Main inst = Main.getInstance();
		String name = file.getName();
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (file.listFiles() != null && !path.endsWith(".donot"))
				for(File f : file.listFiles())
					if (!(f.getName().charAt(0) == '.' || f.getName().charAt(0) == '$'))
						new DirWalker(f, isLibrary);
		} else
			if (file.isFile() && name.lastIndexOf('.') > -1) {
				if (name.endsWith("jar"))
					loadZIP(new File(path), isLibrary);
				if (name.endsWith("class"))
					loadClass(new File(path), isLibrary);
			}
	}

	public void loadClass(File f, boolean isLibrary) throws Throwable {
		Main inst = Main.getInstance();
		ClassLibrary lib = new ClassLibrary(f, isLibrary);
		if(isLibrary)
			; // classList.add(next.getName().replaceAll("(.*)\\.class", "$1")); # Now we can't use .class as library ._.
		else
			inst.classes.add(lib.classNode);
		inst.loadedAPI.add(lib);
	}

	public void loadZIP(File f, boolean isLibrary) throws Throwable {
		Main inst = Main.getInstance();
		final ZipFile zipIn = new ZipFile(f);
		final Enumeration<? extends ZipEntry> e = zipIn.entries();
		ArrayList<String> classList = new ArrayList<>();
		while (e.hasMoreElements()) {
			final ZipEntry next = e.nextElement();
			if (next.getName().endsWith(".class")) {
				if(isLibrary)
					classList.add(next.getName().replaceAll("(.*)\\.class", "$1"));
				else
					inst.classes.add(CustomClassWriter.loadClass(zipIn.getInputStream(next), ClassReader.SKIP_FRAMES));
			} else
				if (next.isDirectory())
					continue;
		}
		zipIn.close();
		inst.loadedAPI.add(new ZIPLibrary(f, classList, isLibrary));
	}
}
