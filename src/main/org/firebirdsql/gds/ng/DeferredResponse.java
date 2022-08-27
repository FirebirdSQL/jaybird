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
package org.firebirdsql.gds.ng;

import org.firebirdsql.logging.LoggerFactory;

/**
 * Interface for receiving deferred/async responses.
 * <p>
 * GDS-ng implementations which are not capable of asynchronous or delayed processing of responses are expected to
 * synchronously invoke the {@link #onResponse(Object)} and - optionally - {@link #onException(Exception)}
 * methods within the method call.
 * </p>
 *
 * @param <T>
 *         response type expected ({@code Void} if no object, but {@code null} is expected)
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface DeferredResponse<T> {

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
     * For GDS-ng implementations that can only perform synchronous processing, it is implementation-defined whether
     * or not this method is called, or if the exception is thrown directly from the invoked method.
     * </p>
     *
     * @param exception
     *         exception received processing the response
     */
    default void onException(Exception exception) {
        // Default expectation: this only happen if the connection is no longer available.
        // We ignore the exception and assume the next operation by the caller will fail as well.
        LoggerFactory.getLogger(getClass()).debug("Exception in processDeferredActions", exception);
    }

}
