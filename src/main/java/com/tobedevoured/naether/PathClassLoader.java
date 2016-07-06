package com.tobedevoured.naether;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * ClassLoader that allows paths to be added at runtime.
 * 
 * @author Michael Guymon
 *
 */
public class PathClassLoader extends URLClassLoader {

    private Set<URL> loadedPaths = new HashSet<URL>();
    
    /**
     * Create new instance using default parent ClassLoader
     */
    public PathClassLoader() {
        super(new URL[] {});
    }
    
    /**
     * Create new instance for parent classLoader
     * @param classLoader ClassLoader
     */
    public PathClassLoader(ClassLoader classLoader) {
        super( new URL[] {}, classLoader );
    }
    
    /**
     * Add a new path to the ClassLoader, such as a Jar or directory of compiled
     * classes.
     * 
     * @param path String
     * @return boolean if path is loaded.
     * @throws MalformedURLException exception
     */
    public boolean addPath(String path) throws MalformedURLException {
        File file = new File(path);
        return addPath( file );
    }
    
    /**
     * Add a new path to the ClassLoader, such as a Jar or directory of compiled
     * classes.
     * 
     * @param file {@link File}
     * @return boolean if file is loaded.
     * @throws MalformedURLException exception
     */
    public boolean addPath(File file) throws MalformedURLException {
        URL url = file.toURI().toURL();
        if ( !loadedPaths.contains( url ) ) {
            super.addURL( url );
            this.loadedPaths.add( url );
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Get paths loaded by ClassLoader
     * 
     * @return Set
     */
    public Set<URL> getLoadedPaths() {
        return loadedPaths;
    }
    
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass( name );
    }
    
    
    /**
     * Create new instance of Object using the ClassLoader
     * 
     * @param name String fully qualified class name
     * @return Object
     * @throws ClassLoaderException exception
     */
    public Object newInstance( String name ) throws ClassLoaderException {
        return newInstance( name, (Object[])null );
    }
    
    /**
     * Create new instance of Object with constructor parameters using the ClassLoader
     * 
     * @param name String
     * @param params Object parameters for constructor
     * @return Object
     * @throws ClassLoaderException exception
     */
    public Object newInstance( String name, Object... params ) throws ClassLoaderException {
        return newInstance( name, params, null );
    }
    
    /**
     * Create new instance of Object with constructor parameters using the ClassLoader
     * 
     * @param name String
     * @param params Object[] parameters for constructor
     * @param types String[] Class type for each parameter
     * @return Object
     * @throws ClassLoaderException exception
     */
    public Object newInstance( String name, Object[] params, String[] types ) throws ClassLoaderException {
        Class<?> clazz;
        try {
            clazz = loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new ClassLoaderException(e);
        }
        
        List<Class> paramTypes = new ArrayList<Class>();
        
        if ( types != null ) {
            for( String type: types ) {
                try {
                    paramTypes.add( loadClass(type) );
                } catch (ClassNotFoundException e) {
                    throw new ClassLoaderException(e);
                }
            }
        } else if ( params != null ) {
            for ( Object param: params ) {
                paramTypes.add( param.getClass() );
            }
        } 
        
        if ( params != null ) {
            Constructor<?> constructor;
            try {
                constructor = clazz.getConstructor( paramTypes.toArray( new Class[paramTypes.size()] ) );
            } catch (SecurityException e) {
                throw new ClassLoaderException("Private constructor for " + clazz.getSimpleName(), e);
            } catch (NoSuchMethodException e) {
                throw new ClassLoaderException("No such constructor for " + clazz.getSimpleName(), e);
            } catch (Exception e) {
                throw new ClassLoaderException("Attempted to access constructor for " + clazz.getSimpleName(), e);
            }
            
            try {
                return constructor.newInstance(params);
            } catch (IllegalArgumentException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (InstantiationException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (IllegalAccessException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (InvocationTargetException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (Exception e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            }

        } else {
            Constructor<?> constructor;
            try {
                constructor = clazz.getConstructor();
            } catch (SecurityException e) {
                throw new ClassLoaderException("Private constructor for " + clazz.getSimpleName(),e);
            } catch (NoSuchMethodException e) {
                throw new ClassLoaderException("No such constructor for " + clazz.getSimpleName(),e);
            } catch (Exception e) {
                throw new ClassLoaderException("Attempted to access constructor for " + clazz.getSimpleName(), e);
            }
            
            try {
                return constructor.newInstance();
            } catch (IllegalArgumentException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (InstantiationException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (IllegalAccessException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (InvocationTargetException e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            } catch (Exception e) {
                throw new ClassLoaderException("Attempted to created new instance of " + clazz.getSimpleName(),e);
            }
        }
    }
    

    /**
     * Helper for executing static methods on a Class
     * 
     * @param className String fully qualified class
     * @param methodName String method name
     * @return Object result
     * @throws ClassLoaderException exception
     */
    public Object execStaticMethod( String className, String methodName ) throws ClassLoaderException {
        return execStaticMethod( className, methodName, null );
    }
    
    /**
     * Helper for executing static methods on a Class
     * 
     * @param className String fully qualified class
     * @param methodName String method name
     * @param params List of method parameters
     * @return Object result
     * @throws ClassLoaderException exception
     */
    public Object execStaticMethod( String className, String methodName, List params ) throws ClassLoaderException {
        return execStaticMethod( className, methodName, params, null );
    }
    
    /**
     * Helper for executing static methods on a Class
     * 
     * @param className String fully qualified class
     * @param methodName String method name
     * @param params List of method parameters
     * @param paramTypes List of fully qualified class for each params.
     * @return Object result
     * @throws ClassLoaderException exception
     */
    public Object execStaticMethod( String className, String methodName, List params, List<String> paramTypes ) throws ClassLoaderException {
        Class<?> clazz;
        try {
            clazz = loadClass( className );
        } catch (ClassNotFoundException e) {
            throw new ClassLoaderException( e );
        }
        
        List<Class> types = null;
        
        if ( params != null ) {
            types = new ArrayList<Class>();
            if (paramTypes == null) {
                for ( Object param: params ) {
                    types.add( param.getClass() );
                }
            } else {
                for ( String type : paramTypes ) {
                    try {
                        types.add( loadClass( type ) );
                    } catch (ClassNotFoundException e) {
                        throw new ClassLoaderException( e );
                    }
                }
            }
        }
        
        if ( params != null ) {
            Method method;
            try {
                method = clazz.getDeclaredMethod(methodName, types.toArray(new Class[types.size()]));
            } catch (SecurityException e) {
                throw new ClassLoaderException( e );
            } catch (NoSuchMethodException e) {
                throw new ClassLoaderException( e );
            }
            
            try {
                return method.invoke( clazz, params.toArray( new Object[params.size()] ) );
            } catch (IllegalArgumentException e) {
                throw new ClassLoaderException( e );
            } catch (IllegalAccessException e) {
                throw new ClassLoaderException( e );
            } catch (InvocationTargetException e) {
                throw new ClassLoaderException( e );
            }
        } else {
            
            Method method;
            try {
                method = clazz.getDeclaredMethod(methodName);
            } catch (SecurityException e) {
                throw new ClassLoaderException( e );
            } catch (NoSuchMethodException e) {
                throw new ClassLoaderException( e );
            }
            
            try {
                return method.invoke( clazz );
            } catch (IllegalArgumentException e) {
                throw new ClassLoaderException( e );
            } catch (IllegalAccessException e) {
                throw new ClassLoaderException( e );
            } catch (InvocationTargetException e) {
                throw new ClassLoaderException( e );
            }
        }
    }
}
