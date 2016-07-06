package com.tobedevoured.naether;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.tobedevoured.naether.deploy.DeployException;
import com.tobedevoured.naether.deploy.InstallException;
import com.tobedevoured.naether.maven.ProjectException;

public class ExceptionsTest {

    private RuntimeException runtimeException = new RuntimeException("test");
    
    @Test
    public void classLoaderException() throws Exception {
        checkThrowable( ClassLoaderException.class );
    }
    
    @Test
    public void dependencyException() throws Exception {
        checkThrowable( DependencyException.class );
    }
    
    @Test
    public void resolveException() throws Exception {
        checkThrowable( ResolveException.class );
    }
    
    @Test
    public void urlException() throws Exception {
        checkThrowable( URLException.class );
    }
    
    @Test
    public void deployException() throws Exception {
        checkThrowable( DeployException.class );
    }
    
    @Test
    public void installException() throws Exception {
        checkThrowable( InstallException.class );
    }
    
    @Test
    public void projectException() throws Exception {
        checkThrowable( ProjectException.class );
    }
    
    private void checkThrowable( Class<? extends Throwable> throwableClass ) throws Exception {
        Constructor<? extends Throwable> constructor;
        Throwable throwable;
        
        try {
            constructor = throwableClass.getConstructor(String.class);
            throwable = constructor.newInstance("test");
            assertEquals( "test", throwable.getMessage() );
            assertEquals( null, throwable.getCause() );
        } catch ( NoSuchMethodException exception ) {
            // Not all Exceptions have a String constructor
        }
        
        constructor = throwableClass.getConstructor(String.class, Throwable.class);
        throwable = constructor.newInstance("test", runtimeException);
        assertEquals( "test", throwable.getMessage() );
        assertEquals( runtimeException, throwable.getCause() );
        
        constructor = throwableClass.getConstructor(Throwable.class);
        throwable = constructor.newInstance(runtimeException);
        assertEquals( "java.lang.RuntimeException: test", throwable.getMessage() );
        assertEquals( runtimeException, throwable.getCause() );
    }
}
