package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.NaetherException;

public class ProjectException  extends NaetherException {

	private static final long serialVersionUID = -483028771930335840L;

	public ProjectException( String msg ) {
		super(msg);
	}
	
	public ProjectException( String msg, Throwable throwable ) {
		super(msg, throwable );
	}
	
	public ProjectException( Throwable throwable ) {
		super(throwable);
	}
}
