package com.slackworks.naether;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
   *
 * http://www.apache.org/licenses/LICENSE-2.0
   *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Java SE
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Exclusion;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

// Slackworks Naether
import com.slackworks.naether.Bootstrap;
import com.slackworks.naether.Naether;
import com.slackworks.naether.Notation;
import com.slackworks.naether.deploy.DeployArtifact;
import com.slackworks.naether.maven.Project;
import com.slackworks.naether.maven.ProjectException;


/**
 * Test for {@link Naether}
 * 
 * @author Michael Guymon
 *
 */
public class NaetherTest {
	
	private static Logger log = LoggerFactory.getLogger(NaetherTest.class);
	private Naether naether;
	
	@Before
	public void createNaether() {
		naether = new Naether();
		naether.setLocalRepoPath( "target/test-repo" );
		
		File dir = new File("target/test-repo");
		if ( dir.exists() ) {
			log.info( "Removing test maven repo: target/test-repo" );
			dir.delete();
		}
	}

	@Test
	public void addRemoteRepository() throws NaetherException {
		List<RemoteRepository> repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
		assertEquals( "central", repos.get(0).getId() );
		
		naether.addRemoteRepositoryByUrl( "http://test.net/hamster:7011" );
		repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
		assertEquals( "test.net-hamster-7011", repos.get(1).getId() );
		
		naether.addRemoteRepository( "test-id", "test-type", "http://test.net" );
		repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
		assertEquals( "test-id", repos.get(2).getId() );
	}
	
	@Test
	public void getDependenciesNotation() throws URLException, DependencyException {
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( false );
        assertEquals( "junit:junit:jar:4.8.2", naether.getDependenciesNotation().get(0) );
	}
	
	@Test
	public void getDependenciesPath() throws URLException, DependencyException {
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( true );
        
        Map<String, String> match =  naether.getDependenciesPath();
        assertTrue( "Has notation key", match.containsKey("junit:junit:jar:4.8.2") );
        assertTrue( "Has path", match.get("junit:junit:jar:4.8.2").contains("test-repo/junit/junit/4.8.2/junit-4.8.2.jar") );
	}
	
	@Test
	public void addDependenciesFromPom() throws ProjectException, URLException, DependencyException {
		naether.addDependencies( "src/test/resources/valid_pom.xml" );
		
		List<String> dependencies = new ArrayList<String>(Arrays.asList( 
	        "ch.qos.logback:logback-classic:jar:0.9.29", "ch.qos.logback:logback-core:jar:0.9.29", 
	        "org.slf4j:slf4j-api:jar:1.6.1", "junit:junit:jar:4.8.2", "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1", 
	        "com.google.code.gson:gson:jar:1.7.1", "com.google.code.greaze:greaze-definition:jar:0.5.1" ));
        naether.resolveDependencies(true);
		
		assertEquals( dependencies, naether.getDependenciesNotation() );
	}
	
	@Test
	public void addDependenciesFromPomWithScopes() throws ProjectException, URLException, DependencyException {
		List<String> scopes = new ArrayList<String>();
		List<String> expectedDependencies = new ArrayList<String>();
		
		scopes.add("test");
		naether.addDependencies( "src/test/resources/valid_pom.xml", scopes );
		naether.resolveDependencies(false);
		
		expectedDependencies.add( "junit:junit:jar:4.8.2" );
		expectedDependencies.add( "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1");
		expectedDependencies.add( "com.google.code.gson:gson:jar:1.7.1" ); 
		expectedDependencies.add( "com.google.code.greaze:greaze-definition:jar:0.5.1" );
		assertEquals( expectedDependencies, naether.getDependenciesNotation() );
		
		/* XXX: invalid system scopes are removed from dependencies
		scopes.add("system");
		naether.addDependencies( "src/test/resources/valid_pom.xml", scopes );
		naether.resolveDependencies(false);
		
		expectedDependencies.add( "google:collect:jar:1.0" );
		assertEquals( expectedDependencies, naether.getDependenciesNotation() );
		*/
		
		scopes.add("compile");
		naether.addDependencies( "src/test/resources/valid_pom.xml", scopes );
		naether.resolveDependencies(false);
		
		expectedDependencies.addAll( Arrays.asList( "ch.qos.logback:logback-classic:jar:0.9.29", 
        	"ch.qos.logback:logback-core:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1") );
		assertEquals( expectedDependencies, naether.getDependenciesNotation() );
	}
	
