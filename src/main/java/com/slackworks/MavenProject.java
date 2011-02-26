package com.slackworks;

// Java SE
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Apache Maven
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

// Codehause Plexus
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maven Project Model
 * 
 * @author Michael
 *
 */
public class MavenProject {

	private static Logger log = LoggerFactory.getLogger(MavenProject.class);
	
	private Model mavenModel;
	private Pattern propertyPattern = Pattern.compile("^\\$\\{(.+)\\}$");
	
	private boolean allowCompileScope = true;
	private boolean allowRuntimeScope = true;
	private boolean allowTestScope = false;
	private boolean allowSystemScope = true;
	private boolean allowProvidedScope = false;
	
	/**
	 * New Instance
	 */
	public MavenProject() {
		
	}
	
	/**
	 * New Instance loading Maven pom
	 * 
	 * @param pomPath String path
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public MavenProject( String pomPath ) throws FileNotFoundException, IOException, XmlPullParserException {
		loadPOM( pomPath );
	}
	
	/**
	 * Load Maven pom
	 * 
	 * @param pomPath String path
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public void loadPOM( String pomPath ) throws FileNotFoundException, IOException, XmlPullParserException {
		log.debug( "Loading pom {}", pomPath );
		MavenXpp3Reader reader = new MavenXpp3Reader();
		setMavenModel(reader.read( new BufferedReader( new FileReader( new File( pomPath ) ) ) ));
	}
	
	/**
	 * Get version of Maven project
	 * 
	 * @return String
	 */
	public String getVersion() {
		return getMavenModel().getVersion();
	}
	
	/**
	 * Get List of {@link Dependency} for Maven Project
	 * 
	 * @return List<Dependency>
	 */
	public List<Dependency> getDependencies() {
		return getDependencies(true);
	}
	
	/**
	 * Get List of {@link Depedencies} for the Maven Project, with boolean
	 * to substitute Project Properties.
	 * 
	 * @param substituteProperties boolean
	 * @return List<Dependency>
	 */
	public List<Dependency> getDependencies( boolean substituteProperties ) {
		List<Dependency> dependencies = new ArrayList<Dependency>();
		
		List<String> scopes = this.allowedScopes();
		
		log.info( "Allowed Maven Scopes: {}", scopes );
		
		// Substitute Properties
		if ( substituteProperties ) {
			// XXX: There has to be a way maven handles this automatically
			for( Dependency dependency: getMavenModel().getDependencies() ) {
				
				// Check that scope of the Dependency has been marked allowed
				if ( scopes.indexOf( dependency.getScope() ) >= 0 ) {
			
					String artifactId = substituteProperty( dependency.getArtifactId() );
					String groupId = substituteProperty( dependency.getGroupId() );
					String version = substituteProperty( dependency.getVersion() );
					
					dependency.setArtifactId( artifactId );
					dependency.setGroupId( groupId );
					dependency.setVersion( version );
					dependencies.add( dependency );
				}
			}
			
		// Keep vals
		} else {
			for( Dependency dependency: getMavenModel().getDependencies() ) {
				// Check that scope of the Dependency has been marked allowed
				if ( scopes.indexOf( dependency.getScope() ) >= 0 ) {
					dependencies.add( dependency );
				}
			};
		}
		
		return dependencies;
	}
	
	/**
	 * Return {@link List<String>} of dependencies in the format of
	 * {@code groupId:artifactId:packageType:version}
	 * 
	 * @return List<String>
	 */
	public List<String> getDependenciesNotation() {
		return getDependenciesNotation(true);
	}
	
	/**
	 * Get List<String> of dependencies in format of {@link groupId:artifactId:packageType:version}
	 * 
	 * @param substituteProperties boolean
	 * @return List<String>
	 */
	public List<String> getDependenciesNotation(boolean substituteProperties) {
		List<String> notations = new ArrayList<String>();
		
		for ( Dependency dependency: getDependencies() ) {
			StringBuffer notation = new StringBuffer()
				.append( dependency.getGroupId() )
				.append( ":" )
				.append( dependency.getArtifactId() )
				.append( ":" )
				.append( dependency.getType() )
				.append( ":" )
				.append( dependency.getVersion() );
			
			notations.add( notation.toString() );
		}
		
		return notations;
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
	
	/**
	 * Substitute a Maven Property expression, i.e. ${aetherVersion}, to its
	 * corresponding Maven pom definition, i.e. 1.11 from {@code <aetherVersion>1.11</aetherVersion>}
	 * 
	 * @param field
	 * @return
	 */
	private String substituteProperty( String field ) {
		String property = null;
		Matcher matcher = propertyPattern.matcher( field );
		while (matcher.find()) {
			property = matcher.group(1);
		}
		
		if ( property != null ) {
			return this.getMavenModel().getProperties().getProperty( property );
		} else {
			return field;
		}
	}
	
	/**
	 * List<String> of scopes allows scopes.
	 * 
	 * Possible scopes: compile, provided, runtime, system, test
	 * 
	 * The compile scope is represented by {@code compile} and {@code null}
	 * 
	 * @return List<String>
	 */
	private List<String> allowedScopes() {
		List<String> scopes = new ArrayList<String>();
		
		if ( this.allowCompileScope ) {
			scopes.add( "compile" );
			scopes.add( null );
		}
		
		if ( this.allowProvidedScope ) {
			scopes.add( "provided" );
		}
		
		if ( this.allowRuntimeScope ) {
			scopes.add( "runtime" );
		}
		
		if ( this.allowSystemScope ) {
			scopes.add( "system" );
		}
		
		if ( this.allowTestScope ) {
			scopes.add( "test" );
		}
		
		return scopes;
	}

	public void setAllowProvidedScope(boolean allowProvidedScope) {
		this.allowProvidedScope = allowProvidedScope;
	}

	public boolean isAllowProvidedScope() {
		return allowProvidedScope;
	}

	private void setAllowSystemScope(boolean allowSystemScope) {
		this.allowSystemScope = allowSystemScope;
	}

	private boolean isAllowSystemScope() {
		return allowSystemScope;
	}

	public void setAllowTestScope(boolean allowTestScope) {
		this.allowTestScope = allowTestScope;
	}

	public boolean isAllowTestScope() {
		return allowTestScope;
	}

	public void setAllowRuntimeScope(boolean allowRuntimeScope) {
		this.allowRuntimeScope = allowRuntimeScope;
	}

	public boolean isAllowRuntimeScope() {
		return allowRuntimeScope;
	}

	public void setAllowCompileScope(boolean allowCompileScope) {
		this.allowCompileScope = allowCompileScope;
	}

	public boolean isAllowCompileScope() {
		return allowCompileScope;
	}
}
