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

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.ng.FbBlob;
import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbStatement;
import org.firebirdsql.gds.ng.FbTransaction;

import java.sql.SQLException;
import java.util.List;

/**
 * Helper class for all GDS-related operations.
 */
public class GDSHelper {
    
    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16 * 1024;

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
    public GDSHelper(GDS gds, DatabaseParameterBuffer dpb, ExceptionListener listener, FbDatabase database) {
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
     * @return Connection dialect
     */
    public int getDialect() {
        return database.getConnectionDialect();
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
            return database.createStatement(getCurrentTransaction());
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
            BlobParameterBuffer blobParameterBuffer = database.createBlobParameterBuffer();

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
            BlobParameterBuffer blobParameterBuffer = database.createBlobParameterBuffer();

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
     * Get Firebird API handler (sockets/native/embeded/etc)
     * 
     * @return handler object for internal API calls
     */
    @Deprecated
    public GDS getInternalAPIHandler() {
        return gds;
    }
}