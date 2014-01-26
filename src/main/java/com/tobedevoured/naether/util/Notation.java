package com.tobedevoured.naether.util;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.artifact.DefaultArtifact;

import com.tobedevoured.naether.Const;
import com.tobedevoured.naether.NaetherException;
import com.tobedevoured.naether.maven.Project;

/**
 * Helper for converting the dependency notations:
 * 
 *   groupId:artifactId:version, groupId:artifactId:type:version, groupId:artifactId:type:classifier:version 
 * 
 * @author Michael Guymon
 * 
 */
public final class Notation {
	
	private static final Pattern NOTATION_PATTERN = Pattern.compile("^(.+?):(.+?):(.+?)(:(.+))?$");
	
	private Notation() { }
	
	/**
	 * Convert a {@link org.apache.maven.model.Dependency} to String notation of
	 * groupId:artifactId:type:version or groupId:artifactId:type:classifier:version
	 * 
	 * @param dependency
	 * @return String notation
	 */
	public static String generate(org.apache.maven.model.Dependency dependency) {
		StringBuilder notation = new StringBuilder()
				.append(dependency.getGroupId()).append(":")
				.append(dependency.getArtifactId()).append(":");
		
		String classifier = dependency.getClassifier();
		if ( classifier != null && Const.TEST_JAR.equals( classifier ) ) {
			notation.append( "jar:test-jar:");
		} else {
			String type = dependency.getType();
			if ( Const.TEST_JAR.equals( type ) ) {
				notation.append("jar:test-jar:");
			} else {
				notation.append(dependency.getType()).append(":");
			}
		}
		notation.append(dependency.getVersion());
		
		return notation.toString();
	}

	/**
	 * Convert a {@link Project} to String notation of
	 * groupId:artifactId:type:version
	 * 
	 * @param pom
	 * @return String notation
	 */
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
	
	/**
	 * Convert a {@link org.eclipse.aether.graph.Dependency} to String notation of
	 * groupId:artifactId:extension:version or groupId:artifactId::type:classifier:version
	 * 
	 * @param dependency
	 * @return String notation
	 */
	public static String generate(org.eclipse.aether.graph.Dependency dependency) {
		return dependency.getArtifact().toString();		
	}
	
	/**
	 * Convert a {@link Map<String,String>} to String notation of
	 * groupId:artifactId:version or if type is present, groupId:artifactId:type:version
	 * 
	 * @param notationMap Map<String,String>
	 * @return String notation
	 * @see {@link #parse(String)}
	 */
	public static String generate( Map<String,String> notationMap ) {
		StringBuilder notation = new StringBuilder();
		notation.append( notationMap.get( "groupId" ) ).append(":").append( notationMap.get( "artifactId") ).append(":");
		
		if ( notationMap.get( "type" ) != null ) {
			notation.append( notationMap.get("type") ).append( ":" );
		}
		
		notation.append( notationMap.get( "version" ) );
		
		return notation.toString();
	}
	
	/**
	 * Parsing the notation and returns a Map with keys
	 *   <ul>
	 *   <li>groupId</li>
	 *   <li>artifactId</li>
	 *   <li>type - may be null if not in the notation param</li>
	 *   <li>version</li>
	 *   </ul>
	 *      
	 * @param notation String
	 * @return Map<String,String>
	 */
	public static Map<String,String> parse( String notation ) {
		Matcher matcher = NOTATION_PATTERN.matcher(notation);
		if ( matcher.find() ) {
			
			Map<String,String> notationMap = new HashMap<String,String>();
			
			int groupIdMatch = 1;
			notationMap.put( "groupId", matcher.group(groupIdMatch) );
			
			int artifactIdMatch = 2;
			notationMap.put( "artifactId", matcher.group(artifactIdMatch) );
			
			// if type is null
			int groupingMatch = 4;
			if ( matcher.group(groupingMatch) == null ) {
				int versionMatch = 3;
				notationMap.put( "version", matcher.group(versionMatch) );
				
			// if type exists
			} else {
				int typeMatch = 3;
				notationMap.put( "type", matcher.group(typeMatch) );
				
				int versionMatch = 5;
				notationMap.put( "version", matcher.group(versionMatch) );
			}
			
			return notationMap;
		} else {
			return null;
		}
	}

	/**
	 * Convert a {@link Artifact} to String notation of
	 * groupId:artifactId::type:classifier:version
	 * 
	 * @param artifact {@link Artifact}
	 * @return String notation
	 */
	public static String generate(Artifact artifact) {
		StringBuilder notation = new StringBuilder()
			.append(artifact.getGroupId()).append(":")
			.append(artifact.getArtifactId()).append(":")
			.append(artifact.getExtension()).append(":");
		
		if ( artifact.getClassifier() != null && artifact.getClassifier().length() > 0 ) {
			notation.append(artifact.getClassifier()).append(":");
		}
		
		notation.append(artifact.getBaseVersion());
		
		return notation.toString();
	}
	
	/**
	 * Get local paths for notations
	 *
	 * @param notations List<String> of notations
	 * @return List<String> of paths
	 * @throws NaetherException
	 */
	public static List<String> getLocalPaths( RepositorySystemSession repoSession, List<String> notations ) throws NaetherException {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		SimpleLocalRepositoryManagerFactory factory = new SimpleLocalRepositoryManagerFactory();
		factory.initService( locator );

		LocalRepositoryManager manager = repoSession.getLocalRepositoryManager();
		
		List<String> localPaths = new ArrayList<String>();
		
		for ( String notation : notations ) {
			Dependency dependency = new Dependency(new DefaultArtifact(notation), "compile");
			String path = new StringBuilder( repoSession.getLocalRepository().getBasedir().getAbsolutePath() )
				.append( File.separator ).append( manager.getPathForLocalArtifact( dependency.getArtifact() ) ).toString();
			localPaths.add( path );
		}
		
		return localPaths;
	}
}
