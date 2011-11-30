package com.slackworks.naether;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class NotationTest {

	@Test
	public void parse() {
		Map<String,String> results = Notation.parse( "testGroup:testArtifact:testType:testVersion" );
		assertEquals( "testGroup", results.get( "groupId" ) );
		assertEquals( "testArtifact", results.get( "artifactId" ) );
		assertEquals( "testType", results.get( "type" ) );
		assertEquals( "testVersion", results.get( "version" ) );
		
		results = Notation.parse( "testGroup:testArtifact:testVersion" );
		assertEquals( "testGroup", results.get( "groupId" ) );
		assertEquals( "testArtifact", results.get( "artifactId" ) );
		assertEquals( null, results.get( "type" ) );
		assertEquals( "testVersion", results.get( "version" ) );
	}
}
