package com.tobedevoured.naether.util;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
   *
 * http://www.apache.org/licenses/LICENSE-2.0
   *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.maven.model.Repository;
import org.sonatype.aether.repository.RemoteRepository;

/**
 * Helper for creating {@link RemoteRepository}
 * 
 * @author Michael Guymon
 *
 */
public final class RepoBuilder {

	private RepoBuilder() { }
	
	/**
	 * Create a {@link RemoteRepository} from a String url
	 * 
	 * @param url
	 * @return {@link RemoteRepository}
	 * @throws MalformedURLException
	 */
	public static RemoteRepository remoteRepositoryFromUrl(String url) throws MalformedURLException {
		URL parsedUrl = new URL(url);

		StringBuffer id = new StringBuffer(parsedUrl.getHost());
		String path = parsedUrl.getPath();
		if (path.length() > 0) {
			path = path.replaceFirst("/", "").replaceAll("/", "-").replaceAll(":", "-");
			id.append("-");
			id.append(path);
		}

		if (parsedUrl.getPort() > -1) {
			id.append("-");
			id.append(parsedUrl.getPort());
		}

		return new RemoteRepository(id.toString(), "default", url);
	}
	
	/**
	 * Create a {@link Repository} from a String url
	 * 
	 * @param url
	 * @return {@link Repository}
	 * @throws MalformedURLException
	 */
	public static Repository repositoryFromUrl(String url) throws MalformedURLException {
		URL parsedUrl = new URL(url);

		StringBuffer id = new StringBuffer(parsedUrl.getHost());
		String path = parsedUrl.getPath();
		if (path.length() > 0) {
			path = path.replaceFirst("/", "").replaceAll("/", "-").replaceAll(":", "-");
			id.append("-");
			id.append(path);
		}

		if (parsedUrl.getPort() > -1) {
			id.append("-");
			id.append(parsedUrl.getPort());
		}

		Repository repo = new Repository();
		repo.setId( id.toString() );
		repo.setName( parsedUrl.getHost() );
		repo.setUrl( url );
		
		return repo;
	}
}
