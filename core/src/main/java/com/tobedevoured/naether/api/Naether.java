package com.tobedevoured.naether.api;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.repository.RemoteRepository;

import com.tobedevoured.naether.DependencyException;
import com.tobedevoured.naether.NaetherException;
import com.tobedevoured.naether.URLException;
import com.tobedevoured.naether.deploy.DeployArtifact;
import com.tobedevoured.naether.deploy.DeployException;
import com.tobedevoured.naether.deploy.InstallException;
import com.tobedevoured.naether.maven.Project;
import com.tobedevoured.naether.maven.ProjectException;

public interface Naether {

	/**
	 * Clear dependencies
	 */
	public void clearDependencies();

	/**
	 * Clear local BuildArtifacts
	 */
	public void clearBuildArtifacts();

	/**
	 * Add a local Build Artifact manually by String notation, path to the artifact,
	 * and path to the POM. 
	 * 
	 * The Build Artifact will override matchingdependencies found
	 * when resolving dependencies.
	 * 
	 * @param notation String
	 * @param path String
	 * @param pom String
	 */
	public void addBuildArtifact(String notation, String path,
			String pom);

	/**
	 * Add a local Build Artifact manually by String notation and path to the artifact.
	 * The POM for the artifact is created automatically based on the String notation.
	 * 
	 * The Build Artifact will override matchingdependencies found
	 * when resolving dependencies.
	 * 
	 * @param notation String
	 * @param path String
	 * @throws NaetherException if failed to create a POM
	 */
	public void addBuildArtifact(String notation, String path)
			throws NaetherException;

	/**
	 * Add dependency by String notation with default compile scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 */
	public void addDependency(String notation);

	/**
	 * Add dependency by String notation and Maven scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 * @param scope String
	 */
	public void addDependency(String notation, String scope);

	/**
	 * Add {@link Dependency}
	 * 
	 * @param dependency {@link Dependency}
	 */
	public void addDependency(Dependency dependency);

	/**
	 * Add {@link org.apache.maven.model.Dependency}
	 */
	public void addDependency(
			org.apache.maven.model.Dependency projectDependency);

	/**
	 * Add dependences from a Maven POM
	 * 
	 * @param pomPath String path to POM
	 * @throws ProjectException
	 */
	public void addDependencies(String pomPath)
			throws ProjectException;

	/**
	 * Add dependencies from a Maven POM, limited to a {@link List<String>} of scopes.
	 * 
	 * @param pomPath String path to POM
	 * @param scopes Link<String> of scopes
	 * @throws ProjectException
	 * @see {{@link #addDependencies(Project, List)}
	 */
	public void addDependencies(String pomPath, List<String> scopes)
			throws ProjectException;

	/**
	 * Add dependencies from a Maven POM
	 * 
	 * @param project {@link Model}
	 * @throws ProjectException 
	 * @see {{@link #addDependencies(Project, List)}
	 */
	public void addDependencies(Project project)
			throws ProjectException;

	/**
	 * Add dependencies from a Maven POM, limited to a {@link List<String>} of scopes. Adds
	 * all Repositories from the Maven Pom.
	 * 
	 * @param project {@link Project}
	 * @param scopes List<String> of dependency scopes
	 * @throws ProjectException 
	 */
	public void addDependencies(Project project, List<String> scopes)
			throws ProjectException;

	/**
	 * Remove all {@link RemoteRepository}
	 */
	public void clearRemoteRepositories();

	/**
	 * Add a {@link RemoteRepository} by String url
	 * 
	 * @param url String
	 * @throws URLException
	 * @throws MalformedURLException
	 */
	public void addRemoteRepositoryByUrl(String url)
			throws NaetherException;

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
	public void addRemoteRepositoryByUrl(String url, String username,
			String password) throws URLException;

	/**
	 * Add a {@link RemoteRepository}
	 * 
	 * @param id String
	 * @param type String
	 * @param url String
	 */
	public void addRemoteRepository(String id, String type, String url);

	/**
	 * Add {@link RemoteRepository}
	 * 
	 * @param remoteRepository {@link RemoteRepository}
	 */
	public void addRemoteRepository(RemoteRepository remoteRepository);

