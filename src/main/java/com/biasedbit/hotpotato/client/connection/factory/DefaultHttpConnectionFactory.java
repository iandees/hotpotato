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

import com.biasedbit.hotpotato.client.connection.DefaultHttpConnection;
import com.biasedbit.hotpotato.client.connection.HttpConnection;
import com.biasedbit.hotpotato.client.connection.HttpConnectionListener;
import com.biasedbit.hotpotato.client.timeout.TimeoutManager;

/**
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public class DefaultHttpConnectionFactory implements HttpConnectionFactory {

    // configuration defaults -----------------------------------------------------------------------------------------

    private static final boolean DISCONNECT_IF_NON_KEEP_ALIVE_REQUEST = false;
    private static final boolean RESTORE_NON_IDEMPOTENT_OPERATIONS = false;

    // configuration --------------------------------------------------------------------------------------------------

    private boolean disconnectIfNonKeepAliveRequest;
    private boolean restoreNonIdempotentOperations;

    // constructors ---------------------------------------------------------------------------------------------------

    public DefaultHttpConnectionFactory() {
        this.disconnectIfNonKeepAliveRequest = DISCONNECT_IF_NON_KEEP_ALIVE_REQUEST;
        this.restoreNonIdempotentOperations = RESTORE_NON_IDEMPOTENT_OPERATIONS;
    }

    // HttpConnectionFactory ------------------------------------------------------------------------------------------


    public HttpConnection createConnection(final String id, final String host, final int port, final HttpConnectionListener listener,
                                           final TimeoutManager timeoutManager) {
        return this.createConnection(id, host, port, listener, timeoutManager, null);
    }


    public HttpConnection createConnection(final String id, final String host, final int port, final HttpConnectionListener listener,
                                           final TimeoutManager timeoutManager, final Executor executor) {
        DefaultHttpConnection connection =
                new DefaultHttpConnection(id, host, port, listener, timeoutManager, executor);
        connection.setDisconnectIfNonKeepAliveRequest(this.disconnectIfNonKeepAliveRequest);
        return connection;
    }

    // getters & setters ----------------------------------------------------------------------------------------------

    public boolean isDisconnectIfNonKeepAliveRequest() {
        return disconnectIfNonKeepAliveRequest;
    }

    /**
     * Causes an explicit request to disconnect a channel after the response to a non-keepalive request is received.
     * <p/>
     * Use this only if the server you're connecting to doesn't follow standards and keeps HTTP/1.0 connections open or
     * doesn't close HTTP/1.1 connections with 'Connection: close' header.
     *
     * @param disconnectIfNonKeepAliveRequest
     *         Whether the generated {@link HttpConnection}s should explicitly disconnect after executing a
     *         non-keepalive request.
     */
    public void setDisconnectIfNonKeepAliveRequest(final boolean disconnectIfNonKeepAliveRequest) {
        this.disconnectIfNonKeepAliveRequest = disconnectIfNonKeepAliveRequest;
    }

    public boolean isRestoreNonIdempotentOperations() {
        return restoreNonIdempotentOperations;
    }

    /**
     * Explicitly enables or disables recovery of non-idempotent operations when connections go down.
     * <p/>
     * When a connection goes down while executing a request it can restore that request by sending it back to the
     * listener inside the {@link HttpConnectionListener#connectionTerminated(HttpConnection, java.util.Collection)}
     * call.
     * <p/>
     * This can be dangerous for non-idempotent operations, because there is no guarantee that the request reached the
     * server and executed.
     * <p/>
     * By default, this option is disabled (safer).
     *
     * @param restoreNonIdempotentOperations
     *         Whether the generated {@link HttpConnection} should restore non-idempotent operations when the
     *         connection goes down.
     */
    public void setRestoreNonIdempotentOperations(final boolean restoreNonIdempotentOperations) {
        this.restoreNonIdempotentOperations = restoreNonIdempotentOperations;
    }
}
