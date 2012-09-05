package com.tobedevoured.naether;

public class NaetherException extends Exception {

	private static final long serialVersionUID = 6600188769024224357L;

	public NaetherException( String msg ) {
		super(msg);
	}
	
	public NaetherException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public NaetherException( Throwable throwable ) {
		super(throwable);
	}
}
