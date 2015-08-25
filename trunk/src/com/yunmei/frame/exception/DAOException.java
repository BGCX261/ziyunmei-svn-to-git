package com.yunmei.frame.exception;

public class DAOException extends Exception {
	private static final long serialVersionUID = 8014536628694723159L;

	public DAOException() {
	}

	public DAOException(String paramString) {
		super(paramString);
	}

	public DAOException(Throwable paramThrowable) {
		super(paramThrowable);
	}

	public DAOException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}
}
