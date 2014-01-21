package com.tobedevoured.naether.util;

import java.util.Map;

public class Env {

    public static String getMavenHome() {

        Map<String, String> env = System.getenv();
        String mavenHome = env.get("MAVEN_HOME");
        if ( mavenHome == null ) {
            mavenHome = System.getProperty("maven.home");
            if (mavenHome == null) {
                // XXX: Default home for Ubuntu maven deb, probably not the best default
                mavenHome = "/usr/share/maven";
            }
        }

        return mavenHome;
    }

    public static String get(String name ){
        Map<String, String> env = System.getenv();
        return env.get(name);
    }
}
