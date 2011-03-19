package com.slackworks.naether;

public class DependencyException extends NaetherException {

	private static final long serialVersionUID = -9173565689543634165L;

	public DependencyException( String msg ) {
		super(msg);
	}
	
	public DependencyException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public DependencyException( Throwable throwable ) {
		super(throwable);
	}
}
