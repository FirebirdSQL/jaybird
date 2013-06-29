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

import org.firebirdsql.gds.ng.fields.RowDescriptor;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbStatement {

    /**
     * @return Transaction currently associated with this statement
     */
    FbTransaction getTransaction() throws SQLException;

    /**
     * Associates a transaction with this statement
     *
     * @param transaction
     *         The transaction
     * @throws SQLException
     */
    void setTransaction(FbTransaction transaction) throws SQLException;

    /**
     * @return descriptor of the parameters of this statement
     */
    RowDescriptor getParameters() throws SQLException;

    /**
     * @return descriptor of the fields returned by this statement
     */
    RowDescriptor getFields() throws SQLException;

    /**
     * @return The statement type
     */
    StatementType getType();

    /**
     * @return The current state of this statement
     */
    StatementState getState();

    /**
     * @return The Firebird statement handle identifier
     */
    int getHandle();

    /**
     * Close and deallocate this statement.
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * Closes the cursor associated with this statement, leaving the
     * statement itself allocated.
     *
     * @throws SQLException
     */
    void closeCursor() throws SQLException;

    /**
     * Prepare the statement text
     *
     * @param statementText
     *         Statement text
     * @throws SQLException
     */
    void prepare(String statementText) throws SQLException;

    /**
     * Execute the statement.
     *
     * @throws SQLException
     */
    void execute() throws SQLException;

    /**
     * Prepares and executes the statement. This method cannot be used for statements expecting parameters.
     *
     * @param statementText
     *         Statement text
     * @throws SQLException
     */
    void execute(String statementText) throws SQLException;

    /**
     * Get synchronization object.
     *
     * @return object, cannot be <code>null</code>.
     */
    Object getSynchronizationObject();
}
