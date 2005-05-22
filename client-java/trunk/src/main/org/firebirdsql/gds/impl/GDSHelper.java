/*
 * Firebird Open Source J2ee connector - jdbc driver
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

package org.firebirdsql.gds.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.GDSException;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.XSQLDA;
import org.firebirdsql.gds.IscBlobHandle;
import org.firebirdsql.gds.IscDbHandle;
import org.firebirdsql.gds.IscTrHandle;
import org.firebirdsql.jdbc.FBConnectionDefaults;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 * Helper class for all GDS-related operations.
 */
public class GDSHelper {

    private final Logger log = LoggerFactory.getLogger(getClass(), false);

    private GDS gds;
    private AbstractIscDbHandle currentDbHandle;
    private AbstractIscTrHandle currentTr;
    private int timeout = 0;
    /**
     * Needed from mcf when killing a db handle when a new tx cannot be started.
     */
    protected DatabaseParameterBuffer dpb;
    private boolean autoCommit = true;
    
    private boolean registerResultSets;
    
    /**
     * 
     */
    public GDSHelper(GDS gds, DatabaseParameterBuffer dpb, AbstractIscDbHandle dbHandle) {
        this.gds = gds;
        this.dpb = dpb;
        this.currentDbHandle = dbHandle;

        this.registerResultSets = !getDatabaseParameterBuffer().hasArgument(
                ISCConstants.isc_dpb_no_result_set_tracking);
    }

    public IscTrHandle getCurrentTrHandle() {
        return currentTr;
    }
    
    public void setCurrentTrHandle(AbstractIscTrHandle currentTr) {
        this.currentTr = currentTr;
    }
    
    public IscDbHandle getCurrentDbHandle() {
        return currentDbHandle;
    }
    
    public DatabaseParameterBuffer getDatabaseParameterBuffer() {
        return dpb;
    }
    
    /**
     * Retrieve a newly allocated statment handle with the current connection.
     * 
     * @return The new statement handle
     * @throws GDSException
     *             if a database access error occurs
     */
    public AbstractIscStmtHandle allocateStatement() throws GDSException {
        AbstractIscStmtHandle stmt = (AbstractIscStmtHandle)gds.createIscStmtHandle();
        gds.iscDsqlAllocateStatement(getCurrentDbHandle(), stmt);
        return stmt;
    }

    /**
     * Retrieve whether this connection is currently involved in a transaction
     * 
     * @return <code>true</code> if this connection is currently in a
     *         transaction, <code>false</code> otherwise.
     */
    public boolean inTransaction() {
        return currentTr != null;
    }

    public int getTransactionId(IscTrHandle trHandle) 
            throws GDSException {
        byte [] trInfo = gds.iscTransactionInformation(
                trHandle, new byte[]{ ISCConstants.isc_info_tra_id}, 32);
        if (trInfo.length < 3 || trInfo[0] != ISCConstants.isc_info_tra_id){
            throw new GDSException("Unexpected response buffer");
        }
        int length = gds.iscVaxInteger(trInfo, 1, 2);
        return gds.iscVaxInteger(trInfo, 3, length);
    }
    
    /**
     * Prepare an SQL string for execution (within the database server) in the
     * context of a statement handle.
     * 
     * @param stmt
     *            The statement handle within which the SQL statement will be
     *            prepared
     * @param sql
     *            The SQL statement to be prepared
     * @param describeBind
     *            Send bind data to the database server
     * @throws GDSException
     *             if a Firebird-specific error occurs
     * @throws SQLException
     *             if a database access error occurs
     */
    public void prepareStatement(AbstractIscStmtHandle stmt, String sql,
            boolean describeBind) throws GDSException, SQLException {
        
        if (log != null) log.debug("preparing sql: " + sql);

        String localEncoding = 
            dpb.getArgumentAsString(ISCConstants.isc_dpb_local_encoding);
        
        String mappingPath = 
            dpb.getArgumentAsString(ISCConstants.isc_dpb_mapping_path);

        Encoding encoding = 
            EncodingFactory.getEncoding(localEncoding, mappingPath);

        int dialect = ISCConstants.SQL_DIALECT_CURRENT;
        if (dpb.hasArgument(ISCConstants.isc_dpb_sql_dialect))
            dialect = dpb.getArgumentAsInt(ISCConstants.isc_dpb_sql_dialect);

        XSQLDA out = gds.iscDsqlPrepare(currentTr, stmt, 
            encoding.encodeToCharset(sql), dialect);
        
        if (out.sqld != out.sqln) 
            throw new GDSException("Not all columns returned"); 
        
        if (describeBind) 
            gds.iscDsqlDescribeBind(stmt, ISCConstants.SQLDA_VERSION1);
    }

