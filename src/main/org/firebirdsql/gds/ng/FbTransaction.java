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

import org.firebirdsql.gds.ng.listeners.ExceptionListenable;
import org.firebirdsql.gds.ng.listeners.TransactionListener;

import java.sql.SQLException;

/**
 * Handle for a transaction.
 * <p>
 * All methods defined in this interface are required to notify all {@code SQLException} thrown from the methods
 * defined in this interface.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface FbTransaction extends ExceptionListenable {

    /**
     * @return Current transaction state
     */
    TransactionState getState();

    /**
     * @return The Firebird transaction handle identifier
     */
    int getHandle();

    /**
     * Adds a {@link org.firebirdsql.gds.ng.listeners.TransactionListener} to the list of strongly referenced listeners.
     *
     * @param listener
     *         TransactionListener to register
     */
    void addTransactionListener(TransactionListener listener);

    /**
     * Adds a {@link org.firebirdsql.gds.ng.listeners.TransactionListener} to the list of weakly referenced listeners.
     * <p>
     * If the listener is already strongly referenced, this call will be ignored
     * </p>
     *
     * @param listener
     *         TransactionListener to register
     */
    void addWeakTransactionListener(TransactionListener listener);

    /**
     * Removes the {@link org.firebirdsql.gds.ng.listeners.TransactionListener} from the list of listeners.
     *
     * @param listener
     *         TransactionListener to remove
     */
    void removeTransactionListener(TransactionListener listener);

    /**
     * Commit the transaction
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * Roll back the transaction
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * Prepare the transaction for two-phase commit/rollback.
     *
     * @param recoveryInformation
     *         Transaction recovery information (stored in RDB$TRANSACTION_DESCRIPTION of RDB$TRANSACTIONS),
     *         or {@code null} to prepare without recovery information.
     * @throws SQLException
     */
    void prepare(byte[] recoveryInformation) throws SQLException;

    /**
     * Request transaction info.
     *
     * @param requestItems
     *         Array of info items to request
     * @param bufferLength
     *         Response buffer length to use
     * @param infoProcessor
     *         Implementation of {@link InfoProcessor} to transform
     *         the info response
     * @return Transformed info response of type T
     * @throws SQLException
     *         For errors retrieving or transforming the response.
     */
    <T> T getTransactionInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException;

    /**
     * Performs a transaction info request.
     *
     * @param requestItems
     *         Information items to request
     * @param maxBufferLength
     *         Maximum response buffer length to use
     * @return The response buffer (note: length is the actual length of the
     * response, not <code>maxBufferLength</code>
     * @throws SQLException
     *         For errors retrieving the information.
     */
    byte[] getTransactionInfo(byte[] requestItems, int maxBufferLength) throws SQLException;

    /**
     * Retrieves the transaction id.
     * <p>
     * The transaction id is the database transaction id, not to be confused with the attachment level transaction
     * handle provided by {@link #getHandle()}.
     * </p>
     *
     * @return Database transaction id.
     * @throws SQLException
     */
    long getTransactionId() throws SQLException;
}
