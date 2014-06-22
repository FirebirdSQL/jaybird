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
package org.firebirdsql.gds.impl;

import java.sql.SQLException;
import java.util.List;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;


/**
 * Helper class for all GDS-related operations.
 */
public class GDSHelper {
    
    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16 * 1024;

    private static final Logger log = LoggerFactory.getLogger(GDSHelper.class, false);

    private final GDS gds;
    private final FbDatabase database;
    private FbTransaction transaction;

    /**
     * Needed from mcf when killing a db handle when a new tx cannot be started.
     */
    protected DatabaseParameterBuffer dpb;
    
    private boolean registerResultSets;
    
    private final ExceptionListener listener;
    
    /**
     * Create instance of this class.
     */
    public GDSHelper(GDS gds, DatabaseParameterBuffer dpb, IscDbHandle dbHandle, ExceptionListener listener, FbDatabase database) {
        this.gds = gds;
        this.dpb = dpb;

        this.registerResultSets = !getDatabaseParameterBuffer().hasArgument(
                DatabaseParameterBufferExtension.NO_RESULT_SET_TRACKING);
        
        this.listener = listener != null ? listener : ExceptionListener.NULL_LISTENER;
        this.database = database;
    }

    private void notifyListeners(GDSException ex) {
        listener.errorOccurred(ex);
    }

    private void notifyListeners(SQLException ex) {
        listener.errorOccurred(ex);
    }

    public synchronized FbTransaction getCurrentTransaction() {
        return transaction;
    }

    public synchronized void setCurrentTransaction(FbTransaction transaction) {
        this.transaction = transaction;
        notifyAll();
    }

