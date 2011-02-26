package com.slackworks;

// Java SE
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Apache Maven
import org.apache.maven.repository.internal.MavenRepositorySystemSession;

// Codehaus Plexus IoC
import org.codehaus.plexus.DefaultPlexusContainer;

// Sonatype Aether Dependency Management
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;

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
	
	public void addDependency( String notation) {
		addDependency( notation, "compile" );
	}
	
	public void addDependency( String notation, String scope ) {
		Dependency dependency =
            new Dependency( new DefaultArtifact( notation ), scope );
		addDependency( dependency );
	}
	
	public void addDependency( Dependency dependency ) {
		dependencies.add( dependency );
	}
	
	public void addRemoteRepository( RemoteRepository remoteRepository ) {
		getRemoteRepositories().add( remoteRepository );
	}
	
	public RepositorySystem newRepositorySystem() throws Exception {
		return new DefaultPlexusContainer().lookup( RepositorySystem.class );
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
        log.debug( "?" );
        DependencyNode node = repoSystem.collectDependencies( session, collectRequest ).getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest( node, null );

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
