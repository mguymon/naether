package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.NaetherException;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;

public class Invoker {

    private DefaultInvoker invoker;

    public Invoker(String localRepo, String mavenHome) {
        invoker = new DefaultInvoker();
        invoker.setLocalRepositoryDirectory( new File(localRepo) );
        if ( mavenHome != null ) {
            invoker.setMavenHome(new File(mavenHome));
        }
    }

    public InvocationResult execute(String pom, String... goals) throws NaetherException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pom));
        request.setInteractive(false);
        request.setGoals( Arrays.asList(goals) );

        try {
            return invoker.execute( request );
        } catch (MavenInvocationException e) {
            throw new NaetherException("Failed to execute maven task",e);
        }
    }
}
