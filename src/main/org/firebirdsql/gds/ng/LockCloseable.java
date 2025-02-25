// SPDX-FileCopyrightText: Copyright 2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

/**
 * Unlocks the lock on {@link #close()}, intended for use with try-with-resources.
 * <p>
 * Implementations do not guard against multiple invocations of {@code close()}. That means, each call to {@code close}
 * will result in an {@link java.util.concurrent.locks.Lock#unlock()} or equivalent.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 5
 */
@FunctionalInterface
public interface LockCloseable extends AutoCloseable {

    /**
     * Lock closeable that can be used as a no-op (e.g. if there is no lock, and thus nothing to unlock).
     */
    LockCloseable NO_OP = () -> { };

    /**
     * Performs an {@link java.util.concurrent.locks.Lock#unlock()} or equivalent on the lock.
     */
    @Override
    void close();

}
