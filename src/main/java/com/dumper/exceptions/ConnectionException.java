package com.dumper.exceptions;

/**
 * 
 * @author ksalnis
 *
 */
public class ConnectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConnectionException(String message) {
		super(message);
	}

	public ConnectionException(Throwable cause) {
		super(cause);
	}

	public ConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}