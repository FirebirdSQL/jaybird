// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
