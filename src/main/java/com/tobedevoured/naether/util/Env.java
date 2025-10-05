package com.tobedevoured.naether.util;

import java.io.File;

public class Env {
    public static String getMavenHome() {
        String mavenHome = get("MAVEN_HOME");
        if (mavenHome == null) {
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
            return System.getProperty("user.home")
                    + File.separator
                    + ".m2"
                    + File.separator
                    + "repository";
        } else {
            return new File(m2Repo).getAbsolutePath();
        }
    }

    public static String get(String name) {
        return System.getenv(name);
    }
}
