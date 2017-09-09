package com.neonObf;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.cli.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.neonObf.transformers.*;

public class Main extends Thread {
	private static Main instance;
	public static final HashMap<String, Transformer> transformers = new HashMap<>();

	public File inF, outF;
	public ArrayList<ClassNode> classes = new ArrayList<ClassNode>();
	public ArrayList<ClassFile> files = new ArrayList<ClassFile>();
	public HashMap<String, ClassNode> nameToNode = new HashMap<String, ClassNode>();
	public HashMap<ClassNode, String> nodeToName = new HashMap<ClassNode, String>();
	public String[] libraries;
	public ArrayList<Library> loadedAPI = new ArrayList<Library>();
	public String[] usedTransformers;
	public HashMap<String, Integer> pkgLens = new HashMap<String, Integer>();
	public SmartNameGen nameGen;
	public String[] args;
	public CommandLine cmd;

	public static Main getInstance() {
		return instance;
	}

	/***
	 * Parses argument
	 *
	 * @throws Throwable
	 */
	public void parseArgs() throws Throwable {
		Options options = new Options();

		Option inputArg = new Option("i", "input", true, "Input .jar/.class file/folder path");
		inputArg.setRequired(true);
		inputArg.setArgName("path");
		options.addOption(inputArg);

		Option outputArg = new Option("o", "output", true, "Output .jar path");
		outputArg.setRequired(true);
		outputArg.setArgName("path");
		options.addOption(outputArg);

		Option librariesArg = new Option("l", "libraries", true, "Libraries path (separated by semicolons/multiple arguments)");
		librariesArg.setRequired(false);
		librariesArg.setArgName("path");
		options.addOption(librariesArg);

		Option transformersArg = new Option("t", "transformers", true, "Transformers (separated by semicolons/multiple arguments)");
		transformersArg.setRequired(true);
		transformersArg.setArgName("transformers");
		options.addOption(transformersArg);

		Option dictionaryArg = new Option("d", "dictionary", true, "Dictionary type");
		dictionaryArg.setRequired(true);
		dictionaryArg.setArgName("1/2/3");
		options.addOption(dictionaryArg);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar NeonObf.jar", options, true);
			System.out.println("Example: java -jar NeonObf.jar --input IN.jar --output OUT.jar --transformers SourceFileRemover;LineNumberObfuscation;FinalRemover;LocalVariableNameObfuscator;BasicTypesEncryption;GotoFloodObfuscation;CodeHider --dictionary 3");

			System.exit(1);
			return;
		}

		if (!(inF = new File(cmd.getOptionValue("input"))).exists())
			throw new Throwable("Error: Input file does not exist.");

		if ((outF = new File(cmd.getOptionValue("output"))).exists())
			throw new Throwable("Error: Output file already exists.");

		ArrayList<String> librariesList = new ArrayList<>();
		if(cmd.hasOption("libraries"))
			for (String libraryName1 : cmd.getOptionValues("libraries"))
				for (String libraryName2 : libraryName1.split(";"))
					librariesList.add(libraryName2);
		libraries = librariesList.toArray(new String[librariesList.size()]);

		ArrayList<String> usedTransformersList = new ArrayList<>();
		for(String transformerName1 : cmd.getOptionValues("transformers"))
			for(String transformerName2 : transformerName1.split(";"))
				usedTransformersList.add(transformerName2);
		usedTransformers = usedTransformersList.toArray(new String[usedTransformersList.size()]);

		int dictiounary;
		try {
			dictiounary = Integer.parseInt(cmd.getOptionValue("dictionary"));
		} catch(NumberFormatException t) {
			throw new Throwable("Dictionary must be a number (1-3)");
		}
		switch(dictiounary) {
			case 1:
				nameGen = new SmartNameGen("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_");
				break;
			case 2:
				nameGen = new SmartNameGen("abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "_.#{}");
				break;
			case 3:
				nameGen = new SmartNameGen("\r" + "\t");
				break;
			default:
				throw new Throwable("Dictionary number must be in range of 1 to 3");
		}

		//existTransformers.put("AntiMemoryDump", new AntiMemoryDump());
		transformers.put("BasicTypesEncryption", new BasicTypesEncryption());
		transformers.put("CodeHider", new CodeHider());
		transformers.put("FinalRemover", new FinalRemover());
		transformers.put("GotoFloodObfuscation", new GotoFloodObfuscation());
		transformers.put("LineNumberObfuscation", new LineNumberObfuscation());
		transformers.put("LocalVariableNameObfuscator", new LocalVariableNameObfuscator());
		transformers.put("SourceFileRemover", new SourceFileRemover());
		transformers.put("TryCatch", new TryCatch());
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
				parseArgs();
			} catch(Throwable t) {
				String msg = t.getMessage();
				if (msg != null)
					System.out.println(msg);

				return;
			}

			System.out.println("Loading java APIs...");
			new DirWalker(new File(System.getProperty("java.home") + File.separatorChar + "lib"), true);
			System.out.println("Loading user APIs...");
			for(String lib : libraries)
				new DirWalker(new File(lib), true);
			System.out.println("All APIs loaded!");

			System.out.println("--------------------------------------------------");

			System.out.println("Loading input file...");
			new DirWalker(inF, false);

			System.out.println("--------------------------------------------------");

			System.out.println("Making class tree...");
			CustomClassWriter.loadHierachy();

			ArrayList<ClassNode> modClasses = new ArrayList<>(classes);

			System.out.println("Starting transforming.... " + getDateTag());

			System.out.println("--------------------------------------------------");

			for(String transformerName : usedTransformers) {
				System.out.println("Started transformation with " + transformerName + " transformer");

				try {
					modClasses = transformers.get(transformerName).obfuscate(modClasses);
				} catch(NullPointerException npe) {
					throw new Throwable("Transformer name \"" + transformerName + "\" aren't defined in Main#transformers.", npe);
				}
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
			System.out.println(t.getMessage());
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
		if (node.innerClasses != null)
			((List<InnerClassNode>) node.innerClasses).parallelStream().filter(in -> in.innerName != null).forEach(in -> {
				if (in.innerName.indexOf('/') != -1)
					in.innerName = in.innerName.substring(in.innerName.lastIndexOf('/') + 1); // Stringer
			});
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
							System.out.println("ClassNode contained JSR/RET so COMPUTE_MAXS instead");
							writer = new CustomClassWriter(ClassWriter.COMPUTE_MAXS);
							node.accept(writer);
						} else
							throw e;
					} else
						throw e;
			}
			byte[] classBytes = writer.toByteArray();

			if (autoAdd)
				files.add(new ClassFile(node.name.replaceAll("\\.", "/") + ".class", classBytes));

			return classBytes;
		} catch(Throwable t) {
			System.out.println("Error occurred while writing " + node.name + ". This class will be original. Exception: " + t.getMessage());
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

			for(ClassFile mc : files)
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
		for(ClassFile mc : files)
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
		ze = ze
				.setCreationTime(FileTime.fromMillis(time))
				.setLastAccessTime(FileTime.fromMillis(time))
				.setLastModifiedTime(FileTime.fromMillis(time));

		return ze;
	}
}
