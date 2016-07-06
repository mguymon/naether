package com.tobedevoured.naether;

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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Exclusion;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.artifact.DefaultArtifact;

// Slackworks Naether
import com.tobedevoured.naether.Bootstrap;
import com.tobedevoured.naether.DependencyException;
import com.tobedevoured.naether.NaetherException;
import com.tobedevoured.naether.URLException;
import com.tobedevoured.naether.api.Naether;
import com.tobedevoured.naether.deploy.DeployArtifact;
import com.tobedevoured.naether.impl.NaetherImpl;
import com.tobedevoured.naether.maven.Project;
import com.tobedevoured.naether.maven.ProjectException;
import com.tobedevoured.naether.util.Notation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;


/**
 * Test for {@link NaetherImpl}
 * 
 * @author Michael Guymon
 *
 */
public class NaetherTest {
    
    private static Logger log = LoggerFactory.getLogger(NaetherTest.class);
    private Naether naether;
    
    @Before
    public void createNaether() {
        naether = new NaetherImpl();
        naether.setLocalRepoPath( "target/test-repo" );
        
        File dir = new File("target/test-repo");
        if ( dir.exists() ) {
            log.info( "Removing test maven repo: target/test-repo" );
            dir.delete();
        }
    }

    @Test
    public void defaultLocalRepo() {
        naether = new NaetherImpl();
        assertEquals(  System.getProperty("user.home") + File.separator + ".m2" + File.separator +  "repository", naether.getLocalRepoPath() );
    
        Map env = new HashMap(System.getenv());
        env.put( "M2_REPO", "/m2_repo_test" );
        setEnv( env );
        
        naether = new NaetherImpl();
        assertEquals( "/m2_repo_test", naether.getLocalRepoPath() );
    
    }
    
    @Test
    public void addRemoteRepositoryByUrl() throws NaetherException {
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "central", repos.get(0).getId() );
        
