package com.tobedevoured.naether.util;

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

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * Helper for changing the logging levels at runtime.
 * 
 * @author Michael Guymon
 */
public final class LogUtil {

	private LogUtil() { }
	
	/**
	 * Change log {@link Level} to only output error logging
	 * 
	 * @return {@link Level}
	 */
	public static Level errorOnlyLogging() {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	 	Level originalLevel = currentRootLevel();
	 	root.setLevel(Level.ERROR);
	 	
	 	return originalLevel;
	}
	
	/**
	 * Change the log {@link Level} for root logger.
	 * 
	 * @param level {@link Level}
	 */
	public static void changeRootLevel( Level level ) {
		changeLevel(Logger.ROOT_LOGGER_NAME, level );
	}
	
	/**
	 * Change the log {@link Level} for a logger
	 * 
	 * @param logger String 
	 * @param level {@link Level}
	 */
	public static void changeLevel( String logger, Level level ) {
		Logger log = (Logger)LoggerFactory.getLogger(logger);
	 	log.setLevel( level );
	}
	
	/**
	 * Change the log {@link Level} for a logger
	 * 
	 * @param logger String 
	 * @param level {@link String}
	 */
	public static void changeLevel( String logger, String level ) {
		Level logLevel = null;
		
		if ( "debug".equalsIgnoreCase( level ) ) {
			logLevel = Level.DEBUG;
		} else if ( "warn".equalsIgnoreCase( level ) ) {
			logLevel = Level.WARN;
		} else if ( "error".equalsIgnoreCase( level ) ) {
			logLevel = Level.ERROR;
		} else if ( "trace".equalsIgnoreCase( level ) ) {
			logLevel = Level.TRACE;
		} else {
			logLevel = Level.INFO;
		}
		
		Logger log = (Logger)LoggerFactory.getLogger(logger);
	 	log.setLevel( logLevel );
	}
	
	public static Level getLogLevel( String logger ) {
		Logger log = (Logger)LoggerFactory.getLogger(logger);
	 	return log.getLevel();
	}
	
	/**
	 * Get the current {@link Level} for the root logger
	 * @return {@link Level}
	 */
	public static Level currentRootLevel() {
		Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		return root.getLevel();
	}
}