    public FbDatabase getCurrentDatabase() {
        return database;
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() {
        return dpb;
    }
    
    /**
     * Retrieve a newly allocated statement handle with the current connection.
     *
     * @return The new statement handle
     * @throws java.sql.SQLException
     *             if a database access error occurs
     */
    public FbStatement allocateStatement() throws SQLException {
        try {
            final FbStatement statement = database.createStatement(null);
            statement.allocateStatement();
            return statement;
        } catch (SQLException ex) {
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
        // TODO Check state?
        return transaction != null;
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
    @Deprecated
    public void prepareStatement(AbstractIscStmtHandle stmt, String sql,
            boolean describeBind) throws GDSException, SQLException {

        throw new UnsupportedOperationException("prepareStatement is no longer implemented/supported");
        /*
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
            
            stmt.setStatementText(sql);
            
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
        */
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
    @Deprecated
    public void executeStatement(AbstractIscStmtHandle stmt, boolean sendOutSqlda) throws GDSException {
        throw new UnsupportedOperationException("executeStatement is no longer supported/implemented");
        /*
        try {
            
            if (log != null && log.isDebugEnabled())
                log.debug("Executing " + stmt.getStatementText());
            
            // System.out.println("Executing " + stmt.statement);
            
            gds.iscDsqlExecute2(currentTr, stmt, ISCConstants.SQLDA_VERSION1,
                stmt.getInSqlda(), (sendOutSqlda) ? stmt.getOutSqlda() : null);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
        */
    }

    /**
     * Execute a SQL statement directly with the current connection.
     * 
     * @param statement
     *            The SQL statement to execute
     * @throws SQLException
     *             if a Firebird-specific error occurs
     */
    public void executeImmediate(String statement) throws SQLException {
        database.executeImmediate(statement, getCurrentTransaction());
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
    @Deprecated
    public void fetch(AbstractIscStmtHandle stmt, int fetchSize) throws GDSException {
        throw new UnsupportedOperationException("fetch is no longer supported/implemented");
        /*
        try {
            gds.iscDsqlFetch(stmt, ISCConstants.SQLDA_VERSION1, 
                stmt.getOutSqlda(), fetchSize);
            
            if (registerResultSets)
                currentTr.registerStatementWithTransaction(stmt);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
        */
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
     * Open a handle to a new blob within the current transaction with the given
     * id.
     * 
     * @param blob_id
     *            The identifier to be given to the blob
     * @param segmented
     *            If <code>true</code>, the blob will be segmented, otherwise
     *            is will be streamed
     * @throws SQLException
     *             if a Firebird-specific database error occurs
     */
    public FbBlob openBlob(long blob_id, boolean segmented) throws SQLException {
        try {
            BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();

            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                    segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                            : BlobParameterBuffer.TYPE_STREAM);

            FbBlob blob = database.createBlobForInput(getCurrentTransaction(), blobParameterBuffer, blob_id);
            blob.open();
    
            return blob;
        } catch(SQLException ex) {
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
     * @throws SQLException
     *             if a Firebird-specific database error occurs
     */
    public FbBlob createBlob(boolean segmented) throws SQLException {
        try {
            BlobParameterBuffer blobParameterBuffer = gds.createBlobParameterBuffer();

            blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                    segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                            : BlobParameterBuffer.TYPE_STREAM);

            FbBlob blob = database.createBlobForOutput(getCurrentTransaction(), blobParameterBuffer);
            blob.open();

            return blob;
        } catch(SQLException ex) {
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
            
            if (info.length == 0 || info[0] != ISCConstants.isc_info_blob_total_length)
                throw new GDSException(ISCConstants.isc_req_sync);
                
            int dataLength = gds.iscVaxInteger(info, 1, 2);
                
            return gds.iscVaxInteger(info, 3, dataLength);
        } catch(GDSException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }
    
    public FbTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        try {
            FbTransaction transaction = database.startTransaction(tpb);
            setCurrentTransaction(transaction);
            
            return transaction;
        } catch (SQLException e) {
            notifyListeners(e);
            throw e;
        }
    }

    public void prepareTransaction(FbTransaction transaction, byte[] message) throws SQLException {
        try {
            transaction.prepare(message);
        } catch(SQLException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }

    public void commitTransaction(FbTransaction transaction) throws SQLException {
        try {
            transaction.commit();
        } catch (SQLException e) {
            notifyListeners(e);
            throw e;
        }
    }

    public void rollbackTransaction(FbTransaction transaction) throws SQLException {
        try {
            transaction.rollback();
        } catch (SQLException e) {
            notifyListeners(e);
            throw e;
        }
    }
    
    public void detachDatabase() throws SQLException {
        try {
            database.detach();
        } catch(SQLException ex) {
            notifyListeners(ex);
            throw ex;
        }
    }

    /**
     * Cancel the currently running operation.
     */
    public void cancelOperation() throws GDSException {
        // TODO rewrite to throw SQLException
        try {
            database.cancelOperation(ISCConstants.fb_cancel_raise);
        } catch(SQLException ex) {
            notifyListeners(ex);
            throw new GDSException(ex.getErrorCode(), ex);
        }
    }

    public int iscVaxInteger(byte[] buffer, int pos, int length) {
        return gds.iscVaxInteger(buffer, pos, length);
    }
    
    public long iscVaxLong(byte[] buffer, int pos, int length) {
        return gds.iscVaxLong(buffer, pos, length);
    }

    // for DatabaseMetaData.
    
    /**
     * Get the name of the database product that we're connected to.
     * 
     * @return The database product name (i.e. Firebird or Interbase)
     */
    public String getDatabaseProductName() {
        /** @todo add check if mc is not null */
        return database.getServerVersion().getServerName();
    }

    /**
     * Get the version of the the database that we're connected to
     * 
     * @return the database product version
     */
    public String getDatabaseProductVersion() {
        /** @todo add check if mc is not null */
        return database.getServerVersion().getFullVersion();
    }

    /**
     * Get the major version number of the database that we're connected to.
     * 
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        /** @todo add check if mc is not null */
        return database.getServerVersion().getMajorVersion();
    }

    /**
     * Get the minor version number of the database that we're connected to.
     * 
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
        /** @todo add check if mc is not null */
        return database.getServerVersion().getMinorVersion();
    }
    
    /**
     * Compares the version of this database to the specified major and
     * minor version.
     * <p>
     * This method follows the semantics of {@link Comparable}: returns a
     * negative value if the version of this database connection is smaller than
     * the supplied arguments, 0 if they are equal or positive if its bigger.
     * </p>
     * 
     * @param major
     *            Major version to compare
     * @param minor
     *            Minor version to compare
     * @return a negative integer, zero, or a positive integer as this database
     *         version is less than, equal to, or greater than the specified
     *         major and minor version
     */
    public int compareToVersion(int major, int minor) {
        int differenceMajor = getDatabaseProductMajorVersion() - major;
        if (differenceMajor == 0) {
            return getDatabaseProductMinorVersion() - minor;
        }
        return differenceMajor;
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
    @Deprecated
    public List<GDSException> getWarnings() {
        // TODO consider resurrecting?
        throw new UnsupportedOperationException("getWarnings is no longer supported/implemented");
    }

    /**
     * Clear warnings for this database connection.
     */
    public void clearWarnings() {
        // TODO Remove or reimplement
        //if (currentDbHandle != null) currentDbHandle.clearWarnings();
    }

    /**
     * Get connection handle for direct Firebird API access
     *
     * @return internal handle for connection
     */
    @Deprecated
    public IscDbHandle getIscDBHandle() {
        throw new UnsupportedOperationException("getIscDBHandle is no longer supported/implemented");
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