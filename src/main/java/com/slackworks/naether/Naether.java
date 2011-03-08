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

// Java SE
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Apache Maven
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;

// SL4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Sonatype Aether Dependency Management
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;


/**
 * Dependency Resolver using Maven's Aether
 * 
 * Based on {@link https
 * ://docs.sonatype.org/display/AETHER/Home#Home-AetherinsideMaven}
 * 
 * @author Michael Guymon
 * 
 */
public class Naether {

	private static Logger log = LoggerFactory.getLogger(Naether.class);

	private String localRepoPath;
	private List<Dependency> dependencies;
	private List<RemoteRepository> remoteRepositories;
	private PreorderNodeListGenerator preorderedNodeList;

	/**
	 * Create new instance. Default repository locations is environment M2_REPO setting
	 * or user home .m2/
	 */
	public Naether() {
		dependencies = new ArrayList<Dependency>();
		setRemoteRepositories(new ArrayList<RemoteRepository>());
		addRemoteRepository("central", "default", "http://repo1.maven.org/maven2/");

		Map<String, String> env = System.getenv();
		String m2Repo = env.get( "M2_REPO" );
		if ( m2Repo == null ) {
			String userHome = System.getProperty("user.home");
			setLocalRepoPath(userHome + File.separator + ".m2" + File.separator + "repository");
		} else {
			setLocalRepoPath( (new File( m2Repo ) ).getAbsolutePath() );
		}
	}

	/**
	 * Add dependency by String notation
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 */
	public void addDependency(String notation) {
		addDependency(notation, "compile");
	}

	/**
	 * * Add dependency by String notation and Maven scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 * @param scope String
	 */
	public void addDependency(String notation, String scope) {
		Dependency dependency = new Dependency(new DefaultArtifact(notation), scope);
		addDependency(dependency);
	}

	/**
	 * Add Dependency
	 * 
	 * @param dependency {@link Dependency}
	 */
	public void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}

	public void clearRemoteRepositories() {
		setRemoteRepositories(new ArrayList<RemoteRepository>());
	}

	public void addRemoteRepositoryByUrl(String url) throws MalformedURLException {
		addRemoteRepository(RemoteRepoBuilder.createFromUrl(url));
	}
	
	public void addRemoteRepositoryByUrl(String url, String username, String password) throws MalformedURLException {
		RemoteRepository remoteRepo = RemoteRepoBuilder.createFromUrl(url);
		remoteRepo.setAuthentication( new Authentication( username, password ) );
		addRemoteRepository( remoteRepo );
	}

	public void addRemoteRepository(String id, String type, String url) {
		getRemoteRepositories().add(new RemoteRepository( id, type, url ));
	}
	
	/**
	 * Add RemoteRepository
	 * 
	 * @param remoteRepository
	 */
	public void addRemoteRepository(RemoteRepository remoteRepository) {
		getRemoteRepositories().add(remoteRepository);
	}

	public RepositorySystem newRepositorySystem() throws Exception {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		locator.setServices(WagonProvider.class, new ManualWagonProvider());
		locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);

	}

	public RepositorySystemSession newSession(RepositorySystem system) {
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		session.setTransferListener( new LogTransferListener() );
        session.setRepositoryListener( new LogRepositoryListener() );
		LocalRepository localRepo = new LocalRepository(getLocalRepoPath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

		return session;
	}

	/**
	 * Resolve Dependencies
	 * 
	 * @throws Exception
	 */
	public void resolveDependencies() throws Exception {
		resolveDependencies( true );
	}
	
	public void resolveDependencies( boolean downloadArtifacts ) throws Exception {
		log.info("Local Repo Path: {}", localRepoPath);

		log.info("Remote Repositories:");
		for (RemoteRepository repo : getRemoteRepositories()) {
			log.info("  {}", repo.toString());
		}

		RepositorySystem repoSystem = newRepositorySystem();

		RepositorySystemSession session = newSession(repoSystem);

		// Dependency dependency =
		// new Dependency( new DefaultArtifact(
		// "org.apache.activemq:activemq-spring:jar:5.4.2" ), "compile" );

		CollectRequest collectRequest = new CollectRequest();
		// collectRequest.setRoot( dependency );
		collectRequest.setDependencies(getDependencies());

		for (RemoteRepository repo : getRemoteRepositories()) {
			collectRequest.addRepository(repo);
		}
		
		CollectResult collectResult = repoSystem.collectDependencies( session, collectRequest );
		preorderedNodeList = new PreorderNodeListGenerator();
		if ( downloadArtifacts ) {
			DependencyRequest dependencyRequest = new DependencyRequest( collectRequest, null );
	
			log.info("Resolving dependencies to files");
			DependencyResult dependencyResult = repoSystem.resolveDependencies(session, dependencyRequest);
			dependencyResult.getRoot().accept( preorderedNodeList );
			
		} else {
			collectResult.getRoot().accept(preorderedNodeList);
		}
		
		this.setDependencies(preorderedNodeList.getDependencies(true));
		log.debug("Setting resolved dependencies: {}", this.getDependencies() );
	}
	
	/**
	 * Deploy an Artifact
	 * 
	 * @param deployArtifact {@link DeployArtifact}
	 * @throws Exception
	 */
	public void deployArtifact( DeployArtifact deployArtifact ) throws Exception {
		log.debug( "deploy artifact: {} ", deployArtifact.getNotation() );
		RepositorySystem system = newRepositorySystem();

        RepositorySystemSession session = newSession(system);

        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact( deployArtifact.getJarArtifact() );
        if ( deployArtifact.getPomArtifact() != null ) {
	        deployRequest.addArtifact( deployArtifact.getPomArtifact() );
        }
        deployRequest.setRepository( deployArtifact.getRemoteRepo() );
        
        log.debug( "uploading artifact" );
        system.deploy( session, deployRequest );
	}

	public String getResolvedClassPath() {
		return preorderedNodeList.getClassPath();
	}

	public void setLocalRepoPath(String repoPath) {
		this.localRepoPath = repoPath;
	}

	public String getLocalRepoPath() {
		return localRepoPath;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public List<String> getDependenciesNotation() {
		List<String> notations = new ArrayList<String>();
		for (Dependency dependency : getDependencies()) {
			notations.add(Notation.generate(dependency));
		}

		return notations;
	}

	public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	public List<RemoteRepository> getRemoteRepositories() {
		return remoteRepositories;
	}

}
