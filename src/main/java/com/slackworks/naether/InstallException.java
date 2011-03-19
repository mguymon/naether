package com.slackworks.naether;

public class InstallException extends NaetherException {

	private static final long serialVersionUID = -9173565689543634165L;

	public InstallException( String msg ) {
		super(msg);
	}
	
	public InstallException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public InstallException( Throwable throwable ) {
		super(throwable);
	}
}
