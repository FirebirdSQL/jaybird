/*
 * Firebird Open Source JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.event;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class OneTimeEventListener implements EventListener {

    private int eventCount = -1;

    private final Lock lock = new ReentrantLock();
    private final Condition receivedEvent = lock.newCondition();

    @Override
    public void eventOccurred(DatabaseEvent event) {
        lock.lock();
        try {
            if (eventCount == -1) {
                eventCount = event.getEventCount();
            }
            receivedEvent.signalAll();
        } finally {
            lock.unlock();
        }
    }

    void await(long time, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            // Event already received, no need to wait
            if (eventCount != -1) return;
            if (time == 0) {
                receivedEvent.await();
            } else {
                receivedEvent.await(time, unit);
            }
        } finally {
            lock.unlock();
        }
    }

    int getEventCount() {
        return eventCount;
    }
}
