package com.tobedevoured.naether.util;

import java.io.File;
import java.util.Map;

public class Env {

    public static String getMavenHome() {

        String mavenHome = get("MAVEN_HOME");
        if ( mavenHome == null ) {
            mavenHome = System.getProperty("maven.home");
            if (mavenHome == null) {
                // XXX: Default home for Ubuntu maven deb, probably not the best default
                mavenHome = "/usr/share/maven";
            }
        }

        return mavenHome;
    }

    public static String getLocalRepository() {
        String m2Repo = get("M2_REPO");
        if (m2Repo == null) {
            return new StringBuilder()
                .append(System.getProperty("user.home"))
                .append(File.separator)
                .append(".m2")
                .append(File.separator)
                .append("repository")
                .toString();
        } else {
            return new File(m2Repo).getAbsolutePath();
        }
    }

    public static String get(String name ){
        Map<String, String> env = System.getenv();
        return env.get(name);
    }
}
