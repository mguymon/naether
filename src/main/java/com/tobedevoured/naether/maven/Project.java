package com.tobedevoured.naether.maven;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Java SE
import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Apache Maven
import com.tobedevoured.naether.impl.NaetherImpl;
import com.tobedevoured.naether.repo.RepositoryClient;
import com.tobedevoured.naether.util.Env;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

// Codehause Plexus
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

// SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

import com.tobedevoured.naether.util.Notation;
import com.tobedevoured.naether.util.RepoBuilder;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Helper for working with Maven's Project Model
 *
 * @author Michael Guymon
 *
 */
public class Project {

    private static Logger log = LoggerFactory.getLogger(Project.class);
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+)\\}");

    private Model mavenModel;
    private String basePath;

    /**
     * Create an empty instance. Local Repository and Remote Repositories will not be set.
     * The {@link #getBasePath()} defaults to current working directory.
     */
    public Project() {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setPackaging("jar");
        this.mavenModel = model;
        setBasePath( new File("." ) );
    }

    /**
     * Create an instance based on an existing pom xml. The default Local Repository and Remote Repositories
     * will be used. The {@link #getBasePath()} defaults to the pom's parent directory.
     *
     * @param pomPath String file path
     * @throws ProjectException exception
     */
    public Project(String pomPath) throws ProjectException {
         this(pomPath, null, null);
    }

    /**
     *  Create an instance based on an existing pom xml. The defaults will be used if localRepo or remoteRepo params
     *  are null. The {@link #getBasePath()} defaults to the pom's parent directory.
     *
     * @param pomPath String file path
     * @param localRepo String directory path
     * @param remoteRepos {@link Collection}
     * @throws ProjectException exception
     */
    public Project(String pomPath, String localRepo, Collection<RemoteRepository> remoteRepos) throws ProjectException {

        if (localRepo == null) {
            localRepo = Env.getLocalRepository();
        }

        if (remoteRepos == null) {
            remoteRepos = Arrays.asList(NaetherImpl.DEFAULT_REPOSITORY);
        }

        this.mavenModel = loadPOM(pomPath, localRepo, remoteRepos);
        this.mavenModel.setPomFile( new File(pomPath) );

        File parent = (new File(pomPath)).getParentFile();
        if ( parent != null ) {
            setBasePath( (new File(pomPath)).getParentFile() );
        } else {
            setBasePath( new File("." ) );
        }
    }

    /**
     * Load Maven pom
     *
     * @param pomPath String path
     * @param localRepo String
     * @param repositories {@link Collection}
     * @return {@link Model}
     * @throws ProjectException if fails to open, read, or parse the POM
     */
    public static Model loadPOM(String pomPath, String localRepo, Collection<RemoteRepository> repositories) throws ProjectException {
        RepositoryClient repoClient = new RepositoryClient(localRepo);
        NaetherModelResolver resolver = new NaetherModelResolver(repoClient, repositories);

        ModelBuildingRequest req = new DefaultModelBuildingRequest();
        req.setProcessPlugins( false );
        req.setPomFile( new File(pomPath) );
        req.setModelResolver( resolver );
        req.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );

        DefaultModelBuilder builder = (new DefaultModelBuilderFactory()).newInstance();
        try {
            return builder.build( req ).getEffectiveModel();
        } catch ( ModelBuildingException e ) {
            throw new ProjectException("Failed to build project from pom", e);
        }
    }

    /**
     * Get version of the Project. If null, checks the Parent Project's version.
     *
     * @return String
     */
    public String getVersion() {
        String version = getMavenModel().getVersion();
        if ( version == null && getMavenModel().getParent() != null ) {
            version = getMavenModel().getParent().getVersion();
        }

        return version;
    }

    /**
     * Set the Version of the Project
     *
     * @param version String
     */
    public void setVersion(String version) {
        getMavenModel().setVersion(version);
    }

    public String getArtifactId() {
        return getMavenModel().getArtifactId();
    }

    public void setArtifactId(String artifactId) {
        getMavenModel().setArtifactId(artifactId);
    }

    public String getGroupId() {
        return getMavenModel().getGroupId();
    }

    public void setGroupId(String groupId) {
        getMavenModel().setGroupId(groupId);
    }

    public String getType() {
        return getMavenModel().getPackaging();
    }

    public void setType(String type) {
        getMavenModel().setPackaging(type);
    }


    /**
     * Add a Repository to the Project
     *
     * @param url String
     * @throws ProjectException exception
     */
    public void addRepository(String url) throws ProjectException {
        List<Repository> repositories = getRepositories();

        if ( repositories == null ) {
            repositories = new ArrayList<Repository>();
        }

        try {
            Repository repository = RepoBuilder.repositoryFromUrl( url );
            repositories.add( repository );
        } catch (MalformedURLException e) {
            throw new ProjectException( e );
        }

        getMavenModel().setRepositories( repositories );
    }

    /**
     * Set Repositories
     *
     * @param urls List
     * @throws ProjectException exception
     */
    public void setRepositories( List<String> urls ) throws ProjectException {
        List<Repository> repositories = new ArrayList<Repository>();
        for ( String url : urls  ) {
            try {
                Repository repository = RepoBuilder.repositoryFromUrl( url );
                repositories.add( repository );
            } catch (MalformedURLException e) {
                throw new ProjectException( e );
            }
        }
        getMavenModel().setRepositories( repositories);
    }

    /**
     * Get Repositories
     *
     * @return List
     */
    public List<Repository> getRepositories() {
        return getMavenModel().getRepositories();
    }

    /**
     * Get List of Repositories as String url
     *
     * @return List
     */
    public List<String> getRepositoryUrls() {
        List<String> urls = new ArrayList<String>();

        for ( Repository repo : getRepositories() ) {
            urls.add( repo.getUrl() );
        }

        return urls;
    }

    /**
     * Get the String notation for the Maven Project
     *
     * @return String
     */
    public String getProjectNotation() {
        return Notation.generate( this );
    }

    /**
     * Set Maven Project from String notation
     *
     * @param notation String
     */
    public void setProjectNotation(String notation) {
        Map<String, String> notationMap = Notation.parse(notation);
        this.setGroupId(notationMap.get("groupId"));
        this.setArtifactId(notationMap.get("artifactId"));
        this.setType(notationMap.get("type"));
        this.setVersion(notationMap.get("version"));
    }

    /**
     * Get List of {@link Dependency} for the Maven Project for scopes compile
     * and runtime
     *
     * @return List
     */
    public List<Dependency> getDependencies() {
        return getDependencies(null);
    }

    /**
     * Get List of {@link Dependency} for the Maven Project for specific scopes
     *
     * @param scopes List
     * @return List
     */
    public List<Dependency> getDependencies(List<String> scopes) {
        log.debug( "Valid scopes: {}", scopes );
        List<Dependency> dependencies = new ArrayList<Dependency>();

        for (Dependency dependency : getMavenModel().getDependencies()) {

            // Substitute Properties
            // XXX: There has to be a way maven handles this automatically
            log.debug( "Project properties: {} ", this.mavenModel.getProperties() );
            String artifactId = substituteProperty(dependency.getArtifactId());
            String groupId    = substituteProperty(dependency.getGroupId());
            String version    = substituteProperty(dependency.getVersion());
            String type       = substituteProperty(dependency.getType());
            String classifier = substituteProperty(dependency.getClassifier());
            String systemPath = substituteProperty(dependency.getSystemPath());

            dependency.setSystemPath(systemPath);
            dependency.setArtifactId(artifactId);
            dependency.setGroupId(groupId);
            dependency.setVersion(version);
            dependency.setType(type);
            dependency.setClassifier( classifier );

            dependencies.add(dependency);
        }


        if ( scopes == null ) {
            scopes = Arrays.asList( "compile", "runtime" );
        }

        for ( Iterator<Dependency> iterator = dependencies.iterator(); iterator.hasNext(); ) {
            Dependency dependency = iterator.next();

            String scope = dependency.getScope();

            // Default scope for dependencies is compile
            if ( scope == null ) {
                scope = "compile";
            }

            if ( !scopes.contains( scope ) ) {
                log.debug( "Removing {} with scope {}", dependency, dependency.getScope() );
                iterator.remove();
            }
        }

        return dependencies;
    }

    /**
     * Return {@link List} of dependencies in the format of {@code groupId:artifactId:packageType:version}
     *
     * @return List
     */
    public List<String> getDependenciesNotation() {
        return getDependenciesNotation(null);
    }

    /**
     * Get List of dependencies in format of groupId:artifactId:packageType:version
     *
     * @param scopes List
     * @return List
     */
    public List<String> getDependenciesNotation(List<String> scopes) {
        List<String> notations = new ArrayList<String>();

        for (Dependency dependency : getDependencies(scopes)) {
            notations.add(Notation.generate(dependency));
        }

        return notations;
    }

    /**
     * Add a {@link Dependency}
     *
     * @param dependency {@link Dependency}
     */
    public void addDependency(Dependency dependency) {
        mavenModel.addDependency(dependency);
    }

    /**
     * Add a Dependency by String notation with default scope of compile
     *
     * @param notation String
     */
    public void addDependency(String notation ) {
        addDependency( notation, "compile" );
    }

    /**
     * Add a Dependency by String notation
     *
     * @param notation String
     * @param scope String
     */
    public void addDependency(String notation, String scope ) {
        Map<String, String> notationMap = Notation.parse(notation);
        Dependency dependency = new Dependency();
        dependency.setGroupId(notationMap.get("groupId"));
        dependency.setArtifactId(notationMap.get("artifactId"));
        dependency.setType(notationMap.get("type"));
        dependency.setVersion(notationMap.get("version"));
        dependency.setScope( scope );
        addDependency(dependency);
    }

    /**
     * Add a Dependency of {@link org.sonatype.aether.graph.Dependency}
     * @param aetherDep {@link org.sonatype.aether.graph.Dependency}
     */
    public void addDependency(org.sonatype.aether.graph.Dependency aetherDep) {
        Artifact artifact = aetherDep.getArtifact();

        Dependency dependency = new Dependency();
        dependency.setGroupId( artifact.getGroupId() );
        dependency.setArtifactId( artifact.getArtifactId() );
        dependency.setType( artifact.getExtension() );
        dependency.setVersion( artifact.getVersion() );
        dependency.setScope( aetherDep.getScope() );
        addDependency( dependency );
    }

    /**
     * Set Dependencies
     *
     * @param dependencies Collection
     */
    public void setDependencies( Collection<org.sonatype.aether.graph.Dependency> dependencies ) {
        for ( org.sonatype.aether.graph.Dependency dep : dependencies ) {
            addDependency( dep );
        }
    }

    /**
     * Set the Maven {@link Model}
     *
     * @param mavenModel {@link Model}
     */
    public void setMavenModel(Model mavenModel) {
        this.mavenModel = mavenModel;
    }

    /**
     * Get the Maven {@link Model}
     *
     * @return {@link Model}
     */
    public Model getMavenModel() {
        return mavenModel;
    }

    public void addProperty(String property, String value) {
        this.mavenModel.getProperties().put( property, value );
    }

    public void removeProperty(String property) {
        this.mavenModel.getProperties().remove( property );
    }

    /**
     * Substitute a Maven Property expression, i.e. ${aetherVersion}, to its
     * corresponding Maven pom definition, i.e. 1.11 from
     * {@code <properties><aetherVersion>1.11</aetherVersion></properties>}
     *
     * @param field
     * @return
     */
    private String substituteProperty(String field) {
        if ( field != null ) {
            String property = null;
            Matcher matcher = PROPERTY_PATTERN.matcher(field);
            while (matcher.find()) {
                property = matcher.group(1);
            }

            if (property != null) {
                log.debug("Set property {} to {}", property, this.getMavenModel().getProperties().getProperty(property));
                return this.getMavenModel().getProperties().getProperty(property);
            } else {
                return field;
            }
        }

        return null;
    }

    /**
     * Get Pom {@link File}
     * @return {@link File}
     */
    public File getPomFile() {
        return this.mavenModel.getPomFile();
    }

    /**
     * Write pom to path
     *
     * @param filePath String
     * @throws ProjectException exception
     */
    public void writePom(String filePath) throws ProjectException {
        writePom( new File( filePath ) );
    }

    /**
     * Write pom to {@link File}
     *
     * @param file {@link File}
     * @throws ProjectException exception
     */
    public void writePom(File file) throws ProjectException {
        log.debug("Writing pom: {}", file.getPath());

        Project copy = this;
        copy.removeProperty( "project.basedir" );

        Writer writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new ProjectException(e);
        }

        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        try {
            pomWriter.write(writer, copy.mavenModel);
        } catch (IOException e) {
            throw new ProjectException("Failed to write pom", e);
        }
    }

    /**
     * Convert Project to POM XML
     *
     * @return String
     * @throws ProjectException exception
     */
    public String toXml() throws ProjectException {
        log.debug("Writing xml");

        Project copy = this;
        copy.removeProperty( "project.basedir" );

        StringWriter writer = new StringWriter();

        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        try {
            pomWriter.write(writer, copy.mavenModel);
        } catch (IOException e) {
            throw new ProjectException("Failed to create pom xml", e);
        }

        writer.flush();
        return writer.toString();
    }

    /**
     * Get the base path of the Project
     *
     * @return String
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Set the BasePath for the POM. Path is converted to absolute path.
     *
     * @param basePath File
     */
    public final void setBasePath(File basePath) {
        this.basePath = basePath.getAbsolutePath();
        this.addProperty( "project.basedir", this.basePath);
    }

    /**
     * Get the final name of the build artifact for this project
     * @return String
     */
    public String getFinalName() {
        if ( this.mavenModel.getBuild() != null ) {
            return this.mavenModel.getBuild().getFinalName();
        } else {
            return null;
        }
    }
}
