package com.quantimodo.oldData;

/**
 * An <code>AuthorizationException</code> is thrown when an attempt is made to violate access controls.
 * 
 * @author C Erler
 * @version 1.0, 07/08/13 
 */

// Class should be final to prevent subclasses with access-controlled instances.
public final class AuthorizationException extends RuntimeException {
	public AuthorizationException() { super(); }
	public AuthorizationException(final String message) { super(message); }
	public AuthorizationException(final String message, final Throwable cause) { super(message, cause); }
	public AuthorizationException(final String message, final Throwable cause, boolean enableSuppression, boolean writeableStackTrace) { super(message, cause, enableSuppression, writeableStackTrace); }
	public AuthorizationException(final Throwable cause) { super(cause); }
}
