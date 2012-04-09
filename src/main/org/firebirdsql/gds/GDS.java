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

/* The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 * Contributors:
 * 
 *   Blas Rodrigues Somoza
 *   David Jencks               d_jencks@users.sourceforge.net
 *   Gabriel Reid
 *   Rick Fincher
 *   Roman Rokytskyy
 *   Ryan Baldwin
 *   Steven Jardine
 */

package org.firebirdsql.gds;

import org.firebirdsql.gds.impl.GDSType;

/**
 * The interface <code>GDS</code> has most of the C client interface functions
 * lightly mapped to java, as well as the constants returned from the server.
 * 
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface GDS {

    // Handle declaration methods

    /**
     * Factory method to create a new {@link IscDbHandle} instance specific to
     * the implementation of this interface.
     * 
     * @return instance of {@link IscDbHandle}
     */
    IscDbHandle createIscDbHandle();

    /**
     * Factory method to create a new {@link IscTrHandle} instance that
     * is linked to the current <code>GDS</code> implementation.
     * 
     * @return A new {@link IscTrHandle} instance
     */
    IscTrHandle createIscTrHandle();

    /**
     * Factory method to create a new {@link IscStmtHandle} instance
     * specific to the implementation of this interface.
     * 
     * @return A new {@link IscStmtHandle}  instance
     */
    IscStmtHandle createIscStmtHandle();

    /**
     * Factory method to create a new {@link IscBlobHandle} instance
     * specific to the implementation of this interface.
     * 
     * @return A new {@link IscBlobHandle} instance
     */
    IscBlobHandle createIscBlobHandle();

    /**
     * Factory method to create a new {@link IscSvcHandle} instance
     * that is linked to the current <code>GDS</code> implemenation.
     * 
     * @return A new {@link IscSvcHandle} instance
     */
    IscSvcHandle createIscSvcHandle();

    /**
     * Create a new {@link ServiceParameterBuffer} instance for setting
     * service parameters in the current GDS implementation.
     * 
     * @return a new {@link ServiceParameterBuffer} 
     */
    ServiceParameterBuffer createServiceParameterBuffer();

    /**
     * Create a new {@link ServiceRequestBuffer} instance for setting
     * service request parameters in the current GDS implementation. The
     * returned {@link ServiceRequestBuffer} is linked to a specific
     * Services API task, and must be one of the <code>isc_info_svc_*</code>
     * or <code>isc_action_svc</code> constants from {@link ISCConstants}.
     * 
     * @param taskIdentifier
     *            The specific Services API task for which the
     *            {@link ServiceRequestBuffer} is created
     * @return A new {@link ServiceRequestBuffer}
     */
    ServiceRequestBuffer createServiceRequestBuffer(int taskIdentifier);

    /**
     * Create a new {@link DatabaseParameterBuffer} instance for setting
     * database parameters in the current GDS implementation.
     * 
     * @return A new {@link DatabaseParameterBuffer}
     */
    DatabaseParameterBuffer createDatabaseParameterBuffer();

    /**
     * Create a new {@link BlobParameterBuffer} instance for setting blob
     * parameters in the current GDS implementation.
     * 
     * @return A new {@link BlobParameterBuffer}
     */
    BlobParameterBuffer createBlobParameterBuffer();

    /**
     * Create new {@link TransactionParameterBuffer} instance for setting
     * transaction parameters in the current GDS implementation.
     * 
     * @return A new {@link TransactionParameterBuffer} .
     */
    TransactionParameterBuffer newTransactionParameterBuffer();

    /**
     * Close this GDS instance.
     */
    void close();

    // -------------------- Database functions -----------------------

    /**
     * Create a database based on the supplied filename and database parameters.
     * The supplied <code>IscDbHandle</code> is attached to the newly
     * created database.
     * 
     * @param fileName
     *            The filename for the new database, including host and port.
     *            The expected format is host:port:filepath. The value for host
     *            is localhost if not supplied. The value for port is 3050 if
     *            not supplied.
     * @param dbHandle
     *            The handle to attach to the new database
     * @param databaseParameterBuffer
     *            The parameters for the new database and attachment to it
     * @throws GDSException
     *             if an error occurs while creating the database
     */
    void iscCreateDatabase(String fileName, IscDbHandle dbHandle,
            DatabaseParameterBuffer databaseParameterBuffer)
            throws GDSException;

    /**
     * Attach to an existing database via a filename.
     * 
     * 
     * @param fileName
     *            The filename for the database, including host and port. The
     *            expected format is
     *            <code>&lt;host name&gt;/&lt;port&gt;:&lt;file path&gt;</code>.
     *            The value for host is localhost if not supplied. The value for
     *            port is 3050 if not supplied.
     * @param dbHandle
     *            The handle to attach to the database
     * @param databaseParameterBuffer
     *            parameters for the database attachment
     * @throws GDSException
     *             if an error occurs while attaching to the database
     */
    void iscAttachDatabase(String fileName, IscDbHandle dbHandle,
            DatabaseParameterBuffer databaseParameterBuffer)
            throws GDSException;

    /**
     * Get information about the database to which {@link IscDbHandle} is
     * attached. The requested parameters are values set in <code>items</code>,
     * and the values in the returned byte-array correspond to the requested
     * parameters in <code>items</code>
     * 
     * @param dbHandle
     *            Handle to the database for which info is to be retrieved
     * @param items
     *            An array of values from the <code>isc_info_*</code> constant
     *            fields from {@link ISCConstants}
     * @param bufferLength
     *            The size of the byte array that is to be returned
     * @return array of bytes whose values correspond the requested parameters
     *         in <code>items</code>
     * @throws GDSException
     *             if an error occurs while retrieving database info
     */
    byte[] iscDatabaseInfo(IscDbHandle dbHandle, byte[] items, int bufferLength)
            throws GDSException;

    /**
     * Detach the given database handle from its database. This effectively
     * closes the connection to the database.
     * 
     * @param dbHandle
     *            The handle to be detached
     * @throws GDSException
     *             if an error occurs while detaching from the database
     */
    void iscDetachDatabase(IscDbHandle dbHandle) throws GDSException;

    /**
     * Drop (delete) the database to which <code>IscDbHandle</code> is attached.
     * 
     * @param dbHandle
     *            Handle to the database to be dropped
     * @throws GDSException
     *             if an error occurs while dropping the database
     */
    void iscDropDatabase(IscDbHandle dbHandle) throws GDSException;

    // ------------------ Transactions -------------------------
    /**
     * Start a transaction based on a handle to a transaction.
     * 
     * @param trHandle
     *            Handle to the transaction that is to be started
     * @param dbHandle
     *            Handle to the database in which the transaction is to be
     *            started
     * @param tpb
     *            Transaction Parameter Block in the form of a byte array,
     *            contains parameter data for the transaction attributes
     * @throws GDSException
     *             if an error occurs while starting the transaction
     * @see #createIscTrHandle()
     */
    void iscStartTransaction(IscTrHandle trHandle, IscDbHandle dbHandle,
            TransactionParameterBuffer tpb) throws GDSException;

    /**
     * Reconnect "in limbo" transaction using new database handle.
     * 
     * @param trHandle
     *            transaction handle that will be reconnected.
     * @param dbHandle
     *            database handle in which "in limbo" transaction will be
     *            reconnected.
     * @param transactionId ID of the transaction to reconnect.
     * 
     * @throws GDSException
     *             if something went wrong.
     */
    void iscReconnectTransaction(IscTrHandle trHandle, IscDbHandle dbHandle,
            long transactionId) throws GDSException;

    /**
     * Commit a transaction.
     * 
     * @param trHandle
     *            Handle to the transaction to be committed.
     * @throws GDSException
     *             if an error occurs while committing the transaction
     * @see #iscRollbackTransaction(IscTrHandle)
     */
    void iscCommitTransaction(IscTrHandle trHandle) throws GDSException;

    void iscCommitRetaining(IscTrHandle trHandle) throws GDSException;

    void iscPrepareTransaction(IscTrHandle trHandle) throws GDSException;

    void iscPrepareTransaction2(IscTrHandle trHandle, byte[] bytes)
            throws GDSException;

    /**
     * Rollback a transaction.
     * 
     * @param trHandle
     *            Handle to the transaction that is to be rolled back
     * @throws GDSException
     *             if an error occurs while rolling back
     * @see #iscCommitTransaction(IscTrHandle)
     */
    void iscRollbackTransaction(IscTrHandle trHandle) throws GDSException;

    void iscRollbackRetaining(IscTrHandle trHandle) throws GDSException;

    byte [] iscTransactionInformation(IscTrHandle trHandle, 
            byte [] requestBuffer, int bufferLen) throws GDSException;

    // ---------------------- Dynamic SQL ------------------------

    /**
     * Allocate a dynamic SQL (DSQL) statement on the database to which
     * <code>IscDbHandle</code> is attached.
     * 
     * @param dbHandle
     *            Handle to the database where the statement is to be allocated
     * @param stmtHandle
     *            Handle to attach to the newly allocated statement
     * @throws GDSException
     *             if an error occurs while allocating the statement
     */
    void iscDsqlAllocateStatement(IscDbHandle dbHandle, IscStmtHandle stmtHandle)
            throws GDSException;

    /**
     * Retrieve data for a statement.
     * 
     * @param stmtHandle
     *            Handle to the statement about which data is to be retrieved
     * @param daVersion
     *            Version of the XSQLDA to be retrieved
     * @return data for the given statement
     * @throws GDSException
     *             if an error occurs while retrieving statement data
     */
    XSQLDA iscDsqlDescribe(IscStmtHandle stmtHandle, int daVersion)
            throws GDSException;

    /**
     * Retrieve data for a bind statement.
     * 
     * @param stmtHandle
     *            Handle to the bind statement about which bind data is to be
     *            retrieved
     * @param daVersion
     *            Version of the XSQLDA to be retrieved
     * @return data for the given bind statement
     * @throws GDSException
     *             if an error occurs while retrieving statement data
     */
    XSQLDA iscDsqlDescribeBind(IscStmtHandle stmtHandle, int daVersion)
            throws GDSException;

    /**
     * Execute a statement with only outgoing data.
     * 
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param stmtHandle
     *            Handle to the statement to be executed
     * @param daVersion
     *            Version of XSQLDA to be used
     * @param xsqlda
     *            Input data for executing the statement
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecute(IscTrHandle trHandle, IscStmtHandle stmtHandle,
            int daVersion, XSQLDA xsqlda) throws GDSException;

    /**
     * Execute a statement with outgoing and incoming data.
     * 
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param stmtHandle
     *            Handle to the statement to be executed
     * @param daVersion
     *            Version of XSQLDA to be used
     * @param inXSQLDA
     *            Data to be sent to the database for the statement
     * @param outXSQLDA
     *            Holder for data to be received from executing the statement
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecute2(IscTrHandle trHandle, IscStmtHandle stmtHandle,
            int daVersion, XSQLDA inXSQLDA, XSQLDA outXSQLDA)
            throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a
     * statement handle. No data is retrieved using this method.
     * 
     * @param dbHandle
     *            Handle to the database where the statement is to be executed
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param statement
     *            SQL command to be executed
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @param xsqlda
     *            Data to be sent to the database for the statement
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecuteImmediate(IscDbHandle dbHandle, IscTrHandle trHandle,
            String statement, int dialect, XSQLDA xsqlda) throws GDSException;

    /**
     * @deprecated use
     *             {@link #iscDsqlExecuteImmediate(IscDbHandle, IscTrHandle, byte[], int, XSQLDA)}
     */
    void iscDsqlExecuteImmediate(IscDbHandle dbHandle, IscTrHandle trHandle,
            String statement, String encoding, int dialect, XSQLDA xsqlda)
            throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a
     * statement handle. No data is retrieved using this method.
     * 
     * @param dbHandle
     *            Handle to the database where the statement is to be executed
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param statement
     *            byte array holding the SQL to be executed
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @param xsqlda
     *            Data to be sent to the database for the statement
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecuteImmediate(IscDbHandle dbHandle, IscTrHandle trHandle,
            byte[] statement, int dialect, XSQLDA xsqlda) throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a
     * statement handle. Data is retrieved using this method.
     * 
     * @param dbHandle
     *            Handle to the database where the statement is to be executed
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param statement
     *            byte array holding the SQL to be executed
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @param inXSQLDA
     *            Data to be sent to the database for the statement
     * @param outXSQLDA
     *            Placeholder for data retrieved from executing the SQL
     *            statement
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecImmed2(IscDbHandle dbHandle, IscTrHandle trHandle,
            String statement, int dialect, XSQLDA inXSQLDA, XSQLDA outXSQLDA)
            throws GDSException;

    /**
     * @deprecated use
     *             {@link #iscDsqlExecImmed2(IscDbHandle, IscTrHandle, byte[], int, XSQLDA, XSQLDA)}
     */
    void iscDsqlExecImmed2(IscDbHandle dbHandle, IscTrHandle trHandle,
            String statement, String encoding, int dialect, XSQLDA inXSQLDA,
            XSQLDA outXSQLDA) throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a
     * statement handle. Output data from executing the statement is stored in
     * outXSQLDA.
     * 
     * @param dbHandle
     *            Handle to the database where the statement is to be executed
     * @param trHandle
     *            Handle to the transaction in which the statement is to be
     *            executed
     * @param statement
     *            byte array holding the SQL to be executed
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @param inXSQLDA
     *            Data to be sent to the database for the statement
     * @param outXSQLDA
     *            Holder for data retrieved from the database
     * @throws GDSException
     *             if an error occurs while executing the statement
     */
    void iscDsqlExecImmed2(IscDbHandle dbHandle, IscTrHandle trHandle,
            byte[] statement, int dialect, XSQLDA inXSQLDA, XSQLDA outXSQLDA)
            throws GDSException;

    /**
     * Retrieve record data from a statement. A maximum of
     * <code>fetchSize</code> records will be fetched.
     * 
     * @param stmt_handle
     *            Handle to the statement for which records are to be fetched
     * @param daVersion
     *            Version of XSQLDA to be used
     * @param xsqlda
     *            Holder for records that are fetched
     * @param fetchSize
     *            The maximum number of records to be fetched
     * @throws GDSException
     *             if an error occurs while fetching the records
     */
    void iscDsqlFetch(IscStmtHandle stmt_handle, int daVersion, XSQLDA xsqlda,
            int fetchSize) throws GDSException;

    /**
     * Free a statement in the database that is pointed to by a valid handle.
     * The statement can be closed or fully deallocated, depending on the value
     * of <code>option</code>. <code>option</code> should be one of
     * {@link ISCConstants#DSQL_drop} or {@link ISCConstants#DSQL_close}.
     * 
     * @param stmtHandle
     *            Handle to the statement to be freed
     * @param option
     *            Option to be used when freeing the statement. If the value is
     *            {@link ISCConstants#DSQL_drop}, the statement will be
     *            deallocated, if the value is {@link ISCConstants#DSQL_close},
     *            the statement will only be closed
     * @throws GDSException If an error occurs freeing the statement
     */
    void iscDsqlFreeStatement(IscStmtHandle stmtHandle, int option)
            throws GDSException;

    /**
     * Prepare a string SQL statement for execution in the database.
     * 
     * @param trHandle
     *            Handle to the transaction in which the SQL statement is to be
     *            prepared
     * @param stmtHandle
     *            Handle to the statement for which the SQL is to be prepared
     * @param statement
     *            The SQL statement to be prepared
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @return A datastructure with data about the prepared statement
     * @throws GDSException
     *             if an error occurs while preparing the SQL
     */
    XSQLDA iscDsqlPrepare(IscTrHandle trHandle, IscStmtHandle stmtHandle,
            String statement, int dialect) throws GDSException;

    /**
     * @deprecated use
     *             {@link #iscDsqlPrepare(IscTrHandle, IscStmtHandle, byte[], int)}
     */
    XSQLDA iscDsqlPrepare(IscTrHandle trHandle, IscStmtHandle stmtHandle,
            String statement, String encoding, int dialect) throws GDSException;

    /**
     * Prepare a string SQL statement for execution in the database.
     * 
     * @param trHandle
     *            Handle to the transaction in which the SQL statement is to be
     *            prepared
     * @param stmtHandle
     *            Handle to the statement for which the SQL is to be prepared
     * @param statement
     *            byte-array with containing the SQL to be prepared
     * @param dialect
     *            Interbase dialect for the SQL, should be one of the
     *            <code>SQL_DIALECT_*</code> constants from
     *            {@link ISCConstants}
     * @return A datastructure with data about the prepared statement
     * @throws GDSException
     *             if an error occurs while preparing the SQL
     */
    XSQLDA iscDsqlPrepare(IscTrHandle trHandle, IscStmtHandle stmtHandle,
            byte[] statement, int dialect) throws GDSException;

    /**
     * Set the name to be used for a given statement.
     * 
     * @param stmtHandle
     *            Handle to the statement for which the cursor name is to be set
     * @param cursorName
     *            Name to set for the cursor
     * @param type
     *            Reserved for future use
     * @throws GDSException
     *             if an error occurs while setting the cursor name
     */
    void iscDsqlSetCursorName(IscStmtHandle stmtHandle, String cursorName,
            int type) throws GDSException;

    /**
     * Retrieve data about a statement. The parameters that are requested are
     * defined by the <code>isc_info_sql_*</code> constants defined in
     * {@link ISCConstants}. An array with corresponding values for the
     * requested parameters is returned.
     * 
     * @param stmtHandle
     *            Handle to the statement about which data is to be retrieved
     * @param items
     *            Array of parameters whose values are to be retrieved
     * @param bufferLength
     *            The length of the byte-array to be returned
     * @return An array of values corresponding to the requested parameters
     * @throws GDSException
     *             if an error occurs while retrieving the statement info
     */
    byte[] iscDsqlSqlInfo(IscStmtHandle stmtHandle, byte[] items,
            int bufferLength) throws GDSException;

    /**
     * Fetch count information for a statement. The count information that is
     * retrieved includes counts for all CRUD operations, and is set in the
     * handle itself.
     * 
     * @param stmt
     *            Handle to the statement for which count data is to be
     *            retrieved
     * @throws GDSException
     *             if an error occurs while retrieving the count data
     */
    void getSqlCounts(IscStmtHandle stmt) throws GDSException;

    /**
     * Retrieve an integer value from a sequence of bytes.
     * 
     * @param buffer
     *            The byte array from which the integer is to be retrieved
     * @param pos
     *            The offset starting position from which to start retrieving
     *            byte values
     * @param length
     *            The number of bytes to use in retrieving the integer value.
     * @return The integer value retrieved from the bytes
     */
    int iscVaxInteger(byte[] buffer, int pos, int length);

    // -----------------------------------------------
    // Blob methods
    // -----------------------------------------------

    /**
     * Create a new blob within a given transaction.
     * 
     * @param db
     *            Handle to the database in which the blob will be created
     * @param tr
     *            Handle to the transaction in which the blob will be created
     * @param blob
     *            Handle to be attached to the newly created blob
     * @param blobParameterBuffer
     *            contains parameters for creation of the new blob, can be null
     * @throws GDSException
     *             if an error occurs while creating the blob
     */
    void iscCreateBlob2(IscDbHandle db, IscTrHandle tr, IscBlobHandle blob,
            BlobParameterBuffer blobParameterBuffer) throws GDSException;

    /**
     * Open a blob within a given transaction.
     * 
     * @param db
     *            Handle to the database in which the blob will be opened
     * @param tr
     *            Handle to the transaction in which the blob will be opened
     * @param blob
     *            Handle to the blob to be opened
     * @param blobParameterBuffer
     *            Contains parameters for the blob
     * @throws GDSException
     *             if an error occurs while opening the blob
     */
    void iscOpenBlob2(IscDbHandle db, IscTrHandle tr, IscBlobHandle blob,
            BlobParameterBuffer blobParameterBuffer) throws GDSException;

    /**
     * Fetch a segment of a blob.
     * 
     * @param blob
     *            Handle to the blob from which a segment is to be fetched
     * @param maxread
     *            The maximum number of bytes to attempt to fetch
     * @return A segment of data from the blob, with maximum length of
     *         <code>maxread</code>
     * @throws GDSException
     *             if an error occurs while fetching the blob segment
     */
    byte[] iscGetSegment(IscBlobHandle blob, int maxread) throws GDSException;

    /**
     * Write a segment of data to a blob.
     * 
     * @param blob_handle
     *            Handle to the blob to which data is to be written
     * @param buffer
     *            Data to be written to the blob
     * @throws GDSException
     *             if an error occurs while writing to the blob
     */
    void iscPutSegment(IscBlobHandle blob_handle, byte[] buffer)
            throws GDSException;

    /**
     * Close an open blob.
     * 
     * @param blob
     *            Handle to the blob to be closed
     * @throws GDSException
     *             if an error occurs while closing the blob
     */
    void iscCloseBlob(IscBlobHandle blob) throws GDSException;

    /**
     * Retrieve data about an existing blob. The parameters to be retrieved are
     * placed in <code>items</code>, and the corresponding values are
     * returned. The values in <code>items</code> should be
     * <code>isc_info_blob_*</code> constants from {@link ISCConstants}.
     * 
     * @param handle
     *            Handle to the blob for which data is to be retrieved
     * @param items
     *            Parameters to be fetched about the blob
     * @param bufferLength
     *            Length of the byte array to be returned
     * @return Data corresponding to the parameters requested in
     *         <code>items</code>
     * @throws GDSException
     *             if an error occurs while fetching data about the blob
     */
    byte[] iscBlobInfo(IscBlobHandle handle, byte[] items, int bufferLength)
            throws GDSException;

    /**
     * Seek to a given position in a blob. <code>seekMode</code> is used in
     * the same way as the system fseek call, i.e.:
     * <ul>
     * <li>0 - seek relative to the start of the blob
     * <li>1 - seek relative to the current position in the blob
     * <li>2 - seek relative to the end of the blob
     * </ul>
     * <p>
     * <code>position</code> is the offset number of bytes to seek to,
     * relative to the position described by <code>seekMode</code>. Seeking
     * can only be done in a forward direction.
     * 
     * @param handle
     *            Handle to the blob for which seeking will be done
     * @param position
     *            The offset number of bytes to seek to
     * @param seekMode
     *            Describes the base point to be used in seeking, should be
     *            negative if <code>seekMode</code> is equal to 2
     * @throws GDSException
     *             if an error occurs while seeking
     */
    void iscSeekBlob(IscBlobHandle handle, int position, int seekMode)
            throws GDSException;

    // -----------------------------------------------
    // Services API methods
    // -----------------------------------------------

    /**
     * Attach to a Service Manager.
     * 
     * @param service
     *            The name/path to the service manager
     * @param serviceHandle
     *            Handle to be linked to the attached service manager
     * @param serviceParameterBuffer
     *            Contains parameters for attaching to the service manager
     * @throws GDSException
     *             if an error occurs while attaching
     */
    void iscServiceAttach(String service, IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer) throws GDSException;

    /**
     * Detach from a Service Manager.
     * 
     * @param serviceHandle
     *            Handle to the service manager that is to be detached
     * @throws GDSException
     *             if an error occurs while detaching
     */
    void iscServiceDetach(IscSvcHandle serviceHandle) throws GDSException;

    /**
     * Start a service operation.
     * 
     * @param serviceHandle
     *            Handle to the service manager where the operation is to be
     *            started
     * @param serviceRequestBuffer
     *            parameters about the service to be started
     */
    void iscServiceStart(IscSvcHandle serviceHandle,
            ServiceRequestBuffer serviceRequestBuffer) throws GDSException;

    /**
     * Query a service manager
     * 
     * @param serviceHandle
     *            Handle to the service manager to be queried
     * @param serviceParameterBuffer
     *            parameters about the service
     * @param serviceRequestBuffer
     *            parameters requested in the query
     * @param resultBuffer
     *            buffer to hold the query results
     * @throws GDSException
     *             if an error occurs while querying
     */
    void iscServiceQuery(IscSvcHandle serviceHandle,
            ServiceParameterBuffer serviceParameterBuffer,
            ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer)
            throws GDSException;

    /**
     * Queue an EventHandler.
     *
     * @param dbHandle 
     *              Handle to the database where events are to be listened for
     * @param eventHandle 
     *              Handle for event management
     * @param eventHandler 
     *              Callback to be called when an event occurs
     * @throws GDSException
     *              If a database communication error occurs
     */
    int iscQueueEvents(IscDbHandle dbHandle, EventHandle eventHandle,
            EventHandler eventHandler) throws GDSException; 

    /**
     * Initialize the internal databastructures for an 
     * {@link EventHandle}.
     *
     * @param eventHandle 
     *              The event handle to be initialized
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscEventBlock(EventHandle eventHandle) 
            throws GDSException; 

    /**
     * Retrieve count information after an event has occurred.
     *
     * @param eventHandle 
     *              The handle containing event data
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscEventCounts(EventHandle eventHandle)
            throws GDSException;

    /**
     * Cancel event listening based on an {@link EventHandle}.
     *
     * @param dbHandle
     *              Handle to the database where events are being 
     *              listened for
     * @param eventHandle
     *              Datastructure for handling event data
     * @throws GDSException
     *              If a database communication error occurs
     */
    void iscCancelEvents(IscDbHandle dbHandle, EventHandle eventHandle)
            throws GDSException;

    /**
     * Create a new {@link EventHandle} specific to a given 
     * <code>GDS</code> implementation.
     *
     * @return The newly created {@link EventHandle}
     */
    EventHandle createEventHandle(String eventName);
    
    /**
     * Cancel the currently running operation on the server 
     * 
     * @param dbHandle Handle to the database operation of which should be
     * cancelled.
     * 
     * @param kind one of {@link ISCConstants#fb_cancel_disable},
     * {@link ISCConstants#fb_cancel_enable}, {@link ISCConstants#fb_cancel_raise}
     * or {@link ISCConstants#fb_cancel_abort} 
     * 
     * @throws GDSException If a database communication error happens.
     */
    void fbCancelOperation(IscDbHandle dbHandle, int kind) 
        throws GDSException;
    
    /**
     * Get type of this instance.
     * 
     * @return instance of {@link GDSType}.
     */
    GDSType getType();
}
