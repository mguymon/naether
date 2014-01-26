package com.tobedevoured.naether.repo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;

import com.tobedevoured.naether.aether.ValidSystemScopeDependencySelector;

/**
 * Repositoy Client, manages the {@link RepositorySystem} and the {@link RepositorySystemSession}.
 * 
 * @author Michael Guymon
 *
 */
public class RepositoryClient {
	private static Logger log = LoggerFactory.getLogger(RepositoryClient.class);

	private RepositorySystem repositorySystem;
	private DefaultRepositorySystemSession systemSession;
	private String localRepoPath;
	
	public RepositoryClient( String localRepoPath ) {
		this.localRepoPath = localRepoPath;
		
		newRepositorySystem();
	}
	
	/**
	 * Set User Properties for a {@link RepositorySystemSession}
	 * 
	 * @param properties Map<String,String>
	 */
	public void setProperties( Map<String,String> properties ) {
		Map<String,String> userProperties = systemSession.getUserProperties();
		if ( userProperties == null ) {
			userProperties = new HashMap<String,String>();
		}
		userProperties.putAll( properties );
		
		log.debug( "Session userProperties: {}", userProperties );
		
		systemSession = systemSession.setUserProperties( userProperties );
		
	}
	
	/**
	 * Collect Dependencies
	 * 
	 * @param collectRequest {@link CollectRequest}
	 * @return {@link CollectRequest}
	 * @throws DependencyCollectionException
	 */
	public CollectResult collectDependencies( CollectRequest collectRequest) throws DependencyCollectionException {
		return repositorySystem.collectDependencies(systemSession,collectRequest);
	}
	
	/**
	 * Resolve Dependencies
	 * 
	 * @param dependencyRequest {@link DependencyRequest}
	 * @return {@link DependencyResult}
	 * @throws DependencyResolutionException
	 */
	public DependencyResult resolveDependencies( DependencyRequest dependencyRequest ) throws DependencyResolutionException {
		return repositorySystem.resolveDependencies(systemSession, dependencyRequest);
	}
	
	/**
	 * Deploy
	 * 
	 * @param deployRequest {@link DeployRequest}
	 * @throws DeploymentException
	 */
	public void deploy(DeployRequest deployRequest) throws DeploymentException {
		repositorySystem.deploy(systemSession, deployRequest);
	}
	
	/**
	 * Install
	 * 
	 * @param installRequest {@link InstallRequest}
	 * @throws InstallationException
	 */
	public void install(InstallRequest installRequest) throws InstallationException {
		repositorySystem.install(systemSession, installRequest);
	}
	
	/**
	 * Resolve Artifact
	 * 
	 * @param artifactRequest {@link ArtifactResult}
	 * @return {@link ArtifactResult}
	 * @throws ArtifactResolutionException
	 */
	public ArtifactResult resolveArtifact(ArtifactRequest artifactRequest) throws ArtifactResolutionException {
		return repositorySystem.resolveArtifact(systemSession, artifactRequest);
	}
	
	
	/**
	 * Set the {@link BuildWorkspaceReader}
	 * 
	 * @param artifacts List<Artifact>
	 */
	public void setBuildWorkspaceReader(List<Artifact> artifacts) {
		BuildWorkspaceReader reader = new BuildWorkspaceReader();
		
		for ( Artifact artifact : artifacts ) {
			reader.addArtifact( artifact );
		}
		
		systemSession = systemSession.setWorkspaceReader( reader );
		
	}
	
	/**
	 * Create new {@link RepositorySystem}
	 * 
	 * @return {@link RepositorySystem}
	 */
	public void newRepositorySystem() {

        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        repositorySystem = locator.getService( RepositorySystem.class );

        DefaultRepositorySystemSession session = new DefaultRepositorySystemSession();
		//session = session.setDependencySelector( new AndDependencySelector( session.getDependencySelector(), new ValidSystemScopeDependencySelector() ) );
		session = session.setTransferListener(new LogTransferListener());
		session = session.setRepositoryListener(new LogRepositoryListener());


        session = session.setIgnoreArtifactDescriptorRepositories(true);

        final ArtifactDescriptorPolicy policy = new SimpleArtifactDescriptorPolicy(false, true);
        session = session.setArtifactDescriptorPolicy(policy);

		final LocalRepository localRepo = new LocalRepository( localRepoPath );
		session = session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
		
		systemSession = session;
	}

	public RepositorySystem getRepositorySystem() {
		return repositorySystem;
	}

	public RepositorySystemSession getSystemSession() {
		return systemSession;
	}

}
