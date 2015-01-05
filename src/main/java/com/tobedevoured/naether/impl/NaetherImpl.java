package com.tobedevoured.naether.impl;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Apache Maven 
import org.apache.maven.model.Repository;

// SLF4J Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Sonatype Aether
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.artifact.ArtifactType;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.Exclusion;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.Authentication;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.artifact.DefaultArtifactType;
import org.sonatype.aether.util.artifact.SubArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

// Naether
import com.tobedevoured.naether.Const;
import com.tobedevoured.naether.DependencyException;
import com.tobedevoured.naether.NaetherException;
import com.tobedevoured.naether.ResolveException;
import com.tobedevoured.naether.URLException;
import com.tobedevoured.naether.api.Naether;
import com.tobedevoured.naether.deploy.DeployArtifact;
import com.tobedevoured.naether.deploy.DeployException;
import com.tobedevoured.naether.deploy.InstallException;
import com.tobedevoured.naether.maven.Project;
import com.tobedevoured.naether.maven.ProjectException;
import com.tobedevoured.naether.repo.RepositoryClient;
import com.tobedevoured.naether.util.Notation;
import com.tobedevoured.naether.util.RepoBuilder;

/**
 * Dependency Resolver using Maven's Aether
 * 
 * @author Michael Guymon
 * 
 */
public class NaetherImpl implements Naether {
	
	private static Logger log = LoggerFactory.getLogger(NaetherImpl.class);

	private String localRepoPath;
	private Set<Dependency> dependencies;
	private List<Artifact> buildArtifacts;
	private Set<RemoteRepository> remoteRepositories;
  private Set<String> repositoryIds;
	private PreorderNodeListGenerator preorderedNodeList;

