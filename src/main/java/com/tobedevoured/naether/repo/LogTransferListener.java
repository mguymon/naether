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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.transfer.AbstractTransferListener;
import org.sonatype.aether.transfer.TransferEvent;
import org.sonatype.aether.transfer.TransferResource;


/**
 * Base on https://github.com/sonatype/sonatype-aether/blob/master/aether-demo/src/main/java/demo/util/ConsoleTransferListener.java
 *
 */
public class LogTransferListener extends AbstractTransferListener {

    private static final int INITAL_CAPACITY = 64;
    private static final int KB = 1024;
            
    private Logger log = LoggerFactory.getLogger( "NaetherTransfer" ); 
    private Map<TransferResource, Long> downloads = new ConcurrentHashMap<TransferResource, Long>();

    private int lastLength;


    @Override
    public void transferInitiated( TransferEvent event ) {
        String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

        log.info( message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName() );
    }

    @Override
    public void transferProgressed( TransferEvent event ) {
        if ( log.isDebugEnabled() ) {
            TransferResource resource = event.getResource();
            downloads.put( resource, Long.valueOf( event.getTransferredBytes() ) );
    
            
            StringBuilder buffer = new StringBuilder( INITAL_CAPACITY );
    
            for ( Map.Entry<TransferResource, Long> entry : downloads.entrySet() )
            {
                long total = entry.getKey().getContentLength();
                long complete = entry.getValue().longValue();
    
                buffer.append( getStatus( complete, total ) ).append( "  " );
            }
    
            int pad = lastLength - buffer.length();
            lastLength = buffer.length();
            pad( buffer, pad );
    
            log.debug( buffer.toString() );
        }
    }

    private String getStatus( long complete, long total ) {
        if ( total >= KB )
        {
            return toKB( complete ) + "/" + toKB( total ) + " KB ";
        }
        else if ( total >= 0 )
        {
            return complete + "/" + total + " B ";
        }
        else if ( complete >= KB )
        {
            return toKB( complete ) + " KB ";
        }
        else
        {
            return complete + " B ";
        }
    }

    private void pad( StringBuilder buffer, int spaces ) {
        String block = "                                        ";
        int spacesLeft = spaces;
        while ( spacesLeft > 0 ) {
            int n = Math.min( spaces, block.length() );
            buffer.append( block, 0, n );
            spacesLeft -= n;
        }
    }

    @Override
    public void transferSucceeded( TransferEvent event ) {
        transferCompleted( event );

        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if ( contentLength >= 0 )
        {
            String type = ( event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded" );
            String len = contentLength >= KB ? toKB( contentLength ) + " KB" : contentLength + " B";

            String throughput = "";
            long duration = System.currentTimeMillis() - resource.getTransferStartTime();
            if ( duration > 0 )
            {
                DecimalFormat format = new DecimalFormat( "0.0", new DecimalFormatSymbols( Locale.ENGLISH ) );
                double kbPerSec = ( contentLength / KB ) / ( duration / 1000.0 );
                throughput = " at " + format.format( kbPerSec ) + " KB/sec";
            }

            log.debug( type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + len
                + throughput + ")" );
        }
    }

    @Override
    public void transferFailed( TransferEvent event ) {
        transferCompleted( event );

        log.debug( "Transfer Failed from repo {} for {}", event.getResource().getRepositoryUrl(), event.getResource().getResourceName() );
        log.debug( "Transfer Failed Exception", event.getException() );
    }

    private void transferCompleted( TransferEvent event ) {
        downloads.remove( event.getResource() );

        StringBuilder buffer = new StringBuilder( INITAL_CAPACITY );
        pad( buffer, lastLength );
        log.debug( buffer.toString() );
    }

    public void transferCorrupted( TransferEvent event ) {
        log.error( "Transfer Corrupted", event.getException() );
    }

    protected long toKB( long bytes ) {
        return ( bytes + 1023 ) / KB ;
    }

}
