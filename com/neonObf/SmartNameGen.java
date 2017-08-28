package com.neonObf;


import java.security.SecureRandom;
import java.util.Random;

import com.neonObf.transformers.BasicTypesEncryption;

public class SmartNameGen {
	public char[] nameGen;
	private static final SecureRandom rand = new SecureRandom();

	public SmartNameGen(String _nameGen) {
		nameGen = _nameGen.toCharArray();
	}

	public String get(int len) {
		char[] name = new char[len];

		for(int i = 0; i < len; i++)
			name[i] = nameGen[rand.nextInt(nameGen.length)];

		return new String(name);
	}

	public String get(int num, String clName) {
		String ret = get(num);

		BasicTypesEncryption.decryptorsNames.put(clName, ret);
		return ret;
	}
}
