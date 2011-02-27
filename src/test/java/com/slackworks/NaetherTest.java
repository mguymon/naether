package com.slackworks;

// Java SE
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// JUnit
import org.junit.Test;
import static org.junit.Assert.*;

// Sonatype Aether
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;


/**
 * Test for {@link Naether}
 * 
 * @author Michael Guymon
 *
 */
public class NaetherTest {
	
	private static Logger log = LoggerFactory.getLogger(NaetherTest.class);
	
	@Test
	public void getDependenciesNotation() {
		Naether naether = new Naether();
		naether.setRepoPath( "target/test-repo" );
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        
        assertEquals( "junit:junit:jar:4.8.2", naether.getDependenciesNotation().get(0) );
	}
	
	@Test
	public void resolveDepedencies() throws Exception {
		Naether naether = new Naether();
		naether.setRepoPath( "target/test-repo" );
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String classpath = System.getProperty("user.dir") + "/target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar";
        assertEquals( classpath, naether.getResolvedClassPath() );
        assertTrue( (new File( classpath ).exists()) );
	}
	
	@Test
	public void resolveNaetherDependencies() throws Exception {
		Naether naether = new Naether();
		naether.setRepoPath( "target/test-repo" );
		
		MavenProject mavenProject = new MavenProject("pom.xml");
		for( org.apache.maven.model.Dependency mavenDep : mavenProject.getDependencies() ) {
			String notation = Notation.generate( mavenDep );
			
			Dependency dependency =
	            new Dependency( new DefaultArtifact( notation ), mavenDep.getScope() );
			naether.addDependency( dependency );
		}
		
		naether.resolveDependencies();
		
		for( Dependency dependency : naether.getDependencies() ) {
			String notation = Notation.generate( dependency );
			log.debug( "Dependency: {} {}", notation, dependency.getScope() );
		}
		
		List<String> completeDeps = Bootstrap.dependencies;
		
		// Jars excluded from bootstrap dependencies
		completeDeps.add( "junit:junit:jar:3.8.2");
		completeDeps.add( "log4j:log4j:jar:1.2.12");
		completeDeps.add( "commons-logging:commons-logging-api:jar:1.1");
		completeDeps.add( "commons-logging:commons-logging:jar:1.1.1");
		
		List<String> missingDeps = new ArrayList<String>();
        for( String dep : naether.getDependenciesNotation() ) {
        	if ( completeDeps.indexOf( dep ) == -1 ) {
        		missingDeps.add( dep );
        	}
        }
        
        if ( missingDeps.size() > 0 ) {
        	fail( "Missing Dependencies: " + missingDeps );
        }
		
	}

}