    /**
     * Execute a statement in the database.
     * 
     * @param stmt
     *            The handle to the statement to be executed
     * @param sendOutSqlda
     *            Determines if the XSQLDA structure should be sent to the
     *            database
     * @throws GDSException
     *             if a Firebird-specific error occurs
     */
    public void executeStatement(AbstractIscStmtHandle stmt, boolean sendOutSqlda)
            throws GDSException {
        gds.iscDsqlExecute2(currentTr, stmt, ISCConstants.SQLDA_VERSION1,
            stmt.getInSqlda(), (sendOutSqlda) ? stmt.getOutSqlda() : null);
}

    /**
     * Execute a SQL statement directly with the current connection.
     * 
     * @param statement
     *            The SQL statement to execute
     * @throws GDSException
     *             if a Firebird-specific error occurs
     */
    public void executeImmediate(String statement) throws GDSException {
        gds.iscDsqlExecImmed2(getIscDBHandle(), currentTr, statement, 3,
            null, null);
    }

    /**
     * Fetch data from a statement in the database.
     * 
     * @param stmt
     *            handle to the statement from which data will be fetched
     * @param fetchSize
     *            The number of records to fetch
     * @throws GDSException
     *             if a Firebird-specific error occurs
     */
    public void fetch(AbstractIscStmtHandle stmt, int fetchSize) throws GDSException {
        gds.iscDsqlFetch(stmt, ISCConstants.SQLDA_VERSION1, 
            stmt.getOutSqlda(), fetchSize);
        
        if (registerResultSets)
            currentTr.registerStatementWithTransaction(stmt);
    }

    /**
     * Set the cursor name for a statement.
     * 
     * @param stmt
     *            handle to statement for which the cursor name will be set
     * @param cursorName
     *            the name for the cursor
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public void setCursorName(AbstractIscStmtHandle stmt, String cursorName)
            throws GDSException {

        gds.iscDsqlSetCursorName(stmt, cursorName, 0); 
    }

    /**
     * Close a statement that is allocated in the database. The statement can be
     * optionally deallocated.
     * 
     * @param stmt
     *            handle to the statement to be closed
     * @param deallocate
     *            if <code>true</code>, the statement will be deallocated,
     *            otherwise it will not be deallocated
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public void closeStatement(AbstractIscStmtHandle stmt, boolean deallocate)
            throws GDSException {

        if (!deallocate && !stmt.hasOpenResultSet())
            return;
        
        try {
            gds.iscDsqlFreeStatement(stmt, (deallocate) ? ISCConstants.DSQL_drop
                    : ISCConstants.DSQL_close);
        } catch(GDSException ex) {
            
            // we do not handle exceptions comming from statement closing
            if (deallocate)
                throw ex;
            
            boolean recloseClosedCursorError = false;
            
            GDSException tempEx = ex;
            do {
                if (tempEx.getIntParam() == ISCConstants.isc_dsql_cursor_close_err) {
                    recloseClosedCursorError = true;
                    break;
                }
                
                tempEx = tempEx.getNext();
            } while(tempEx != null);
            
            if (!recloseClosedCursorError)
                throw ex;
        }
    }

    /**
     * Open a handle to a new blob within the current transaction with the given
     * id.
     * 
     * @param blob_id
     *            The identifier to be given to the blob
     * @param segmented
     *            If <code>true</code>, the blob will be segmented, otherwise
     *            is will be streamed
     * @throws GDSException
     *             if a Firebird-specific database error occurs
     */
    public IscBlobHandle openBlob(long blob_id, boolean segmented)
            throws GDSException {

        IscBlobHandle blob = gds.createIscBlobHandle();
        blob.setBlobId(blob_id);

        BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();

        blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
            segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                    : BlobParameterBuffer.TYPE_STREAM);

        gds.iscOpenBlob2(currentDbHandle, currentTr, blob,
            blobParameterBuffer);

