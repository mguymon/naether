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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Apache Maven
import org.apache.maven.model.Model;
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
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;

// Naether
import com.slackworks.naether.deploy.DeployArtifact;
import com.slackworks.naether.deploy.DeployException;
import com.slackworks.naether.deploy.InstallException;
import com.slackworks.naether.maven.Project;
import com.slackworks.naether.maven.ProjectException;
import com.slackworks.naether.repo.LogRepositoryListener;
import com.slackworks.naether.repo.LogTransferListener;
import com.slackworks.naether.repo.ManualWagonProvider;
import com.slackworks.naether.repo.RemoteRepoBuilder;

/**
 * Dependency Resolver using Maven's Aether
 * 
 * Based on {@link https://docs.sonatype.org/display/AETHER/Home#Home-AetherinsideMaven}
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
	 * Create new instance. Default local repository is environment M2_REPO
	 * setting or user home .m2/. The local repository is the destination for
	 * downloaded metadata and artifacts.
	 */
	public Naether() {
		dependencies = new ArrayList<Dependency>();
		setRemoteRepositories(new ArrayList<RemoteRepository>());
		addRemoteRepository("central", "default",
				"http://repo1.maven.org/maven2/");

		Map<String, String> env = System.getenv();
		String m2Repo = env.get("M2_REPO");
		if (m2Repo == null) {
			String userHome = System.getProperty("user.home");
			setLocalRepoPath(userHome + File.separator + ".m2" + File.separator
					+ "repository");
		} else {
			setLocalRepoPath((new File(m2Repo)).getAbsolutePath());
		}
	}

	/**
	 * Clear dependencies
	 */
	public void clearDependencies() {
		setDependencies(new ArrayList<Dependency>());
	}

	/**
	 * Add dependency by String notation with default compile scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation
	 *            String
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
	 * @param scope  String
	 */
	public void addDependency(String notation, String scope) {
		log.debug("Add dep {notation} {scope}");
		Dependency dependency = new Dependency(new DefaultArtifact(notation), scope);
		addDependency(dependency);
	}

	/**
	 * Add {@link Dependency}
	 * 
	 * @param dependency {@link Dependency}
	 */
	public void addDependency(Dependency dependency) {
		dependencies.add(dependency);
	}
	
	/**
	 * Add {@link org.apache.maven.model.Dependency}
	 */
	public void addDependency(org.apache.maven.model.Dependency projectDependency) {
		log.debug( "Adding dependency: {}", projectDependency );
		Dependency dependency = new Dependency(new DefaultArtifact(Notation.generate( projectDependency ) ), projectDependency.getScope());
		dependency.setOptional( projectDependency.isOptional() );
		
		List<Exclusion> exclusions = new ArrayList<Exclusion>();
		for ( org.apache.maven.model.Exclusion projectExclusion : projectDependency.getExclusions() ) {
			exclusions.add( new Exclusion(projectExclusion.getGroupId(), projectExclusion.getArtifactId(), "*", "*") );			
		}
		log.debug( "Exclusion: {}", exclusions );
		dependency = dependency.setExclusions( exclusions );
		
		dependencies.add( dependency );
	}
	
	public void addDependencies( String pomPath ) throws ProjectException {
		addDependencies( new Project( pomPath), (List<String>)null );
	}
	
	public void addDependencies( String pomPath, List<String> scopes ) throws ProjectException {
		addDependencies( new Project( pomPath), scopes );
	}
	
	/**
	 * Add dependencies from a Maven Pom
	 * 
	 * @param project {@link Model}
	 */
	public void addDependencies( Project project ) {
		addDependencies( project, (List<String>)null );		
	}
	
	/**
	 * Add dependencies from a Maven Pom
	 * 
	 * @param project {@link Project}
	 * @param scopes List<String> of dependency scopes
	 */
	public void addDependencies( Project project, List<String> scopes ) {
		for ( org.apache.maven.model.Dependency dependency : project.getDependencies(scopes, true) ) {
			addDependency( dependency );
		}
	}

	/**
	 * Remove all {@link RemoteRepository}
	 */
	public void clearRemoteRepositories() {
		setRemoteRepositories(new ArrayList<RemoteRepository>());
	}

	/**
	 * Add a {@link RemoteRepository} by String url
	 * 
	 * @param url String
	 * @throws URLException
	 * @throws MalformedURLException
	 */
	public void addRemoteRepositoryByUrl(String url) throws NaetherException {
		try {
			addRemoteRepository(RemoteRepoBuilder.createFromUrl(url));
		} catch (MalformedURLException e) {
			log.error( "Malformed url: {}", url, e);
			throw new NaetherException(e);
		}
	}

	/**
	 * Add a {@link RemoteRepository} by String url with String username and
	 * password authentication.
	 * 
	 * @param url String
	 * @param username String
	 * @param password String
	 * @throws URLException
	 * @throws MalformedURLException
	 */
	public void addRemoteRepositoryByUrl(String url, String username, String password) throws URLException {
		RemoteRepository remoteRepo;
		try {
			remoteRepo = RemoteRepoBuilder.createFromUrl(url);
		} catch (MalformedURLException e) {
			throw new URLException(e);
		}
		remoteRepo = remoteRepo.setAuthentication(new Authentication(username, password));
		addRemoteRepository(remoteRepo);
	}

	/**
	 * Add a {@link RemoteRepository}
	 * 
	 * @param id String
	 * @param type String
	 * @param url String
	 */
	public void addRemoteRepository(String id, String type, String url) {
		getRemoteRepositories().add(new RemoteRepository(id, type, url));
	}

	/**
	 * Add {@link RemoteRepository}
	 * 
	 * @param remoteRepository {@link RemoteRepository}
	 */
	public void addRemoteRepository(RemoteRepository remoteRepository) {
		getRemoteRepositories().add(remoteRepository);
	}

	/**
	 * Set {@link List} of {@link RemoteRepository}
	 * 
	 * @param remoteRepositories {@link List}
	 */
	public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	/**
	 * Get {@link List} of {@link RemoteRepository}
	 * 
	 * @return {@link List}
	 */
	public List<RemoteRepository> getRemoteRepositories() {
		return remoteRepositories;
	}

	/**
	 * Create new {@link RepositorySystem}
	 * 
	 * @return {@link RepositorySystem}
	 */
	public RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		locator.setServices(WagonProvider.class, new ManualWagonProvider());
		locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);

		return locator.getService(RepositorySystem.class);
	}

	/**
	 * Create new {@link RepositorySystemSession}
	 * 
	 * @param system {@link RepositorySystem}
	 * @return {@link RepositorySystemSession}
	 */
	public RepositorySystemSession newSession(RepositorySystem system) {
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		session.setTransferListener(new LogTransferListener());
		session.setRepositoryListener(new LogRepositoryListener());
		LocalRepository localRepo = new LocalRepository(getLocalRepoPath());
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

		return session;
	}

	/**
	 * Resolve dependencies and download artifacts
	 * 
	 * @throws DependencyException
	 * @throws URLException
	 * 
	 * @throws Exception
	 */
	public void resolveDependencies() throws URLException, DependencyException {
		resolveDependencies(true);
	}

	/**
	 * Resolve Dependencies, boolean if artifacts are to be downloaded
	 * 
	 * @param downloadArtifacts boolean
	 * @throws URLException
	 * @throws DependencyException
	 */
	public void resolveDependencies(boolean downloadArtifacts) throws URLException, DependencyException {
		log.info( "Resolving Dependencies" );
		log.info("Local Repo Path: {}", localRepoPath);

		if ( log.isDebugEnabled() ) {
			log.debug("Remote Repositories:");
			for (RemoteRepository repo : getRemoteRepositories()) {
				log.debug("  {}", repo.toString());
			}
		}

		RepositorySystem repoSystem = newRepositorySystem();

		RepositorySystemSession session = newSession(repoSystem);

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setDependencies(getDependencies());

		try {
			collectRequest.addRepository(RemoteRepoBuilder.createFromUrl("file:" + this.getLocalRepoPath()));
		} catch (MalformedURLException e) {
			throw new URLException("Failed to add local repo to request", e);
		}

		for (RemoteRepository repo : getRemoteRepositories()) {
			collectRequest.addRepository(repo);
		}

		CollectResult collectResult;
		try {
			collectResult = repoSystem.collectDependencies(session,collectRequest);
		} catch (DependencyCollectionException e) {
			throw new DependencyException(e);
		}

		preorderedNodeList = new PreorderNodeListGenerator();
		if (downloadArtifacts) {
			DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

			log.debug("Resolving dependencies to files");
			DependencyResult dependencyResult;
			try {
				dependencyResult = repoSystem.resolveDependencies(session, dependencyRequest);
			} catch (DependencyResolutionException e) {
				throw new DependencyException(e);
			}
			dependencyResult.getRoot().accept(preorderedNodeList);

		} else {
			collectResult.getRoot().accept(preorderedNodeList);
		}

		this.setDependencies(preorderedNodeList.getDependencies(true));
		log.debug("Setting resolved dependencies: {}", this.getDependencies());
	}

	/**
	 * Deploy an Artifact
	 * 
	 * @param deployArtifact {@link DeployArtifact}
	 * @throws DeploymentException
	 */
	public void deployArtifact(DeployArtifact deployArtifact) throws DeployException {
		log.debug("deploy artifact: {} ", deployArtifact.getNotation());
		RepositorySystem system = newRepositorySystem();

		RepositorySystemSession session = newSession(system);

		DeployRequest deployRequest = new DeployRequest();
		deployRequest.addArtifact(deployArtifact.getJarArtifact());
		if (deployArtifact.getPomArtifact() != null) {
			deployRequest.addArtifact(deployArtifact.getPomArtifact());
		}
		deployRequest.setRepository(deployArtifact.getRemoteRepo());

		log.debug("deploying artifact {}", deployArtifact.getNotation());
		try {
			system.deploy(session, deployRequest);
		} catch (DeploymentException e) {
			log.error("Failed to deploy artifact", e);
			throw new DeployException(e);
		}
	}

	/**
	 * Install Artifact to local repo
	 * 
	 * @param deployArtifact {@link DeployArtifact}
	 * @throws InstallException
	 */
	public void installArtifact(DeployArtifact deployArtifact) throws InstallException {
		log.debug("deploy artifact: {} ", deployArtifact.getNotation());
		RepositorySystem system = newRepositorySystem();

		RepositorySystemSession session = newSession(system);

		InstallRequest installRequest = new InstallRequest();
		installRequest.addArtifact(deployArtifact.getJarArtifact());
		if (deployArtifact.getPomArtifact() != null) {
			installRequest.addArtifact(deployArtifact.getPomArtifact());
		}

		log.debug("installing artifact {}", deployArtifact.getNotation());
		try {
			system.install(session, installRequest);
		} catch (InstallationException e) {
			log.error("Failed to install artifact", e);
			throw new InstallException(e);
		}
	}

	/**
	 * Classpath from resolved artifacts
	 * 
	 * @return String
	 */
	public String getResolvedClassPath() {
		return preorderedNodeList.getClassPath();
	}

	/**
	 * Set local repository path. This is the destination for downloaded
	 * metadata and artifacts.
	 * 
	 * @param repoPath
	 *            String
	 */
	public void setLocalRepoPath(String repoPath) {
		this.localRepoPath = repoPath;
	}

	/**
	 * Get local repository path. This is the destination for downloaded
	 * metadata and artifacts.
	 * 
	 * @return String
	 */
	public String getLocalRepoPath() {
		return localRepoPath;
	}

	/**
	 * Set the {@link List} of {@link Dependency}
	 * 
	 * @param dependencies
	 *            {@link List}
	 */
	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	/**
	 * {@link List} of {@link Dependency}
	 * 
	 * @return {@link List}
	 */
	public List<Dependency> getDependencies() {
		return dependencies;
	}

	/**
	 * {@link List} of {@link Dependency} converted to String notation
	 * 
	 * @return {@link List}
	 */
	public List<String> getDependenciesNotation() {
		List<String> notations = new ArrayList<String>();
		for (Dependency dependency : getDependencies()) {
			notations.add(Notation.generate(dependency));
		}

		return notations;
	}

}
