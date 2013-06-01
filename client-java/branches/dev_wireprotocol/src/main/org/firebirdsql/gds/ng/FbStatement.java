/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.XSQLDA;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbStatement {

    /**
     * @return FbDatabase of this statement
     */
    FbDatabase getDatabase() throws SQLException;

    /**
     * @return Transaction currently associated with this statement
     */
    FbTransaction getTransaction() throws SQLException;

    /**
     * @return XSQLDA descriptor of the parameters of this statement
     */
    XSQLDA getParameters() throws SQLException;

    /**
     * @return XSQLDA descriptor of the fields returned by this statement
     */
    XSQLDA getFields() throws SQLException;

    /**
     * @return The statement type
     */
    FbStatementType getStatementType();

    /**
     * @return The current state of this statement
     */
    StatementState getStatementState();

    /**
     * @return The current fetch size
     */
    int getFetchSize();

    /**
     * @param fetchSize
     *         Fetch size
     */
    void setFetchSize(int fetchSize) throws SQLException;

    /**
     * @return The execution plan for this statement
     */
    String getExecutionPlan() throws SQLException;

    /**
     * Closes this statement.
     *
     * @throws SQLException
     */
    void close() throws SQLException;

    /**
     * Releases this statement. TODO: What is the differences with {@link #close()}?
     *
     * @throws SQLException
     */
    void release() throws SQLException;

    /**
     * Describe this statement.
     *
     * @throws SQLException
     */
    void describe() throws SQLException;

    /**
     * Describe the parameters for this statement.
     *
     * @throws SQLException
     */
    void describeParameters() throws SQLException;

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
     * Fetch a row of values. TODO: Reuse existing fetcher code instead?
     *
     * @return
     * @throws SQLException
     */
    FbValue[] fetch() throws SQLException;

    //TODO: relevant for JDBC implementation?
    //FbValue[] GetOutputParameters() throws SQLException;

    /**
     * Creates an FbBlob instance
     *
     * @return
     * @throws SQLException
     */
    FbBlob createBlob() throws SQLException;

    /**
     * Creates an FbBlob instance for the specified blob handle.
     *
     * @param handle
     * @return
     * @throws SQLException
     */
    FbBlob createBlob(long handle) throws SQLException;

    // TODO Array
}
