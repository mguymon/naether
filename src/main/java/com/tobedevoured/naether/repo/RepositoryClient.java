package com.tobedevoured.naether.repo;

import com.tobedevoured.naether.aether.ValidSystemScopeDependencySelector;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.supplier.SessionBuilderSupplier;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Repository Client, manages the {@link RepositorySystem} and the {@link RepositorySystemSession}.
 *
 * @author Michael Guymon
 */
public class RepositoryClient {
    private static final Logger log = LoggerFactory.getLogger(RepositoryClient.class);

    private RepositorySystem repositorySystem = null;
    private RepositorySystemSession systemSession = null;
    private final String localRepoPath;

    public RepositoryClient(String localRepoPath) {
        this.localRepoPath = localRepoPath;

        newRepositorySystem();
    }

    /**
     * Set User Properties for a {@link RepositorySystemSession}
     *
     * @param properties Map
     */
    public void setProperties(Map<String, String> properties) {
        Map<String, String> userProperties = systemSession.getUserProperties();
        Map<String, String> newUserProperties = new HashMap<>();
        if (userProperties != null) {
            newUserProperties.putAll(userProperties);
        }
        newUserProperties.putAll(properties);

        log.debug("Session userProperties: {}", newUserProperties);

        // Rebuild session with updated properties using withLocalRepositoryBaseDirectories
        RepositorySystemSession.SessionBuilder sessionBuilder =
                new SessionBuilderSupplier(repositorySystem).get();

        systemSession =
                sessionBuilder
                        .setUserProperties(newUserProperties)
                        .withLocalRepositoryBaseDirectories(Paths.get(localRepoPath))
                        .setTransferListener(new LogTransferListener())
                        .setRepositoryListener(new LogRepositoryListener())
                        .setConfigProperty("aether.artifactDescriptor.ignoreMissing", false)
                        .build();
    }

    /**
     * Collect Dependencies
     *
     * @param collectRequest {@link CollectRequest}
     * @return {@link CollectRequest}
     * @throws DependencyCollectionException exception
     */
    public CollectResult collectDependencies(CollectRequest collectRequest)
            throws DependencyCollectionException {
        return repositorySystem.collectDependencies(systemSession, collectRequest);
    }

    /**
     * Resolve Dependencies
     *
     * @param dependencyRequest {@link DependencyRequest}
     * @return {@link DependencyResult}
     * @throws DependencyResolutionException exception
     */
    public DependencyResult resolveDependencies(DependencyRequest dependencyRequest)
            throws DependencyResolutionException {
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
    public ArtifactResult resolveArtifact(ArtifactRequest artifactRequest)
            throws ArtifactResolutionException {
        return repositorySystem.resolveArtifact(systemSession, artifactRequest);
    }

    /**
     * Set the {@link BuildWorkspaceReader}
     *
     * @param artifacts List
     */
    public void setBuildWorkspaceReader(List<Artifact> artifacts) {
        BuildWorkspaceReader reader = new BuildWorkspaceReader();

        for (Artifact artifact : artifacts) {
            reader.addArtifact(artifact);
        }

        // Rebuild session with workspace reader using withLocalRepositoryBaseDirectories
        DependencySelector currentSelector = systemSession.getDependencySelector();
        RepositorySystemSession.SessionBuilder sessionBuilder =
                new SessionBuilderSupplier(repositorySystem).get();

        systemSession =
                sessionBuilder
                        .setWorkspaceReader(reader)
                        .setDependencySelector(currentSelector)
                        .withLocalRepositoryBaseDirectories(Paths.get(localRepoPath))
                        .setTransferListener(new LogTransferListener())
                        .setRepositoryListener(new LogRepositoryListener())
                        .setConfigProperty("aether.artifactDescriptor.ignoreMissing", false)
                        .build();
    }

    /** Create new {@link RepositorySystem} */
    public void newRepositorySystem() {
        // Validate localRepoPath before using it
        if (localRepoPath == null) {
            throw new IllegalStateException(
                    "localRepoPath must be set before creating RepositorySystem");
        }

        // Use RepositorySystemSupplier to create a fully configured RepositorySystem
        RepositorySystemSupplier repositorySystemSupplier = new RepositorySystemSupplier();
        repositorySystem = repositorySystemSupplier.get();

        // Use SessionBuilderSupplier to create a Maven-like session
        SessionBuilderSupplier sessionBuilderSupplier =
                new SessionBuilderSupplier(repositorySystem);

        // Create a temporary session to get the default dependency selector
        Path path = Paths.get(localRepoPath);

        try (RepositorySystemSession.CloseableSession tempSession =
                sessionBuilderSupplier.get().withLocalRepositoryBaseDirectories(path).build()) {

            DependencySelector defaultSelector = tempSession.getDependencySelector();
            DependencySelector customSelector =
                    new AndDependencySelector(
                            defaultSelector, new ValidSystemScopeDependencySelector());

            // Build final session with a NEW session builder
            systemSession =
                    sessionBuilderSupplier
                            .get()
                            .setDependencySelector(customSelector)
                            .withLocalRepositoryBaseDirectories(path)
                            .setTransferListener(new LogTransferListener())
                            .setRepositoryListener(new LogRepositoryListener())
                            .setConfigProperty("aether.artifactDescriptor.ignoreMissing", false)
                            .build();
        }
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem;
    }

    public RepositorySystemSession getSystemSession() {
        return systemSession;
    }
}
