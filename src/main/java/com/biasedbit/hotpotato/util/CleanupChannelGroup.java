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

package com.biasedbit.hotpotato.util;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Extension of {@link DefaultChannelGroup} that's used mainly as a cleanup container, where {@link #close()} is only
 * supposed to be called once.
 *
 * @author <a href="http://bruno.biasedbit.com/">Bruno de Carvalho</a>
 */
public class CleanupChannelGroup extends DefaultChannelGroup {

    // internal vars --------------------------------------------------------------------------------------------------

    private final AtomicBoolean closed;
    private final ReentrantReadWriteLock lock;

    // constructors ---------------------------------------------------------------------------------------------------

    public CleanupChannelGroup() {
        this.closed = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();
    }

    public CleanupChannelGroup(String name) {
        super(name);
        this.closed = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();
    }

    // DefaultChannelGroup --------------------------------------------------------------------------------------------

    @Override
    public ChannelGroupFuture close() {
        this.lock.writeLock().lock();
        try {
            if (!this.closed.getAndSet(true)) {
                // First time close() is called.
                return super.close();
            } else {
                throw new IllegalStateException("close() already called on " + this.getClass().getSimpleName() +
                                                " with name " + this.getName());
            }
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean add(Channel channel) {
        // Synchronization must occur to avoid add() and close() overlap (thus potentially leaving one channel open).
        // This could also be done by synchronizing the method itself but using a read lock here (rather than a
        // synchronized() block) allows multiple concurrent calls to add().
        this.lock.readLock().lock();
        try {
            if (this.closed.get()) {
                // Immediately close channel, as close() was already called.
                channel.close();
                return false;
            }

            return super.add(channel);
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
