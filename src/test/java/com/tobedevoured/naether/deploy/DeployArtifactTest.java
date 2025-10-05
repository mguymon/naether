package com.tobedevoured.naether.deploy;

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

// JUnit
import static org.junit.Assert.*;

import com.tobedevoured.naether.util.RepoBuilder;
import java.net.MalformedURLException;
import org.junit.Before;
import org.junit.Test;

public class DeployArtifactTest {

    private DeployArtifact instance;

    @Before
    public void createDeployArtifact() {
        instance = new DeployArtifact();
    }

    @Test
    public void createDeployArtifactWithParameters() throws MalformedURLException {
        instance =
                new DeployArtifact(
                        "groupId:artifactId:extension:version",
                        "non-existing-path",
                        RepoBuilder.remoteRepositoryFromUrl("http://example.net/repo"));
        assertEquals("artifactId", instance.getJarArtifact().getArtifactId());
        assertEquals("groupId", instance.getJarArtifact().getGroupId());
        assertEquals("version", instance.getJarArtifact().getVersion());
        assertEquals("extension", instance.getJarArtifact().getExtension());
        assertEquals("non-existing-path", instance.getFilePath());
        assertEquals("non-existing-path", instance.getJarArtifact().getFile().getPath());
        assertEquals("example.net-repo", instance.getRemoteRepo().getId());
    }

    @Test
    public void setFilePath() {
        instance.setFilePath("file-path");
        assertEquals("file-path", instance.getFilePath());
        assertNull(instance.getJarArtifact());

        instance.setNotation("groupId:artifactId:extension:version");
        assertEquals("file-path", instance.getJarArtifact().getFile().getPath());
    }

    @Test
    public void setNotation() {
        instance.setNotation("groupId:artifactId:extension:version");
        assertEquals("groupId:artifactId:extension:version", instance.getJarArtifact().toString());
    }

    @Test
    public void setPomPath() {

        instance.setPomPath("pom-path");
        assertNull(instance.getPomArtifact());

        instance.setNotation("groupId:artifactId:extension:version");
        assertEquals("pom-path", instance.getPomArtifact().getFile().getPath());
    }

    @Test
    public void setAuth() throws MalformedURLException {
        instance.setAuth("username", "password");
        assertNull(instance.getJarArtifact());

        instance.setRemoteRepo("http://example.com");
        // Authentication is now opaque in Maven Resolver 2.x - cannot inspect username/password
        // assertEquals( "username", instance.getAuthentication().getUsername() );
        // assertEquals( "password", instance.getAuthentication().getPassword() );
        assertNotNull(instance.getAuthentication());
        assertEquals(instance.getAuthentication(), instance.getRemoteRepo().getAuthentication());
    }

    @Test
    public void setAuthForPubKey() throws MalformedURLException {
        instance.setAuth(null, null, "pub-key-path", "pub-key-passphrase");
        assertNull(instance.getJarArtifact());

        instance.setRemoteRepo("http://example.com");
        // Authentication is now opaque in Maven Resolver 2.x - cannot inspect private key details
        // assertEquals( "pub-key-path", instance.getAuthentication().getPrivateKeyFile() );
        // assertEquals( "pub-key-passphrase", instance.getAuthentication().getPassphrase() );
        assertNotNull(instance.getAuthentication());
        assertEquals(instance.getAuthentication(), instance.getRemoteRepo().getAuthentication());
    }
}
