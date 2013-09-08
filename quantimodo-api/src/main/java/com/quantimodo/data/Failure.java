package com.quantimodo.data;

import java.util.Arrays;

public class Failure implements Result {
	public final String error;
	
	public Failure(final Throwable error) {
		this.error = String.format("%s >> %s", error.toString(), Arrays.toString(error.getStackTrace()));
	}
	
	public Failure(final String errorMessage) {
		this.error = errorMessage;
	}
}
