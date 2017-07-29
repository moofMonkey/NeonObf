package com.neonObf;

public class ClassFile {
	public String name;
	public byte[] bytecode;

	public ClassFile(String name, byte[] bytecode) {
		this.name = name;
		this.bytecode = bytecode;
	}
}
