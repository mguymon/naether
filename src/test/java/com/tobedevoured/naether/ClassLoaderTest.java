package com.tobedevoured.naether;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.tobedevoured.naether.api.Naether;
import com.tobedevoured.naether.impl.NaetherImpl;
import com.tobedevoured.naether.maven.ProjectException;

public class ClassLoaderTest {

	private PathClassLoader classLoader;
	
	@Before
	public void setup() {
		classLoader = new PathClassLoader();
	}
	
	@Test
	public void defaultConstructor() {
		assertNotNull( new PathClassLoader() );
	}
	
	@Test
	public void addPath() throws MalformedURLException {
		assertTrue( classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" ) );
		assertFalse( classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" ) );
	}
	
	@Test
	public void getLoadedPaths() throws MalformedURLException {
		classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" );
		Set set = new HashSet( Arrays.asList( (new File("src/test/resources/commons-beanutils-1.8.3.jar").toURL() ) ) );
		assertEquals( set,  classLoader.getLoadedPaths() );
	}
	
	@Test(expected=ClassLoaderException.class)
	public void newIntanceNotFound() throws Exception {
		classLoader.newInstance("org.apache.commons.collections.ArrayStack");
	}
	
	@Test
	public void newIntance() throws Exception {
		classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" );
		
		Object instance = classLoader.newInstance("org.apache.commons.collections.ArrayStack");
		assertEquals( "org.apache.commons.collections.ArrayStack", instance.getClass().getName() );
	}
	
	@Test
	public void newInstanceWithParams() throws Exception {
		File file = (File)classLoader.newInstance("java.io.File", "Gemfile" );
		assertEquals( "java.io.File", file.getClass().getName() );
		
		assertEquals( "Gemfile", file.getPath() );
	}
	
	@Test(expected=ClassLoaderException.class)
	public void newInstanceWithParamTypesNotFound() throws Exception {
		classLoader.newInstance("java.io.File", new String[] { "Gemfile" }, new String[] { "bad.String" } );
	}
	
	@Test
	public void newInstanceWithParamTypes() throws Exception {
		File file = (File)classLoader.newInstance("java.io.File", new String[] { "Gemfile" }, new String[] { "java.lang.String" } );
		assertEquals( "java.io.File", file.getClass().getName() );
		
		assertEquals( "Gemfile", file.getPath() );
	}
	
	@Test
	public void execStaticMethod() throws Exception {
		Object result = classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "getTempDirectoryPath" );
		assertNotNull( result );
	}
	
	@Test(expected=ClassLoaderException.class)
	public void execStaticMethodWithClassNotFound() throws Exception {
		classLoader.execStaticMethod("bad.FileUtils", "getTempDirectoryPath" );
	}
	
	@Test
	public void execStaticMethodWithParams() throws Exception {
		File file = new File( "target/classloader.test");
		if ( file.exists() ) {
			file.delete();
		}
		
		Object result = classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "touch", Arrays.asList( file ) );
		assertNull( result );
		
		assertTrue( file.exists() );
	}
	
	@Test
	public void execStaticMethodWithParamTypes() throws ClassLoaderException {
		File file = new File( "target/classloader.test");
		if ( file.exists() ) {
			file.delete();
		}
		
		Object result = classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "touch", Arrays.asList( file ), Arrays.asList( "java.io.File" ) );
		assertNull( result );
		
		assertTrue( file.exists() );
	}
	
	@Test(expected=ClassLoaderException.class)
	public void execStaticMethodWithParamTypesWithClassNotFound() throws Exception {
		classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "touch", Arrays.asList( "file" ), Arrays.asList( "bad class" ) );
	}
}
