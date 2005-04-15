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
 */

package org.firebirdsql.gds;

/**
 * The interface <code>GDS</code> has most of the C client interface functions
 * lightly mapped to java, as well as the constants returned from the server.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public interface GDS {
    
    /**
     * Create a new <code>ServiceParameterBuffer</code> instance for setting
     * service parameters in the current GDS implementation.
     *
     * @return a new <code>ServiceParameterBuffer</code>
     */
    ServiceParameterBuffer  newServiceParameterBuffer();

    /**
     * Create a new <code>ServiceRequestBuffer</code> instance for setting
     * service request parameters in the current GDS implementation. The 
     * returned <code>ServiceRequestBuffer</code> is linked to a specific
     * Services API task, and must be one of the <code>isc_info_svc_*</code> or
     * <code>isc_action_svc</code> constants from {@link ISCConstants}.
     *
     * @param taskIdentifier The specific Services API task for which the 
     *        <code>ServiceRequestBuffer</code> is created
     * @return A new <code>ServiceRequestBuffer</code>
     */
    ServiceRequestBuffer    newServiceRequestBuffer(int taskIdentifier);

    /**
     * Create a new <code>DatabaseParameterBuffer</code> instance for setting
     * database parameters in the current GDS implementation.
     *
     * @return A new <code>DatabaseParameterBuffer</code>
     */
    DatabaseParameterBuffer newDatabaseParameterBuffer();

    /**
     * Create a new <code>BlobParameterBuffer</code> instance for setting
     * blob parameters in the current GDS implementation.
     *
     * @return A new <code>BlobParameterBuffer</code>
     */
    BlobParameterBuffer     newBlobParameterBuffer();

    /**
     * Create new <code>TransactionParameterBuffer</code> instance for setting
     * transaction parameters in the current GDS implementation.
     * 
     * @return A new <code>TransactionParameterBuffer</code>.
     */
    TransactionParameterBuffer newTransactionParameterBuffer();

    // --------------------  Database functions -----------------------

    /**
     * Create a database based on the supplied filename and database 
     * parameters. The supplied <code>isc_db_handle</code> is attached to the
     * newly created database.
     *
     * @param file_name The filename for the new database, including host
     *        and port. The expected format is host:port:filepath. The value
     *        for host is localhost if not supplied. The value for port is
     *        3050 if not supplied.
     * @param db_handle The handle to attach to the new database
     * @param databaseParameterBuffer The parameters for the new database and
     *        attachment to it
     * @throws GDSException if an error occurs while creating the database
     */
    void isc_create_database(String file_name,
                            isc_db_handle db_handle,
                            DatabaseParameterBuffer databaseParameterBuffer ) throws GDSException;

    /**
     * Attach to an existing database via a filename.
     *
     *
     * @param file_name The filename for the database, including host and port.
     *        The expected format is host:port:filepath. The value for host is 
     *        localhost if not supplied. The value for port is 3050 if not 
     *        supplied.
     * @param db_handle The handle to attach to the database
     * @param databaseParameterBuffer parameters for the database attachment
     * @throws GDSException if an error occurs while attaching to the database
     */
    void isc_attach_database(String file_name,
                            isc_db_handle db_handle,
                            DatabaseParameterBuffer databaseParameterBuffer) throws GDSException;

    /**
     * Get information about the database to which <code>db_handle</code> is
     * attached. The requested parameters are values set in <code>items</code>,
     * and the values in the returned byte-array correspond to the 
     * requested parameters in <code>items</code>
     *
     * @param db_handle Handle to the database for which info is to 
     *        be retrieved
     * @param items An array of values from the <code>isc_info_*</code> 
     *        constant fields from {@link ISCConstants}
     * @param buffer_length The size of the byte array that is to be returned
     * @return array of bytes whose values correspond the requested 
     *         parameters in <code>items</code>
     * @throws GDSException if an error occurs while retrieving database info
     */
    byte[] isc_database_info(isc_db_handle db_handle,
                            byte[] items,
                            int buffer_length) throws GDSException;

    /**
     * Detach the given database handle from its database. This effectively
     * closes the connection to the database.
     *
     * @param db_handle The handle to be detached
     * @throws GDSException if an error occurs while detaching from 
     *         the database
     */
    void isc_detach_database(isc_db_handle db_handle) throws GDSException;

    /**
     * Drop (delete) the database to which <code>db_handle</code> is attached.
     *
     * @param db_handle Handle to the database to be dropped
     * @throws GDSException if an error occurs while dropping the database
     */
    void isc_drop_database(isc_db_handle db_handle) throws GDSException;

    /**
     * Expand the given database parameter buffer.
     *
     * @deprecated This method had been deprecated on the Firebird side
     */
    byte[] isc_expand_dpb(byte[] dpb, int dpb_length,
                          int param, Object[] params) throws GDSException;


    // ------------------ Transactions -------------------------

    /**
     * Start a transaction based on a handle to a transaction. 
     *
     * @param tr_handle Handle to the transaction that is to be started
     * @param db_handle Handle to the database in which the transaction
     *        is to be started
     * @param tpb Transaction Parameter Block in the form of a byte array, 
     *        contains parameter data for the transaction attributes
     * @throws GDSException if an error occurs while starting the transaction
     * @see get_new_isc_tr_handle
     */
    void isc_start_transaction(isc_tr_handle tr_handle, isc_db_handle db_handle,
                                TransactionParameterBuffer tpb) throws GDSException;


    /**
     * Reconnect "in limbo" transaction using new database handle.
     * 
     * @param tr_handle transaction handle that will be reconnected.
     * @param db_handle database handle in which "in limbo" transaction will
     * be reconnected.
     * @param message message that was passed in {@link #isc_prepare_transaction2(isc_tr_handle, byte[])}
     * method call.
     * 
     * @throws GDSException if something went wrong.
     */
    void isc_reconnect_transaction(isc_tr_handle tr_handle,
            isc_db_handle db_handle, long transactionId) throws GDSException;

    /**
     * Commit a transaction.
     *
     * @param tr_handle Handle to the transaction to be committed.
     * @throws GDSException if an error occurs while committing the transaction
     * @see isc_rollback_transaction
     */
    void isc_commit_transaction(    isc_tr_handle tr_handle) throws GDSException;

    void isc_commit_retaining(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_prepare_transaction2(isc_tr_handle tr_handle,
                                   byte[] bytes) throws GDSException;

    /**
     * Rollback a transaction.
     *
     * @param tr_handle Handle to the transaction that is to be rolled back
     * @throws GDSException if an error occurs while rolling back
     * @see isc_commit_transaction
     */
    void isc_rollback_transaction(isc_tr_handle tr_handle) throws GDSException;

    void isc_rollback_retaining(isc_tr_handle tr_handle) throws GDSException;


    // ---------------------- Dynamic SQL ------------------------

    /**
     * Allocate a dynamic SQL (DSQL) statement on the database to which
     * <code>db_handle</code> is attached.
     *
     * @param db_handle Handle to the database where the statement is to
     *        be allocated
     * @param stmt_handle Handle to attach to the newly allocated statement
     * @throws GDSException if an error occurs while allocating the statement
     */
    void isc_dsql_allocate_statement(isc_db_handle db_handle,
                                       isc_stmt_handle stmt_handle) throws GDSException;

    void isc_dsql_alloc_statement2(isc_db_handle db_handle,
                                     isc_stmt_handle stmt_handle) throws GDSException;

    /**
     * Retrieve data for a statement.
     *
     * @param stmt_handle Handle to the statement about which data is
     *        to be retrieved
     * @param da_version Version of the XSQLDA to be retrieved
     * @return data for the given statement
     * @throws GDSException if an error occurs while retrieving statement data
     */
    XSQLDA isc_dsql_describe(isc_stmt_handle stmt_handle,
                            int da_version) throws GDSException;

    /**
     * Retrieve data for a bind statement.
     *
     * @param stmt_handle Handle to the bind statement about which bind data
     *        is to be retrieved
     * @param da_version Version of the XSQLDA to be retrieved
     * @return data for the given bind statement
     * @throws GDSException if an error occurs while retrieving statement data
     */
    XSQLDA isc_dsql_describe_bind(isc_stmt_handle stmt_handle,
                                  int da_version) throws GDSException;

    /**
     * Execute a statement with only outgoing data.
     *
     * @param tr_handle Handle to the transaction in which the statement is to 
     *        be executed
     * @param stmt_handle Handle to the statement to be executed
     * @param da_version Version of XSQLDA to be used
     * @param xsqlda Input data for executing the statement
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_execute(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           int da_version,
                           XSQLDA xsqlda) throws GDSException;

    /**
     * Execute a statement with outgoing and incoming data.
     *
     * @param tr_handle Handle to the transaction in which the statement is to 
     *        be executed
     * @param stmt_handle Handle to the statement to be executed
     * @param da_version Version of XSQLDA to be used
     * @param in_sqlda Data to be sent to the database for the statement
     * @param out_xsqlda Holder for data to be received from executing the 
     *        statement
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_execute2(isc_tr_handle tr_handle,
                            isc_stmt_handle stmt_handle,
                            int da_version,
                            XSQLDA in_xsqlda,
                            XSQLDA out_xsqlda) throws GDSException;

   
    /**
     * Execute a string SQL statement directly, without first allocating
     * a statement handle. No data is retrieved using this method.
     *
     * @param db_handle Handle to the database where the statement is 
     *        to be executed
     * @param tr_handle Handle to the transaction in which the
     *        statement is to be executed
     * @param statement SQL command to be executed
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @param xsqlda Data to be sent to the database for the statement
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;
    
    /**
     * @deprecated use {@link #isc_dsql_execute_immediate(isc_db_handle, isc_tr_handle, byte[], int, XSQLDA)
     */
    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                      isc_tr_handle tr_handle,
                                      String statement,
                                      String encoding,
                                      int dialect,
                                      XSQLDA xsqlda) throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a 
     * statement handle. No data is retrieved using this method.
     *
     * @param db_handle Handle to the database where the statement is
     *        to be executed
     * @param tr_handle Handle to the transaction in which the
     *        statement is to be executed
     * @param statement byte array holding the SQL to be executed
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @param xsqlda Data to be sent to the database for the statement
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_execute_immediate(isc_db_handle db_handle,
                                    isc_tr_handle tr_handle,
                                    byte[] statement,
                                    int dialect,
                                    XSQLDA xsqlda) throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating a 
     * statement handle. Data is retrieved using this method.
     *
     * @param db_handle Handle to the database where the statement is
     *        to be executed
     * @param tr_handle Handle to the transaction in which the
     *        statement is to be executed
     * @param statement byte array holding the SQL to be executed
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @param in_xsqlda Data to be sent to the database for the statement
     * @param out_xsqlda Placeholder for data retrieved from executing the 
     *        SQL statement
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;
                               
    /**
     * @deprecated use {@link #isc_dsql_exec_immed2(isc_db_handle, isc_tr_handle, byte[], int, XSQLDA)
     */
    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                               isc_tr_handle tr_handle,
                               String statement,
                               String encoding,
                               int dialect,
                               XSQLDA in_xsqlda,
                               XSQLDA out_xsqlda) throws GDSException;

    /**
     * Execute a string SQL statement directly, without first allocating
     * a statement handle. Output data from executing the statement is stored
     * in out_xsqlda.
     *
     * @param db_handle Handle to the database where the statement is
     *        to be executed
     * @param tr_handle Handle to the transaction in which the
     *        statement is to be executed
     * @param statement byte array holding the SQL to be executed
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @param in_xsqlda Data to be sent to the database for the statement
     * @param out_xsqlda Holder for data retrieved from the database
     * @throws GDSException if an error occurs while executing the statement
     */
    void isc_dsql_exec_immed2(isc_db_handle db_handle,
                              isc_tr_handle tr_handle,
                              byte[] statement,
                              int dialect,
                              XSQLDA in_xsqlda,
                              XSQLDA out_xsqlda) throws GDSException;

    /**
     * Retrieve record data from a statement. A maximum of 
     * <code>fetchSize</code> records will be fetched.
     *
     * @param stmt_handle Handle to the statement for which records are
     *        to be fetched
     * @param da_version Version of XSQLDA to be used
     * @param xsqlda Holder for records that are fetched
     * @param fetchSize The maximum number of records to be fetched
     * @throws GDSException if an error occurs while fetching the records
     */
    void isc_dsql_fetch(isc_stmt_handle stmt_handle,
                         int da_version,
                         XSQLDA xsqlda, int fetchSize) throws GDSException;

    /**
     * Free a statement in the database that is pointed to by a valid handle.
     * The statement can be closed or fully deallocated, depending on the 
     * value of <code>option</code>. <code>option</code> should be one of
     * {@link ISCConstants#DSQL_drop} or {@link ISCConstants#DSQL_close}.
     *
     * @param stmt_handle Handle to the statement to be freed
     * @param option Option to be used when freeing the statement. If the value
     *        is {@link ISCConstants#DSQL_drop}, the statement will be
     *        deallocated, if the value is {@link ISCConstants#DSQL_close},
     *        the statement will only be closed
     */
    void isc_dsql_free_statement(isc_stmt_handle stmt_handle,
                                   int option) throws GDSException;

    /**
     * Prepare a string SQL statement for execution in the database.
     *
     * @param tr_handle Handle to the transaction in which the SQL statement is
     *        to be prepared
     * @param stmt_handle Handle to the statement for which the SQL is
     *        to be prepared
     * @param statement The SQL statement to be prepared
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @return A datastructure with data about the prepared statement
     * @throws GDSException if an error occurs while preparing the SQL
     */
    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           int dialect) throws GDSException;
                           
    /**
     * @deprecated use {@link #isc_dsql_prepare(isc_tr_handle, isc_stmt_handle, byte[], int)
     */
    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                           isc_stmt_handle stmt_handle,
                           String statement,
                           String encoding,
                           int dialect) throws GDSException;

    /**
     * Prepare a string SQL statement for execution in the database.
     *
     * @param tr_handle Handle to the transaction in which the SQL statement
     *        is to be prepared
     * @param stmt_handle Handle to the statement for which the SQL is
     *        to be prepared
     * @param statement byte-array with containing the SQL to be prepared
     * @param dialect Interbase dialect for the SQL, should be one of the 
     *        <code>SQL_DIALECT_*</code> constants from {@link ISCConstants}
     * @return A datastructure with data about the prepared statement
     * @throws GDSException if an error occurs while preparing the SQL
     */
    XSQLDA isc_dsql_prepare(isc_tr_handle tr_handle,
                            isc_stmt_handle stmt_handle,
                            byte[] statement,
                            int dialect) throws GDSException;
    
    /**
     * Set the name to be used for a given statement.
     *
     * @param stmt_handle Handle to the statement for which the cursor 
     *        name is to be set
     * @param cursor_name Name to set for the cursor
     * @param type Reserved for future use
     * @throws GDSException if an error occurs while setting the cursor name
     */
    void isc_dsql_set_cursor_name(isc_stmt_handle stmt_handle,
                                    String cursor_name,
                                    int type) throws GDSException;


    /**
     * Retrieve data about a statement. The parameters that are requested
     * are defined by the <code>isc_info_sql_*</code> constants defined
     * in {@link ISCConstants}. An array with corresponding values for the
     * requested parameters is returned.
     *
     * @param stmt_handle Handle to the statement about which data is 
     *        to be retrieved
     * @param items Array of parameters whose values are to be retrieved 
     * @param buffer_length The length of the byte-array to be returned
     * @return An array of values corresponding to the requested parameters
     * @throws GDSException if an error occurs while retrieving the 
     *         statement info
     */
    byte[] isc_dsql_sql_info(isc_stmt_handle stmt_handle,
                            /* int item_length, */
                            byte[] items,
                            int buffer_length) throws GDSException;

    /**
     * Fetch count information for a statement. The count information that is
     * retrieved includes counts for all CRUD operations, and is set in
     * the handle itself.
     *
     * @param stmt Handle to the statement for which count data is to 
     *        be retrieved
     * @throws GDSException if an error occurs while retrieving the count data
     */
    void getSqlCounts(isc_stmt_handle stmt) throws GDSException;

    /**
     * Retrieve an integer value from a sequence of bytes.
     *
     * @param buffer The byte array from which the integer is to be retrieved
     * @param pos The offset starting position from which to start retrieving
     *        byte values
     * @param length The number of bytes to use in retrieving the integer
     *        value.
     * @return The integer value retrieved from the bytes
     */
    int isc_vax_integer(byte[] buffer, int pos, int length);


    //-----------------------------------------------
    //Blob methods
    //-----------------------------------------------

    /**
     * Create a new blob within a given transaction.
     *
     * @param db Handle to the database in which the blob will be created
     * @param tr Handle to the transaction in which the blob will be created
     * @param blob Handle to be attached to the newly created blob
     * @param blobParameterBuffer contains parameters for creation of the
     *        new blob, can be null
     * @throws GDSException if an error occurs while creating the blob
     */
    void isc_create_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        BlobParameterBuffer blobParameterBuffer) throws GDSException;

    /**
     * Open a blob within a given transaction.
     *
     * @param db Handle to the database in which the blob will be opened
     * @param tr Handle to the transaction in which the blob will be opened
     * @param blob Handle to the blob to be opened
     * @param blobParameterBuffer Contains parameters for the blob
     * @throws GDSException if an error occurs while opening the blob
     */
    void isc_open_blob2(isc_db_handle db,
                        isc_tr_handle tr,
                        isc_blob_handle blob,
                        BlobParameterBuffer blobParameterBuffer) throws GDSException;

    /**
     * Fetch a segment of a blob.
     *
     * @param blob Handle to the blob from which a segment is to be fetched
     * @param maxread The maximum number of bytes to attempt to fetch
     * @return A segment of data from the blob, with maximum length 
     *         of <code>maxread</code>
     * @throws GDSException if an error occurs while fetching the blob segment
     */
    byte[] isc_get_segment(isc_blob_handle blob,
                           int maxread) throws GDSException;

    /**
     * Write a segment of data to a blob.
     *
     * @param blob_handle Handle to the blob to which data is to be written
     * @param buffer Data to be written to the blob
     * @throws GDSException if an error occurs while writing to the blob
     */
    void isc_put_segment(isc_blob_handle blob_handle,
             byte[] buffer) throws GDSException;

    /**
     * Close an open blob.
     *
     * @param blob Handle to the blob to be closed
     * @throws GDSException if an error occurs while closing the blob
     */
    void isc_close_blob(isc_blob_handle blob) throws GDSException;
    
    
    /**
     * Retrieve data about an existing blob. The parameters to be retrieved 
     * are placed in <code>items</code>, and the corresponding values are 
     * returned. The values in <code>items</code> should be 
     * <code>isc_info_blob_*</code> constants from {@link ISCConstants}.
     *
     * @param handle Handle to the blob for which data is to be retrieved
     * @param items Parameters to be fetched about the blob
     * @param buffer_length Length of the byte array to be returned
     * @return Data corresponding to the parameters requested 
     *         in <code>items</code>
     * @throws GDSException if an error occurs while fetching data 
     *         about the blob
     */
    byte[] isc_blob_info(isc_blob_handle handle, byte[] items, int buffer_length) 
        throws GDSException;
        

    /**
     * Seek to a given position in a blob. <code>seekMode</code> is used 
     * in the same way as the system fseek call, i.e.:
     * <ul>
     *      <li>0 - seek relative to the start of the blob
     *      <li>1 - seek relative to the current position in the blob
     *      <li>2 - seek relative to the end of the blob
     * </ul>
     * <p>
     * <code>position</code> is the offset number of bytes to seek to, 
     * relative to the position described by <code>seekMode</code>. Seeking 
     * can only be done in a forward direction.
     *
     * @param handle Handle to the blob for which seeking will be done
     * @param position The offset number of bytes to seek to
     * @param seekMode Describes the base point to be used in seeking, should
     *        be negative if <code>seekMode</code> is equal to 2
     * @throws GDSException if an error occurs while seeking
     */
    void isc_seek_blob(isc_blob_handle handle, int position, int seekMode) 
        throws GDSException;


    //-----------------------------------------------
    //Services API methods
    //-----------------------------------------------

    /**
     * Attach to a Service Manager.
     *
     * @param service The name/path to the service manager
     * @param serviceHandle Handle to be linked to the attached service manager
     * @param serviceParameterBuffer Contains parameters for attaching to the
     *        service manager
     * @throws GDSException if an error occurs while attaching
     */
    void isc_service_attach(String service, isc_svc_handle serviceHandle, ServiceParameterBuffer serviceParameterBuffer ) throws GDSException;

    /**
     * Detach from a Service Manager.
     *
     * @param serviceHandle Handle to the service manager that is to be detached
     * @throws GDSException if an error occurs while detaching
     */
    void isc_service_detach(isc_svc_handle serviceHandle) throws GDSException;

    /**
     * Start a service operation.
     *
     * @param serviceHandle Handle to the service manager where the operation
     *        is to be started
     * @param serviceRequestBuffer parameters about the service to be started
     */
    void isc_service_start(isc_svc_handle serviceHandle, ServiceRequestBuffer serviceRequestBuffer) throws GDSException;

    /**
     * Query a service manager
     *
     * @param serviceHandle Handle to the service manager to be queried
     * @param serviceParameterBuffer parameters about the service
     * @param serviceRequestBuffer parameters requested in the query
     * @param resultBuffer buffer to hold the query results
     * @throws GDSException if an error occurs while querying
     */
    void isc_service_query(isc_svc_handle serviceHandle, ServiceParameterBuffer serviceParameterBuffer, ServiceRequestBuffer serviceRequestBuffer, byte[] resultBuffer) throws GDSException;



    // Handle declaration methods

    /**
     * Factory method to create a new <code>isc_db_handle</code> instance that 
     * is linked to the current <code>GDS</code> implementation.
     *
     * @return A new <code>isc_db_handle</code> instance
     */
    isc_db_handle get_new_isc_db_handle();

    /**
     * Factory method to create a new <code>isc_tr_handle</code> instance that
     * is linked to the current <code>GDS</code> implementation.
     *
     * @return A new <code>isc_tr_handle</code> instance
     */
    isc_tr_handle get_new_isc_tr_handle();

    /**
     * Factory method to create a new <code>isc_stmt_handle</code> instance
     * that is linked to the current <code>GDS</code> implementation.
     *
     * @return A new <code>isc_stmt_handle</code> instance
     */
    isc_stmt_handle get_new_isc_stmt_handle();

    /**
     * Factory method to create a new <code>isc_blob_handle</code> instance
     * that is linked to the current <code>GDS</code> implementation.
     *
     * @return A new <code>isc_blob_handle</code> instance
     */
    isc_blob_handle get_new_isc_blob_handle();

    /**
     * Factory method to create a new <code>isc_svc_handle</code> instance
     * that is linked to the current <code>GDS</code> implemenation.
     *
     * @return A new <code>isc_svc_handle</code> instance
     */
    isc_svc_handle get_new_isc_svc_handle();

    /**
     * Close this GDS instance.
     */
    void close();

}

