package com.tobedevoured.naether;

public class URLException extends NaetherException {

	private static final long serialVersionUID = -9173565689543634165L;

	public URLException( String msg ) {
		super(msg);
	}
	
	public URLException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public URLException( Throwable throwable ) {
		super(throwable);
	}
}
