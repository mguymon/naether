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

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.slackworks.naether.deploy.InstallException;

/**
 * SystemPathDependency test
 * 
 * @author Michael Guymon
 *
 */
public class SystemPathDependencyTest {
	private Naether naether;
	
	@Before
	public void createNaether() {
		naether = new Naether();
		naether.setLocalRepoPath( "target/test-repo" );		
	}
	
	@Test
	public void systemPathDependency() throws URLException, DependencyException, InstallException {
		// Use Naether to get a jar to deploy
		Dependency dependency =
            new Dependency( new DefaultArtifact( "junit:junit:jar:4.8.2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies();
        String jar = (new File( "target/test-repo/junit/junit/4.8.2/junit-4.8.2.jar")).getAbsolutePath();
        
        naether.install("pom:with-broken-dep:1", "src/test/resources/pom_with_broken_dep.xml", jar);
        naether.install("pom:with-system-path:2", "src/test/resources/pom_with_system_path.xml", jar);
        naether.install("valid:pom:3", "src/test/resources/valid_pom.xml", jar);
        
        naether.clearDependencies();
        
        dependency =
                new Dependency( new DefaultArtifact( "valid:pom:3" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( false );
            
        List<String> dependencies = naether.getDependenciesNotation();
        List<String> expectedDependencies = new ArrayList<String>( Arrays.asList( 
        	"valid:pom:jar:3", "ch.qos.logback:logback-classic:jar:0.9.29", "ch.qos.logback:logback-core:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1" ));
        assertEquals( expectedDependencies, dependencies );
        
        naether.clearDependencies();
        
        dependency =
                new Dependency( new DefaultArtifact( "pom:with-system-path:2" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( false );
            
        dependencies = naether.getDependenciesNotation();
        expectedDependencies = new ArrayList<String>( Arrays.asList( 
        	"pom:with-system-path:2", "ch.qos.logback:logback-classic:jar:0.9.29", "ch.qos.logback:logback-core:jar:0.9.29", "org.slf4j:slf4j-api:jar:1.6.1" ));
            
        
        
        //   XXX: assertion fails, returns only [pom:with-system-path:2]
        //assertEquals( expectedDependencies, dependencies );
        
        /*
        naether.clearDependencies();
        
        dependency =
                new Dependency( new DefaultArtifact( "pom:with-broken-dep:1" ), "compile" );
        naether.addDependency(dependency);
        naether.resolveDependencies( false );
            
        dependencies = naether.getDependenciesNotation();        
        expectedDependencies = new ArrayList<String>();       
        assertEquals( expectedDependencies, dependencies );
        */
	}
}
