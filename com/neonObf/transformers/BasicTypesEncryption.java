package com.neonObf.transformers;

import java.util.*;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import com.neonObf.Main;
import com.neonObf.SmartNameGen;

public class BasicTypesEncryption extends Transformer {
	static enum Value {
		INT,
		LONG,
		DOUBLE,
		FLOAT,
		STRING,
		OTHER;
		
		public static Value getLDCtypeof(Object o) {
			if(o instanceof Integer)
				return INT;
			if(o instanceof Long)
				return LONG;
			if(o instanceof Double)
				return DOUBLE;
			if(o instanceof Float)
				return FLOAT;
			if(o instanceof String)
				return STRING;
			
			return OTHER;
		}
	}

	public static HashMap<String, String> decryptorsNames = new HashMap<String, String>();
	private static final String decMethDesc = "(Ljava/lang/String;J)Ljava/lang/String;";
	private long rnd;
	
	public BasicTypesEncryption(MethodNode _mn, ClassNode _parent, long _rnd) {
		super(_mn, _parent);
		rnd = _rnd;
	}
	
	public BasicTypesEncryption() {
		super(null, null);
	}
	
	@Override
	public void run() {
		String decMethName = decryptorsNames.get(parent.name); //FIXME
		ListIterator<AbstractInsnNode> iterator;
		AbstractInsnNode next;
		iterator = mn.instructions.iterator();
		while(iterator.hasNext()) {
			next = iterator.next();
			
			if(next.getOpcode() >= LDC && next.getOpcode() <= LDC+2) {
				LdcInsnNode lin = (LdcInsnNode) next;
				Object o = lin.cst;
				
				switch(Value.getLDCtypeof(o)) {
					case STRING:
						if(((String) o).equals(""))
							break;
						
						iterator.set (
								new LdcInsnNode (
									encrypt((String) o, rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
								)
						);
						addObfuscatedLong(iterator, rnd);
						iterator.add (
								new MethodInsnNode (
										INVOKESTATIC,
										parent.name.replaceAll("\\.", "/"),
										decMethName,
										decMethDesc,
										false
								)
						);
						
						break;
					case INT:
						iterator.set (
								new LdcInsnNode (
									encrypt(Character.toString((char) (int) o), rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
								)
						);
						addObfuscatedLong(iterator, rnd);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									parent.name.replaceAll("\\.", "/"),
									decMethName,
									decMethDesc,
									false
								)
						);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									"java/lang/Integer",
									"parseInt",
									"(Ljava/lang/String;)I",
									false
								)
						);
						
						break;
					case LONG:
						iterator.set (
								new LdcInsnNode (
									encrypt(Long.toString((long) o), rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
								)
						);
						addObfuscatedLong(iterator, rnd);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									parent.name.replaceAll("\\.", "/"),
									decMethName,
									decMethDesc,
									false
								)
						);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									"java/lang/Long",
									"parseLong",
									"(Ljava/lang/String;)J",
									false
								)
						);
						
						break;
					case DOUBLE:
						iterator.set (
								new LdcInsnNode (
									encrypt(Double.toString((double) o), rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
								)
						);
						addObfuscatedLong(iterator, rnd);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									parent.name.replaceAll("\\.", "/"),
									decMethName,
									decMethDesc,
									false
								)
						);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									"java/lang/Double",
									"parseDouble",
									"(Ljava/lang/String;)D",
									false
								)
						);
						
						break;
					case FLOAT:
						iterator.set (
								new LdcInsnNode (
									encrypt(Float.toString((float) o), rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
								)
						);
						addObfuscatedLong(iterator, rnd);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									parent.name.replaceAll("\\.", "/"),
									decMethName,
									decMethDesc,
									false
								)
						);
						iterator.add (
								new MethodInsnNode (
									INVOKESTATIC,
									"java/lang/Float",
									"parseFloat",
									"(Ljava/lang/String;)F",
									false
								)
						);
						
						break;
					case OTHER:
						break;
				}
				
			}
			
			if(next.getOpcode() == BIPUSH || next.getOpcode() == SIPUSH) {
				IntInsnNode iin = (IntInsnNode) next;
				int num = iin.operand;
				
				iterator.set(
						new LdcInsnNode (
							encrypt(Character.toString((char) num), rnd, parent.sourceFile, getLineNumber(iterator.previousIndex()))
						)
				);
				addObfuscatedLong(iterator, rnd);
				iterator.add(
						new MethodInsnNode (
							INVOKESTATIC,
							parent.name.replaceAll("\\.", "/"),
							decMethName,
							"(Ljava/lang/String;J)Ljava/lang/String;",
							false
						)
				);
				iterator.add(
						new InsnNode (
							ICONST_0
						)
				);
				iterator.add(
						new MethodInsnNode (
							INVOKEVIRTUAL,
							"java/lang/String",
							"charAt",
							"(I)C",
							false
						)
				);
			}
		}
	}
	
	public static String decrypt(String str, long rnd) {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		rnd ^= ste.getLineNumber() + ste.getFileName().hashCode();
		char[] newC = new char[str.length()];
		int i = -1;
		
		for(char c : str.toCharArray())
			newC[++i] = (char) (c ^ i ^ rnd);
		
		return new String(newC);
	}
	
	public static String encrypt(String str, long rnd, String fileName, int line) {
		rnd ^= line + fileName.hashCode();
		char[] newC = new char[str.length()];
		int i = -1;
		
		for(char c : str.toCharArray())
			newC[++i] = (char) (c ^ i ^ rnd);
		
		return new String(newC);
	}

	@Override
	public ArrayList<ClassNode> obfuscate(ArrayList<ClassNode> classes) throws Throwable {
		SmartNameGen nameGen = Main.getInstance().nameGen;
		
		for (int i = 0; i < classes.size(); i++) {
			ClassNode cn = classes.get(i);
			
			if(!hasInsnsToObf(cn))
				continue;

			makeDecryptorMethod (
				cn,
				nameGen.get (
					1,
					cn.name
				)
			);
			for (int i2 = 0; i2 < cn.methods.size(); i2++) {
				MethodNode mn = (MethodNode) cn.methods.get(i2);
				new BasicTypesEncryption(mn, cn, rand.nextLong()).start();
			}
		}
		
		return classes;
	}
	
	private static boolean hasInsnsToObf(ClassNode cn) {
		for(MethodNode mn : (List<MethodNode>) cn.methods) {
			ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
			while(iterator.hasNext()) {
				AbstractInsnNode next = iterator.next();
				
				if(
					(next.getOpcode() >= LDC && next.getOpcode() <= LDC+2) ||
					next.getOpcode() == BIPUSH ||
					next.getOpcode() == SIPUSH
				)
					return true;
				
			}
		}
		
		return false;
	}
	
	public void makeDecryptorMethod(ClassNode cn, String name) {
		SmartNameGen nameGen = Main.getInstance().nameGen;
		
		MethodNode mn = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC,
				name, "(Ljava/lang/String;J)Ljava/lang/String;", null, null);
		
		mn.visitCode();
		Label l0 = new Label();
		mn.visitLabel(l0);
		mn.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
		mn.visitInsn(ICONST_2);
		mn.visitInsn(AALOAD);
		mn.visitVarInsn(ASTORE, 3);
		Label l1 = new Label();
		mn.visitLabel(l1);
		mn.visitVarInsn(LLOAD, 1);
		mn.visitVarInsn(ALOAD, 3);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getLineNumber", "()I", false);
		mn.visitVarInsn(ALOAD, 3);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getFileName", "()Ljava/lang/String;", false);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		mn.visitInsn(IADD);
		mn.visitInsn(I2L);
		mn.visitInsn(LXOR);
		mn.visitVarInsn(LSTORE, 1);
		Label l2 = new Label();
		mn.visitLabel(l2);
		mn.visitVarInsn(ALOAD, 0);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
		mn.visitIntInsn(NEWARRAY, T_CHAR);
		mn.visitVarInsn(ASTORE, 4);
		Label l3 = new Label();
		mn.visitLabel(l3);
		mn.visitInsn(ICONST_M1);
		mn.visitVarInsn(ISTORE, 5);
		Label l4 = new Label();
		mn.visitLabel(l4);
		mn.visitVarInsn(ALOAD, 0);
		mn.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
		mn.visitInsn(DUP);
		mn.visitVarInsn(ASTORE, 9);
		mn.visitInsn(ARRAYLENGTH);
		mn.visitVarInsn(ISTORE, 8);
		mn.visitInsn(ICONST_0);
		mn.visitVarInsn(ISTORE, 7);
		Label l5 = new Label();
		mn.visitJumpInsn(GOTO, l5);
		Label l6 = new Label();
		mn.visitLabel(l6);
		mn.visitFrame(Opcodes.F_FULL, 9, new Object[] {"java/lang/String", Opcodes.LONG, "java/lang/StackTraceElement", "[C", Opcodes.INTEGER, Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER, "[C"}, 0, new Object[] {});
		mn.visitVarInsn(ALOAD, 9);
		mn.visitVarInsn(ILOAD, 7);
		mn.visitInsn(CALOAD);
		mn.visitVarInsn(ISTORE, 6);
		Label l7 = new Label();
		mn.visitLabel(l7);
		mn.visitVarInsn(ALOAD, 4);
		mn.visitIincInsn(5, 1);
		mn.visitVarInsn(ILOAD, 5);
		mn.visitVarInsn(ILOAD, 6);
		mn.visitVarInsn(ILOAD, 5);
		mn.visitInsn(IXOR);
		mn.visitInsn(I2L);
		mn.visitVarInsn(LLOAD, 1);
		mn.visitInsn(LXOR);
		mn.visitInsn(L2I);
		mn.visitInsn(I2C);
		mn.visitInsn(CASTORE);
		Label l8 = new Label();
		mn.visitLabel(l8);
		mn.visitIincInsn(7, 1);
		mn.visitLabel(l5);
		mn.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
		mn.visitVarInsn(ILOAD, 7);
		mn.visitVarInsn(ILOAD, 8);
		mn.visitJumpInsn(IF_ICMPLT, l6);
		Label l9 = new Label();
		mn.visitLabel(l9);
		mn.visitTypeInsn(NEW, "java/lang/String");
		mn.visitInsn(DUP);
		mn.visitVarInsn(ALOAD, 4);
		mn.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
		mn.visitInsn(ARETURN);
		Label l10 = new Label();
		mn.visitLabel(l10);
		mn.visitLocalVariable(nameGen.get(3), "Ljava/lang/String;", null, l0, l10, 0);
		mn.visitLocalVariable(nameGen.get(4), "J", null, l0, l10, 1);
		mn.visitLocalVariable(nameGen.get(5), "Ljava/lang/StackTraceElement;", null, l1, l10, 3);
		mn.visitLocalVariable(nameGen.get(6), "[C", null, l3, l10, 4);
		mn.visitLocalVariable(nameGen.get(7), "I", null, l4, l10, 5);
		mn.visitLocalVariable(nameGen.get(8), "C", null, l7, l8, 6);
		mn.visitMaxs(6, 10);
		mn.visitEnd();
		
		cn.methods.add(new Random().nextInt(cn.methods.size()), mn);
	}
	
	private void addObfuscatedLong(ListIterator<AbstractInsnNode> iterator, long l) {
		boolean negative = l < 0;
		iterator.add(
				new LdcInsnNode
				(
					negative?-l:l
				)
		);
		int val = ((rand.nextInt(10) + 1) * 2) + (negative?1:0);
		for(int i = 0; i < val; i++)
			iterator.add(
					new InsnNode
					(
						LNEG
					)
			);
	}
	
	public int getLineNumber(int pos) {
		ListIterator<AbstractInsnNode> iterator;
		AbstractInsnNode prev;
		iterator = mn.instructions.iterator(pos);
		while(iterator.hasPrevious())
			if((prev = iterator.previous()) instanceof LineNumberNode)
				return ((LineNumberNode) prev).line;
		
		return -1;
	}
}
