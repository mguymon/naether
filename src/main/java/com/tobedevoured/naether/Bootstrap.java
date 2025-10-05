package com.tobedevoured.naether;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.util.ArrayList;
import java.util.List;

/**
 * Helper for bootstrapping Naether
 *
 * @author Michael Guymon
 */
public final class Bootstrap {

    private Bootstrap() {}

    /** Dependencies for Naether to run */
    public static final List<String> DEPENDENCIES;

    static {
        List<String> deps = new ArrayList<String>();
        deps.add("com.jcraft:jsch:jar:0.1.55");
        deps.add("org.apache.maven.shared:maven-invoker:jar:3.2.0");
        deps.add("org.apache.maven:maven-resolver-provider:jar:3.9.9");
        deps.add("org.apache.maven:maven-model:jar:3.9.9");
        deps.add("org.apache.maven:maven-model-builder:jar:3.9.9");
        deps.add("org.apache.maven:maven-repository-metadata:jar:3.9.9");
        deps.add("org.apache.maven.wagon:wagon-file:jar:3.5.3");
        deps.add("org.apache.maven.wagon:wagon-http-lightweight:jar:3.5.3");
        deps.add("org.apache.maven.wagon:wagon-http-shared:jar:3.5.3");
        deps.add("org.apache.maven.wagon:wagon-provider-api:jar:3.5.3");
        deps.add("org.apache.maven.wagon:wagon-ssh:jar:3.5.3");
        deps.add("org.apache.maven.wagon:wagon-ssh-common:jar:3.5.3");
        deps.add("org.codehaus.plexus:plexus-interpolation:jar:1.27");
        deps.add("org.codehaus.plexus:plexus-utils:jar:3.5.1");
        deps.add("org.slf4j:slf4j-api:jar:1.7.36");
        deps.add("org.slf4j:slf4j-simple:jar:1.7.36");
        deps.add("org.apache.maven.resolver:maven-resolver-api:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-util:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-impl:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-spi:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-transport-file:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-connector-basic:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-transport-wagon:jar:2.0.11");
        deps.add("org.apache.maven.resolver:maven-resolver-supplier-mvn3:jar:2.0.11");
        DEPENDENCIES = deps;
    }

    public static List<String> dependencies() {
        return DEPENDENCIES;
    }
}
