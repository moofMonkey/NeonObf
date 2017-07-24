package com.neonObf;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import com.neonObf.transformers.*;

public class Main extends Thread {
	private static Main instance;
	public static final HashMap<String, Transformer> transformers = new HashMap<>();

	public File inF, outF;
	public ArrayList<ClassNode> classes = new ArrayList<ClassNode>();
	public ArrayList<MyFile> files = new ArrayList<MyFile>();
	/**
	 * name => node
	 */
	public HashMap<String, ClassNode> hmSCN = new HashMap<String, ClassNode>();
	/**
	 * node => name
	 */
	public HashMap<ClassNode, String> hmSCN2 = new HashMap<ClassNode, String>();
	public ArrayList<File> paths = new ArrayList<File>();
	public String[] usedTransformers;
	public HashMap<String, Integer> pkgLens = new HashMap<String, Integer>();
	public SmartNameGen nameGen;
	public String[] args;

	public static Main getInstance() {
		return instance;
	}

	private void checkArgs() throws Throwable {
		if (args.length < 5) {
			System.out
					.println("Usage: java -jar NeonObf.jar <jar_to_obfuscate> <jar_to_obfuscate_out> </path/to/libs/> <transformers> <min/norm/max>");
			throw new Throwable();
		}

		for(int i = 0; i < args.length; i++)
			if (parseArg(args[i], i, args)) {
				checkArgs();
				break;
			}
	}

	/***
	 * Parses argument
	 * 
	 * @param arg
	 *            Argument from massive
	 * @param index
	 *            Index of argument in massive
	 * @param args
	 *            All arguments
	 * @return Needed in restart argument checking?
	 * @throws Throwable
	 */
	public boolean parseArg(String arg, int index, String[] args) throws Throwable {
		File f;
		switch (index) {
			case 0:
				if (!(f = new File(arg)).exists())
					throw new Throwable("Jar to obfuscate not found :L");
				inF = f;

				break;
			case 1:
				if ((f = new File(arg)).exists()) {
					String postfix = "." + new Random().nextInt(100);
					args[1] += postfix;
					System.out.println("Out file already exists. Using " + postfix);
					return true;
				}
				outF = f;

				break;
			case 2:
				if (!(f = new File(arg)).exists() && !arg.equalsIgnoreCase("null"))
					throw new Throwable(".JAR/.class/folder with libraries must exist! (it can be empty folder or \'null\')");

				break;
			case 3:
				usedTransformers = arg.split(";");

				break;
			case 4:
				if (arg.equalsIgnoreCase("min"))
					nameGen = new SmartNameGen("abcdefghijklmnopqrstuvwxyz"
							+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_");
				else
				if (arg.equalsIgnoreCase("norm"))
					nameGen = new SmartNameGen("abcdefghijklmnopqrstuvwxyz"
							+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_.#{}");
				else
				if (arg.equalsIgnoreCase("max"))
					nameGen = new SmartNameGen("\r" + "\t");
				else
					throw new Throwable("Arg " + index + " is not valid. Arg is " + arg);

				//existTransformers.put("AntiMemoryDump", new AntiMemoryDump());
				transformers.put("BasicTypesEncryption", new BasicTypesEncryption());
				transformers.put("CodeHider", new CodeHider());
				transformers.put("FinalRemover", new FinalRemover());
				transformers.put("GotoFloodObfuscation", new GotoFloodObfuscation());
				transformers.put("LineNumberObfuscation", new LineNumberObfuscation());
				transformers.put("LocalVariableNameObfuscator", new LocalVariableNameObfuscator());
				transformers.put("SourceFileRemover", new SourceFileRemover());
				transformers.put("TryCatch", new TryCatch());

				break;
			default:
				System.out.println("Arg " + index + " is excess.");
				break;
		}

		return false;
	}

	public static boolean isEmpty(MethodNode mn) {
		return mn.instructions.getFirst() != null;
	}

	public static void printLogo() {
		System.out.println("|---------------------------------------------------------------------------|");
		System.out.println("|   __   __    ______    ______    __   __    ______    ______    ______    |");
		System.out.println("|  /\\ \"-.\\ \\  /\\  ___\\  /\\  __ \\  /\\ \"-.\\ \\  /\\  __ \\  /\\  == \\  /\\  ___\\   |");
		System.out.println("|  \\ \\ \\-.  \\ \\ \\  __\\  \\ \\ \\/\\ \\ \\ \\ \\-.  \\ \\ \\ \\/\\ \\ \\ \\  __<  \\ \\  __\\   |");
		System.out.println("|   \\ \\_\\\\\"\\_\\ \\ \\_____\\ \\ \\_____\\ \\ \\_\\\\\"\\_\\ \\ \\_____\\ \\ \\_____\\ \\ \\_\\     |");
		System.out.println("|    \\/_/ \\/_/  \\/_____/  \\/_____/  \\/_/ \\/_/  \\/_____/  \\/_____/  \\/_/     |");
		System.out.println("|                                                                           |");
		System.out.println("|---------------------------------------------------------------------------|");
	}

	@Override
	public void run() {
		try {
			printLogo();
			try {
				checkArgs();
			} catch(Throwable t) {
				String msg = t.getMessage();
				if (msg != null)
					System.out.println(msg);

				return;
			}

			System.out.println("Loading java APIs...");
			new DirWalker(new File(System.getProperty("java.home") + File.separatorChar
					+ "lib"), ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG
							| ClassReader.SKIP_FRAMES, false);
			if (!args[2].equalsIgnoreCase("null")) {
				System.out.println("Loading user APIs...");
				new DirWalker(new File(args[2]), ClassReader.SKIP_CODE
						| ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES, false);
			}
			System.out.println("Loading input file...");
			new DirWalker(inF, ClassReader.SKIP_FRAMES, true);
			System.out.println("All APIs loaded!");

			System.out.println("--------------------------------------------------");

			System.out.println("Making class tree...");
			CustomClassWriter.loadHierachy();

			ArrayList<ClassNode> modClasses = classes;

			System.out.println("Starting transforming.... " + getDateTag());

			System.out.println("--------------------------------------------------");

			for(String transformerName : usedTransformers) {
				System.out.println("Started transformation with " + transformerName + " transformer");

				modClasses = transformers.get(transformerName).obfuscate(modClasses);

				System.out.println("Transformation completed with " + transformerName + " transformer");
			}

			System.out.println("--------------------------------------------------");
			System.out.println("All transformations completed! " + getDateTag());
			System.out.println("Dumping all classes...  " + getDateTag());
			for(ClassNode cn : modClasses)
				dump(cn, true);
			System.out.println("--------------------------------------------------");
			System.out.println("All classes dumped! " + getDateTag());
			System.out.println("Saving all classes...");
			saveAll();
			System.out.println("All classes saved!");
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public Main(String[] args) {
		this.args = args;
	}

	public static void main(String[] args) throws Throwable {
		(instance = new Main(args)).start();
	}

	public int classesInPackageOfClass(String className) {
		return classesInPackage(getPackageByClass(className));
	}

	public int classesInPackage(String pkg) {
		if (pkgLens.containsKey(pkg))
			return pkgLens.get(pkg);

		pkg = pkg + ".";
		int len = 0;

		for(ClassNode cn : classes) {
			if (cn.name.startsWith(pkg)) {
				String className = cn.name.substring(pkg.length());

				if (className.indexOf('.') == -1)
					len++;
			}
		}

		pkgLens.put(pkg.substring(0, pkg.length() - 1), len);

		return len;
	}

	public static String getPackageByClass(String className) {
		return className.substring(0, className.lastIndexOf('.'));
	}

	public static SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

	public static String getDateTag() {
		return "[" + sdf.format(new Date()) + "]";
	}

	public byte[] dump(ClassNode node, boolean autoAdd) {
		if (node.innerClasses != null) {
			((List<InnerClassNode>) node.innerClasses).stream().filter(in -> in.innerName != null).forEach(in -> {
				if (in.innerName.indexOf('/') != -1)
					in.innerName = in.innerName.substring(in.innerName.lastIndexOf('/') + 1); // Stringer
			});
		}
		ClassWriter writer = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES);
		try {
			try {
				node.accept(writer);
			} catch(RuntimeException e) {
				if (e instanceof NoClassInPathException) {
					NoClassInPathException ex = (NoClassInPathException) e;
					System.out.println("Error: " + ex.getMessage()
							+ " could not be found while writing " + node.name
							+ ". Using COMPUTE_MAXS");
					writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS);
					node.accept(writer);
				} else
					if (e.getMessage() != null) {
						if (e.getMessage().contains("JSR/RET")) {
							System.out
									.println("ClassNode contained JSR/RET so COMPUTE_MAXS instead");
							writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS);
							node.accept(writer);
						} else {
							throw e;
						}
					} else {
						throw e;
					}
			}
			byte[] classBytes = writer.toByteArray();

			ClassReader cr = new ClassReader(classBytes);
			try {
				cr.accept(new CheckClassAdapter(new ClassWriter(0)), 0);
			} catch(Throwable t) {
				// System.out.println("Error: " + node.name + " failed
				// verification");
				// t.printStackTrace();
			}

			if (autoAdd)
				files.add(new MyFile(node.name.replaceAll("\\.", "/")
						+ ".class", classBytes));

			return classBytes;
		} catch(Throwable t) {
			System.out.println("Error while writing " + node.name);
			t.printStackTrace(System.out);
		}
		return null;
	}

	public void saveAll() throws Throwable {
		outF.createNewFile();
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outF));
		/* Start of combination of jar's */
		ZipFile zf = new ZipFile(inF);
		Enumeration<? extends ZipEntry> in = zf.entries();

