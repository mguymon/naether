package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.NaetherException;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Invoke Maven goals
 */
public class Invoker {

    private final static Logger logger = LoggerFactory.getLogger(Invoker.class);
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
     * @throws NaetherException exception
     */
    public InvocationResult execute(String pom, String... goals) throws NaetherException {
        return execute( pom, Arrays.asList(goals) );
    }

    /**
     * Execute goals for a pom
     *
     * @param pom String path
     * @param goals List
     * @return {@link InvocationResult}
     * @throws NaetherException exception
     */
    public InvocationResult execute(String pom, List<String> goals) throws NaetherException {
        if ( pom == null ) {
            throw new NaetherException("Pom path cannot be null");
        }

        if ( goals == null ) {
            throw new NaetherException("List<String> of goals cannot be null");
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(pom));
        request.setInteractive(false);
        request.setGoals( goals );

        logger.debug("Running {} goals: {}", pom, goals);

        try {
            return invoker.execute( request );
        } catch (MavenInvocationException e) {
            throw new NaetherException("Failed to execute "  + pom + " goals: " + StringUtils.join(goals.iterator(), " "),e);
        }
    }
}
