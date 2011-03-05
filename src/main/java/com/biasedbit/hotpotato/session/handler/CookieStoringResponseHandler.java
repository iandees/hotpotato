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

package com.biasedbit.hotpotato.session.handler;

import java.util.List;

import org.jboss.netty.handler.codec.http.HttpHeaders;

import com.biasedbit.hotpotato.request.HttpRequestFuture;
import com.biasedbit.hotpotato.response.HttpResponseProcessor;
import com.biasedbit.hotpotato.session.HandlerSessionFacade;
import com.biasedbit.hotpotato.session.RecursiveAwareHttpRequest;
import com.biasedbit.hotpotato.util.HostPortAndUri;

/**
 * Automatically stores Set-Cookie headers received in 200 responses.
 *
 * @author <a:mailto="bruno.carvalho@wit-software.com" />Bruno de Carvalho</a>
 */
public class CookieStoringResponseHandler implements ResponseCodeHandler {


    public int[] handlesResponseCodes() {
        return new int[]{200};
    }


    public <T> void handleResponse(final HandlerSessionFacade session, final HttpRequestFuture<T> originalFuture,
                                   final HttpRequestFuture<T> future, final HostPortAndUri target,
                                   final RecursiveAwareHttpRequest request, final HttpResponseProcessor<T> processor) {

        // Store the cookies...
        List<String> cookies = future.getResponse().getHeaders(HttpHeaders.Names.SET_COOKIE);
        if ((cookies != null) && !cookies.isEmpty()) {
            for (String cookie : cookies) {
                session.addHeader(HttpHeaders.Names.COOKIE, cookie);
            }
        }

        // And release the original future
        originalFuture.setSuccess(future.getProcessedResult(), future.getResponse());
    }
}
