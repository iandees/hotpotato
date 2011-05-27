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

package com.biasedbit.hotpotato.request;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.biasedbit.hotpotato.logging.Logger;

/**
 * Implementation of {@link HttpRequestFuture} that resorts to stuff in {@linkplain java.util.concurrent} package.
 * <p/>
 * Uses less synchronization blocks which is potentially faster for high concurrency scenarios.
 *
 * @deprecated Use {@link com.biasedbit.hotpotato.request.DefaultHttpRequestFuture} instead; it's faster and safer.
 *
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
@Deprecated
public class ConcurrentHttpRequestFuture<T> implements HttpRequestFuture<T> {

    // constants ------------------------------------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(ConcurrentHttpRequestFuture.class);

    // configuration --------------------------------------------------------------------------------------------------

    private final boolean cancellable;

    // internal vars --------------------------------------------------------------------------------------------------

    private final CountDownLatch waitLatch;
    private T result;
    private HttpResponse response;
    private Object attachment;
    private final AtomicBoolean done;
    private final List<HttpRequestFutureListener<T>> listeners;
    private Throwable cause;
    private long executionStart;
    private long executionEnd;
    private final long creation;

    // constructors ---------------------------------------------------------------------------------------------------

    public ConcurrentHttpRequestFuture() {
        this(false);
    }

    public ConcurrentHttpRequestFuture(final boolean cancellable) {
        this.cancellable = cancellable;
        this.creation = System.nanoTime();
        this.executionStart = -1;
        this.done = new AtomicBoolean(false);
        // It's just a couple of bytes and memory is cheaper than CPU...
        this.listeners = new ArrayList<HttpRequestFutureListener<T>>(2);

        this.waitLatch = new CountDownLatch(1);
    }

    // HttpRequestFuture ----------------------------------------------------------------------------------------------


    public T getProcessedResult() {
        return this.result;
    }


    public HttpResponse getResponse() {
        return this.response;
    }


    public HttpResponseStatus getStatus() {
        if (this.response == null) {
            return null;
        }
        return this.response.getStatus();
    }


    public int getResponseStatusCode() {
        if (this.response == null) {
            return -1;
        }

        return this.response.getStatus().getCode();
    }


    public boolean isSuccessfulResponse() {
        int code = this.getResponseStatusCode();
        return (code >= 200) && (code <= 299);
    }


    public void markExecutionStart() {
        this.executionStart = System.nanoTime();
    }


    public long getExecutionTime() {
        if (this.done.get()) {
            return this.executionStart == -1 ? 0 : (this.executionEnd - this.executionStart) / 1000000;
        } else {
            return -1;
        }
    }


    public long getExistenceTime() {
        if (this.done.get()) {
            return (this.executionEnd - this.creation) / 1000000;
        } else {
            return (System.nanoTime() - this.creation) / 1000000;
        }
    }


    public boolean isDone() {
        return this.done.get();
    }


    public boolean isSuccess() {
        return this.response != null;
    }


    public boolean isCancelled() {
        return this.cause == CANCELLED;
    }


    public Throwable getCause() {
        return this.cause;
    }


    public boolean cancel() {
        if (!this.cancellable) {
            return false;
        }

        // Get previous value and set to true
        if (this.done.getAndSet(true)) {
            // If previous value was already true, then bail out.
            return false;
        }

        this.executionEnd = System.nanoTime();
        this.cause = CANCELLED;
        this.waitLatch.countDown();

        this.notifyListeners();
        return true;
    }


    public boolean setSuccess(final T processedResponse, final HttpResponse response) {
        // Get previous value and set to true
        if (this.done.getAndSet(true)) {
            // If previous value was already true, then bail out.
            return false;
        }

        this.executionEnd = System.nanoTime();
        this.result = processedResponse;
        this.response = response;
        this.waitLatch.countDown();

        this.notifyListeners();
        return true;
    }


    public boolean setFailure(final Throwable cause) {
        // Get previous value and set to true
        if (this.done.getAndSet(true)) {
            // If previous value was already true, then bail out.
            return false;
        }

        this.executionEnd = System.nanoTime();
        this.cause = cause;
        this.waitLatch.countDown();

        this.notifyListeners();
        return true;
    }


    public boolean setFailure(final HttpResponse response, final Throwable cause) {
        // Get previous value and set to true
        if (this.done.getAndSet(true)) {
            // If previous value was already true, then bail out.
            return false;
        }

        this.executionEnd = System.nanoTime();
        this.response = response;
        this.cause = cause;
        this.waitLatch.countDown();

        this.notifyListeners();
        return true;
    }


    public void addListener(final HttpRequestFutureListener<T> listener) {
        if (this.done.get()) {
            this.notifyListener(listener);
            return;
        }

        synchronized (this.listeners) {
            if (this.done.get()) {
                this.notifyListener(listener);
                return;
            }

            this.listeners.add(listener);
        }
    }


    public void removeListener(final HttpRequestFutureListener<T> listener) {
        if (this.done.get()) {
            return;
        }

        synchronized (this.listeners) {
            if (!this.done.get()) {
                this.listeners.remove(listener);
            }
        }
    }


    public Object getAttachment() {
        return attachment;
    }


    public void setAttachment(final Object attachment) {
        this.attachment = attachment;
    }


    public HttpRequestFuture<T> await() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        this.waitLatch.await();

        return this;
    }


    public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
        return this.waitLatch.await(timeout, unit);
    }


    public boolean await(final long timeoutMillis) throws InterruptedException {
        return this.waitLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
    }


    public HttpRequestFuture<T> awaitUninterruptibly() {
        boolean interrupted = false;
        while (!this.done.get() || (this.waitLatch.getCount() > 0)) {
            try {
                this.waitLatch.await();
            } catch (InterruptedException e) {
                // Stubborn basterd!
                interrupted = true;
            }
        }

        // Preserve interruption.
        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return this;
    }


    public boolean awaitUninterruptibly(final long timeout, final TimeUnit unit) {
        long start;
        long waitTime = unit.toNanos(timeout);
        boolean interrupted = false;
        boolean onTime = false;

        while (!this.done.get() && (waitTime > 0)) {
            start = System.nanoTime();
            try {
                onTime = this.waitLatch.await(waitTime, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                // subtract delta to remaining wait time
                waitTime -= (System.nanoTime() - start);
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.interrupted();
        }

        return onTime;
    }


    public boolean awaitUninterruptibly(final long timeoutMillis) {
        return this.awaitUninterruptibly(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    // private helpers ------------------------------------------------------------------------------------------------

    private void notifyListeners() {
        synchronized (this.listeners) {
            for (HttpRequestFutureListener<T> listener : this.listeners) {
                this.notifyListener(listener);
            }
        }
    }

    private void notifyListener(final HttpRequestFutureListener<T> listener) {
        try {
            listener.operationComplete(this);
        } catch (Throwable t) {
            LOG.warn("An exception was thrown by an instance of {}.", t, listener.getClass().getSimpleName());
        }
    }

    // low level overrides --------------------------------------------------------------------------------------------


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append("HttpRequestFuture{")
                .append("existenceTime=").append(this.getExistenceTime())
                .append(", executionTime=").append(this.getExecutionTime());
        if (!this.isDone()) {
            builder.append(", inProgress");
        } else if (this.isSuccess()) {
            builder.append(", succeeded (code ").append(this.response.getStatus().getCode()).append(')');
        } else {
            builder.append(", failed (").append(this.cause).append(')');
        }
        return builder.append('}').toString();
    }
}
