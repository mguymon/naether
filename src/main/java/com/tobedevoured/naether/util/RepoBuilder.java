package com.tobedevoured.naether.util;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.maven.model.Repository;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Helper for creating repository objects
 *
 * @author Michael Guymon
 */
public final class RepoBuilder {

    private RepoBuilder() {}

    /**
     * Create a {@link RemoteRepository} from a String url
     *
     * @param url String
     * @return {@link RemoteRepository}
     * @throws MalformedURLException exceptions
     */
    public static RemoteRepository remoteRepositoryFromUrl(String url)
            throws MalformedURLException {
        try {
            URI parsedUri = new URI(url);

            String host = parsedUri.getHost();
            StringBuffer id = new StringBuffer(host != null ? host : "local");
            String path = parsedUri.getPath();
            if (path != null && !path.isEmpty()) {
                path = path.replaceFirst("/", "").replaceAll("/", "-").replaceAll(":", "-");
                id.append("-");
                id.append(path);
            }

            if (parsedUri.getPort() > -1) {
                id.append("-");
                id.append(parsedUri.getPort());
            }

            return new RemoteRepository.Builder(id.toString(), "default", url).build();
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Invalid URL: " + url + " - " + e.getMessage());
        }
    }

    /**
     * Create a {@link Repository} from a String url
     *
     * @param url String
     * @return {@link Repository}
     * @throws MalformedURLException exception
     */
    public static Repository repositoryFromUrl(String url) throws MalformedURLException {
        try {
            URI parsedUri = new URI(url);

            String host = parsedUri.getHost();
            StringBuffer id = new StringBuffer(host != null ? host : "local");
            String path = parsedUri.getPath();
            if (path != null && !path.isEmpty()) {
                path = path.replaceFirst("/", "").replaceAll("/", "-").replaceAll(":", "-");
                id.append("-");
                id.append(path);
            }

            if (parsedUri.getPort() > -1) {
                id.append("-");
                id.append(parsedUri.getPort());
            }

            Repository repo = new Repository();
            repo.setId(id.toString());
            repo.setName(host != null ? host : "local");
            repo.setUrl(url);

            return repo;
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Invalid URL: " + url + " - " + e.getMessage());
        }
    }
}
