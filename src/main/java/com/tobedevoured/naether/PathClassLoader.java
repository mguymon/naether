package com.tobedevoured.naether;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;

/**
 * 
 * @author Michael Guymon
 *
 */
public class PathClassLoader extends URLClassLoader {

	private Set<URL> loadedPaths = new HashSet<URL>();
	
	public PathClassLoader() {
		super(new URL[] {});
	}
	
	public PathClassLoader(ClassLoader classLoader) {
		super( new URL[] {}, classLoader );
	}
	
	public PathClassLoader(URL[] urls, ClassLoader classLoader ) {
		super( urls, classLoader );
		loadedPaths.addAll( Arrays.asList( urls ) );
	}
	
	public boolean addPath(String path) throws MalformedURLException {
		File file = new File(path);
		URL url = file.toURI().toURL();
		if ( !loadedPaths.contains( url ) ) {
			super.addURL( url );
			this.loadedPaths.add( url );
			return true;
		} else {
			return false;
		}
	}
	
	public Set<URL> getLoadedPaths() {
		return loadedPaths;
	}
	
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return super.findClass( name );
	}
	
	public Object newInstance( String name ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		return newInstance( name, null );
	}
	
	public Object newInstance( String name, Object... params ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<?> clazz = loadClass(name);
		
		List<Class> types = null;
		
		if ( params != null ) {
			types = new ArrayList<Class>();
			for ( Object param: params ) {
				types.add( param.getClass() );
			}
		}
		
		if ( params != null ) {
			Constructor<?> constructor = clazz.getConstructor( types.toArray( new Class[types.size()] ) );
			return constructor.newInstance(params);
		} else {
			Constructor<?> constructor = clazz.getConstructor();
			return constructor.newInstance();
		}
	}
	
	public Object execStaticMethod( String className, String methodName ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return execStaticMethod( className, methodName, null );
	}
	
	public Object execStaticMethod( String className, String methodName, List params ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return execStaticMethod( className, methodName, params, null );
	}
	
	public Object execStaticMethod( String className, String methodName, List params, List<String> paramTypes ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Class<?> clazz = loadClass( className );
		
		List<Class> types = null;
		
		if ( params != null ) {
			types = new ArrayList<Class>();
			if (paramTypes == null) {
				for ( Object param: params ) {
					types.add( param.getClass() );
				}
			} else {
				for ( String type : paramTypes ) {
					types.add( loadClass( type ) );
				}
			}
		}
		
		if ( params != null ) {
			Method method = clazz.getDeclaredMethod(methodName, types.toArray(new Class[types.size()]));
			return method.invoke( clazz, params.toArray( new Object[params.size()] ) );
		} else {
			Method method = clazz.getDeclaredMethod(methodName);
			return method.invoke( clazz );
		}
	}
}