		byte[] data;

		while (in.hasMoreElements()) {
			ZipEntry ze = in.nextElement();
			boolean finded = false;

			for(MyFile mc : files)
				if (mc != null && ze != null && mc.name != null && ze.getName() != null
						&& mc.name.equals(ze.getName())) {
					finded = true;
					break;
				}

			if (zf != null && ze != null && !finded) {
				DataInputStream dis = new DataInputStream(zf.getInputStream(ze));
				data = new byte[(int) ze.getSize()];
				dis.readFully(data);
				dis.close();

				ze = modifyEntry(new ZipEntry(ze.getName()));

				out.putNextEntry(ze);
				out.write(data, 0, data.length);
				out.closeEntry();
			}
		}
		zf.close();
		/* End of combination of jar's */
		for(MyFile mc : files)
			try {
				data = mc.bytecode;
				ZipEntry ze = modifyEntry(new ZipEntry(mc.name));

				out.putNextEntry(ze);
				out.write(data, 0, data.length);
				out.closeEntry();
			} catch(Throwable t) {
			}

		out.setComment("P4ck3d by m00fm0nk3y 4u70-0bfu5c470r (1337 15 u53d)");
		out.setLevel(9);
		out.close();
	}

	public static ZipEntry modifyEntry(ZipEntry ze) {
		final long time = 0;

		ze.setTime(time);
		ze = ze.setCreationTime(FileTime.fromMillis(time))
				.setLastAccessTime(FileTime.fromMillis(time))
				.setLastModifiedTime(FileTime.fromMillis(time));

		return ze;
	}
}
