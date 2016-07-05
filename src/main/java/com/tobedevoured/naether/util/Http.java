package com.tobedevoured.naether.util;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Http {
    private static Logger log = LoggerFactory.getLogger(Http.class);

    public static File fetch(String url, File destination) throws IOException {
        log.debug("Fetching {}", url);
        FileUtils.copyURLToFile(new URL(url), destination);

        return destination;
    }

    public static boolean exists(String url){
        try {
            log.debug("Checking {}", url);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection connection =
                (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            log.debug("Did not find {}", url, e);
            return false;
        }
    }
}
