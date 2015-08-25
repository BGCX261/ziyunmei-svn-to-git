package com.yunmei.frame.exception;

public class BOException extends Exception {

	private static final long serialVersionUID = 1L;

	public BOException() {
	}

	public BOException(String paramString) {
		super(paramString);
	}

	public BOException(Throwable paramThrowable) {
		super(paramThrowable);
	}

	public BOException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}
}