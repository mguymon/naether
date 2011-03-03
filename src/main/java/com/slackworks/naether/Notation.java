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

import org.sonatype.aether.artifact.Artifact;

/**
 * Helper for converting a Dependency object into String
 * 
 * groupId:artifactId:type:version
 * 
 * @author Michael Guymon
 * 
 */
public class Notation {
	public static String generate(org.apache.maven.model.Dependency dependency) {
		StringBuffer notation = new StringBuffer()
				.append(dependency.getGroupId()).append(":")
				.append(dependency.getArtifactId()).append(":")
				.append(dependency.getType()).append(":")
				.append(dependency.getVersion());

		return notation.toString();
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
}
