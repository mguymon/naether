package com.tobedevoured.naether.repo;

import com.tobedevoured.naether.util.Notation;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;

public class BuildWorkspaceReader implements WorkspaceReader {

    private final Map<String, Artifact> artifacts = new HashMap<>();

    private final WorkspaceRepository workspaceRepository;

    public BuildWorkspaceReader() {
        this.workspaceRepository = new WorkspaceRepository("local-build", getClass());
    }

    public WorkspaceRepository getRepository() {
        return workspaceRepository;
    }

    public void addArtifact(Artifact artifact) {
        artifacts.put(Notation.generate(artifact), artifact);
    }

    public File findArtifact(Artifact artifact) {
        Artifact buildArtifact = artifacts.get(Notation.generate(artifact));
        if (buildArtifact != null) {
            return buildArtifact.getPath().toFile();
        } else {
            return null;
        }
    }

    public List<String> findVersions(Artifact artifact) {
        return Collections.emptyList();
    }
}
