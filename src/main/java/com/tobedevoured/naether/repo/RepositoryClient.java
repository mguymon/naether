package com.tobedevoured.naether.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.resolution.DependencyResult;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.graph.selector.AndDependencySelector;

import com.tobedevoured.naether.aether.ValidSystemScopeDependencySelector;

/**
 * Repository Client, manages the {@link RepositorySystem} and the {@link MavenRepositorySystemSession}.
 * 
 * @author Michael Guymon
 *
 */
public class RepositoryClient {
	private static Logger log = LoggerFactory.getLogger(RepositoryClient.class);

	private RepositorySystem repositorySystem = null;
	private MavenRepositorySystemSession systemSession = null;
	private String localRepoPath;
	
	public RepositoryClient( String localRepoPath ) {
		this.localRepoPath = localRepoPath;
		
		newRepositorySystem();
	}
	
	/**
	 * Set User Properties for a {@link MavenRepositorySystemSession}
	 * 
	 * @param properties Map
	 */
	public void setProperties( Map<String,String> properties ) {
		Map<String,String> userProperties = systemSession.getUserProperties();
		if ( userProperties == null ) {
			userProperties = new HashMap<String,String>();
		}
		userProperties.putAll( properties );
		
		log.debug( "Session userProperties: {}", userProperties );
		
		systemSession = (MavenRepositorySystemSession)systemSession.setUserProperties( userProperties );
		
	}
	
	/**
	 * Collect Dependencies
	 * 
	 * @param collectRequest {@link CollectRequest}
	 * @return {@link CollectRequest}
	 * @throws DependencyCollectionException exception
	 */
	public CollectResult collectDependencies( CollectRequest collectRequest) throws DependencyCollectionException {
		return repositorySystem.collectDependencies(systemSession,collectRequest);
	}
	
	/**
	 * Resolve Dependencies
	 * 
	 * @param dependencyRequest {@link DependencyRequest}
	 * @return {@link DependencyResult}
	 * @throws DependencyResolutionException exception
	 */
	public DependencyResult resolveDependencies( DependencyRequest dependencyRequest ) throws DependencyResolutionException {
		return repositorySystem.resolveDependencies(systemSession, dependencyRequest);
	}
	
	/**
	 * Deploy
	 * 
	 * @param deployRequest {@link DeployRequest}
	 * @throws DeploymentException exception
	 */
	public void deploy(DeployRequest deployRequest) throws DeploymentException {
		repositorySystem.deploy(systemSession, deployRequest);
	}
	
	/**
	 * Install
	 * 
	 * @param installRequest {@link InstallRequest}
	 * @throws InstallationException exception
	 */
	public void install(InstallRequest installRequest) throws InstallationException {
		repositorySystem.install(systemSession, installRequest);
	}
	
	/**
	 * Resolve Artifact
	 * 
	 * @param artifactRequest {@link ArtifactResult}
	 * @return {@link ArtifactResult}
	 * @throws ArtifactResolutionException exception
	 */
	public ArtifactResult resolveArtifact(ArtifactRequest artifactRequest) throws ArtifactResolutionException {
		return repositorySystem.resolveArtifact(systemSession, artifactRequest);
	}
	
	
	/**
	 * Set the {@link BuildWorkspaceReader}
	 * 
	 * @param artifacts List
	 */
	public void setBuildWorkspaceReader(List<Artifact> artifacts) {
		BuildWorkspaceReader reader = new BuildWorkspaceReader();
		
		for ( Artifact artifact : artifacts ) {
			reader.addArtifact( artifact );
		}
		
		systemSession = (MavenRepositorySystemSession)systemSession.setWorkspaceReader( reader );
		
	}

	/**
	 * Create new {@link RepositorySystem}
	 */
	public void newRepositorySystem() {
		DefaultServiceLocator locator = new DefaultServiceLocator();
		locator.setServices(WagonProvider.class, new ManualWagonProvider());
		locator.addService(RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class);

		repositorySystem = locator.getService(RepositorySystem.class);

		MavenRepositorySystemSession session = new MavenRepositorySystemSession();
		session = (MavenRepositorySystemSession)session.setDependencySelector( new AndDependencySelector( session.getDependencySelector(), new ValidSystemScopeDependencySelector() ) );
		session = (MavenRepositorySystemSession)session.setTransferListener(new LogTransferListener());
		session = (MavenRepositorySystemSession)session.setRepositoryListener(new LogRepositoryListener());

		session = (MavenRepositorySystemSession)session.setIgnoreMissingArtifactDescriptor( false );

		LocalRepository localRepo = new LocalRepository( localRepoPath );
		session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(localRepo));

		systemSession = session;
	}

	public RepositorySystem getRepositorySystem() {
		return repositorySystem;
	}

	public MavenRepositorySystemSession getSystemSession() {
		return systemSession;
	}

}
