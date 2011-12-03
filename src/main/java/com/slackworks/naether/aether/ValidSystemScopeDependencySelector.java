package com.slackworks.naether.aether;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

/**
 * {@link DependencySelector} that checks the existence of system scoped 
 * dependencie's systemPath. If it does not exist on the file system, the 
 * dependency is excluded. 
 * 
 * @author Michael Guymon
 *
 */
public class ValidSystemScopeDependencySelector implements DependencySelector {

	//private static Logger log = LoggerFactory.getLogger(ValidSystemScopeDependencySelector.class);
	private static final String SYSTEM_SCOPE = "system";
	
	public boolean selectDependency(Dependency dependency) {
		if ( SYSTEM_SCOPE.equals( dependency.getScope() ) ) {
			String localPath = dependency.getArtifact().getProperties().get( "localPath" );
			if ( localPath == null || !(new File( localPath )).exists() ) {
				return false;
			}
		}
		
		return true;
	}

	public DependencySelector deriveChildSelector( DependencyCollectionContext context) {
		return this;
	}

}
