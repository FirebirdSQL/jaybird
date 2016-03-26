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

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.WarningMessageCallback;
import org.firebirdsql.gds.ng.fields.BlrCalculator;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface FbWireDatabase extends FbDatabase, FbWireAttachment {

    /**
     * Reads the response from the server.
     *
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return {@link Response} read.
     * @throws SQLException
     *         For errors returned from the server, or when attempting to read
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    Response readResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * Release object.
     *
     * @param operation
     *         Operation
     * @param objectId
     *         Id of the object to release
     * @throws SQLException
     */
    void releaseObject(int operation, int objectId) throws SQLException;

    /**
     * Convenience method to read a Response to a SqlResponse
     *
     * @param callback
     *         Callback object for warnings, <code>null</code> for default callback
     * @return SqlResponse
     * @throws SQLException
     *         For errors returned from the server, or when attempting to
     *         read.
     * @throws IOException
     *         For errors reading the response from the connection.
     */
    SqlResponse readSqlResponse(WarningMessageCallback callback) throws SQLException, IOException;

    /**
     * @return The {@link BlrCalculator} instance for this database.
     */
    BlrCalculator getBlrCalculator();

    /**
     * Enqueue a deferred action.
     * <p>
     * FbDatabase implementations that do not support deferred actions are allowed to throw an
     * {@link java.lang.UnsupportedOperationException}
     * </p>
     *
     * @param deferredAction Deferred action
     */
    void enqueueDeferredAction(DeferredAction deferredAction);

    /**
     * Consumes packets notifying for warnings, but ignoring exceptions thrown from the packet.
     * <p>
     * This method should only be used inside the implementation if either packets need to be ignored,
     * or to ensure that there is no backlog of packets (eg when an exception occurs during processing of multiple
     * package responses).
     * </p>
     *
     * @param numberOfResponses Number of responses to consume.
     * @param warningCallback Callback for warnings
     */
    void consumePackets(int numberOfResponses, WarningMessageCallback warningCallback);
}
