package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.repo.RepositoryClient;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.impl.internal.DefaultRemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.util.*;

public class NaetherModelResolver implements ModelResolver {

    private List<RemoteRepository> repositories;
    private Set<String> repositoryIds;

    private RepositorySystem system;
    private RepositorySystemSession session;

    private RemoteRepositoryManager remoteRepositoryManager;

    public NaetherModelResolver(RepositoryClient repositoryClient, List<RemoteRepository> remoteRepositories) {
        this.system = repositoryClient.getRepositorySystem();
        this.session = repositoryClient.getSystemSession();
        if ( remoteRepositories != null ) {
            this.repositories = new ArrayList<RemoteRepository>(remoteRepositories);
        } else {
            this.repositories = new ArrayList<RemoteRepository>();
        }
        this.repositoryIds = new HashSet<String>();
        this.remoteRepositoryManager = new DefaultRemoteRepositoryManager();
    }

    public NaetherModelResolver(NaetherModelResolver original) {
        this.system = original.system;
        this.session = original.session;
        this.repositories = new ArrayList<RemoteRepository>(original.repositories);
        this.repositoryIds = new HashSet<String>(original.repositoryIds);
        this.remoteRepositoryManager = original.remoteRepositoryManager;
    }

    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
        Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "", "pom", version );

        try {
             ArtifactRequest request = new ArtifactRequest( pomArtifact, repositories, null );
             //request.setTrace( trace );
             pomArtifact = system.resolveArtifact( session, request ).getArtifact();
         } catch ( ArtifactResolutionException e ) {
            throw new UnresolvableModelException( e.getMessage(), groupId, artifactId, version, e );
         }

        File pomFile = pomArtifact.getFile();

        return new FileModelSource( pomFile );
    }

    public void addRepository(Repository repository) throws InvalidRepositoryException {
        if ( !repositoryIds.add( repository.getId() ) ) {
            return;
        }

        List<RemoteRepository> newRepositories =
             Collections.singletonList( ArtifactDescriptorUtils.toRemoteRepository( repository ) );

         this.repositories =
             remoteRepositoryManager.aggregateRepositories( session, repositories, newRepositories, true );
    }

    public ModelResolver newCopy() {
        return new NaetherModelResolver(this);
    }
}
