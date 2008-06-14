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

package org.firebirdsql.gds.impl;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 * Helper class for all GDS-related operations.
 */
public class GDSHelper {
    
    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16 * 1024;
    
    /**
     * Notification listener about any error that occured in this class.
     */
    public interface GDSHelperErrorListener {

        /**
         * Notify about the error in this class.
         * 
         * @param ex error that occured.
         */
        void errorOccured(GDSException ex);
    }

    private static final Logger log = LoggerFactory.getLogger(GDSHelper.class, false);

    private GDS gds;
    private AbstractIscDbHandle currentDbHandle;
    private AbstractIscTrHandle currentTr;
    /**
     * Needed from mcf when killing a db handle when a new tx cannot be started.
     */
    protected DatabaseParameterBuffer dpb;
    
    private boolean registerResultSets;
    
    private GDSHelperErrorListener listener;
    
    /**
     * Create instance of this class.
     */
    public GDSHelper(GDS gds, DatabaseParameterBuffer dpb, AbstractIscDbHandle dbHandle, GDSHelperErrorListener listener) {
        this.gds = gds;
        this.dpb = dpb;
        this.currentDbHandle = dbHandle;

        this.registerResultSets = !getDatabaseParameterBuffer().hasArgument(
                DatabaseParameterBufferExtension.NO_RESULT_SET_TRACKING);
        
        this.listener = listener;
    }
    
    private void notifyListeners(GDSException ex) {
        if (listener != null)
            listener.errorOccured(ex);
    }
        
    public AbstractIscTrHandle getCurrentTrHandle() {
        return currentTr;
    }
    
