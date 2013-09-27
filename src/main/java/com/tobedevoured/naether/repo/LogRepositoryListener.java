package com.tobedevoured.naether.repo;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;

import com.tobedevoured.naether.Const;


/**
 * Based on Sonatype's
 * https://github.com/sonatype/sonatype-aether/blob/master/aether-demo/src/main/java/demo/util/ConsoleRepositoryListener.java
 */
public class LogRepositoryListener
    extends AbstractRepositoryListener {

	private static Logger log = LoggerFactory.getLogger("NaetherRepository");

    public void artifactDeployed( RepositoryEvent event )
    {
        log.info( "Deployed " + event.getArtifact() + Const._TO_ + event.getRepository() );
    }

    public void artifactDeploying( RepositoryEvent event )
    {
        log.info( "Deploying " + event.getArtifact() + Const._TO_ + event.getRepository() );
    }

    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        log.warn( "Invalid artifact descriptor for " + event.getArtifact() + ": "
            + event.getException().getMessage() );
    }

    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        log.warn( "Missing artifact descriptor for " + event.getArtifact() );
    }

    public void artifactInstalled( RepositoryEvent event )
    {
        log.info( "Installed " + event.getArtifact() + Const._TO_ + event.getFile() );
    }

    public void artifactInstalling( RepositoryEvent event )
    {
        log.debug( "Installing " + event.getArtifact() + Const._TO_ + event.getFile() );
    }

    public void artifactResolved( RepositoryEvent event )
    {
        log.debug( "Resolved artifact " + event.getArtifact() + Const._FROM_ + event.getRepository() );
    }

    public void artifactDownloading( RepositoryEvent event )
    {
        log.debug( "Downloading artifact " + event.getArtifact() + Const._FROM_ + event.getRepository() );
    }

    public void artifactDownloaded( RepositoryEvent event )
    {
        log.debug( "Downloaded artifact " + event.getArtifact() + Const._FROM_ + event.getRepository() );
    }

    public void artifactResolving( RepositoryEvent event )
    {
        log.debug( "Resolving artifact " + event.getArtifact() );
    }

    public void metadataDeployed( RepositoryEvent event )
    {
        log.info( "Deployed " + event.getMetadata() + Const._TO_ + event.getRepository() );
    }

    public void metadataDeploying( RepositoryEvent event )
    {
        log.debug( "Deploying " + event.getMetadata() + Const._TO_ + event.getRepository() );
    }

    public void metadataInstalled( RepositoryEvent event )
    {
        log.info( "Installed " + event.getMetadata() + Const._TO_ + event.getFile() );
    }

    public void metadataInstalling( RepositoryEvent event )
    {
        log.debug( "Installing " + event.getMetadata() + Const._TO_ + event.getFile() );
    }

    public void metadataInvalid( RepositoryEvent event )
    {
        log.debug( "Invalid metadata " + event.getMetadata() );
    }

    public void metadataResolved( RepositoryEvent event )
    {
        log.debug( "Resolved metadata " + event.getMetadata() + Const._FROM_ + event.getRepository() );
    }

    public void metadataResolving( RepositoryEvent event )
    {
        log.debug( "Resolving metadata " + event.getMetadata() + Const._FROM_ + event.getRepository() );
    }

}
