package com.tobedevoured.naether;

public class LogUtil {

    public static void setDefaultLogLevel(String level) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", level);
    }
}
