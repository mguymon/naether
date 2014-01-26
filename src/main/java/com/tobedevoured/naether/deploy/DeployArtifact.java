package com.tobedevoured.naether.deploy;

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
import java.net.MalformedURLException;

// Sonatype Aether
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.artifact.SubArtifact;

import com.tobedevoured.naether.util.RepoBuilder;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

/**
 * Information for deploying an {@link Artifact}
 * 
 * @author Michael Guymon
 *
 */
public class DeployArtifact {

	private RemoteRepository remoteRepo;
	private String pomPath;
	private String filePath;
	private String notation;
	private Artifact jarArtifact;
	private Artifact pomArtifact;
	private Authentication authentication;
	
	public DeployArtifact() {
		
	}
	
	public DeployArtifact( String notation, String filePath, RemoteRepository remoteRepo ) {
		this.notation = notation;
		jarArtifact = new DefaultArtifact( notation );
		
		this.filePath = filePath;
		jarArtifact = jarArtifact.setFile( new File( filePath ) );
		
		this.remoteRepo = remoteRepo;
	}
	
	public void setAuth( String username, String password ) {
        final AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        authBuilder.addUsername(username);
        authBuilder.addPassword(password);
        authentication = authBuilder.build();

        // If RemoteRepo was already set, reapply to set the new auth settings
        if ( getRemoteRepo() != null ) {
           setRemoteRepo( getRemoteRepo() );
		}
	}

	public void setAuth( String username, String password, String keyFile, String passphrase ) {

        final AuthenticationBuilder authBuilder = new AuthenticationBuilder();;
        authBuilder.addUsername(username);
        authBuilder.addPassword(password);
        authBuilder.addPrivateKey(keyFile, passphrase);

        authentication = authBuilder.build();

        // If RemoteRepo was already set, reapply to set the new auth settings
        if ( getRemoteRepo() != null ) {
            setRemoteRepo( getRemoteRepo() );
        }

	}


	public void setRemoteRepo( String url ) throws MalformedURLException {
		this.setRemoteRepo( RepoBuilder.remoteRepositoryFromUrl( url ) );
	}
	
	public void setRemoteRepo( RemoteRepository remoteRepo ) {
		this.remoteRepo = remoteRepo;

		if ( authentication != null ) {
            RemoteRepository.Builder repoBuilder = new RemoteRepository.Builder( this.remoteRepo );
            repoBuilder.setAuthentication( authentication );
			remoteRepo = repoBuilder.build();
		}
	}
	
	public RemoteRepository getRemoteRepo() {
		return remoteRepo;
	}

	public void setPomPath(String pomPath) {
		this.pomPath = pomPath;
		
		// Depends on notation
		if ( getJarArtifact() != null ) {
			pomArtifact = new SubArtifact( getJarArtifact(), "", "pom" );
	        pomArtifact = pomArtifact.setFile( new File( this.pomPath ) );
		}
	}

	public Authentication getAuthentication() {
		return authentication;
	}
	
	public String getPomPath() {
		return pomPath;
	}

	public Artifact getPomArtifact() {
		return pomArtifact;
	}

	public void setFilePath( String filePath ) {
		this.filePath = filePath;
		
		// Depends on notation
		if ( getJarArtifact() != null ) {
			jarArtifact = getJarArtifact().setFile( new File( filePath ) );
		}
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setNotation( String notation ) {
		this.notation = notation;
		jarArtifact = new DefaultArtifact( notation );
		
		// Depends on notation
		if ( getFilePath() != null ) {
			jarArtifact = jarArtifact.setFile( new File( getFilePath() ) );
		}
		
		// Depends on notation
		if ( getPomPath() != null ) {
			pomArtifact = new SubArtifact( getJarArtifact(), "", "pom" );
	        pomArtifact = pomArtifact.setFile( new File( getPomPath() ) );
		}
	}
	
	public String getNotation() {
		return notation;
	}

	public Artifact getJarArtifact() {
		return jarArtifact;
	}
}
