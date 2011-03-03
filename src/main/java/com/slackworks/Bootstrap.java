package com.slackworks;

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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for bootstrapping Naether
 * 
 * @author Michael Guymon
 * 
 */
public class Bootstrap {

	/**
	 * Dependencies for Naether to run
	 */
	public static final List<String> dependencies;
	static {
		List<String> deps = new ArrayList<String>();
		deps.add("com.jcraft:jsch:jar:0.1.38");
		deps.add("com.ning:async-http-client:jar:1.6.1");
		deps.add("org.apache.xbean:xbean-reflect:jar:3.4");
		deps.add("org.jboss.netty:netty:jar:3.2.4.Final");
		deps.add("com.google.collections:google-collections:jar:1.0");
		deps.add("ch.qos.logback:logback-classic:jar:0.9.24");
		deps.add("ch.qos.logback:logback-core:jar:0.9.24");
		deps.add("org.slf4j:slf4j-api:jar:1.6.1");
		deps.add("org.slf4j:jcl-over-slf4j:jar:1.6.1");
		deps.add("org.slf4j:log4j-over-slf4j:jar:1.6.1");
		deps.add("org.sonatype.aether:aether-api:jar:1.11");
		deps.add("org.sonatype.aether:aether-util:jar:1.11");
		deps.add("org.sonatype.aether:aether-impl:jar:1.11");
		deps.add("org.sonatype.aether:aether-spi:jar:1.11");
		deps.add("org.sonatype.aether:aether-connector-file:jar:1.11");
		deps.add("org.sonatype.aether:aether-connector-asynchttpclient:jar:1.11");
		deps.add("org.sonatype.aether:aether-connector-wagon:jar:1.11");
		deps.add("org.apache.maven:maven-model-v3:jar:2.0");
		deps.add("org.apache.maven:maven-aether-provider:jar:3.0.2");
		deps.add("org.apache.maven:maven-model:jar:3.0.2");
		deps.add("org.apache.maven:maven-model-builder:jar:3.0.2");
		deps.add("org.apache.maven:maven-repository-metadata:jar:3.0.2");
		deps.add("org.apache.maven.wagon:wagon-provider-api:jar:1.0-beta-6");
		deps.add("org.apache.maven.wagon:wagon-ssh-common:jar:1.0-beta-7");
		deps.add("org.apache.maven.wagon:wagon-http-lightweight:jar:1.0-beta-7");
		deps.add("org.apache.maven.wagon:wagon-http-shared:jar:1.0-beta-7");
		deps.add("org.apache.maven.wagon:wagon-ssh:jar:1.0-beta-7");
		deps.add("org.codehaus.plexus:plexus-utils:jar:1.5.8");
		deps.add("org.codehaus.plexus:plexus-container-default:jar:1.5.5");
		deps.add("org.codehaus.plexus:plexus-classworlds:jar:2.2.2");
		deps.add("org.codehaus.plexus:plexus-component-annotations:jar:1.5.5");
		deps.add("org.codehaus.plexus:plexus-interpolation:jar:1.14");
		deps.add("org.codehaus.plexus:plexus-interactivity-api:jar:1.0-alpha-6");
		dependencies = deps;
	}
}