	@Test
	public void addDependenciesFromPomWithExclusions() throws ProjectException, URLException, DependencyException {
		Project project = new Project();
		org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
		dependency.setArtifactId( "jasper" );
		dependency.setGroupId( "org.apache.tomcat" );
		dependency.setVersion( "6.0.33" );
		
		Exclusion exclusion = new Exclusion();
		exclusion.setArtifactId( "catalina" );
		exclusion.setGroupId( "org.apache.tomcat" );
		dependency.addExclusion( exclusion );
		
		project.addDependency( dependency );
		
		naether.addDependencies( project );
		naether.resolveDependencies( false );
		
		List<String> resolvedDependencies = naether.getDependenciesNotation();
		
		List<String> results = new ArrayList<String>();
		results.add( "org.apache.tomcat:jasper:jar:6.0.33" );
		results.add( "org.apache.tomcat:servlet-api:jar:6.0.33" );
		results.add( "org.apache.tomcat:juli:jar:6.0.33" );
		results.add( "org.apache.tomcat:jsp-api:jar:6.0.33" );		
		results.add( "org.apache.tomcat:el-api:jar:6.0.33" );
		results.add( "org.eclipse.jdt.core.compiler:ecj:jar:3.3.1" );
		results.add( "org.apache.tomcat:jasper-el:jar:6.0.33" );
		assertEquals( results, resolvedDependencies );
	}
	
	@Test
	public void addDependenciesFromPomWithTestScope() throws ProjectException {
		List<String> scopes = new ArrayList<String>();
		scopes.add("test");
		
		naether.addDependencies( "src/test/resources/valid_pom.xml", scopes );
		List<String> dependencies = new ArrayList<String>();
		dependencies.add( "junit:junit:jar:4.8.2" );
		dependencies.add( "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1" );
		assertEquals( dependencies, naether.getDependenciesNotation() );
	}
	
	@Test
	public void resolveAPomShouldIncludeParent() throws ProjectException {
		naether.addDependencies( "src/test/resources/pomWithParent/parentTest/pom.xml" );
		List<String> dependencies = Arrays.asList( "org.apache.maven:maven-model-v3:jar:2.0", "ch.qos.logback:logback-classic:jar:0.9.29", "junit:junit:jar:4.8.2" );
		assertEquals( dependencies, naether.getDependenciesNotation() );
	
		List<String> repos = new ArrayList<String>();
		for ( RemoteRepository repo : naether.getRemoteRepositories()  ) {
			repos.add( repo.getId() );
		}
		
		assertEquals( Arrays.asList( "central", "org.jboss.repository" ), repos );
	}
	
	@Test
	public void resolveDepedenciesAndDownloadArtifacts() throws Exception {
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String classpath = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")).getAbsolutePath();
        assertEquals( classpath, naether.getResolvedClassPath() );
        assertTrue( (new File( classpath ).exists()) );
	}
	

	@Test
	public void resolveDependencyWithTestType() throws ProjectException, URLException, DependencyException {
		DefaultArtifact artifact = new DefaultArtifact( "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1");
		
		Dependency dependency =
	            new Dependency( artifact, "compile" );
		
		naether.addDependency(dependency);
        naether.resolveDependencies();
        
        File testClient = new File( "target/test-repo/com/google/code/greaze/greaze-client/0.5.1/greaze-client-0.5.1-test-jar.jar" );
        
        assertTrue( "test-jar is in classpath", naether.getResolvedClassPath().contains( testClient.getAbsolutePath()) );
        assertTrue( testClient.exists() );
	}
	
