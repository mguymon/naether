package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.NaetherException;
import com.tobedevoured.naether.util.Env;
import org.apache.maven.shared.invoker.InvocationResult;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InvokerTest {

    @Test
    public void execute() throws NaetherException {
        // Disable this test for travis
        if ( Env.get("TRAVIS") != "yes") {
            Invoker invoker = new Invoker("target/test-repo", Env.getMavenHome());
            InvocationResult result = invoker.execute("pom.xml", "compile");
            assertThat( "Exit code should be zero", result.getExitCode(), is(0) );
        }
    }
}
