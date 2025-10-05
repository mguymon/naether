package com.tobedevoured.naether.maven;

import com.tobedevoured.naether.repo.RepositoryClient;
import java.io.File;
import java.util.*;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.apache.maven.repository.internal.ArtifactDescriptorUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;

public class NaetherModelResolver implements ModelResolver {

    private final List<RemoteRepository> repositories;
    private final Set<String> repositoryIds;

    private final RepositorySystem system;
    private final RepositorySystemSession session;

    public NaetherModelResolver(
            RepositoryClient repositoryClient, Collection<RemoteRepository> remoteRepositories) {
        this.system = repositoryClient.getRepositorySystem();
        this.session = repositoryClient.getSystemSession();
        if (remoteRepositories != null) {
            this.repositories = new ArrayList<>(remoteRepositories);
        } else {
            this.repositories = new ArrayList<>();
        }
        this.repositoryIds = new HashSet<>();
    }

    public NaetherModelResolver(NaetherModelResolver original) {
        this.system = original.system;
        this.session = original.session;
        this.repositories = new ArrayList<>(original.repositories);
        this.repositoryIds = new HashSet<>(original.repositoryIds);
    }

    public ModelSource resolveModel(String groupId, String artifactId, String version)
            throws UnresolvableModelException {
        Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);

        try {
            ArtifactRequest request = new ArtifactRequest(pomArtifact, repositories, null);
            // request.setTrace( trace );
            pomArtifact = system.resolveArtifact(session, request).getArtifact();
        } catch (ArtifactResolutionException e) {
            throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
        }

        File pomFile = pomArtifact.getPath().toFile();

        return new FileModelSource(pomFile);
    }

    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
    }

    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        return resolveModel(
                dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    public void addRepository(Repository repository) throws InvalidRepositoryException {
        addRepository(repository, false);
    }

    public void addRepository(Repository repository, boolean replace)
            throws InvalidRepositoryException {
        if (!replace && !repositoryIds.add(repository.getId())) {
            return;
        }

        if (replace) {
            repositoryIds.add(repository.getId());
        }

        RemoteRepository remoteRepo = ArtifactDescriptorUtils.toRemoteRepository(repository);
        repositories.add(remoteRepo);
    }

    public ModelResolver newCopy() {
        return new NaetherModelResolver(this);
    }
}
