/*
 * $Id$
 *
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

import org.firebirdsql.gds.TransactionParameterBuffer;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbTransaction {

    // TODO Evaluate whether this structure matches with needs for JDBC

    /**
     * @return Current transaction state
     */
    TransactionState getState();

    /**
     * @return The Firebird transaction handle identifier
     */
    int getHandle();

    /**
     * Adds a {@link TransactionEventListener} to the list of listeners.
     * <p>
     * The implementation may use {@link java.lang.ref.WeakReference} for the listeners, so
     * make sure the listener remains strongly reachable for its useful
     * lifetime.
     * </p>
     *
     * @param listener
     *         TransactionEventListener to register
     */
    void addTransactionEventListener(TransactionEventListener listener);

    /**
     * Removes the {@link TransactionEventListener} from the list of listeners.
     *
     * @param listener
     *         TransactionEventListener to remove
     */
    void removeTransactionEventListener(TransactionEventListener listener);

    /**
     * Begin the transaction.
     *
     * @param tpb
     *         TransactionParameterBuffer with the transaction configuration
     * @throws SQLException
     */
    void beginTransaction(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Commit the transaction
     *
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * Commit with retaining
     *
     * @throws SQLException
     */
    void commitRetaining() throws SQLException;

    /**
     * Roll back the transaction
     *
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * Roll back with retaining
     *
     * @throws SQLException
     */
    void rollbackRetaining() throws SQLException;

    // TODO Implement two-phase to match with current Jaybird implementation
    //    void prepare() throws SQLException;
    //
    //    void prepare(byte[] buffer) throws SQLException;
}
