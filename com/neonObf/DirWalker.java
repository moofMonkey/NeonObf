package com.neonObf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DirWalker {
	public static ClassNode loadClass(InputStream is, int mode) {
		try {
			final ClassReader reader = new ClassReader(is);
			final ClassNode node = new ClassNode();

			reader.accept(node, mode);
			for (int i = 0; i < node.methods.size(); ++i) {
				final MethodNode methodNode2 = node.methods.get(i);
				final JSRInlinerAdapter adapter = new JSRInlinerAdapter(methodNode2, methodNode2.access,
						methodNode2.name, methodNode2.desc, methodNode2.signature,
						methodNode2.exceptions.<String> toArray(new String[0]));
				methodNode2.accept(adapter);
				node.methods.set(i, adapter);
			}

			Main.getInstance().hmSCN.put(node.name, node);
			Main.getInstance().hmSCN2.put(node, node.name);

			return node;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	public DirWalker(File file, int mode, boolean addToClassList) throws Throwable {
		String name = file.getName();
		String path = file.getAbsolutePath();
		if (file.isDirectory()) {
			if (file.listFiles() != null && !path.endsWith(".donot"))
				for (File f : file.listFiles())
					if (!(f.getName().charAt(0) == '.' || f.getName().charAt(0) == '$'))
						new DirWalker(f, mode, addToClassList);
		} else if (file.isFile() && name.lastIndexOf('.') > -1 && (name.endsWith("jar") || name.endsWith("class"))) {
			if (name.endsWith("jar"))
				bruteZipFile(new File(path), mode, addToClassList);
			if (name.endsWith("class")) {
				ClassNode cn = loadClass(new FileInputStream(path), mode);
				if (addToClassList)
					Main.getInstance().classes.add(cn);
			}
		}
	}

	public void bruteZipFile(File f, int mode, boolean addToClassList) throws Throwable {
		try {
			final ZipFile zipIn = new ZipFile(f);
			final Enumeration<? extends ZipEntry> e = zipIn.entries();
			while (e.hasMoreElements()) {
				final ZipEntry next = e.nextElement();
				if (next.getName().endsWith(".class")) {
					ClassNode cn = loadClass(zipIn.getInputStream(next), mode);
					if (addToClassList)
						Main.getInstance().classes.add(cn);
				} else if (next.isDirectory())
					continue;
			}
			zipIn.close();
		} catch(Throwable t) {  }
	}
}
