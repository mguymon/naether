package com.tobedevoured.naether.util;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;

public class LogUtilTest {

	private Level defaultRootLevel;
	
	@Before
	public void setUp() {
		defaultRootLevel = LogUtil.currentRootLevel();
	}
	
	@After
	public void tearDown() {
		LogUtil.changeRootLevel( defaultRootLevel );
	}
	
	@Test
	public void changeRootLevel() {
		LogUtil.changeRootLevel(Level.WARN);
		assertEquals( Level.WARN, LogUtil.currentRootLevel() );
	}
	
	@Test
	public void errorOnlyLogging() {
		assertEquals( Level.INFO, LogUtil.currentRootLevel() );
		LogUtil.errorOnlyLogging();
		assertEquals( Level.ERROR, LogUtil.currentRootLevel() );
	}
	
	@Test
	public void changeLevel() {
		LogUtil.changeLevel("test logger", Level.TRACE );
		assertEquals( Level.TRACE, LogUtil.getLogLevel("test logger") );
	}
	
	@Test
	public void changeLevelByString() {
		LogUtil.changeLevel("test logger", "TRACE" );
		assertEquals( Level.TRACE, LogUtil.getLogLevel("test logger") );
		
		LogUtil.changeLevel("test logger", "debug" );
		assertEquals( Level.DEBUG, LogUtil.getLogLevel("test logger") );
		
		LogUtil.changeLevel("test logger", "wARN" );
		assertEquals( Level.WARN, LogUtil.getLogLevel("test logger") );
		
		LogUtil.changeLevel("test logger", "error" );
		assertEquals( Level.ERROR, LogUtil.getLogLevel("test logger") );
		
		LogUtil.changeLevel("test logger", "invalid" );
		assertEquals( Level.INFO, LogUtil.getLogLevel("test logger") );
	}
}
