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

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface FbDatabase {

    /**
     * Attach to a database.
     * 
     * @param dpb
     *            The DatabaseParameterBuffer with all required values
     * @param databaseName
     *            Path or alias of the database
     * @throws FbException
     */
    void attach(DatabaseParameterBuffer dpb, String databaseName) throws FbException;

    /**
     * Detaches from the current database.
     * 
     * @throws FbException
     */
    void detach() throws FbException;

    /**
     * Creates a new database, connection remains attached to database.
     * 
     * @param dpb
     *            DatabaseParameterBuffer with all required values
     * @param databaseName
     *            Path or alias of the database
     * @throws FbException
     */
    void createDatabase(DatabaseParameterBuffer dpb, String databaseName) throws FbException;

    /**
     * Drops (and deletes) the currently attached database.
     * 
     * @throws FbException
     */
    void dropDatabase() throws FbException;

    /**
     * Creates and starts a transaction.
     * 
     * @param tpb
     *            TransactionParameterBuffer with the required transaction
     *            options
     * @return Transaction
     * @throws FbException
     */
    FbTransaction createTransaction(TransactionParameterBuffer tpb) throws FbException;

    /**
     * Creates a statement with an implicit transaction.
     * 
     * @return GdsStatement with implicit transaction
     * @throws FbException
     */
    FbStatement createStatement() throws FbException;

    /**
     * Creates a statement associated with a transaction
     * 
     * @param transaction
     *            GdsTransaction to associate with this statement
     * @return GdsStatement
     * @throws FbException
     */
    FbStatement createStatement(FbTransaction transaction) throws FbException;

    /**
     * Request database info.
     * 
     * @param requestItems
     *            Array of info items to request
     * @param bufferLength
     *            Response buffer length to use
     * @param infoProcessor
     *            Implementation of {@link InfoProcessor} to transform
     *            the info response
     * @return Transformed info response of type T
     * @throws FbException
     *             For errors retrieving or transforming the response.
     */
    <T> T getDatabaseInfo(byte[] requestItems, int bufferLength, InfoProcessor<T> infoProcessor)
            throws FbException;

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
     *            WarningMessageCallback
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
