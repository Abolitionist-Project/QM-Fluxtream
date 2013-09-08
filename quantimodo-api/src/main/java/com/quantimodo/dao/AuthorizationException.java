package com.quantimodo.dao;

public class AuthorizationException extends RuntimeException {
	private static final String DEFAULT_MESSAGE = "That operation was not authorized";
	public AuthorizationException() {
		super(DEFAULT_MESSAGE);
	}
	
	public AuthorizationException(final String message) {
		super(message);
	}
	
	public AuthorizationException(final String message, final Throwable cause) {
		super(message, cause);
	}
	
	public AuthorizationException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writeableStackTrace) {
		super(message, cause, enableSuppression, writeableStackTrace);
	}
	
	public AuthorizationException(final Throwable cause) {
		super(DEFAULT_MESSAGE, cause);
	}
}