    public synchronized void setCurrentTrHandle(AbstractIscTrHandle currentTr) {
        this.currentTr = currentTr;
        notify();
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
        try {
            AbstractIscStmtHandle stmt = (AbstractIscStmtHandle)gds.createIscStmtHandle();
            gds.iscDsqlAllocateStatement(getCurrentDbHandle(), stmt);
            return stmt;
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        
        try {
            byte [] trInfo = gds.iscTransactionInformation(
                    trHandle, new byte[]{ ISCConstants.isc_info_tra_id}, 32);
            if (trInfo.length < 3 || trInfo[0] != ISCConstants.isc_info_tra_id){
                throw new GDSException("Unexpected response buffer");
            }
            int length = gds.iscVaxInteger(trInfo, 1, 2);
            return gds.iscVaxInteger(trInfo, 3, length);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        
        try {
            if (log != null) log.trace("preparing sql: " + sql);
    
            String localEncoding = 
                dpb.getArgumentAsString(DatabaseParameterBufferExtension.LOCAL_ENCODING);
            
            String mappingPath = 
                dpb.getArgumentAsString(DatabaseParameterBufferExtension.MAPPING_PATH);
    
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
            
            stmt.statement = sql;
            
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            
            if (log != null && log.isDebugEnabled())
                log.debug("Executing " + stmt.statement);
            
            // System.out.println("Executing " + stmt.statement);
            
            gds.iscDsqlExecute2(currentTr, stmt, ISCConstants.SQLDA_VERSION1,
                stmt.getInSqlda(), (sendOutSqlda) ? stmt.getOutSqlda() : null);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            gds.iscDsqlExecImmed2(getIscDBHandle(), currentTr, statement, 3,
                null, null);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            gds.iscDsqlFetch(stmt, ISCConstants.SQLDA_VERSION1, 
                stmt.getOutSqlda(), fetchSize);
            
            if (registerResultSets)
                currentTr.registerStatementWithTransaction(stmt);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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

        try {
            gds.iscDsqlSetCursorName(stmt, cursorName, 0);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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

        try {
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
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            gds.getSqlCounts(stmt);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void populateStatementInfo(AbstractIscStmtHandle fixedStmt) throws GDSException {
        final byte [] REQUEST = new byte [] {
                ISCConstants.isc_info_sql_get_plan,
                ISCConstants.isc_info_sql_stmt_type,
                ISCConstants.isc_info_end };

        try {
            int bufferSize = 1024;
            byte[] buffer;
            while (true){
                buffer = gds.iscDsqlSqlInfo(fixedStmt, REQUEST, bufferSize);
                
                if (buffer[0] != ISCConstants.isc_info_truncated){
                    break;
                } 
                bufferSize *= 2;
            }

            if (buffer[0] == ISCConstants.isc_info_end){
                throw new GDSException(ISCConstants.isc_req_sync);
            }

            String executionPlan = null;
            int statementType = IscStmtHandle.TYPE_UNKNOWN;
            
            int dataLength = -1; 
            for (int i = 0; i < buffer.length; i++){
                switch(buffer[i]){
                    case ISCConstants.isc_info_sql_get_plan:
                        dataLength = gds.iscVaxInteger(buffer, ++i, 2);
                        i += 2;
                        executionPlan = new String(buffer, i + 1, dataLength);
                        i += dataLength - 1;
                        break;
                    case ISCConstants.isc_info_sql_stmt_type:
                        dataLength = gds.iscVaxInteger(buffer, ++i, 2);
                        i += 2;
                        statementType = gds.iscVaxInteger(buffer, i, dataLength);
                        i += dataLength;
                    case ISCConstants.isc_info_end:
                    case 0:
                        break;
                    default:
                        throw new GDSException(ISCConstants.isc_req_sync);
                }
            }
            
            fixedStmt.setExecutionPlan(executionPlan);
            fixedStmt.setStatementType(statementType);
            
        } catch(GDSException ex) {
            notifyListeners(ex);
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

        try {
            IscBlobHandle blob = gds.createIscBlobHandle();
            blob.setBlobId(blob_id);
    
            BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    
            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                        : BlobParameterBuffer.TYPE_STREAM);
    
            gds.iscOpenBlob2(currentDbHandle, currentTr, blob,
                blobParameterBuffer);
    
            return blob;
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        
        try {
            IscBlobHandle blob = gds.createIscBlobHandle();
    
            BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();
    
            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                        : BlobParameterBuffer.TYPE_STREAM);
    
            gds.iscCreateBlob2(currentDbHandle, currentTr, blob,
                blobParameterBuffer);
    
            return blob;
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            return gds.iscGetSegment(blob, len);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            gds.iscCloseBlob(blob);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void seekBlob(IscBlobHandle blob, int position, int mode) throws GDSException {
        try {
            gds.iscSeekBlob(blob, position, mode);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
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
        try {
            gds.iscPutSegment(blob, buf);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public byte[] getBlobInfo(IscBlobHandle blob, byte[] requestItems, int bufferLength) throws GDSException {
        try {
            return gds.iscBlobInfo(blob, requestItems, bufferLength);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }

    public static final byte[] BLOB_LENGTH_REQUEST = new byte[]{ISCConstants.isc_info_blob_total_length};
    
    public int getBlobLength(IscBlobHandle blob) throws GDSException {
        try {
            byte[] info = gds.iscBlobInfo(blob, BLOB_LENGTH_REQUEST, 20);
            
            if (info[0] != ISCConstants.isc_info_blob_total_length)
                throw new GDSException(ISCConstants.isc_req_sync);
                
            int dataLength = gds.iscVaxInteger(info, 1, 2);
                
            return gds.iscVaxInteger(info, 3, dataLength);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public AbstractIscTrHandle startTransaction(TransactionParameterBuffer tpb) throws GDSException {
        try {
            AbstractIscTrHandle trHandle = (AbstractIscTrHandle)gds.createIscTrHandle();
            
            gds.iscStartTransaction(trHandle, currentDbHandle, tpb);
            setCurrentTrHandle(trHandle);
            
            return trHandle;
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void prepareTransaction(AbstractIscTrHandle trHandle, byte[] message) throws GDSException {
        try {
            gds.iscPrepareTransaction2(trHandle, message);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void commitTransaction(AbstractIscTrHandle trHandle) throws GDSException {
        try {
            gds.iscCommitTransaction(trHandle);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void rollbackTransaction(AbstractIscTrHandle trHandle) throws GDSException {
        try {
            gds.iscRollbackTransaction(trHandle);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public void detachDatabase() throws GDSException {
        try {
            gds.iscDetachDatabase(currentDbHandle);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public int iscVaxInteger(byte[] buffer, int pos, int length) {
        return gds.iscVaxInteger(buffer, pos, length);
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
        if (dpb.hasArgument(DatabaseParameterBufferExtension.BLOB_BUFFER_SIZE))
            return dpb.getArgumentAsInt(DatabaseParameterBufferExtension.BLOB_BUFFER_SIZE);
        else
            return DEFAULT_BLOB_BUFFER_SIZE;
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
    public IscDbHandle getIscDBHandle() {
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