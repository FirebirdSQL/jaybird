/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.DeferredResponse;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.logging.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface for processing deferred responses from the server.
 * <p>
 * This interfaces is used in protocol 11 or higher.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface DeferredAction {

    /**
     * Steps to process the deferred response.
     *
     * @param response Response object.
     */
    void processResponse(Response response);

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
        LoggerFactory.getLogger(getClass()).debug("Exception in processDeferredActions", exception);
    }

    /**
     * @return The warning callback to use when executing this deferred action.
     */
    WarningMessageCallback getWarningMessageCallback();

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
            Consumer<Exception> exceptionConsumer) {
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
        };
    }
}
