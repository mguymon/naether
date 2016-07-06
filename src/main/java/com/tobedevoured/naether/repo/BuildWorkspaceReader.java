package com.tobedevoured.naether.repo;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;

import com.tobedevoured.naether.util.Notation;

@Component(role = WorkspaceReader.class, hint = "local-build")
public class BuildWorkspaceReader implements WorkspaceReader {

    private Map<String,Artifact> artifacts = new HashMap<String,Artifact>();
    
    private WorkspaceRepository workspaceRepository;
    
    public BuildWorkspaceReader() {
        this.workspaceRepository = new WorkspaceRepository("local-build", getClass());
    }
    
    public WorkspaceRepository getRepository() {
        return workspaceRepository;
    }
    
    public void addArtifact( Artifact artifact ) {
        artifacts.put( Notation.generate( artifact ), artifact );
    }

    public File findArtifact(Artifact artifact) {
        Artifact buildArtifact = artifacts.get( Notation.generate( artifact ) );
        if ( buildArtifact != null ) {
            return buildArtifact.getFile();
        } else {
            return null;
        }
    }

    public List<String> findVersions(Artifact artifact) {
        return Collections.emptyList();
    }

}
