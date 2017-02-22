package com.neonObf;

public class NoClassInPathException extends RuntimeException {
	private static final long serialVersionUID = 6934937513415169336L;

	public NoClassInPathException(String className) {
		super(className);
	}
}
