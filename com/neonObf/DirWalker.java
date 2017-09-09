package com.neonObf;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;

public class DirWalker {
	public DirWalker(File file, boolean isLibrary) throws Throwable {
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
					if(!name.equals("module-info.class"))
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
		ArrayList<String> classList = new ArrayList<>();
		Collections.list(zipIn.entries()).forEach((next) -> {
			try {
				if (next.getName().endsWith(".class") && !next.getName().equals("module-info.class"))
					if (isLibrary)
						classList.add(next.getName().replaceAll("(.*)\\.class", "$1"));
					else
						inst.classes.add(CustomClassWriter.loadClass(zipIn.getInputStream(next), ClassReader.SKIP_FRAMES));
			} catch(Throwable t) {
				t.printStackTrace();
			}
		});
		zipIn.close();
		inst.loadedAPI.add(new ZIPLibrary(f, classList, isLibrary));
	}
}
