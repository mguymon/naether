package com.slackworks;

// Java SE
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Apache Maven
import org.apache.maven.repository.internal.DefaultServiceLocator;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;

// SL4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Sonatype Aether Dependency Management
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.sonatype.aether.connector.wagon.WagonProvider;
import org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory;


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
	
	private String repoPath;
	private List<Dependency> dependencies;
	private List<RemoteRepository> remoteRepositories;
	private PreorderNodeListGenerator nlg;
	
	/**
	 * Create new instance
	 */
	public Naether() {
		dependencies = new ArrayList<Dependency>();
		setRemoteRepositories(new ArrayList<RemoteRepository>());
		getRemoteRepositories().add( new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" ) );
		
		String userHome = System.getProperty("user.home");
		setRepoPath(userHome + File.separator + ".m2" + File.separator + "repository");
	}
	
	/**
	 * Add dependency by String notation
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 */
	public void addDependency( String notation) {
		addDependency( notation, "compile" );
	}
	
	/**
	 * * Add dependency by String notation and Maven scope
	 * 
	 * groupId:artifactId:type:version
	 * 
	 * @param notation String
	 * @param scope String
	 */
	public void addDependency( String notation, String scope ) {
		Dependency dependency =
            new Dependency( new DefaultArtifact( notation ), scope );
		addDependency( dependency );
	}
	
	/**
	 * Add Dependency
	 * 
	 * @param dependency {@link Dependency}
	 */
	public void addDependency( Dependency dependency ) {
		dependencies.add( dependency );
	}
	
	/**
	 * Add RemoteRepository
	 * 
	 * @param remoteRepository
	 */
	public void addRemoteRepository( RemoteRepository remoteRepository ) {
		getRemoteRepositories().add( remoteRepository );
	}
	
	public RepositorySystem newRepositorySystem() throws Exception {
		DefaultServiceLocator locator = new DefaultServiceLocator();
        locator.setServices( WagonProvider.class, new ManualWagonProvider() );
        locator.addService( RepositoryConnectorFactory.class, WagonRepositoryConnectorFactory.class );

        return locator.getService( RepositorySystem.class );

	}

	public RepositorySystemSession newSession( RepositorySystem system ) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();

        LocalRepository localRepo = new LocalRepository( getRepoPath() );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( localRepo ) );

        return session;
    }
	
	/**
	 * Resolve Dependencies 
	 * 
	 * @throws Exception
	 */
	public void resolveDependencies() throws Exception {
		log.info( "Repo Path: {}", repoPath );
		
		log.info( "Remote Repositories:" );
		for( RemoteRepository repo: getRemoteRepositories() ) {
			log.info( "  {}", repo.toString() );
		}
		
		RepositorySystem repoSystem = newRepositorySystem();

        RepositorySystemSession session = newSession( repoSystem );

        //Dependency dependency =
        //    new Dependency( new DefaultArtifact( "org.apache.activemq:activemq-spring:jar:5.4.2" ), "compile" );
        
        CollectRequest collectRequest = new CollectRequest();
        //collectRequest.setRoot( dependency );
        collectRequest.setDependencies(getDependencies());
        
        for ( RemoteRepository repo: getRemoteRepositories() ) {
        	collectRequest.addRepository( repo );
        }
        
        DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest( node, null );

        log.info( "Resolving dependencies to files" );
        repoSystem.resolveDependencies( session, dependencyRequest  );

        nlg = new PreorderNodeListGenerator();
        node.accept( nlg );
        
        log.debug( "Setting resolved dependencies" );
        this.setDependencies( nlg.getDependencies(true) );
	}
	
	public String getResolvedClassPath() {
		return nlg.getClassPath();
	}
	
	public void setRepoPath(String repoPath) {
		this.repoPath = repoPath;
	}

	public String getRepoPath() {
		return repoPath;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}
	
	public List<String> getDependenciesNotation() {
		List<String> notations = new ArrayList<String>();
		for ( Dependency dependency: getDependencies() ) {
			notations.add( Notation.generate( dependency ) );
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
