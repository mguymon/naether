package com.slackworks.naether.maven;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.junit.Test;

/**
 * 
 * @author Michael Guymon
 * 
 */
public class ProjectTest {

	@Test
	public void createDefaultInstance() {
		Project mavenProject = new Project();
		assertNotNull(mavenProject);
	}

	@Test
	public void createInstanceWithPomPath() throws IOException {
		Project mavenProject = null;
		try {
			mavenProject = new Project("pom.xml");
		} catch (ProjectException e) {
			fail("failed to read pom: " + e.toString());
		}
		
		assertNotNull(mavenProject.getMavenModel());
		assertEquals( FileUtils.readFileToString( new File("VERSION") ), mavenProject.getVersion());
	}

	@Test
	public void getDependenciesByScope() throws ProjectException {
		Project mavenProject = new Project("src/test/resources/valid_pom.xml");
		
		List<String> scopes = new ArrayList<String>();
		scopes.add("test");
		
		List<Dependency> dependencies = mavenProject.getDependencies( scopes, true );
		assertEquals( "Only junit and greaze-client should be in deps", 2, dependencies.size() );
		assertEquals( "junit", dependencies.get(0).getArtifactId() );
		assertEquals( "junit", dependencies.get(0).getGroupId() );
		
		assertEquals( "greaze-client", dependencies.get(1).getArtifactId() );
		assertEquals( "com.google.code.greaze", dependencies.get(1).getGroupId() );
				
		scopes = new ArrayList<String>();
		scopes.add("compile");
		
		dependencies = mavenProject.getDependencies( scopes, true );
		assertEquals( "Only logback classic should be in deps", 1, dependencies.size() );
		assertEquals( "logback-classic", dependencies.get(0).getArtifactId() );
		assertEquals( "ch.qos.logback", dependencies.get(0).getGroupId() );
		
		for ( Dependency dependency : dependencies ) {
			assertFalse( "junit dep should not be in dependencies", "junit".equals( dependency.getArtifactId() ) );
		}
	}
	
	@Test
	public void getDependenciesNotation() throws ProjectException {
		Project mavenProject = new Project("pom.xml");
		List<String> notations = new ArrayList<String>();
		notations.add("ch.qos.logback:logback-classic:jar:0.9.29");
		notations.add("org.slf4j:slf4j-api:jar:1.6.2");
		notations.add("org.slf4j:jcl-over-slf4j:jar:1.6.2");
		notations.add("org.slf4j:log4j-over-slf4j:jar:1.6.2");
		notations.add("org.codehaus.plexus:plexus-utils:jar:3.0");
		notations.add("org.apache.maven:maven-model-v3:jar:2.0");
		notations.add("org.codehaus.plexus:plexus-container-default:jar:1.5.5");
		notations.add("org.sonatype.aether:aether-api:jar:1.13");
		notations.add("org.sonatype.aether:aether-util:jar:1.13");
		notations.add("org.sonatype.aether:aether-impl:jar:1.13");
		notations.add("org.sonatype.aether:aether-connector-file:jar:1.13");
		notations.add("org.sonatype.aether:aether-connector-asynchttpclient:jar:1.13");
		notations.add("org.sonatype.aether:aether-connector-wagon:jar:1.13");
		notations.add("org.apache.maven:maven-aether-provider:jar:3.0.3");
		notations.add("org.apache.maven.wagon:wagon-ssh:jar:1.0");
		notations.add("org.apache.maven.wagon:wagon-http-lightweight:jar:1.0");
		notations.add("org.apache.maven.wagon:wagon-file:jar:1.0");
		notations.add("commons-logging:commons-logging:jar:1.1.1" );
		notations.add("commons-logging:commons-logging-api:jar:1.1" );
		notations.add("junit:junit:jar:4.8.2");
				
		List<String> missingDeps = new ArrayList<String>();
		for (String dep : mavenProject.getDependenciesNotation()) {
			if (notations.indexOf(dep) == -1) {
				missingDeps.add(dep);
			}
		}

		if (missingDeps.size() > 0) {
			fail("Missing Dependencies: " + missingDeps);
		}
	}

	@Test
	public void writePom() throws ProjectException {
		Project project = new Project();
		project.setArtifactId("testArtifact");
		project.setGroupId("testGroup");
		project.setVersion("test");
		project.setType("jar");
		project.addDependency("org.apache.maven.wagon:wagon-file:jar:1.0");
		project.addDependency("junit:junit:jar:4.8.2", "test");
	
		project.writePom("target/test-classes/test-pom.xml");
		assertTrue((new File("target/test-classes/test-pom.xml")).exists());
		
		Project testProject = new Project( "target/test-classes/test-pom.xml" );
		assertEquals( project.getArtifactId(), testProject.getArtifactId() );
		assertEquals( project.getGroupId(), testProject.getGroupId() );
		assertEquals( project.getVersion(), testProject.getVersion() );
		assertEquals( project.getType(), testProject.getType() );
	}
}