        naether.addRemoteRepositoryByUrl( "http://test.net/hamster:7011" );
        repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "test.net-hamster-7011", repos.get(1).getId() );
    }
    
    @Test
    public void addRemoteRepositoryByUrlWithAuth() throws NaetherException {
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "central", repos.get(0).getId() );
        
        naether.addRemoteRepositoryByUrl( "http://test.net/hamster:7011", "test", "test" );
        repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "test.net-hamster-7011", repos.get(1).getId() );
    }
    
    @Test(expected=NaetherException.class)
    public void addRemoteRepositoryByUrlThatIsMalformed() throws NaetherException {
        naether.addRemoteRepositoryByUrl( "a bad url" );
    }
    
    @Test
    public void addRemoteRepository() throws NaetherException {
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "central", repos.get(0).getId() );
        
        naether.addRemoteRepository( "test-id", "test-type", "http://test.net" );
        repos = new ArrayList<RemoteRepository>( naether.getRemoteRepositories() );
        assertEquals( "test-id", repos.get(1).getId() );
    }
    
    @Test
    public void clearRemoteRepositories() throws NaetherException {
        naether.addRemoteRepositoryByUrl( "http://test.net/hamster:7011" );
        naether.clearRemoteRepositories();
        assertEquals( new HashSet(), naether.getRemoteRepositories() );
    }
    
    @Test
    public void getRemoteRepositoryUrls() throws NaetherException {
        naether.addRemoteRepositoryByUrl( "http://test.net/hamster:7011" );
        
        assertEquals( Arrays.asList("http://repo1.maven.org/maven2/", "http://test.net/hamster:7011"), naether.getRemoteRepositoryUrls() );
    }
    
    @Test
    public void getDependenciesNotation() throws URLException, DependencyException {
        Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( false );
        assertEquals( "junit:junit:jar:4.8.2", new ArrayList( naether.getDependenciesNotation() ).get(0) );
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
    public void getDependencyGraph() throws NaetherException {
        Dependency dependency =
            new Dependency( new DefaultArtifact( "org.springframework:org.springframework.orm:3.0.5.RELEASE" ), "compile" );
        naether.addRemoteRepositoryByUrl( "http://repository.springsource.com/maven/bundles/release" );
        naether.addRemoteRepositoryByUrl( "http://repository.springsource.com/maven/bundles/external" );
        naether.addDependency(dependency);
        naether.resolveDependencies(false);
        
        /*
         * This is the expected out:
         *  
            org.springframework:org.springframework.orm:jar:3.0.5.RELEASE={
                org.springframework:org.springframework.jdbc:jar:3.0.5.RELEASE={}, 
                org.springframework:org.springframework.transaction:jar:3.0.5.RELEASE={
                   org.springframework:org.springframework.context:jar:3.0.5.RELEASE={
                       org.springframework:org.springframework.expression:jar:3.0.5.RELEASE={}
                   }, 
                   org.springframework:org.springframework.aop:jar:3.0.5.RELEASE={}, 
                   org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0={}
                }, 
                org.springframework:org.springframework.core:jar:3.0.5.RELEASE={},    
                org.springframework:org.springframework.beans:jar:3.0.5.RELEASE={
                   org.springframework:org.springframework.asm:jar:3.0.5.RELEASE={}
                }
           }
       */
        Map<String,Map> ormDeps = new LinkedHashMap<String,Map>();
        ormDeps.put("org.springframework:org.springframework.jdbc:jar:3.0.5.RELEASE", new HashMap() );
        
        Map<String,Map> expressionDeps = new HashMap<String,Map>();
        expressionDeps.put( "org.springframework:org.springframework.expression:jar:3.0.5.RELEASE", new HashMap() );
        
        Map<String,Map> transactionDeps = new LinkedHashMap<String,Map>();
        transactionDeps.put("org.springframework:org.springframework.context:jar:3.0.5.RELEASE", expressionDeps);
        transactionDeps.put("org.springframework:org.springframework.aop:jar:3.0.5.RELEASE", new HashMap() );
        transactionDeps.put("org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0", new HashMap() );
        
        ormDeps.put("org.springframework:org.springframework.transaction:jar:3.0.5.RELEASE", transactionDeps );
        ormDeps.put("org.springframework:org.springframework.core:jar:3.0.5.RELEASE", new HashMap() );

        Map<String,Map> beansDeps = new HashMap<String,Map>();
        beansDeps.put("org.springframework:org.springframework.asm:jar:3.0.5.RELEASE", new HashMap() );
        
        ormDeps.put( "org.springframework:org.springframework.beans:jar:3.0.5.RELEASE", beansDeps );
        
        Map<String,Map> results = new HashMap<String,Map>();
        results.put("org.springframework:org.springframework.orm:jar:3.0.5.RELEASE", ormDeps);
        
        Map<String,Map> graph = naether.getDependenciesGraph();
        assertEquals( results, graph );

        Set<String> resolvedDependencies = naether.getDependenciesNotation();

        Set<String> dependencies = new HashSet<String>();
        dependencies.add("org.springframework:org.springframework.orm:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.beans:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.asm:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.core:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.jdbc:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.transaction:jar:3.0.5.RELEASE");
        dependencies.add("org.aopalliance:com.springsource.org.aopalliance:jar:1.0.0");
        dependencies.add("org.springframework:org.springframework.aop:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.context:jar:3.0.5.RELEASE");
        dependencies.add("org.springframework:org.springframework.expression:jar:3.0.5.RELEASE");
        assertEquals( dependencies, resolvedDependencies );
    }
    
    @Test
    public void addDependecyFromNotation() throws URLException, DependencyException {
        naether.addDependency( "group:artifact1:jar:1" );
        naether.addDependency( "group:artifact2:jar:test:1" );
        naether.addDependency( "group:artifact3:jar:test-jar:1" );
        assertEquals( new HashSet<String>(Arrays.asList( 
            "group:artifact2:jar:test-jar:1", "group:artifact1:jar:1", 
            "group:artifact3:jar:test-jar:1" ) ), 
            naether.getDependenciesNotation() );
    }
    
    @Test
    public void addDependenciesFromPom() throws ProjectException, URLException, DependencyException {
        naether.addDependencies( "src/test/resources/valid_pom.xml" );
        
        Set<String> dependencies = new HashSet<String>(Arrays.asList( 
            "ch.qos.logback:logback-core:jar:0.9.29",
            "org.slf4j:slf4j-api:jar:1.6.1", 
            "ch.qos.logback:logback-classic:jar:0.9.29") );
        naether.resolveDependencies(true);
        
        assertEquals( dependencies, naether.getDependenciesNotation() );
    }
    
    @Test
    public void addDependenciesFromPomWithScopes() throws ProjectException, URLException, DependencyException {
        List<String> scopes = new ArrayList<String>();
        Set<String> expectedDependencies = new HashSet<String>();
        
        scopes.add("test");
        naether.addDependencies( "src/test/resources/valid_pom.xml", scopes );
        naether.resolveDependencies(false);
        
        expectedDependencies.addAll( Arrays.asList( 
            "junit:junit:jar:4.8.2", "com.google.code.gson:gson:jar:1.7.1", 
            "com.google.code.greaze:greaze-definition:jar:0.5.1", 
            "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1" ) );
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
        
        expectedDependencies.addAll( Arrays.asList( 
            "org.slf4j:slf4j-api:jar:1.6.1", 
            "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1", 
            "ch.qos.logback:logback-core:jar:0.9.29",
            "ch.qos.logback:logback-classic:jar:0.9.29") );
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
        
        Set<String> resolvedDependencies = naether.getDependenciesNotation();
        
        Set<String> results = new HashSet<String>();
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
        Set<String> dependencies = new HashSet<String>();
        dependencies.add( "junit:junit:jar:4.8.2" );
        dependencies.add( "com.google.code.greaze:greaze-client:jar:test-jar:0.5.1" );
        assertEquals( dependencies, naether.getDependenciesNotation() );
    }
    
    @Test
    public void resolveAPomShouldIncludeParent() throws ProjectException {
        naether.addDependencies( "src/test/resources/pomWithParent/parentTest/pom.xml" );
        Set<String> dependencies = new HashSet<String>( Arrays.asList( 
            "org.apache.maven:maven-model-v3:jar:2.0",
            "ch.qos.logback:logback-classic:jar:0.9.29") );
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
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.10" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String junit = (new File( "target/test-repo/junit/junit/4.10/junit-4.10.jar")).getAbsolutePath();
        String hamcrest = (new File( "target/test-repo/org/hamcrest/hamcrest-core/1.1/hamcrest-core-1.1.jar")).getAbsolutePath();
        assertEquals( junit + ":" + hamcrest, naether.getResolvedClassPath() );
        assertTrue( (new File( junit ).exists()) );
        assertTrue( (new File( hamcrest ).exists()) );
    }
    

    @Test
    public void resolveDependencyWithTestType() throws ProjectException, URLException, DependencyException {
        DefaultArtifact artifact = new DefaultArtifact( "com.google.code.greaze:greaze-client:jar:test:0.5.1");
        
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
        
        assertEquals( "core", mavenProject.getArtifactId() );
        
        naether.addDependencies(mavenProject);
        
        naether.resolveDependencies();
        
        for( Dependency dependency : naether.getDependencies() ) {
            String notation = Notation.generate( dependency );
            log.debug( "Dependency: {} {}", notation, dependency.getScope() );
        }
        
        List<String> completeDeps = Bootstrap.DEPENDENCIES;
        Set<String> resolvedDeps = naether.getDependenciesNotation();

        assertEquals( completeDeps.size(), resolvedDeps.size() );

        List<String> missingDeps = new ArrayList<String>();
        for( String dep : completeDeps ) {
            if ( !naether.getDependenciesNotation().contains( dep ) ) {
                missingDeps.add( dep );
            }
        }

        if ( missingDeps.size() > 0 ) {
            LoggerFactory.getLogger(this.getClass()).warn( "Resolved Deps: {}", naether.getDependenciesNotation() );
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
        
        Set<String> results = new HashSet<String>();
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
    public void resolveFromBuildArtifacts() throws NaetherException {
        naether.addBuildArtifact("buildArtifact:test:jar:0.1", "target/lib/xbean-reflect-3.4.jar" );
        
        Dependency dependency =
                new Dependency( new DefaultArtifact( "buildArtifact:test:jar:0.1" ), "compile" );
        naether.addDependency(dependency);
        
        dependency =
                new Dependency( new DefaultArtifact( "org.testng:testng:jar:5.14" ), "compile" );
        naether.addDependency(dependency);
        
        naether.resolveDependencies();    
        
        assertEquals( new HashSet<String>(Arrays.asList( "buildArtifact:test:jar:0.1", "org.testng:testng:jar:5.14", "junit:junit:jar:3.8.1", 
                "org.beanshell:bsh:jar:2.0b4", "com.google.inject:guice:jar:2.0", "aopalliance:aopalliance:jar:1.0", 
                "com.beust:jcommander:jar:1.5" )), naether.getDependenciesNotation() );
    }
        
    @Test
    public void resolveFromBuildArtifactsWithPom() throws URLException, DependencyException {
        naether.addBuildArtifact("valid:pom:jar:3", "target/lib/xbean-reflect-3.4.jar", "src/test/resources/valid_pom.xml" );
        
        Dependency dependency =
                new Dependency( new DefaultArtifact( "valid:pom:jar:3" ), "compile" );
        naether.addDependency(dependency);
        
        dependency =
                new Dependency( new DefaultArtifact( "org.testng:testng:jar:5.14" ), "compile" );
        naether.addDependency(dependency);
        
        naether.resolveDependencies();    
        
        assertEquals( new HashSet<String>(Arrays.asList( "valid:pom:jar:3", "ch.qos.logback:logback-classic:jar:0.9.29", 
                "ch.qos.logback:logback-core:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1", "org.testng:testng:jar:5.14", 
                "junit:junit:jar:3.8.1", "org.beanshell:bsh:jar:2.0b4", "com.google.inject:guice:jar:2.0", 
                "aopalliance:aopalliance:jar:1.0", "com.beust:jcommander:jar:1.5" )), naether.getDependenciesNotation() );
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
    public void downloadArtifactsFromStringNotations() throws IOException, NaetherException {
        File jar1 = new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar");
        if ( jar1.exists() ) {
            FileUtils.deleteDirectory( jar1.getParentFile() );
        }
        
        File jar2 = new File( "target/test-repo/junit/junit/4.10/junit-4.10.jar");
        if ( jar2.exists() ) {
            FileUtils.deleteDirectory( jar2.getParentFile() );
        }
        
        assertFalse( "Jar1 should remove to test downloaded", jar1.exists() );
        assertFalse( "Jar2 should remove to test downloaded", jar2.exists() );
        
        List<String> notations = Arrays.asList( "junit:junit:4.10", "junit:junit:4.8.2" );
        naether.downloadArtifacts( notations );
        
        assertTrue( "Jar1 downloaded", jar1.exists() );
        assertTrue( "Jar2 downloaded", jar2.exists() );
    }
    
    @Test
    public void downloadArtifactsFromArtifacts() throws IOException, NaetherException {
        File jar1 = new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar");
        if ( jar1.exists() ) {
            FileUtils.deleteDirectory( jar1.getParentFile() );
        }
        
        File jar2 = new File( "target/test-repo/junit/junit/4.10/junit-4.10.jar");
        if ( jar2.exists() ) {
            FileUtils.deleteDirectory( jar2.getParentFile() );
        }

        assertFalse( "Jar1 should remove to test downloaded", jar1.exists() );
        assertFalse( "Jar2 should remove to test downloaded", jar2.exists() );
        
        List<DefaultArtifact> notations = Arrays.asList( new DefaultArtifact("junit:junit:4.10"), new DefaultArtifact("junit:junit:4.8.2") );
        naether.downloadArtifacts( notations );
        
        assertTrue( "Jar1 downloaded", jar1.exists() );
        assertTrue( "Jar2 downloaded", jar2.exists() );
    }

    @Test
    public void hasParentPom() throws ProjectException, DependencyException, URLException {
        naether.addDependencies("./src/test/resources/pomWithRemoteParent.xml");
        naether.resolveDependencies(true);

        Set expectedDeps = new HashSet(Arrays.asList(
            "org.hamcrest:hamcrest-core:jar:1.3",
            "org.hamcrest:hamcrest-library:jar:1.3",
            "org.springframework:spring-test:jar:4.2.7.RELEASE",
            "org.mockito:mockito-core:jar:1.10.19",
            "junit:junit:jar:4.12",
            "org.springframework.boot:spring-boot-starter-test:jar:1.3.6.RELEASE",
            "org.objenesis:objenesis:jar:2.1",
            "org.springframework:spring-core:jar:4.2.7.RELEASE"
        ));
        assertEquals(expectedDeps, naether.getDependenciesNotation());
    }
    
    // http://stackoverflow.com/a/7201825/1272477
    private static void setEnv(Map<String, String> newenv) {
      try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
          try {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
          } catch (Exception e2) {
            e2.printStackTrace();
          }
        } catch (Exception e1) {
            e1.printStackTrace();
        } 
    }
}