        return blob;
    }

    /**
     * Create a new blob within the current transaction.
     * 
     * @param segmented
     *            If <code>true</code> the blob will be segmented, otherwise
     *            it will be streamed
     * @throws GDSException
     *             if a Firebird-specific database error occurs
     */
    public IscBlobHandle createBlob(boolean segmented) throws GDSException {
        
        IscBlobHandle blob = gds.createIscBlobHandle();

        BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();

        blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
            segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                    : BlobParameterBuffer.TYPE_STREAM);

        gds.iscCreateBlob2(currentDbHandle, currentTr, blob,
            blobParameterBuffer);

        return blob;
    }

    /**
     * Get a segment from a blob.
     * 
     * @param blob
     *            Handle to the blob from which the segment is to be fetched
     * @param len
     *            The maximum length to fetch
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public byte[] getBlobSegment(IscBlobHandle blob, int len)
            throws GDSException {
        return gds.iscGetSegment(blob, len);
    }

    /**
     * Close a blob that has been opened within the database.
     * 
     * @param blob
     *            Handle to the blob to be closed
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public void closeBlob(IscBlobHandle blob) throws GDSException {
        gds.iscCloseBlob(blob);
    }

    /**
     * Write a segment of data to a blob.
     * 
     * @param blob
     *            handle to the blob to which data will be written
     * @param buf
     *            segment of data to be written to the blob
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public void putBlobSegment(IscBlobHandle blob, byte[] buf)
            throws GDSException {
        gds.iscPutSegment(blob, buf);
    }

    /**
     * Fetch the count information for a statement handle. The count information
     * that is updated includes the counts for update, insert, delete and
     * select, and it is set in the handle itself.
     * 
     * @param stmt
     *            handle to the statement for which counts will be fetched
     * @throws GDSException
     *             if a Firebird-specific database access error occurs
     */
    public void getSqlCounts(AbstractIscStmtHandle stmt) throws GDSException {
        gds.getSqlCounts(stmt);
    }

    // for DatabaseMetaData.
    
    /**
     * Get the name of the database product that we're connected to.
     * 
     * @return The database product name (i.e. Firebird or Interbase)
     */
    public String getDatabaseProductName() {
        /** @todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductName();
    }

    /**
     * Get the version of the the database that we're connected to
     * 
     * @return the database product version
     */
    public String getDatabaseProductVersion() {
        /** @todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductVersion();
    }

    /**
     * Get the major version number of the database that we're connected to.
     * 
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        /** @todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductMajorVersion();
    }

    /**
     * Get the minor version number of the database that we're connected to.
     * 
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
        /** @todo add check if mc is not null */
        return currentDbHandle.getDatabaseProductMinorVersion();
    }

    /**
     * Get the database login name of the user that we're connected as.
     * 
     * @return The username of the current database user
     */
    public String getUserName() {
        return dpb.getArgumentAsString(ISCConstants.isc_dpb_user);
    }

    /**
     * Get the buffer length for blobs for this connection.
     * 
     * @return The length of blob buffers
     */
    public int getBlobBufferLength() {
        if (dpb.hasArgument(ISCConstants.isc_dpb_blob_buffer_size))
            return dpb.getArgumentAsInt(ISCConstants.isc_dpb_blob_buffer_size);
        else
            return FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE;
    }

    /**
     * Get the encoding used for this connection.
     * 
     * @return The name of the encoding used
     */
    public String getIscEncoding() {
        try {
            String result = dpb.getArgumentAsString(ISCConstants.isc_dpb_lc_ctype);
            if (result == null) result = "NONE";
            return result;
        } catch (NullPointerException ex) {
            return "NONE";
        }
    }

    public String getJavaEncoding() {
        return dpb.getArgumentAsString(DatabaseParameterBufferExtension.LOCAL_ENCODING);
    }
    
    public String getMappingPath() {
        return dpb.getArgumentAsString(DatabaseParameterBufferExtension.MAPPING_PATH);
    }
    
    /**
     * Get all warnings associated with current connection.
     * 
     * @return list of {@link GDSException}instances representing warnings for
     *         this database connection.
     */
    public List getWarnings() {
        if (currentDbHandle == null)
            return Collections.EMPTY_LIST;
        else
            return currentDbHandle.getWarnings();
    }

    /**
     * Clear warnings for this database connection.
     */
    public void clearWarnings() {
        if (currentDbHandle != null) currentDbHandle.clearWarnings();
    }

    /**
     * Get connection handle for direct Firebird API access
     * 
     * @return internal handle for connection
     */
    public IscDbHandle getIscDBHandle() throws GDSException {
        return currentDbHandle;
    }

    /**
     * Get Firebird API handler (sockets/native/embeded/etc)
     * 
     * @return handler object for internal API calls
     */
    public GDS getInternalAPIHandler() {
        return gds;
    }

}
