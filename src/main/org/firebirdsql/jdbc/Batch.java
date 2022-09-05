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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.ng.fields.RowValue;

import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.util.List;

/**
 * Batch execution of a statement.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
interface Batch {

    /**
     * Adds a batch row value to the batch for the next execute.
     *
     * @param rowValue
     *         row value
     * @throws SQLException
     *         when this batch or the parent statement is closed
     */
    void addBatch(BatchRowValue rowValue) throws SQLException;

    /**
     * Executes the batch with the current row values.
     * <p>
     * After execution, all registered row values are cleared.
     * </p>
     * <p>
     * If the current batch is empty, this method is expected to be a no-op, returning an empty list
     * </p>
     *
     * @return update counts, if all executes succeeded
     * @throws java.sql.BatchUpdateException
     *         when batch execution fails. The content of {@link BatchUpdateException#getUpdateCounts()} depends on
     *         the configured error behaviour. If multi-error is enabled, an update count is provided for all elements,
     *         where those failed are marked with {@link java.sql.Statement#EXECUTE_FAILED}, otherwise only
     *         update counts for the successfully executed row values before the first failure. Exceptions per row are
     *         chained to this exception, and are available from {@link SQLException#getNextException()}.
     * @throws SQLException
     *         when this batch or the parent statement is closed, for other errors executing the batch (including
     *         creation of the batch if this was deferred). Multiple exceptions may be available from
     *         {@link SQLException#getNextException()}.
     */
    List<Long> execute() throws SQLException;

    /**
     * Clears the sets of parameters of this batch. If parameters were already sent to the server, the server-side
     * batch is also cleared.
     *
     * @throws SQLException
     *         for database access errors, or other errors clearing the batch
     */
    void clearBatch() throws SQLException;

    /**
     * Closes this batch.
     * <p>
     * This method is intended to be called only by the owning statement when it is closed, so if, for example,
     * statement close already deallocates the batch on the server, this method does not need to release the server-side
     * batch first.
     * </p>
     */
    void close();

    /**
     * Wrapper for row values, allowing additional work to be done just before sending the row value to the server (e.g.
     * flushing blobs).
     * <p>
     * NOTE: This API will likely change significantly if and when support is added for sending blobs as part of
     * the batch.
     * </p>
     * @since 5
     */
    interface BatchRowValue {

        /**
         * Convert this BatchRowValue to a normal row value.
         * <p>
         * Any work to prepare the row value (like flushing blobs to the server) before execution, needs to be done by
         * this method.
         * </p>
         *
         * @return a row value
         * @throws SQLException
         *         for failures to create the row value (e.g. database access errors writing a blob)
         */
        RowValue toRowValue() throws SQLException;

    }

}
