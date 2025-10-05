package com.tobedevoured.naether;

import static org.junit.Assert.*;

import com.tobedevoured.naether.api.Naether;
import com.tobedevoured.naether.impl.NaetherImpl;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import org.junit.Before;
import org.junit.Test;

public class ClassLoaderTest {

    private PathClassLoader classLoader;

    @Before
    public void setup() {
        classLoader = new PathClassLoader(this.getClass().getClassLoader());
    }

    @Test
    public void defaultConstructor() {
        assertNotNull(new PathClassLoader());
    }

    @Test
    public void addPath() throws MalformedURLException {
        assertTrue(classLoader.addPath("src/test/resources/commons-beanutils-1.8.3.jar"));
        assertFalse(classLoader.addPath("src/test/resources/commons-beanutils-1.8.3.jar"));
    }

    @Test
    public void getLoadedPaths() throws MalformedURLException {
        classLoader.addPath("src/test/resources/commons-beanutils-1.8.3.jar");
        Set set =
                new HashSet(
                        Collections.singletonList(
                                (new File("src/test/resources/commons-beanutils-1.8.3.jar")
                                        .toURL())));
        assertEquals(set, classLoader.getLoadedPaths());
    }

    @Test(expected = ClassLoaderException.class)
    public void newIntanceNotFound() throws Exception {
        classLoader.newInstance("org.apache.commons.collections.ArrayStack");
    }

    @Test
    public void newIntance() throws Exception {
        classLoader.addPath("src/test/resources/commons-beanutils-1.8.3.jar");

        Object instance = classLoader.newInstance("org.apache.commons.collections.ArrayStack");
        assertEquals("org.apache.commons.collections.ArrayStack", instance.getClass().getName());
    }

    @Test
    public void newInstanceWithParams() throws Exception {
        File file = (File) classLoader.newInstance("java.io.File", "Gemfile");
        assertEquals("java.io.File", file.getClass().getName());

        assertEquals("Gemfile", file.getPath());
    }

    @Test(expected = ClassLoaderException.class)
    public void newInstanceWithParamTypesNotFound() throws Exception {
        classLoader.newInstance(
                "java.io.File", new String[] {"Gemfile"}, new String[] {"bad.String"});
    }

    @Test
    public void newInstanceWithParamTypes() throws Exception {
        File file =
                (File)
                        classLoader.newInstance(
                                "java.io.File",
                                new String[] {"Gemfile"},
                                new String[] {"java.lang.String"});
        assertEquals("java.io.File", file.getClass().getName());

        assertEquals("Gemfile", file.getPath());
    }

    @Test
    public void createNaether() throws Exception {
        Naether naether = new NaetherImpl();
        naether.setLocalRepoPath("target/test-repo");
        naether.addDependencies("pom.xml");
        naether.resolveDependencies();

        Map<String, String> paths = naether.getDependenciesPath();

        classLoader.addPath("target/classes");
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            classLoader.addPath(entry.getValue());
        }

        Object instance = classLoader.newInstance(NaetherImpl.class.getName());
        assertEquals(NaetherImpl.class.getName(), instance.getClass().getName());
        assertEquals(NaetherImpl.class, instance.getClass());
    }

    @Test
    public void execStaticMethod() throws Exception {
        Object result =
                classLoader.execStaticMethod(
                        "org.apache.commons.io.FileUtils", "getTempDirectoryPath");
        assertNotNull(result);
    }

    @Test(expected = ClassLoaderException.class)
    public void execStaticMethodWithClassNotFound() throws Exception {
        classLoader.execStaticMethod("bad.FileUtils", "getTempDirectoryPath");
    }

    @Test
    public void execStaticMethodWithParams() throws Exception {
        File file = new File("target/classloader.test");
        if (file.exists()) {
            file.delete();
        }

        Object result =
                classLoader.execStaticMethod(
                        "org.apache.commons.io.FileUtils",
                        "touch",
                        Collections.singletonList(file));
        assertNull(result);

        assertTrue(file.exists());
    }

    @Test
    public void execStaticMethodWithParamTypes() throws ClassLoaderException {
        File file = new File("target/classloader.test");
        if (file.exists()) {
            file.delete();
        }

        Object result =
                classLoader.execStaticMethod(
                        "org.apache.commons.io.FileUtils",
                        "touch",
                        Collections.singletonList(file),
                        Collections.singletonList("java.io.File"));
        assertNull(result);

        assertTrue(file.exists());
    }

    @Test(expected = ClassLoaderException.class)
    public void execStaticMethodWithParamTypesWithClassNotFound() throws Exception {
        classLoader.execStaticMethod(
                "org.apache.commons.io.FileUtils",
                "touch",
                Collections.singletonList("file"),
                Collections.singletonList("bad class"));
    }
}
