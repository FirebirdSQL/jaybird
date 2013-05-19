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

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;

import java.sql.SQLException;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbDatabase {

    /**
     * Attach to a database.
     *
     * @param dpb
     *         The DatabaseParameterBuffer with all required values
     * @throws SQLException
     */
    void attach(DatabaseParameterBuffer dpb) throws SQLException;

    /**
     * Detaches from the current database.
     *
     * @throws SQLException
     */
    void detach() throws SQLException;

    /**
     * Creates a new database, connection remains attached to database.
     *
     * @param dpb
     *         DatabaseParameterBuffer with all required values
     * @throws SQLException
     */
    void createDatabase(DatabaseParameterBuffer dpb) throws SQLException;

    /**
     * Drops (and deletes) the currently attached database.
     *
     * @throws SQLException
     */
    void dropDatabase() throws SQLException;

    /**
     * Cancels the current operation.
     *
     * @param kind
     *         TODO Document parameter kind of cancelOperation
     * @throws SQLException
     *         For errors cancelling, or if the cancel operation is not supported.
     */
    void cancelOperation(int kind) throws SQLException;

    /**
     * Creates and starts a transaction.
     *
     * @param tpb
     *         TransactionParameterBuffer with the required transaction
     *         options
     * @return Transaction
     * @throws SQLException
     */
    FbTransaction createTransaction(TransactionParameterBuffer tpb) throws SQLException;

    /**
     * Creates a statement with an implicit transaction.
     *
     * @return GdsStatement with implicit transaction
     * @throws SQLException
     */
    FbStatement createStatement() throws SQLException;

    /**
     * Creates a statement associated with a transaction
     *
     * @param transaction
     *         GdsTransaction to associate with this statement
     * @return GdsStatement
     * @throws SQLException
     */
    FbStatement createStatement(FbTransaction transaction) throws SQLException;

    /**
     * Request database info.
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
    <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws SQLException;

    /**
     * @return The database dialect
     */
    short getDatabaseDialect();

    /**
     * @return The client connection dialect
     */
    short getConnectionDialect();

    /**
     * @return The database handle value
     */
    int getHandle();

    /**
     * @return Number of open transactions
     */
    int getTransactionCount();

    /**
     * Sets the WarningMessageCallback for this database.
     *
     * @param callback
     *         WarningMessageCallback
     */
    void setWarningMessageCallback(WarningMessageCallback callback);

    /**
     * Current attachment status of the database.
     *
     * @return <code>true</code> if connected to the server and attached to a
     *         database, <code>false</code> otherwise.
     */
    boolean isAttached();

    /**
     * Get synchronization object.
     *
     * @return object, cannot be <code>null</code>.
     */
    Object getSynchronizationObject();

    /**
     * @return ODS major version
     */
    int getOdsMajor();

    /**
     * @return ODS minor version
     */
    int getOdsMinor();

    /**
     * @return Firebird version string
     */
    String getVersionString();
}
