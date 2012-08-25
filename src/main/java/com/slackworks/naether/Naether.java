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
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Apache Maven 
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;

// SLF4J Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Sonatype Aether
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.impl.internal.SimpleLocalRepositoryManagerFactory;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.LocalRepositoryManager;
import org.sonatype.aether.repository.NoLocalRepositoryManagerException;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;

// Naether
import com.slackworks.naether.aether.ValidSystemScopeDependencySelector;
import com.slackworks.naether.deploy.DeployArtifact;
import com.slackworks.naether.deploy.DeployException;
import com.slackworks.naether.deploy.InstallException;
import com.slackworks.naether.maven.Project;
import com.slackworks.naether.maven.ProjectException;
import com.slackworks.naether.repo.BuildWorkspaceReader;
import com.slackworks.naether.repo.LogRepositoryListener;
import com.slackworks.naether.repo.LogTransferListener;
import com.slackworks.naether.repo.ManualWagonProvider;
import com.slackworks.naether.util.Notation;
import com.slackworks.naether.util.RepoBuilder;

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
	private List<Artifact> buildArtifacts;
	private Set<RemoteRepository> remoteRepositories;
	private PreorderNodeListGenerator preorderedNodeList;

	/**
	 * Create new instance. Default local repository is environment M2_REPO
	 * setting or user home .m2/repository. The local repository is the destination for
	 * downloaded metadata and artifacts.
	 * 
	 * The default remote repository is http://repo1.maven.org/maven2/
	 */
	public Naether() {
		// Set the initial ArrayList
		clearDependencies();
		
		// Set the initial ArrayList
		clearBuildArtifacts();
		
		// Set the initial LinkedHashSet
		clearRemoteRepositories();
		
		addRemoteRepository("central", "default", "http://repo1.maven.org/maven2/");

		Map<String, String> env = System.getenv();
		String m2Repo = env.get("M2_REPO");
		if (m2Repo == null) {
			String userHome = System.getProperty("user.home");
			setLocalRepoPath(userHome + File.separator + ".m2" + File.separator + "repository");
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
	 * Clear local BuildArtifacts
	 */
	public void clearBuildArtifacts() {
		setBuildArtifacts(new ArrayList<Artifact>());
	}
	
	/**
	 * Add a local Build Artifact manually by String notation, path to the artifact,
	 * and path to the POM.
	 * 
	 * @param notation String
	 * @param path String
	 * @param pom String
	 */
	public void addBuildArtifact(String notation, String path, String pom) {
		Artifact artifact = new DefaultArtifact(notation);
		artifact = artifact.setFile( new File(path) );
		
		this.buildArtifacts.add( artifact );
		
		String pomNotation = new StringBuilder( artifact.getGroupId() ).append(":")
			.append( artifact.getArtifactId() ).append(":")
			.append( "pom").append(":").append( artifact.getBaseVersion() ).toString();
		
		artifact = new DefaultArtifact(pomNotation);
		artifact = artifact.setFile( new File(pom) );
		
		this.buildArtifacts.add( artifact );
	}
	
	/**
	 * Add a local Build Artifact manually by String notation and path to the artifact.
	 * The POM for the artifact is created automatically based on the String notation.
	 * 
	 * @param notation String
	 * @param path String
	 * @throws NaetherException if failed to create a POM
	 */
	public void addBuildArtifact(String notation, String path) throws NaetherException {
		Artifact artifact = new DefaultArtifact(notation);
		artifact = artifact.setFile( new File(path) );
		
		this.buildArtifacts.add( artifact );
		
		File tempPom = null;
		try {
			tempPom = File.createTempFile( "pom", "xml" );
		} catch (IOException e) {
			throw new NaetherException( "Failed to create temp file", e );
		}
		
		tempPom.deleteOnExit();
		
		Project project = new Project();
		project.setGroupId( artifact.getGroupId() );
		project.setArtifactId( artifact.getArtifactId() );
		project.setVersion( artifact.getBaseVersion() );
		try {
			project.writePom( tempPom );
		} catch (ProjectException e) {
			throw new NaetherException( "Failed to create temp POM", e );
		}
		
		String pomNotation = new StringBuilder( artifact.getGroupId() ).append(":")
			.append( artifact.getArtifactId() ).append(":")
			.append( "pom").append(":").append( artifact.getBaseVersion() ).toString();
		
		artifact = new DefaultArtifact(pomNotation);
		artifact = artifact.setFile( tempPom );
		
		this.buildArtifacts.add( artifact );
	}

	/**
	 * Add dependency by String notation with default compile scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 */
	public void addDependency(String notation) {
		addDependency(notation, "compile");
	}

	/**
	 * Add dependency by String notation and Maven scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 * @param scope String
	 */
	public void addDependency(String notation, String scope) {
		log.debug("Add dep {} {}", notation, scope);
		
		DefaultArtifact artifact = new DefaultArtifact( notation );
		
		if ( "test".equals( artifact.getClassifier() ) || "test-jar".equals( artifact.getClassifier() ) ) {
			
			ArtifactType artifactType = new DefaultArtifactType( "test-jar", "jar", "test-jar", null );
			
			artifact = new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
					null, "jar", artifact.getBaseVersion(), artifactType );
		}
		Dependency dependency = new Dependency(artifact, scope);
		addDependency(dependency);
	}

	/**
	 * Add {@link Dependency}
	 * 
	 * @param dependency {@link Dependency}
	 */
	public void addDependency(Dependency dependency) {
		String classifier = dependency.getArtifact().getClassifier();
		if ( "test".equals( classifier ) || "test-jar".equals( classifier ) ) {
			ArtifactType artifactType = new DefaultArtifactType( "test-jar", "jar", "test-jar", null );
			
			Artifact artifact = dependency.getArtifact();
			
			artifact = new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
					null, "jar", artifact.getBaseVersion(), artifactType );
			dependency = new Dependency(artifact, dependency.getScope());
		}
		dependencies.add(dependency);
	}
	
	/**
	 * Add {@link org.apache.maven.model.Dependency}
	 */
	public void addDependency(org.apache.maven.model.Dependency projectDependency) {
		log.debug( "Adding dependency: {}", projectDependency );
		
		DefaultArtifact artifact = null;
		
		if ( "test".equals( projectDependency.getType() ) || "test-jar".equals( projectDependency.getType() ) ) {
			
			ArtifactType artifactType = new DefaultArtifactType( "test-jar", "jar", "test-jar", null );
			
			artifact = new DefaultArtifact( projectDependency.getGroupId(), projectDependency.getArtifactId(),
					null, "jar", projectDependency.getVersion(), artifactType );
		} else {
			artifact = new DefaultArtifact( projectDependency.getGroupId(), projectDependency.getArtifactId(),
					null, "jar", projectDependency.getVersion() );
		}
		
		Dependency dependency = new Dependency( artifact , projectDependency.getScope());
		dependency.setOptional( projectDependency.isOptional() );
		
		List<Exclusion> exclusions = new ArrayList<Exclusion>();
		for ( org.apache.maven.model.Exclusion projectExclusion : projectDependency.getExclusions() ) {
			exclusions.add( new Exclusion(projectExclusion.getGroupId(), projectExclusion.getArtifactId(), "*", "*") );			
		}
		log.debug( "Exclusion: {}", exclusions );
		dependency = dependency.setExclusions( exclusions );
		
		dependencies.add( dependency );
	}
	
	/**
	 * Add dependences from a Maven POM
	 * 
	 * @param pomPath String path to POM
	 * @throws ProjectException
	 */
	public void addDependencies( String pomPath ) throws ProjectException {
		addDependencies( new Project( pomPath), (List<String>)null );
	}
	
	/**
	 * Add dependencies from a Maven POM, limited to a {@link List<String>} of scopes.
	 * 
	 * @param pomPath String path to POM
	 * @param scopes Link<String> of scopes
	 * @throws ProjectException
	 * @see {{@link #addDependencies(Project, List)}
	 */
	public void addDependencies( String pomPath, List<String> scopes ) throws ProjectException {
		addDependencies( new Project( pomPath), scopes );
	}
	
	/**
	 * Add dependencies from a Maven POM
	 * 
	 * @param project {@link Model}
	 * @throws ProjectException 
	 * @see {{@link #addDependencies(Project, List)}
	 */
	public void addDependencies( Project project ) throws ProjectException {
		addDependencies( project, (List<String>)null );		
	}
	
	/**
	 * Add dependencies from a Maven POM, limited to a {@link List<String>} of scopes. Adds
	 * all Repositories from the Maven Pom.
	 * 
	 * @param project {@link Project}
	 * @param scopes List<String> of dependency scopes
	 * @throws ProjectException 
	 */
	public void addDependencies( Project project, List<String> scopes ) throws ProjectException {
		for ( org.apache.maven.model.Dependency dependency : project.getDependencies(scopes) ) {
			addDependency( dependency );
		}
		
		// Add remote repositories from pom
		for ( Repository repo : project.getMavenModel().getRepositories() ) {
			this.addRemoteRepository( repo.getId(), repo.getLayout(), repo.getUrl() );
		}
		
		
		// Add Dependencies and Repositories from parent
		if ( project.getMavenModel().getParent() != null ) {
			
			Project parent = new Project( project.getBasePath() + File.separator + project.getMavenModel().getParent().getRelativePath() );
			
			for ( org.apache.maven.model.Dependency dependency : parent.getDependencies(scopes) ) {
				addDependency( dependency );
			}
			
			// Add remote repositories from pom
			for ( Repository repo : parent.getMavenModel().getRepositories() ) {
				this.addRemoteRepository( repo.getId(), repo.getLayout(), repo.getUrl() );
			}
		}
	}

	/**
	 * Remove all {@link RemoteRepository}
	 */
	public void clearRemoteRepositories() {
		setRemoteRepositories(new LinkedHashSet<RemoteRepository>());
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
			addRemoteRepository(RepoBuilder.remoteRepositoryFromUrl(url));
		} catch (MalformedURLException e) {
			log.error( "Malformed url: {}", url, e);
			throw new NaetherException(e);
		}
	}

	/**
	 * Add a {@link RemoteRepository} by String url with String username and
	 * password for authentication.
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
			remoteRepo = RepoBuilder.remoteRepositoryFromUrl(url);
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
	public void setRemoteRepositories(Set<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	/**
	 * Get {@link List} of {@link RemoteRepository}
	 * 
	 * @return {@link List}
	 */
	public Set<RemoteRepository> getRemoteRepositories() {
		return remoteRepositories;
	}
	
	public List<String> getRemoteRepositoryUrls() {
		List<String> urls = new ArrayList<String>();
		for( RemoteRepository repo : getRemoteRepositories() ) {
			urls.add( repo.getUrl() );
		}
		
		return urls;
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
	public MavenRepositorySystemSession newSession(RepositorySystem system) {
		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		session = (MavenRepositorySystemSession)session.setDependencySelector( new AndDependencySelector( session.getDependencySelector(), new ValidSystemScopeDependencySelector() ) );
		session = (MavenRepositorySystemSession)session.setTransferListener(new LogTransferListener());
		session = (MavenRepositorySystemSession)session.setRepositoryListener(new LogRepositoryListener());
		
		session = (MavenRepositorySystemSession)session.setIgnoreMissingArtifactDescriptor( false );
		
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
		resolveDependencies(true, null);
	}

	/**
	 * Resolve Dependencies, boolean if artifacts are to be downloaded
	 * 
	 * @param downloadArtifacts boolean
	 * @throws URLException
	 * @throws DependencyException
	 */
	public void resolveDependencies(boolean downloadArtifacts) throws URLException, DependencyException {
		resolveDependencies( downloadArtifacts, null );
	}
	
	public void resolveDependencies(boolean downloadArtifacts, Map<String,String> properties) throws URLException, DependencyException {
		log.debug( "Resolving Dependencies" );
		
		log.debug("Local Repo Path: {}", localRepoPath);

		if ( log.isDebugEnabled() ) {
			log.debug("Remote Repositories:");
			for (RemoteRepository repo : getRemoteRepositories()) {
				log.debug("  {}", repo.toString());
			}
		}

		RepositorySystem repoSystem = newRepositorySystem();
		
		MavenRepositorySystemSession session = newSession(repoSystem);
		if ( properties != null ) {
			Map<String,String> userProperties = session.getUserProperties();
			if ( userProperties == null ) {
				userProperties = new HashMap<String,String>();
			}
			userProperties.putAll( properties );
			
			log.debug( "Session userProperties: {}", userProperties );
			
			session = (MavenRepositorySystemSession)session.setUserProperties( userProperties );
		}
		
		// If there are local build artifacts, create a BuildWorkspaceReader to
		// override remote artifacts with the local build artifacts.
		if ( buildArtifacts.size() > 0 ) {
			BuildWorkspaceReader reader = new BuildWorkspaceReader();
			
			for ( Artifact artifact : buildArtifacts ) {
				reader.addArtifact( artifact );
			}
			
			session = (MavenRepositorySystemSession)session.setWorkspaceReader( reader );
			
		}
		
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setDependencies(getDependencies());
		
		try {
			collectRequest.addRepository(RepoBuilder.remoteRepositoryFromUrl("file:" + this.getLocalRepoPath()));
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
	 * Install Artifact to local repo. 
	 * 
	 * If installing a POM, filePath can be null. If install a Jar without a POM, pomPath
	 * can be null.
	 * 
	 * @param String notation String
	 * @param String pomPath String
	 * @param String filePath String
	 * 
	 * @throws InstallException
	 */
	
	public void install(String notation, String pomPath, String filePath ) throws InstallException {
		log.debug("installing artifact: {} ", notation);
		
		RepositorySystem system = newRepositorySystem();

		RepositorySystemSession session = newSession(system);

		InstallRequest installRequest = new InstallRequest();
		
		if ( filePath != null ) {
			DefaultArtifact jarArtifact = new DefaultArtifact( notation );
			jarArtifact = (DefaultArtifact)jarArtifact.setFile( new File( filePath ) );
			
			installRequest.addArtifact( jarArtifact );
				
			if ( pomPath != null ) {
				SubArtifact pomArtifact = new SubArtifact( jarArtifact, "", "pom" );
				pomArtifact = (SubArtifact)pomArtifact.setFile( new File( pomPath ) );
				installRequest.addArtifact( pomArtifact );
			}
			
		// If Pom only, without a jar, ensure the notation type is set to pom
		} else  if ( pomPath != null ) {
			Map<String,String> notationMap = Notation.parse( notation );
			notationMap.put( "type", "pom" );
			
			DefaultArtifact pomArtifact = new DefaultArtifact( Notation.generate(notationMap) );
			pomArtifact = (DefaultArtifact)pomArtifact.setFile( new File(pomPath ) );
			installRequest.addArtifact( pomArtifact );	
		}
				
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
		if ( preorderedNodeList != null ) {
			return preorderedNodeList.getClassPath();
		} else {
			return null;
		}
	}
	
	/**
	 * Get local path for a {@link List<String>} of dependency notations
	 * 
	 * @param notations {@link List<String>}
	 * @return {@link List<String>} of paths
	 * @throws NaetherException
	 */
	public List<String> getLocalPaths( List<String> notations ) throws NaetherException {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		SimpleLocalRepositoryManagerFactory factory = new SimpleLocalRepositoryManagerFactory();
		factory.initService( locator );
		
		LocalRepository localRepo = new LocalRepository(getLocalRepoPath());
		LocalRepositoryManager manager = null;
		try {
			manager = factory.newInstance( localRepo );
		} catch (NoLocalRepositoryManagerException e) {
			throw new NaetherException( "Failed to initial local repository manage", e  );
		}
		
		List<String> localPaths = new ArrayList<String>();
		
		for ( String notation : notations ) {
			Dependency dependency = new Dependency(new DefaultArtifact(notation), "compile");
			String path = new StringBuilder( localRepo.getBasedir().getAbsolutePath() )
				.append( File.separator ).append( manager.getPathForLocalArtifact( dependency.getArtifact() ) ).toString();
			localPaths.add( path );
		}
		
		return localPaths;
	}

	/**
	 * {@link List<String>} of {@link Dependency} converted to String notation
	 * 
	 * @return {@link List<String>}
	 */
	public List<String> getDependenciesNotation() {
		List<String> notations = new ArrayList<String>();
		for (Dependency dependency : getDependencies()) {
			notations.add(Notation.generate(dependency));
		}

		return notations;
	}
	
	/**
	 * {@link Map} of String notation and the corresponding String file path 
	 * 
	 * @return {@link Map<String,String>}
	 */
	public Map<String,String> getDependenciesPath() {
		Map<String,String> dependencies = new HashMap<String,String>();
		for (Dependency dependency : getDependencies()) {
			if ( dependency.getArtifact().getFile() != null ) {
				dependencies.put( Notation.generate( dependency ), dependency.getArtifact().getFile().getAbsolutePath() );
			}
		}
		
		return dependencies;
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
	 * Get List<Artifact> of local artifacts that are used in dependency
	 * resolution.
	 * 
	 * @return List<Artifact>
	 */
	public List<Artifact> getBuildArtifacts() {
		return buildArtifacts;
	}

	/**
	 * Set List<Artifact> for local artifacts that are used in depdency resolution.
	 * 
	 * @param buildArtifacts List<Artifact>
	 */
	public void setBuildArtifacts(List<Artifact> buildArtifacts) {
		this.buildArtifacts = buildArtifacts;
	}
	
	/**
	 * Download to the local repository a List of {@link Artifact} or String
	 * notations.
	 * 
	 * @param artifactsOrNotations @{link List} of {@link Artifact} or String notation
	 * @return List<File>
	 * @throws NaetherException
	 */
	@SuppressWarnings("rawtypes")
	public List<File> downloadArtifacts( List artifactsOrNotations ) throws NaetherException {
		RepositorySystem system = this.newRepositorySystem();

		List<Artifact> artifacts = new ArrayList<Artifact>();
		
		for ( Object artifactsOrNotation : artifactsOrNotations ) {
			if ( artifactsOrNotation != null ) {
				if ( artifactsOrNotation instanceof String ) {
					artifacts.add( new DefaultArtifact( (String) artifactsOrNotation ) );
				} else if ( artifactsOrNotation instanceof Artifact ) {
					artifacts.add( (Artifact)artifactsOrNotation );
				} else {
					throw new NaetherException( "Only String notations or Artifact instances allowed. Found " + artifactsOrNotation.getClass() );
				}
			} else {
				log.warn( "Null found in list of artifacts to download" );
			}
		}
		
        RepositorySystemSession session = this.newSession(system);

        List<File> files = new ArrayList<File>();
        
        for ( Artifact artifact : artifacts ) {
	        
        	log.debug( "Downloading {}", artifact );
        	
	        ArtifactRequest artifactRequest = new ArtifactRequest();
	        artifactRequest.setArtifact( artifact );
	        for ( RemoteRepository repo : this.getRemoteRepositories() ) {
	        	artifactRequest.addRepository( repo );
	        }
	
	        ArtifactResult artifactResult = null;
			try {
				artifactResult = system.resolveArtifact( session, artifactRequest );
			} catch (ArtifactResolutionException e) {
				throw new ResolveException(e);
			}
	
	        Artifact downloadedArtifact = artifactResult.getArtifact();
	        files.add( downloadedArtifact.getFile() );
        }
        
        return files;
	}

}
