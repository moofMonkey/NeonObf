package com.neonObf;


import java.util.Random;

import com.neonObf.transformers.BasicTypesEncryption;

public class SmartNameGen {
	public char[] nameGen;
	public char last;
	public int nameGenLen;
	private static final Random rand = new Random();

	public SmartNameGen(String _nameGen) {
		nameGen = _nameGen.toCharArray();
		nameGenLen = nameGen.length;
		last = nameGen[nameGenLen - 1];
		shuffleArray();
	}

	private void shuffleArray() {
		int index;
		char temp;
		for(int i = nameGen.length - 1; i > 0; i--) {
			index = rand.nextInt(i + 1);
			temp = nameGen[index];
			nameGen[index] = nameGen[i];
			nameGen[i] = temp;
		}
	}

	public String get(int num) {
		int charCount = num / nameGenLen;
		if (num % nameGen.length != 0)
			charCount++;
		char[] ar = new char[charCount];

		for(int i = 1; i <= charCount; i++) {
			int fixed = (num % ((nameGenLen - 1) * i));
			if (fixed >= nameGenLen)
				ar[i - 1] = last;
			else
				ar[i - 1] = nameGen[fixed];
		}

		return new String(ar);
	}

	public String get(int num, String clName) {
		String ret = get(num);

		BasicTypesEncryption.decryptorsNames.put(clName, ret);
		return ret;
	}
}
