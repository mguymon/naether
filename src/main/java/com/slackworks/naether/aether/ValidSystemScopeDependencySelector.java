package com.slackworks.naether.aether;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;

public class ValidSystemScopeDependencySelector implements DependencySelector {

	private static Logger log = LoggerFactory.getLogger(ValidSystemScopeDependencySelector.class);
	private static final String SYSTEM_SCOPE = "system";
	
	public boolean selectDependency(Dependency dependency) {
		if ( SYSTEM_SCOPE.equals( dependency.getScope() ) ) {
			log.debug( "dependency: {}", dependency );
		}
		
		return true;
	}

	public DependencySelector deriveChildSelector( DependencyCollectionContext context) {
		return this;
	}

}
