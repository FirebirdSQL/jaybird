// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.System.Logger.Level.DEBUG;
import static java.util.Objects.requireNonNull;

/**
 * Interface for processing deferred responses from the server.
 * <p>
 * This interface is used in protocol 11 or higher.
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
    default @Nullable WarningMessageCallback getWarningMessageCallback() {
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
            Function<Response, T> responseMapper, @Nullable WarningMessageCallback warningMessageCallback,
            Consumer<Exception> exceptionConsumer, boolean requiresSync) {
        return builder()
                .withProcessResponse(response -> deferredResponse.onResponse(responseMapper.apply(response)))
                .withOnException(exception -> {
                    try {
                        deferredResponse.onException(exception);
                    } finally {
                        exceptionConsumer.accept(exception);
                    }
                })
                .withWarningMessageCallback(warningMessageCallback)
                .withRequiresSync(requiresSync)
                .build();
    }

    /**
     * @return builder for a deferred action instance.
     * @since 7
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for lambda-based deferred actions.
     *
     * @since 7
     */
    final class Builder {

        private static final Consumer<Response> RESPONSE_NO_OP = r -> {};
        private static final Consumer<Exception> EXCEPTION_NO_OP = e ->
                System.getLogger(DeferredAction.class.getName()).log(DEBUG, "Exception in processDeferredActions", e);

        private Consumer<Response> processResponse = RESPONSE_NO_OP;
        private Consumer<Exception> onException = EXCEPTION_NO_OP;
        private @Nullable WarningMessageCallback warningMessageCallback;
        private boolean requiresSync;

        private Builder() {
        }

        public Builder withProcessResponse(Consumer<Response> processResponse) {
            this.processResponse = processResponse;
            return this;
        }

        public Builder withOnException(Consumer<Exception> onException) {
            this.onException = onException;
            return this;
        }

        public Builder withWarningMessageCallback(@Nullable WarningMessageCallback warningMessageCallback) {
            this.warningMessageCallback = warningMessageCallback;
            return this;
        }

        public Builder withRequiresSync(boolean requiresSync) {
            this.requiresSync = requiresSync;
            return this;
        }

        public DeferredAction build() {
            if (processResponse == RESPONSE_NO_OP && onException == EXCEPTION_NO_OP && warningMessageCallback == null
                    && !requiresSync) {
                return NO_OP_INSTANCE;
            }

            return new DeferredActionImpl(processResponse, onException, warningMessageCallback, requiresSync);
        }

        /**
         * Lambda-based deferred action implementation.
         *
         * @since 7
         */
        private record DeferredActionImpl(Consumer<Response> processResponse, Consumer<Exception> onException,
                @Nullable WarningMessageCallback warningMessageCallback, boolean requiresSync)
                implements DeferredAction {

            @Override
            public void processResponse(Response response) {
                processResponse.accept(response);
            }

            @Override
            public void onException(Exception exception) {
                onException.accept(exception);
            }

            @Override
            public @Nullable WarningMessageCallback getWarningMessageCallback() {
                return warningMessageCallback;
            }

            @Override
            public boolean requiresSync() {
                return requiresSync;
            }

        }

    }

    /**
     * Deferred action implementation that delegates to another deferred action.
     * <p>
     * This class is intended as a base class for implementations that want to decorate method calls. To decorate it,
     * subclass this class, and override the method, and ensure you call {@code super.<overridden-method>} in such a
     * way that it is always called, even if the decoration fails.
     * </p>
     * @since 7
     */
    abstract class DelegatingDeferredAction implements DeferredAction {

        private final DeferredAction delegate;

        DelegatingDeferredAction(DeferredAction delegate) {
            this.delegate = requireNonNull(delegate, "delegate");
        }

        @Override
        public void processResponse(Response response) {
            delegate.processResponse(response);
        }

        @Override
        public void onException(Exception exception) {
            delegate.onException(exception);
        }

        @Override
        public @Nullable WarningMessageCallback getWarningMessageCallback() {
            return delegate.getWarningMessageCallback();
        }

        @Override
        public boolean requiresSync() {
            return delegate.requiresSync();
        }
    }

}