	/**
	 * Set {@link List} of {@link RemoteRepository}
	 * 
	 * @param remoteRepositories {@link List}
	 */
	public void setRemoteRepositories(
			Set<RemoteRepository> remoteRepositories);

	/**
	 * Get {@link Set} of {@link RemoteRepository}
	 * 
	 * @return {@link Set}
	 */
	public Set<RemoteRepository> getRemoteRepositories();

	/**
	 * Get {@link List} of String urls 
	 * 
	 * @return {@link List<String>}
	 */
	public List<String> getRemoteRepositoryUrls();

	/**
	 * Resolve dependencies and download artifacts
	 * 
	 * @throws DependencyException
	 * @throws URLException
	 * 
	 * @throws Exception
	 */
	public void resolveDependencies() throws URLException,
			DependencyException;

	/**
	 * Resolve Dependencies
	 * 
	 * @param downloadArtifacts boolean if artifacts are to be downloaded
	 * @throws URLException
	 * @throws DependencyException
	 */
	public void resolveDependencies(boolean downloadArtifacts)
			throws URLException, DependencyException;

	/**
	 * Resolve Dependencies
	 * 
	 * @param downloadArtifacts if artifacts are to be downloaded
	 * @param properties Map<String,String> of properties
	 * @throws URLException
	 * @throws DependencyException
	 */
	public void resolveDependencies(boolean downloadArtifacts,
			Map<String, String> properties) throws URLException,
			DependencyException;

	/**
	 * Deploy an Artifact
	 * 
	 * @param deployArtifact {@link DeployArtifact}
	 * @throws DeploymentException
	 */
	public void deployArtifact(DeployArtifact deployArtifact)
			throws DeployException;

	/**
	 * Install Artifact to local repo. 
	 * 
	 * If installing a POM, filePath can be null. If install a Jar without a POM, pomPath
	 * can be null.
	 * 
	 * @param String notation String
	 * @param String pomPath String
	 * @param String filePath String
	 * @throws InstallException
	 */
	public void install(String notation, String pomPath,
			String filePath) throws InstallException;

	/**
	 * Classpath from resolved artifacts
	 * 
	 * @return String
	 */
	public String getResolvedClassPath();

	/**
	 * {@link List<String>} of {@link Dependency} converted to String notation
	 * 
	 * @return {@link List<String>}
	 */
	public Set<String> getDependenciesNotation();

	/**
	 * {@link Map} of String notation and the corresponding String file path 
	 * 
	 * @return {@link Map<String,String>}
	 */
	public Map<String, String> getDependenciesPath();

	/**
	 * Set local repository path. This is the destination for downloaded
	 * metadata and artifacts.
	 * 
	 * @param repoPath
	 *            String
	 */
	public void setLocalRepoPath(String repoPath);

	/**
	 * Get local repository path. This is the destination for downloaded
	 * metadata and artifacts.
	 * 
	 * @return String
	 */
	public String getLocalRepoPath();

	/**
	 * Set the {@link List} of {@link Dependency}
	 * 
	 * @param dependencies
	 *            {@link List}
	 */
	public void setDependencies(Set<Dependency> dependencies);

	/**
	 * {@link List} of {@link Dependency}
	 * 
	 * @return {@link List}
	 */
	public Set<Dependency> getDependencies();

	/**
	 * Get List<Artifact> of local artifacts that are used in dependency
	 * resolution.
	 * 
	 * @return List<Artifact>
	 */
	public List<Artifact> getBuildArtifacts();

	/**
	 * Set List<Artifact> for local artifacts that are used in depdency resolution.
	 * 
	 * @param buildArtifacts List<Artifact>
	 */
	public void setBuildArtifacts(List<Artifact> buildArtifacts);

	/**
	 * Download to the local repository a List of {@link Artifact} or String
	 * notations.
	 * 
	 * @param artifactsOrNotations @{link List} of {@link Artifact} or String notation
	 * @return List<File>
	 * @throws NaetherException
	 */
	@SuppressWarnings("rawtypes")
	public List<File> downloadArtifacts(List artifactsOrNotations)
			throws NaetherException;

}