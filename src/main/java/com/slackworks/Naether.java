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
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
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

	private String repoPath;
	private List<Dependency> dependencies;
	private List<RemoteRepository> remoteRepositories;
	private PreorderNodeListGenerator nlg;
	
	public Naether() {
		dependencies = new ArrayList<Dependency>();
		setRemoteRepositories(new ArrayList<RemoteRepository>());
		getRemoteRepositories().add( new RemoteRepository( "central", "default", "http://repo1.maven.org/maven2/" ) );
		
		String userHome = System.getProperty("user.home");
		setRepoPath(userHome + File.separator + ".m2" + File.separator + "repository");
	}
	
	public void addDepedency( Dependency dependency ) {
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
	
	public void resolveDependencies() throws Exception {
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

        repoSystem.resolveDependencies( session, dependencyRequest  );

        nlg = new PreorderNodeListGenerator();
        node.accept( nlg );
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
	
	public void setRemoteRepositories(List<RemoteRepository> remoteRepositories) {
		this.remoteRepositories = remoteRepositories;
	}

	public List<RemoteRepository> getRemoteRepositories() {
		return remoteRepositories;
	}

	public static void main( String[] args ) throws Exception {
		Naether naether = new Naether();
		System.out.println( naether.getRepoPath() );
		Dependency dependency =
            new Dependency( new DefaultArtifact( "org.apache.activemq:activemq-spring:jar:5.4.2" ), "compile" );
        naether.addDepedency(dependency);
        naether.resolveDependencies();
        System.out.println( naether.getResolvedClassPath() );
	}

}
