// SPDX-FileCopyrightText: Copyright 2022-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.jspecify.annotations.Nullable;

/**
 * Interface for receiving deferred/async responses.
 * <p>
 * GDS-ng implementations which are not capable of asynchronous or delayed processing of responses are expected to
 * synchronously invoke the {@link #onResponse(Object)} method, and either throw the exception <em>or</em> call
 * {@link #onException(Exception)} within the method call.
 * </p>
 *
 * @param <T>
 *         response type expected ({@code Void} if no object, but {@code null} is expected)
 * @author Mark Rotteveel
 * @since 5
 */
public interface DeferredResponse<T extends @Nullable Object> {

    /**
     * Called with successful response.
     *
     * @param response
     *         response object, or {@code null} if there is no response, but the request completed successfully
     */
    @SuppressWarnings("unused")
    default void onResponse(T response) {
    }

    /**
     * Exception received when receiving or processing the response.
     * <p>
     * The default implementation only logs the exception on debug level.
     * </p>
     * <p>
     * For GDS-ng implementations that can only perform synchronous processing, it is implementation-defined if this
     * method is called, or if the exception is thrown directly from the invoked method.
     * </p>
     *
     * @param exception
     *         exception received processing the response
     */
    default void onException(Exception exception) {
        // Default expectation: this only happen if the connection is no longer available.
        // We ignore the exception and assume the next operation by the caller will fail as well.
        System.getLogger(getClass().getName())
                .log(System.Logger.Level.DEBUG, "Exception in processDeferredActions", exception);
    }

}