	@Test
	public void resolveDepedenciesWithoutDownloadingArtifacts() throws Exception {
		Dependency dependency =
            new Dependency( new DefaultArtifact( "org.testng:testng:jar:5.14" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies(false);
        assertEquals( "", naether.getResolvedClassPath() );
        
        String jarPath = "target/test-repo/org.testng/testng/5.14/testng-5.14.jar";
        assertFalse( (new File( jarPath ).exists()) );
	}
	
	
	@Test
	public void resolveNaetherDependencies() throws Exception {
		Project mavenProject = new Project("pom.xml");
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
		completeDeps.add( "org.sonatype.sisu:sisu-guice:jar:no_aop:3.0.3");
		completeDeps.add( "org.jboss.netty:netty:jar:3.2.5.Final");
		completeDeps.add( "junit:junit:jar:4.8.2");
		completeDeps.add( "log4j:log4j:jar:1.2.12");
		completeDeps.add( "commons-logging:commons-logging-api:jar:1.1");
		completeDeps.add( "commons-logging:commons-logging:jar:1.1.1");
		completeDeps.add( "nekohtml:xercesMinimal:jar:1.9.6.2" );
		completeDeps.add( "nekohtml:nekohtml:jar:1.9.6.2" );
		
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
	
	@Test
	public void resolveArtifactsNotInMainRepo() throws Exception {
		Dependency dependency =
            new Dependency( new DefaultArtifact( "org.springframework:org.springframework.orm:3.0.5.RELEASE" ), "compile" );
		naether.addRemoteRepositoryByUrl( "http://repository.springsource.com/maven/bundles/release" );
		naether.addRemoteRepositoryByUrl( "http://repository.springsource.com/maven/bundles/external" );
		naether.addDependency(dependency);
        naether.resolveDependencies(false);
        
        List<String> results = new ArrayList<String>();
        results.add("org.springframework:org.springframework.orm:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.beans:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.asm:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.core:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.jdbc:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.transaction:jar:3.0.5.RELEASE");
        results.add("org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0");
        results.add("org.springframework:org.springframework.aop:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.context:jar:3.0.5.RELEASE");
        results.add("org.springframework:org.springframework.expression:jar:3.0.5.RELEASE");
        		
        assertEquals( results, naether.getDependenciesNotation() );
	}
	
	@Test
	public void deployArtifact() throws Exception {
		// Use Naether to get a jar to deploy
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String jar = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")).getAbsolutePath();
        
        File destinationJar = new File("target/test-repo/test/test/0.4/test-0.4.jar");
        if ( destinationJar.exists() ) {
        	destinationJar.delete();
        }
        
        DeployArtifact deployArtifact = new DeployArtifact();
        deployArtifact.setFilePath( jar );
        deployArtifact.setNotation( "test:test:jar:0.4");
        deployArtifact.setRemoteRepo( new File( "target/test-repo" ).toURI().toString() );
        naether.deployArtifact( deployArtifact );
        assertTrue( destinationJar.exists() );
	}
	
	@Test
	public void installAJar() throws Exception {
		
		// Use Naether to get a jar to deploy
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String jar = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")).getAbsolutePath();
        
        File destinationJar = new File("target/test-repo/test/test-install/0.4/test-install-0.4.jar");
        if ( destinationJar.exists() ) {
        	destinationJar.delete();
        }
        
        naether.install( "test:test-install:jar:0.4", null, jar );
        assertTrue( "installed jar should exist", destinationJar.exists() );
	}
	
	@Test
	public void installAPom() throws Exception {
		
		// Use Naether to get a jar to deploy
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String pom = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.pom")).getAbsolutePath();
        
        File destinationPom = new File("target/test-repo/test/test-install/0.4/test-install-0.4.pom");
        if ( destinationPom.exists() ) {
        	destinationPom.delete();
        }
        
        naether.install( "test:test-install:0.4", pom, null );
        assertTrue( "installed pom should exist", destinationPom.exists() );
	}
	
	@Test
	public void installAPomAndJar() throws Exception {
		
		// Use Naether to get a jar to deploy
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        
        String jar = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")).getAbsolutePath();
        
        File destinationJar = new File("target/test-repo/test/test-install/0.4/test-install-0.4.jar");
        if ( destinationJar.exists() ) {
        	destinationJar.delete();
        }
        
        String pom = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.pom")).getAbsolutePath();
        
        File destinationPom = new File("target/test-repo/test/test-install/0.4/test-install-0.4.pom");
        if ( destinationPom.exists() ) {
        	destinationPom.delete();
        }
        
        naether.install( "test:test-install:0.4", pom, jar );
        assertTrue( "installed pom should exist", destinationPom.exists() );
        assertTrue( "installed jar should exist", destinationJar.exists() );
	}

	@Test
	public void getLocalPaths() throws NaetherException {
		assertTrue( "Path to junit in test repo", naether.getLocalPaths( Arrays.asList( "junit:junit:jar:4.8.2" ) ).get(0).contains("test-repo/junit/junit/4.8.2/junit-4.8.2.jar") );
	}
}
