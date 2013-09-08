package com.quantimodo.dao;

public class ConstraintException extends RuntimeException {
	private static final String DEFAULT_MESSAGE = "That operation violates constraints";
	public ConstraintException() {
		super(DEFAULT_MESSAGE);
	}
	
	public ConstraintException(final String message) {
		super(message);
	}
	
	public ConstraintException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public ConstraintException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writeableStackTrace) {
		super(message, cause, enableSuppression, writeableStackTrace);
	}
	
	public ConstraintException(final Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}
}
