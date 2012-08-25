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

import java.util.Map;

import org.junit.Test;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.slackworks.naether.util.Notation;

/**
 * Notation test
 * 
 * @author Michael Guymon
 *
 */
public class NotationTest {

	@Test
	public void parse() {
		Map<String,String> results = Notation.parse( "testGroup:testArtifact:testType:testVersion" );
		assertEquals( "testGroup", results.get( "groupId" ) );
		assertEquals( "testArtifact", results.get( "artifactId" ) );
		assertEquals( "testType", results.get( "type" ) );
		assertEquals( "testVersion", results.get( "version" ) );
		
		results = Notation.parse( "testGroup:testArtifact:testVersion" );
		assertEquals( "testGroup", results.get( "groupId" ) );
		assertEquals( "testArtifact", results.get( "artifactId" ) );
		assertEquals( null, results.get( "type" ) );
		assertEquals( "testVersion", results.get( "version" ) );
	}
	
	@Test
	public void generateForArtifact() {
		Artifact artifact = new DefaultArtifact("groupId", "artifactId", "classifier", "extension", "version");
	
		assertEquals( "groupId:artifactId:extension:classifier:version", Notation.generate(artifact) );
	}
}
