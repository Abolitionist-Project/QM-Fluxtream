package com.fluxtream.services.exceptions;

public class BaseServiceException extends RuntimeException {

	private static final long serialVersionUID = 8508316604435533709L;

	private Class clazz;

	public BaseServiceException(String message) {
		super(message);
	}

	public BaseServiceException(String message, Throwable t) {
		super(message, t);
	}

	public BaseServiceException(String message, Class clazz) {
		super(message);
		this.clazz = clazz;
	}

	public BaseServiceException(String message, Throwable t, Class clazz) {
		super(message, t);
		this.clazz = clazz;
	}

}
