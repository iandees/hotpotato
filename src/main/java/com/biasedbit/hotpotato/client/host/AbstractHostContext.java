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

package com.biasedbit.hotpotato.client.host;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

import com.biasedbit.hotpotato.client.ConnectionPool;
import com.biasedbit.hotpotato.client.HttpRequestContext;
import com.biasedbit.hotpotato.client.connection.HttpConnection;

/**
 * Abstract implementation of the {@link HostContext} interface.
 *
 * This class contains boilerplate code that all implementations of {@link HostContext} would surely have as well.
 * The important logic is present in {@link #drainQueue()}, method that needs to be implemented by extensions of this
 * class.
 *
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public abstract class AbstractHostContext implements HostContext {

    // internal vars --------------------------------------------------------------------------------------------------

    protected final String host;
    protected final int port;
    protected final int maxConnections;
    protected final ConnectionPool connectionPool;
    protected final LinkedList<HttpRequestContext> queue;

    // constructors ---------------------------------------------------------------------------------------------------

    public AbstractHostContext(final String host, final int port, final int maxConnections) {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("MaxConnections must be > 0");
        }

        this.host = host;
        this.port = port;
        this.maxConnections = maxConnections;
        this.connectionPool = new ConnectionPool();
        this.queue = new LinkedList<HttpRequestContext>();
    }

    // HostContext ----------------------------------------------------------------------------------------------------


    public String getHost() {
        return this.host;
    }


    public int getPort() {
        return this.port;
    }


    public ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }


    public Queue<HttpRequestContext> getQueue() {
        return this.queue;
    }


    public void restoreRequestsToQueue(final Collection<HttpRequestContext> requests) {
        this.queue.addAll(0, requests);
    }


    public void addToQueue(final HttpRequestContext request) {
        this.queue.add(request);
    }


    public DrainQueueResult drainQueue() {
        // 1. Test if there's anything to drain
        if (this.queue.isEmpty()) {
            return DrainQueueResult.QUEUE_EMPTY;
        }

        // 2. There are contents to drain, test if there are any connections created.
        if (this.connectionPool.getConnections().isEmpty()) {
            // 2a. No connections open, test if there is still room to create a new one.
            if (this.connectionPool.getTotalConnections() < this.maxConnections) {
                return DrainQueueResult.OPEN_CONNECTION;
            } else {
                return DrainQueueResult.NOT_DRAINED;
            }
        }

        // 3. There is content to drain and there are connections, iterate them to find an available one.
        for (HttpConnection connection : this.connectionPool.getConnections()) {
            while (connection.isAvailable()) {
                // Found an available connection; peek the first request and attempt to execute it.
                HttpRequestContext context = this.queue.peek();
                if (connection.execute(context)) {
                    // If the request was executed it means the connection wasn't terminating and it's still connected.
                    // Remove it from the queue (it was only previously peeked) and return DRAINED.
                    this.queue.remove();
                    return DrainQueueResult.DRAINED;
                }
            }
        }

        // 4. There were connections open but none of them was available; if possible, request a new one.
        if (this.connectionPool.getTotalConnections() < this.maxConnections) {
            return DrainQueueResult.OPEN_CONNECTION;
        } else {
            return DrainQueueResult.NOT_DRAINED;
        }
    }


    public HttpRequestContext pollQueue() {
        return this.queue.poll();
    }


    public void failAllRequests(final Throwable cause) {
        for (HttpRequestContext context : this.queue) {
            context.getFuture().setFailure(cause);
        }
        this.queue.clear();
    }
}
