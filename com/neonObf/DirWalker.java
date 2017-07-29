package com.neonObf;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DirWalker {
	public DirWalker(File file, int mode, boolean isLibrary) throws Throwable {
		String name = file.getName();
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (file.listFiles() != null && !path.endsWith(".donot"))
				for(File f : file.listFiles())
					if (!(f.getName().charAt(0) == '.' || f.getName().charAt(0) == '$'))
						new DirWalker(f, mode, isLibrary);
		} else
			if (file.isFile() && name.lastIndexOf('.') > -1
					&& (name.endsWith("jar") || name.endsWith("class"))) {
				if (name.endsWith("jar"))
					loadFile(new File(path), mode, isLibrary);
			}
	}

	public void loadFile(File f, int mode, boolean isLibrary) throws Throwable {
		try {
			final ZipFile zipIn = new ZipFile(f);
			final Enumeration<? extends ZipEntry> e = zipIn.entries();
			ArrayList<String> classList = new ArrayList<>();
			while (e.hasMoreElements()) {
				final ZipEntry next = e.nextElement();
				if (next.getName().endsWith(".class")) {
					if(isLibrary)
						classList.add(next.getName().replaceAll("(.*)\\.class", "$1"));
					else
						Main.getInstance().classes.add(CustomClassWriter.loadClass(zipIn.getInputStream(next), ClassReader.SKIP_FRAMES));
				} else
					if (next.isDirectory())
						continue;
			}
			zipIn.close();
			Main.getInstance().loadedAPI.add(new Library(f, classList, isLibrary));
		} catch(Throwable t) {
		}
	}
}
