package com.slackworks;

// Java SE
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Apache Maven
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

// Codehause Plexus
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Maven Project Model
 * 
 * @author Michael
 *
 */
public class MavenProject {

	private Model mavenModel;
	private Pattern propertyPattern = Pattern.compile("^\\$\\{(.+)\\}$");
	
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
	
	public List<Dependency> getDependencies( boolean substituteProperties ) {
		List<Dependency> dependencies = null;
		
		if ( substituteProperties ) {
			// XXX: There has to be a way maven handles this automatically
			dependencies = new ArrayList<Dependency>();
			Properties properties = getMavenModel().getProperties();
			for( Dependency dependency: getMavenModel().getDependencies() ) {
				String artifactId = substituteProperty( dependency.getArtifactId() );
				String groupId = substituteProperty( dependency.getGroupId() );
				String version = substituteProperty( dependency.getVersion() );
				
				dependency.setArtifactId( artifactId );
				dependency.setGroupId( groupId );
				dependency.setVersion( version );
				
				dependencies.add( dependency );
			}
		} else {
			dependencies = getMavenModel().getDependencies();
		}
		
		return dependencies;
	}
	
	/**
	 * Return {@link List} of String dependencies in the format of
	 * {@code groupId:artifactId:packageType:version}
	 * 
	 * @return List<String>
	 */
	public List<String> getDependenciesNotation() {
		return getDependenciesNotation(true);
	}
	
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
	 * corresponding Maven pom defination, i.e. {@code <aetherVersion>1.11</aetherVersion>}
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
}
