/*
 * Copyright 2010 Bruno de Carvalho
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.biasedbit.hotpotato.client.connection.factory;

import java.util.concurrent.Executor;

import com.biasedbit.hotpotato.client.connection.HttpConnection;
import com.biasedbit.hotpotato.client.connection.HttpConnectionListener;
import com.biasedbit.hotpotato.client.connection.PipeliningHttpConnection;
import com.biasedbit.hotpotato.client.timeout.TimeoutManager;

/**
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public class PipeliningHttpConnectionFactory implements HttpConnectionFactory {

    // configuration defaults -----------------------------------------------------------------------------------------

    private static final boolean DISCONNECT_IF_NON_KEEP_ALIVE_REQUEST = false;
    private static final boolean ALLOW_POST_PIPELINING = false;
    private static final int MAX_REQUESTS_IN_PIPELINE = 50;

    // configuration --------------------------------------------------------------------------------------------------

    private boolean disconnectIfNonKeepAliveRequest;
    private boolean allowNonIdempotentPipelining;
    private int maxRequestsInPipeline;

    // constructors ---------------------------------------------------------------------------------------------------

    public PipeliningHttpConnectionFactory() {
        this.disconnectIfNonKeepAliveRequest = DISCONNECT_IF_NON_KEEP_ALIVE_REQUEST;
        this.allowNonIdempotentPipelining = ALLOW_POST_PIPELINING;
        this.maxRequestsInPipeline = MAX_REQUESTS_IN_PIPELINE;
    }

    // HttpConnectionFactory ------------------------------------------------------------------------------------------


    public HttpConnection createConnection(final String id, final String host, final int port, final HttpConnectionListener listener,
                                        final TimeoutManager manager) {
        return this.createConnection(id, host, port, listener, manager, null);
    }


    public HttpConnection createConnection(final String id, final String host, final int port, final HttpConnectionListener listener,
                                        final TimeoutManager manager, final Executor executor) {
        PipeliningHttpConnection connection = new PipeliningHttpConnection(id, host, port, listener, manager, executor);
        connection.setDisconnectIfNonKeepAliveRequest(this.disconnectIfNonKeepAliveRequest);
        connection.setAllowNonIdempotentPipelining(this.allowNonIdempotentPipelining);
        connection.setMaxRequestsInPipeline(this.maxRequestsInPipeline);
        return connection;
    }

    // getters & setters ----------------------------------------------------------------------------------------------

    public boolean isDisconnectIfNonKeepAliveRequest() {
        return disconnectIfNonKeepAliveRequest;
    }

    public void setDisconnectIfNonKeepAliveRequest(final boolean disconnectIfNonKeepAliveRequest) {
        this.disconnectIfNonKeepAliveRequest = disconnectIfNonKeepAliveRequest;
    }

    public boolean isAllowNonIdempotentPipelining() {
        return allowNonIdempotentPipelining;
    }

    public void setAllowNonIdempotentPipelining(final boolean allowNonIdempotentPipelining) {
        this.allowNonIdempotentPipelining = allowNonIdempotentPipelining;
    }

    public int getMaxRequestsInPipeline() {
        return maxRequestsInPipeline;
    }

    public void setMaxRequestsInPipeline(final int maxRequestsInPipeline) {
        this.maxRequestsInPipeline = maxRequestsInPipeline;
    }
}