	/**
	 * Create new instance. Default local repository is environment M2_REPO
	 * setting or user home and .m2/repository. The local repository is the destination for
	 * downloaded metadata and artifacts.
	 * 
	 * The default remote repository is http://repo1.maven.org/maven2/
	 */
	public NaetherImpl() {
		
		// Set the initial ArrayList
		this.dependencies = new HashSet<Dependency>();
		
		// Set the initial ArrayList
		this.buildArtifacts = new ArrayList<Artifact>();
		
		// Set the initial LinkedHashSet
		this.remoteRepositories = new LinkedHashSet<RemoteRepository>();

        // Set the initial HashSet
        this.repositoryIds = new HashSet<String>();
		
		addRemoteRepository("central", "default", "http://repo1.maven.org/maven2/");

		Map<String, String> env = System.getenv();
		String m2Repo = env.get("M2_REPO");
		if (m2Repo == null) {
			String userHome = System.getProperty("user.home");
			this.localRepoPath = userHome + File.separator + ".m2" + File.separator + "repository";
		} else {
			this.localRepoPath = (new File(m2Repo)).getAbsolutePath();
		}
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#clearDependencies()
	 */
	public void clearDependencies() {
		setDependencies(new HashSet<Dependency>());
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#clearBuildArtifacts()
	 */
	public void clearBuildArtifacts() {
		setBuildArtifacts(new ArrayList<Artifact>());
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addBuildArtifact(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addBuildArtifact(String notation, String path, String pom) {
		Artifact artifact = new DefaultArtifact(notation);
		artifact = artifact.setFile( new File(path) );
		
		this.buildArtifacts.add( artifact );
		
		String pomNotation = new StringBuilder( artifact.getGroupId() ).append(":")
			.append( artifact.getArtifactId() ).append(":")
			.append( Const.POM).append(":").append( artifact.getBaseVersion() ).toString();
		
		artifact = new DefaultArtifact(pomNotation);
		artifact = artifact.setFile( new File(pom) );
		
		this.buildArtifacts.add( artifact );
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addBuildArtifact(java.lang.String, java.lang.String)
	 */
	public void addBuildArtifact(String notation, String path) throws NaetherException {
		Artifact artifact = new DefaultArtifact(notation);
		artifact = artifact.setFile( new File(path) );
		
		this.buildArtifacts.add( artifact );
		
		File tempPom = null;
		try {
			tempPom = File.createTempFile( Const.POM, "xml" );
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
			.append( Const.POM).append(":").append( artifact.getBaseVersion() ).toString();
		
		artifact = new DefaultArtifact(pomNotation);
		artifact = artifact.setFile( tempPom );
		
		this.buildArtifacts.add( artifact );
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependency(java.lang.String)
	 */
	public void addDependency(String notation) {
		addDependency(notation, "compile");
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependency(java.lang.String, java.lang.String)
	 */
	public void addDependency(String notation, String scope) {
		log.debug("Add dep {} {}", notation, scope);
		
		DefaultArtifact artifact = new DefaultArtifact( notation );
		
		if ( Const.TEST.equals( artifact.getClassifier() ) || Const.TEST_JAR.equals( artifact.getClassifier() ) ) {
			
			ArtifactType artifactType = new DefaultArtifactType( Const.TEST_JAR, Const.JAR, Const.TEST_JAR, null );
			
			artifact = new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
					null, Const.JAR, artifact.getBaseVersion(), artifactType );
		}
		Dependency dependency = new Dependency(artifact, scope);
		addDependency(dependency);
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependency(org.sonatype.aether.graph.Dependency)
	 */
	public void addDependency(Dependency dependency) {
		Dependency newDep = null;
		String classifier = dependency.getArtifact().getClassifier();
		if ( Const.TEST.equals( classifier ) || Const.TEST_JAR.equals( classifier ) ) {
			ArtifactType artifactType = new DefaultArtifactType( Const.TEST_JAR, Const.JAR, Const.TEST_JAR, null );
			
			Artifact artifact = dependency.getArtifact();
			
			artifact = new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(),
					null, Const.JAR, artifact.getBaseVersion(), artifactType );
			newDep =  new Dependency(artifact, dependency.getScope());
		} else {
			newDep = dependency;
		}
		dependencies.add(newDep);
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependency(org.apache.maven.model.Dependency)
	 */
	public void addDependency(org.apache.maven.model.Dependency projectDependency) {
		log.debug( "Adding dependency: {}", projectDependency );
		
		DefaultArtifact artifact = null;
		
		if ( Const.TEST.equals( projectDependency.getType() ) || Const.TEST_JAR.equals( projectDependency.getType() ) ) {
			
			ArtifactType artifactType = new DefaultArtifactType( Const.TEST_JAR, Const.JAR, Const.TEST_JAR, null );
			
			artifact = new DefaultArtifact( projectDependency.getGroupId(), projectDependency.getArtifactId(),
					null, Const.JAR, projectDependency.getVersion(), artifactType );
		} else {
			artifact = new DefaultArtifact( projectDependency.getGroupId(), projectDependency.getArtifactId(),
					null, Const.JAR, projectDependency.getVersion() );
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
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependencies(java.lang.String)
	 */
	public void addDependencies( String pomPath ) throws ProjectException {
		addDependencies( new Project( pomPath), (List<String>)null );
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependencies(java.lang.String, java.util.List)
	 */
	public void addDependencies( String pomPath, List<String> scopes ) throws ProjectException {
		addDependencies( new Project( pomPath), scopes );
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependencies(com.tobedevoured.naether.maven.Project)
	 */
	public void addDependencies( Project project ) throws ProjectException {
		addDependencies( project, (List<String>)null );		
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addDependencies(com.tobedevoured.naether.maven.Project, java.util.List)
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

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#clearRemoteRepositories()
	 */
	public void clearRemoteRepositories() {
		setRemoteRepositories(new LinkedHashSet<RemoteRepository>());
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addRemoteRepositoryByUrl(java.lang.String)
	 */
	public void addRemoteRepositoryByUrl(String url) throws NaetherException {
		try {
			addRemoteRepository(RepoBuilder.remoteRepositoryFromUrl(url));
		} catch (MalformedURLException e) {
			log.error( "Malformed url: {}", url, e);
			throw new NaetherException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addRemoteRepositoryByUrl(java.lang.String, java.lang.String, java.lang.String)
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

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addRemoteRepository(java.lang.String, java.lang.String, java.lang.String)
	 */
	public final void addRemoteRepository(String id, String type, String url) {
        if (this.repositoryIds.add(id) ) {
		    getRemoteRepositories().add(new RemoteRepository(id, type, url));
        }
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#addRemoteRepository(org.sonatype.aether.repository.RemoteRepository)
	 */
	public void addRemoteRepository(RemoteRepository remoteRepository) {
        if (this.repositoryIds.add(remoteRepository.getId()) ) {
		    getRemoteRepositories().add(remoteRepository);
        }
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#setRemoteRepositories(java.util.Set)
	 */
	public void setRemoteRepositories(Set<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getRemoteRepositories()
	 */
	public Set<RemoteRepository> getRemoteRepositories() {
		return remoteRepositories;
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getRemoteRepositoryUrls()
	 */
	public List<String> getRemoteRepositoryUrls() {
		List<String> urls = new ArrayList<String>();
		for( RemoteRepository repo : getRemoteRepositories() ) {
			urls.add( repo.getUrl() );
		}
		
		return urls;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#resolveDependencies()
	 */
	public void resolveDependencies() throws URLException, DependencyException {
		resolveDependencies(true, null);
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#resolveDependencies(boolean)
	 */
	public void resolveDependencies(boolean downloadArtifacts) throws URLException, DependencyException {
		resolveDependencies( downloadArtifacts, null );
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#resolveDependencies(boolean, java.util.Map)
	 */
	public void resolveDependencies(boolean downloadArtifacts, Map<String,String> properties) throws URLException, DependencyException {
		log.debug( "Resolving Dependencies" );
		
		log.debug("Local Repo Path: {}", localRepoPath);

		if ( log.isDebugEnabled() ) {
			log.debug("Remote Repositories:");
			for (RemoteRepository repo : getRemoteRepositories()) {
				log.debug("  {}", repo.toString());
			}
		}

		RepositoryClient repoClient = new RepositoryClient(this.getLocalRepoPath());
		if ( properties != null ) {
			repoClient.setProperties( properties );
		}
		
		// If there are local build artifacts, create a BuildWorkspaceReader to
		// override remote artifacts with the local build artifacts.
		if ( buildArtifacts.size() > 0 ) {
			repoClient.setBuildWorkspaceReader( buildArtifacts );
		}
		
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setDependencies( new ArrayList<Dependency>(getDependencies()));
		
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
			collectResult = repoClient.collectDependencies(collectRequest);
		} catch (DependencyCollectionException e) {
			throw new DependencyException(e);
		}

		preorderedNodeList = new PreorderNodeListGenerator();
		if (downloadArtifacts) {
			DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

			log.debug("Resolving dependencies to files");
			DependencyResult dependencyResult;
			try {
				dependencyResult = repoClient.resolveDependencies(dependencyRequest);
			} catch (DependencyResolutionException e) {
				throw new DependencyException(e);
			}
			dependencyResult.getRoot().accept(preorderedNodeList);

		} else {
			collectResult.getRoot().accept(preorderedNodeList);
		}

		//this.setDependencies( new HashSet<Dependency>(preorderedNodeList.getDependencies(true)));
		log.debug("Setting resolved dependencies: {}", this.getDependencies());
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#deployArtifact(com.tobedevoured.naether.deploy.DeployArtifact)
	 */
	public void deployArtifact(DeployArtifact deployArtifact) throws DeployException {
		log.debug("deploy artifact: {} ", deployArtifact.getNotation());
		
		RepositoryClient repoClient = new RepositoryClient(this.getLocalRepoPath());
		
		DeployRequest deployRequest = new DeployRequest();
		deployRequest.addArtifact(deployArtifact.getJarArtifact());
		if (deployArtifact.getPomArtifact() != null) {
			deployRequest.addArtifact(deployArtifact.getPomArtifact());
		}
		deployRequest.setRepository(deployArtifact.getRemoteRepo());

		log.debug("deploying artifact {}", deployArtifact.getNotation());
		try {
			repoClient.deploy(deployRequest);
		} catch (DeploymentException e) {
			log.error("Failed to deploy artifact", e);
			throw new DeployException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#install(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void install(String notation, String pomPath, String filePath ) throws InstallException {
		log.debug("installing artifact: {} ", notation);
		
		RepositoryClient repoClient = new RepositoryClient(this.getLocalRepoPath());
		InstallRequest installRequest = new InstallRequest();
		
		if ( filePath != null ) {
			DefaultArtifact jarArtifact = new DefaultArtifact( notation );
			jarArtifact = (DefaultArtifact)jarArtifact.setFile( new File( filePath ) );
			
			installRequest.addArtifact( jarArtifact );
				
			if ( pomPath != null ) {
				SubArtifact pomArtifact = new SubArtifact( jarArtifact, "", Const.POM );
				pomArtifact = (SubArtifact)pomArtifact.setFile( new File( pomPath ) );
				installRequest.addArtifact( pomArtifact );
			}
			
		// If Pom only, without a jar, ensure the notation type is set to pom
		} else  if ( pomPath != null ) {
			Map<String,String> notationMap = Notation.parse( notation );
			notationMap.put( "type", Const.POM );
			
			org.sonatype.aether.spi.connector.ArtifactDownload at;
			
			DefaultArtifact pomArtifact = new DefaultArtifact( Notation.generate(notationMap) );
			pomArtifact = (DefaultArtifact)pomArtifact.setFile( new File(pomPath ) );
			installRequest.addArtifact( pomArtifact );	
		}
				
		try {
			repoClient.install(installRequest);
		} catch (InstallationException e) {
			log.error("Failed to install artifact", e);
			throw new InstallException(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getResolvedClassPath()
	 */
	public String getResolvedClassPath() {
		if ( preorderedNodeList != null ) {
			return preorderedNodeList.getClassPath();
		} else {
			return null;
		}
	}
	
	
	public Collection<Dependency> currentDependencies() {
		Collection<Dependency> dependencies = null;
		if ( preorderedNodeList != null ) {
			dependencies = preorderedNodeList.getDependencies(true);
		} else {
			dependencies = this.getDependencies();
		}
		
		return dependencies;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getDependenciesNotation()
	 */
	public Set<String> getDependenciesNotation() {
		Set<String> notations = new HashSet<String>();
		
		for (Dependency dependency : currentDependencies() ) {
			notations.add(Notation.generate(dependency));
		}

		return notations;
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getDependenciesGraph()
	 */
	public Map<String,Map> getDependenciesGraph() {
		Map<String,Map> graph = new HashMap<String,Map>();
		
		if ( preorderedNodeList != null ) {

			List<DependencyNode> nodes = preorderedNodeList.getNodes();
			Set<String> notations = new HashSet<String>();
			
			for (Dependency dependency : getDependencies() ) {
				notations.add(Notation.generate(dependency));
			}
			
			for( DependencyNode node: nodes) {
				String notation = Notation.generate( node.getDependency().getArtifact() );
			
				// Show Graph with dependenices as root and transitive as children
				if ( notations.contains( notation ) ) {
					graph.put( notation, mapDependenciesFromNode( node ) );
				}
			}
		}
		
		return graph;
	}
	
	
	private Map<String,Map> mapDependenciesFromNode(DependencyNode node) {
		Map<String,Map> graph = new HashMap<String,Map>();
		for( DependencyNode child : node.getChildren() ) {
			String notation = Notation.generate( child.getDependency().getArtifact() );
			graph.put( notation, mapDependenciesFromNode( child ) );
		}
		
		return graph;
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getDependenciesPath()
	 */
	public Map<String,String> getDependenciesPath() {
		Map<String,String> dependenciesMap = new HashMap<String,String>();
		for (Dependency dependency : currentDependencies() ) {
			if ( dependency.getArtifact().getFile() != null ) {
				dependenciesMap.put( Notation.generate( dependency ), dependency.getArtifact().getFile().getAbsolutePath() );
			}
		}
		
		return dependenciesMap;
	}
	


	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#setLocalRepoPath(java.lang.String)
	 */
	public void setLocalRepoPath(String repoPath) {
		this.localRepoPath = repoPath;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getLocalRepoPath()
	 */
	public String getLocalRepoPath() {
		return localRepoPath;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#setDependencies(java.util.Set)
	 */
	public void setDependencies(Set<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getDependencies()
	 */
	public Set<Dependency> getDependencies() {
		return dependencies;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#getBuildArtifacts()
	 */
	public List<Artifact> getBuildArtifacts() {
		return buildArtifacts;
	}

	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#setBuildArtifacts(java.util.List)
	 */
	public void setBuildArtifacts(List<Artifact> buildArtifacts) {
		this.buildArtifacts = buildArtifacts;
	}
	
	/* (non-Javadoc)
	 * @see com.tobedevoured.naether.api.Naether#downloadArtifacts(java.util.List)
	 */
	@SuppressWarnings("rawtypes")
	public List<File> downloadArtifacts( List artifactsOrNotations ) throws NaetherException {
		
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
		
        List<File> files = new ArrayList<File>();
        
        for ( Artifact artifact : artifacts ) {
	        
        	log.debug( "Downloading {}", artifact );
        	
	        ArtifactRequest artifactRequest = new ArtifactRequest();
	        artifactRequest.setArtifact( artifact );
	        for ( RemoteRepository repo : this.getRemoteRepositories() ) {
	        	artifactRequest.addRepository( repo );
	        }
	
	        RepositoryClient repoClient = new RepositoryClient(this.getLocalRepoPath());
	        
	        ArtifactResult artifactResult = null;
			try {
				artifactResult = repoClient.resolveArtifact(artifactRequest );
			} catch (ArtifactResolutionException e) {
				throw new ResolveException(e);
			}
	
	        Artifact downloadedArtifact = artifactResult.getArtifact();
	        files.add( downloadedArtifact.getFile() );
        }
        
        return files;
	}
}
