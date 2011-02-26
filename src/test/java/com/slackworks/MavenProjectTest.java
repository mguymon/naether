package com.slackworks;

// Java SE
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

// Junit
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author Michael Guymon
 *
 */
public class MavenProjectTest {

	@Test
	public void createDefaultInstance() {
		MavenProject mavenProject = new MavenProject();
		assertNotNull( mavenProject );
	}
	
	@Test
	public void createInstanceWithPomPath() {
		MavenProject mavenProject = null;
		try {
			mavenProject = new MavenProject("pom.xml");
		} catch (FileNotFoundException e) {
			fail( "pom not found: " + e.toString() );
		} catch (IOException e) {
			fail( "failed to read pom: " + e.toString() );
		} catch (XmlPullParserException e) {
			fail( "failed to parse pom xml: " + e.toString() );
		}
		
		assertNotNull( mavenProject.getMavenModel() );
		assertEquals( "0.1", mavenProject.getVersion() );
	}
	
	@Test
	public void getDependenciesNotation() throws FileNotFoundException, IOException, XmlPullParserException {
		MavenProject mavenProject = new MavenProject("pom.xml");
		List<String> notations = new ArrayList<String>();
		notations.add( "ch.qos.logback:logback-classic:jar:0.9.24" ); 
		notations.add( "org.slf4j:slf4j-api:jar:1.6.1");
		notations.add( "org.codehaus.plexus:plexus-utils:jar:1.5.8" );
		notations.add( "org.apache.maven:maven-model-v3:jar:2.0" );
		notations.add( "org.codehaus.plexus:plexus-container-default:jar:1.5.5" );
		notations.add( "org.sonatype.aether:aether-api:jar:1.11" );
		notations.add( "org.sonatype.aether:aether-util:jar:1.11" );
		notations.add( "org.sonatype.aether:aether-impl:jar:1.11" );
		notations.add( "org.sonatype.aether:aether-connector-file:jar:1.11" );
		notations.add( "org.sonatype.aether:aether-connector-asynchttpclient:jar:1.11" );
		notations.add( "org.sonatype.aether:aether-connector-wagon:jar:1.11" );
		notations.add( "org.apache.maven:maven-aether-provider:jar:3.0.2" );
		notations.add( "org.apache.maven.wagon:wagon-ssh:jar:1.0-beta-7" );
		assertEquals(notations, mavenProject.getDependenciesNotation() );
	}
}
