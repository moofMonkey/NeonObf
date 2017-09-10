package com.neonObf;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;

public class DirWalker {
	public DirWalker(File file, boolean isLibrary) {
		String name = file.getName();
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (file.listFiles() != null && !path.endsWith(".donot"))
				Arrays.stream(file.listFiles())
						.filter(f -> f.isDirectory() || !(!(f.getName().endsWith(".class") || f.getName().endsWith(".jar") || f.getName().endsWith(".zip")) || f.getName().charAt(0) == '.' || f.getName().charAt(0) == '$' || f.getName().equals("module-info.class")))
						.forEach(f -> {
							try {
								new DirWalker(f, isLibrary);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						});
		} else {
			if (file.isFile() && name.lastIndexOf('.') > -1) {
				name = name.toLowerCase();
				if(name.endsWith(".jar") || name.endsWith(".zip"))
					try {
						loadZIP(file, isLibrary);
					} catch(Throwable t) {
						new Throwable("Failed to load JAR @ " + path, t).printStackTrace();
					}
				if(name.endsWith(".class"))
					try {
						loadClass(file, isLibrary);
					} catch(Throwable t) {
						new Throwable("Failed to load class @ " + path, t).printStackTrace();
					}
			}
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
		ArrayList<String> classList = new ArrayList<>();
		Collections.list(zipIn.entries())
				.stream()
				.filter(next -> (next.getName().endsWith(".class") && !next.getName().equals("module-info.class")))
				.forEach(next -> {
					if (isLibrary)
						classList.add(next.getName().replaceAll("(.*)\\.class", "$1"));
					else
						try {
							inst.classes.add(CustomClassWriter.loadClass(zipIn.getInputStream(next), ClassReader.SKIP_FRAMES));
						} catch(Throwable t) {
							t.printStackTrace();
						}
				});
		zipIn.close();
		inst.loadedAPI.add(new ZIPLibrary(f, classList, isLibrary));
	}
}
