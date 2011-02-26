package com.slackworks;

import org.sonatype.aether.artifact.Artifact;

public class Notation {
	public static String generate( org.apache.maven.model.Dependency dependency ) {
		StringBuffer notation = new StringBuffer()
			.append( dependency.getGroupId() )
			.append( ":" )
			.append( dependency.getArtifactId() )
			.append( ":" )
			.append( dependency.getType() )
			.append( ":" )
			.append( dependency.getVersion() );
		
		return notation.toString();
	}
	
	public static String generate( org.sonatype.aether.graph.Dependency dependency ) {
		Artifact artifact = dependency.getArtifact();
		StringBuffer notation = new StringBuffer()
			.append( artifact.getGroupId() )
			.append( ":" )
			.append( artifact.getArtifactId() )
			.append( ":" )
			.append( artifact.getExtension() )
			.append( ":" )
			.append( artifact.getVersion() );
		
		return notation.toString();
	}
}
