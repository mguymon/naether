package com.tobedevoured.naether;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.tobedevoured.naether.maven.ProjectException;

public class ClassLoaderTest {

	private PathClassLoader classLoader;
	
	@Before
	public void setup() {
		classLoader = new PathClassLoader( this.getClass().getClassLoader() );
	}
	
	@Test
	public void addPath() throws MalformedURLException {
		assertTrue( classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" ) );
		assertFalse( classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" ) );
	}
	
	@Test
	public void newIntance() throws MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		classLoader.addPath( "src/test/resources/commons-beanutils-1.8.3.jar" );
		
		Object instance = classLoader.newInstance("org.apache.commons.collections.ArrayStack");
		assertEquals( "org.apache.commons.collections.ArrayStack", instance.getClass().getName() );
	}
	
	@Test
	public void newInstanceWithParams() throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		File file = (File)classLoader.newInstance("java.io.File", "Gemfile" );
		assertEquals( "java.io.File", file.getClass().getName() );
		
		assertEquals( "Gemfile", file.getPath() );
	}
	
	@Test
	public void createNaether() throws ProjectException, URLException, DependencyException, MalformedURLException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Naether naether = new Naether();
		naether.setLocalRepoPath( "target/test-repo" );
		naether.addDependencies( "pom.xml" );
		naether.resolveDependencies();
		
		Map<String,String> paths = naether.getDependenciesPath();
		
		classLoader.addPath( "target/classes" );
		for (Map.Entry<String, String> entry : paths.entrySet() ) {
			classLoader.addPath( entry.getValue() );
		}
		
		Object instance = classLoader.newInstance( Naether.class.getName() );
		assertEquals( Naether.class.getName(), instance.getClass().getName() );
		assertTrue( Naether.class.equals( instance.getClass() ) );
	}
	
	@Test
	public void execStaticMethod() throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Object result = classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "getTempDirectoryPath" );
		assertNotNull( result );
	}
	
	@Test
	public void execStaticMethodWithParams() throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		File file = new File( "target/classloader.test");
		if ( file.exists() ) {
			file.delete();
		}
		
		Object result = classLoader.execStaticMethod("org.apache.commons.io.FileUtils", "touch", Arrays.asList( file ) );
		assertNull( result );
		
		assertTrue( file.exists() );
	}
}
