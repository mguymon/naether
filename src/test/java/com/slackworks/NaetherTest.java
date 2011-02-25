package com.slackworks;

// Java SE
import java.io.File;

// JUnit
import org.junit.Test;
import static org.junit.Assert.*;

// Sonatype Aether
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;


/**
 * Test for {@link Naether}
 * 
 * @author Michael Guymon
 *
 */
public class NaetherTest {
	
	@Test
	public void resolveDepedencies() throws Exception {
		Naether naether = new Naether();
		naether.setRepoPath( "target/test-repo" );
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDepedency(dependency);
        naether.resolveDependencies();
        String classpath = System.getProperty("user.dir") + "/target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar";
        assertEquals( classpath, naether.getResolvedClassPath() );
        assertTrue( (new File( classpath ).exists()) );
	}

}
