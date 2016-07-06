package com.tobedevoured.naether;

public class ResolveException  extends NaetherException {

    private static final long serialVersionUID = -9173565689543634165L;

    public ResolveException( String msg ) {
        super(msg);
    }
    
    public ResolveException( String msg, Throwable throwable ) {
        super(msg, throwable );
    }
    
    public ResolveException( Throwable throwable ) {
        super(throwable);
    }
}
