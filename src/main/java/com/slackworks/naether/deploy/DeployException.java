package com.slackworks.naether.deploy;

import com.slackworks.naether.NaetherException;

public class DeployException extends NaetherException {

	private static final long serialVersionUID = -9173565689543634165L;

	public DeployException( String msg ) {
		super(msg);
	}
	
	public DeployException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public DeployException( Throwable throwable ) {
		super(throwable);
	}
}
