package com.tobedevoured.naether.maven;

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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
			mavenProject = new Project("src/test/resources/valid_pom.xml");
		} catch (ProjectException e) {
			fail("failed to read pom: " + e.toString());
		}
		
		assertNotNull(mavenProject.getMavenModel());
		assertEquals( "3", mavenProject.getVersion());
		assertEquals( "valid:pom:jar:3", mavenProject.getProjectNotation() );
	}
	
	@Test(expected=ProjectException.class)
	public void createInstanceWithPomPathWithException() throws Exception {
		new Project("src/test/resources/does_not_exist_pom.xml");
	}

    @Test
	public void setProjectNotation() {
		Project project = new Project();
		project.setProjectNotation("project:test:10");
        assertEquals( "project", project.getGroupId() );
		assertEquals( "test", project.getArtifactId() );
		assertEquals( "10", project.getVersion() );
	}
	
	@Test
	public void getRepositories() throws ProjectException {
		Project mavenProject = new Project("src/test/resources/valid_pom.xml");
		List<String> repositories = mavenProject.getRepositoryUrls();
		
		assertEquals( Arrays.asList( "http://repository.jboss.org/nexus/content/groups/public-jboss", "http://repo1.maven.org/maven2" ), repositories );
	}

	@Test
	public void getDependenciesByScope() throws ProjectException {
		Project mavenProject = new Project("src/test/resources/valid_pom.xml");
		
		List<String> scopes = new ArrayList<String>();
		scopes.add("test");
		
		List<Dependency> dependencies = mavenProject.getDependencies( scopes );
		assertEquals( "Only junit and greaze-client should be in deps", 2, dependencies.size() );
		assertEquals( "junit", dependencies.get(0).getArtifactId() );
		assertEquals( "junit", dependencies.get(0).getGroupId() );
		
		assertEquals( "greaze-client", dependencies.get(1).getArtifactId() );
		assertEquals( "com.google.code.greaze", dependencies.get(1).getGroupId() );
				
		scopes = new ArrayList<String>();
		scopes.add("compile");
		
		dependencies = mavenProject.getDependencies( scopes );
		assertEquals( "Only logback classic should be in deps", 1, dependencies.size() );
		assertEquals( "logback-classic", dependencies.get(0).getArtifactId() );
		assertEquals( "ch.qos.logback", dependencies.get(0).getGroupId() );
		
		for ( Dependency dependency : dependencies ) {
			assertFalse( "junit dep should not be in dependencies", "junit".equals( dependency.getArtifactId() ) );
		}
	}
	
	@Test
	public void getDependenciesNotation() throws ProjectException {
		Project mavenProject = new Project("src/test/resources/valid_pom.xml");
		List<String> notations = new ArrayList<String>();
		notations.add("ch.qos.logback:logback-classic:jar:0.9.29");
				
		assertEquals( notations, mavenProject.getDependenciesNotation() );

		mavenProject = new Project("src/test/resources/valid_pom.xml");
		notations = new ArrayList<String>();
		notations.add("junit:junit:jar:4.8.2");
		notations.add("com.google.code.greaze:greaze-client:jar:test-jar:0.5.1");
				
		assertEquals( notations, mavenProject.getDependenciesNotation( Arrays.asList( "test" ) ) );
	}

	@Test
	public void substituteProperty() throws ProjectException {
		Project project = new Project( "src/test/resources/pom_with_system_path.xml" );
		List<Dependency> dependencies = project.getDependencies();
		
		for ( Dependency dependency : dependencies ) {
			if ( "gdata-spreadsheet".equals( dependency.getArtifactId() ) ) {
				assertEquals( (new File( "src/test/resources")).getAbsolutePath(), dependency.getSystemPath() );
			}
		}
	}

    /*
     * XXX: Naether pom no longer has a parent
	@Test
	public void hasParentPom() throws ProjectException {
		Project coreProject = new Project("pom.xml");
		Project parentProject = new Project("../pom.xml" );
		
		assertEquals( coreProject.getVersion(), parentProject.getVersion() );
	}
	*/
	
	@Test
	public void toXml() throws ProjectException, FileNotFoundException, IOException {
		Project project = new Project();
		project.setArtifactId("testArtifact");
		project.setGroupId("testGroup");
		project.setVersion("test");
		project.setType("jar");
		project.addDependency("junit:junit:jar:4.8.2", "test");
		project.addDependency("org.apache.maven.wagon:wagon-file:jar:1.0");
		project.addRepository( "http://repo1.maven.org/maven2/" );
		project.addRepository( "http://repository.jboss.org/nexus/content/groups/public-jboss" );
		String xml = project.toXml();
		
		assertEquals( IOUtils.toString( new FileReader("src/test/resources/generated_pom.xml") ), xml );
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

    @Test
    public void finalName() {
        Project mavenProject = null;
        try {
            mavenProject = new Project("pom.xml");
        } catch (ProjectException e) {
            fail("failed to read pom: " + e.toString());
        }

        assertEquals( "core-" + mavenProject.getVersion(), mavenProject.getFinalName() );
    }
}
