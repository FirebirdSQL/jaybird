// SPDX-FileCopyrightText: Copyright 2016 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.listeners;

/**
 * Provides notification of exceptions to {@link ExceptionListener} instance.
 * <p>
 * Implementations are required to use {@code WeakReference} to hold the listener. It is strongly suggested to use
 * {@link ExceptionListenerDispatcher} in your implementation.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface ExceptionListenable {

    /**
     * Adds an exception listener to this object.
     * <p>
     * Implementations use {@code WeakReference}.
     * </p>
     *
     * @param listener
     *         Listener to register
     */
    void addExceptionListener(ExceptionListener listener);

    /**
     * Removes an exception listener to this object.
     *
     * @param listener
     *         Listener to remove
     */
    void removeExceptionListener(ExceptionListener listener);

}
