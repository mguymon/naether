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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonatype.aether.artifact.Artifact;

import com.slackworks.naether.maven.Project;

/**
 * Helper for converting the dependency notations:
 * 
 *   groupId:artifactId:type:version
 * 
 * @author Michael Guymon
 * 
 */
public class Notation {
	
	// groupId:artifactId:extension:version
	private static Pattern notationPattern = Pattern.compile("^(.+):(.+):(.+):(.+)$");
	
	public static String generate(org.apache.maven.model.Dependency dependency) {
		StringBuffer notation = new StringBuffer()
				.append(dependency.getGroupId()).append(":")
				.append(dependency.getArtifactId()).append(":")
				.append(dependency.getType()).append(":")
				.append(dependency.getVersion());

		return notation.toString();
	}

	public static String generate( Project pom ) {
		return new StringBuilder( pom.getGroupId() )
			.append( ":" )
			.append( pom.getArtifactId() )
			.append( ":" )
			.append( pom.getType() )
			.append( ":" )
			.append( pom.getVersion() )
			.toString();
	}
	
	public static String generate(org.sonatype.aether.graph.Dependency dependency) {
		Artifact artifact = dependency.getArtifact();
		StringBuffer notation = new StringBuffer()
				.append(artifact.getGroupId()).append(":")
				.append(artifact.getArtifactId()).append(":")
				.append(artifact.getExtension()).append(":")
				.append(artifact.getVersion());

		return notation.toString();
	}
	
	public static Map<String,String> parse( String notation ) {
		Matcher matcher = notationPattern.matcher(notation);
		if ( matcher.find() ) {
			Map<String,String> notationMap = new HashMap<String,String>();
			notationMap.put( "groupId", matcher.group(1) );
			notationMap.put( "artifactId", matcher.group(2) );
			notationMap.put( "type", matcher.group(3) );
			notationMap.put( "version", matcher.group(4) );
			return notationMap;
		} else {
			return null;
		}
	}
}
