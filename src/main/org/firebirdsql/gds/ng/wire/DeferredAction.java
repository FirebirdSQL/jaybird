// SPDX-FileCopyrightText: Copyright 2014-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.WarningMessageCallback;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * Interface for processing deferred responses from the server.
 * <p>
 * This interfaces is used in protocol 11 or higher.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface DeferredAction {

    /**
     * An instance of {@link DeferredAction} which does nothing (uses the default methods of this interface).
     */
    DeferredAction NO_OP_INSTANCE = new DeferredAction() { };

    /**
     * Steps to process the deferred response.
     * <p>
     * The default implementation does nothing.
     * </p>
     *
     * @param response Response object.
     */
    default void processResponse(Response response) {
        // do nothing
    }

    /**
     * Exception received when receiving or processing the response.
     * <p>
     * The default implementation only logs the exception on debug level.
     * </p>
     *
     * @param exception
     *         exception received processing the response
     * @since 5
     */
    default void onException(Exception exception) {
        // Default expectation: this only happens if the connection is no longer available.
        // We ignore the exception and assume the next operation by the caller will fail as well.
        System.getLogger(getClass().getName()).log(DEBUG, "Exception in processDeferredActions", exception);
    }

    /**
     * Warning message callback.
     * <p>
     * The default implementation returns {@code null}.
     * </p>
     *
     * @return warning callback to use when executing this deferred action, {@code null} signals to use the default
     */
    default WarningMessageCallback getWarningMessageCallback() {
        return null;
    }

    /**
     * Indicates if this deferred action cannot be processed without an explicit sync action (e.g. {@code op_ping} or
     * {@code op_batch_sync}). Should also be used for requests which haven't been explicitly flushed.
     * <p>
     * Failure to perform such a sync action may result in indefinitely blocking on read.
     * </p>
     *
     * @return {@code true} if this deferred action requires an explicit sync action
     * @since 6
     */
    default boolean requiresSync() {
        return false;
    }

    /**
     * Wraps a {@link DeferredResponse} in a {@link DeferredAction}.
     *
     * @param deferredResponse
     *         the deferred response to wrap
     * @param responseMapper
     *         conversion from a {@link Response} to the appropriate object (or {@code null}) to call on
     *         {@link DeferredResponse#onResponse(Object)}
     * @param warningMessageCallback
     *         warning message callback to use when receiving the response
     * @param exceptionConsumer
     *         action to take for exceptions
     * @param <T>
     *         response type of the deferred response
     * @return deferred action
     */
    static <T> DeferredAction wrapDeferredResponse(DeferredResponse<T> deferredResponse,
            Function<Response, T> responseMapper, WarningMessageCallback warningMessageCallback,
            Consumer<Exception> exceptionConsumer, boolean requiresSync) {
        return new DeferredAction() {
            @Override
            public void processResponse(Response response) {
                deferredResponse.onResponse(responseMapper.apply(response));
            }

            @Override
            public void onException(Exception exception) {
                try {
                    deferredResponse.onException(exception);
                } finally {
                    exceptionConsumer.accept(exception);
                }
            }

            @Override
            public WarningMessageCallback getWarningMessageCallback() {
                return warningMessageCallback;
            }

            @Override
            public boolean requiresSync() {
                return requiresSync;
            }

        };
    }

}
