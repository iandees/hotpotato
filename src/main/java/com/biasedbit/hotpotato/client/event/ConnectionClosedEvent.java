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

package com.biasedbit.hotpotato.client.event;

import java.util.Collection;

import com.biasedbit.hotpotato.client.HttpRequestContext;
import com.biasedbit.hotpotato.client.connection.HttpConnection;

/**
 * Event generated when an established connection is closed.
 *
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public class ConnectionClosedEvent implements HttpClientEvent {

    // internal vars --------------------------------------------------------------------------------------------------

    private final HttpConnection connection;
    private final Collection<HttpRequestContext> retryRequests;

    // constructors ---------------------------------------------------------------------------------------------------

    public ConnectionClosedEvent(final HttpConnection connection, final Collection<HttpRequestContext> retryRequests) {
        this.connection = connection;
        this.retryRequests = retryRequests;
    }

    // HttpClientEvent ------------------------------------------------------------------------------------------------


    public EventType getEventType() {
        return EventType.CONNECTION_CLOSED;
    }

    // getters & setters ----------------------------------------------------------------------------------------------

    public HttpConnection getConnection() {
        return this.connection;
    }

    public Collection<HttpRequestContext> getRetryRequests() {
        return retryRequests;
    }

    // low level overrides --------------------------------------------------------------------------------------------


    @Override
    public String toString() {
        return new StringBuilder()
                .append("ConnectionClosedEvent{")
                .append("connection=").append(this.connection)
                .append(", retryRequests=").append(this.retryRequests == null ? 0 : this.retryRequests.size())
                .append('}').toString();
    }
}
