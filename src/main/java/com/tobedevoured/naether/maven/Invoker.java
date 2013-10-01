package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.NaetherException;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;

/**
 * Invoke Maven goals
 */
public class Invoker {

    private DefaultInvoker invoker;

    /**
     * Construct Invoker
     * @param localRepo local Maven repo to use
     * @param mavenHome path to Maven home
     */
    public Invoker(String localRepo, String mavenHome) {
        invoker = new DefaultInvoker();
        invoker.setLocalRepositoryDirectory( new File(localRepo) );
        if ( mavenHome != null ) {
            invoker.setMavenHome(new File(mavenHome));
        }
    }

    /**
     * Execute goals for a pom
     *
     * @param pom String path
     * @param goals String
     * @return {@link InvocationResult}
     * @throws NaetherException
     */
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
