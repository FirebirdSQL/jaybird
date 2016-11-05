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

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.ng.*;
import org.firebirdsql.jdbc.Synchronizable;

import java.sql.SQLException;

/**
 * Helper class for all GDS-related operations.
 */
public final class GDSHelper implements Synchronizable {

    public static final int DEFAULT_BLOB_BUFFER_SIZE = 16 * 1024;

    private final FbDatabase database;
    private final Object syncObject;
    private FbTransaction transaction;

    private boolean registerResultSets;

    /**
     * Create instance of this class.
     */
    public GDSHelper(FbDatabase database) {
        // TODO Make explicit property
        this.registerResultSets = !database.getConnectionProperties().getExtraDatabaseParameters()
                .hasArgument(DatabaseParameterBufferExtension.NO_RESULT_SET_TRACKING);

        this.database = database;
        syncObject = database.getSynchronizationObject();
    }

    public FbTransaction getCurrentTransaction() {
        synchronized (database.getSynchronizationObject()) {
            return transaction;
        }
    }

    public void setCurrentTransaction(FbTransaction transaction) {
        synchronized (database.getSynchronizationObject()) {
            this.transaction = transaction;
        }
    }

    public FbDatabase getCurrentDatabase() {
        return database;
    }

    public IConnectionProperties getConnectionProperties() {
        return database.getConnectionProperties();
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() {
        // TODO Calls to this method should be replaced with an explicit property check
        return database.getConnectionProperties().getExtraDatabaseParameters();
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
     *         if a database access error occurs
     */
    public FbStatement allocateStatement() throws SQLException {
        return database.createStatement(getCurrentTransaction());
    }

    /**
     * Retrieve whether this connection is currently involved in a transaction
     *
     * @return <code>true</code> if this connection is currently in a
     * transaction, <code>false</code> otherwise.
     */
    public boolean inTransaction() {
        return transaction != null && transaction.getState() == TransactionState.ACTIVE;
    }

    /**
     * Execute a SQL statement directly with the current connection.
     *
     * @param statement
     *         The SQL statement to execute
     * @throws SQLException
     *         if a Firebird-specific error occurs
     */
    public void executeImmediate(String statement) throws SQLException {
        database.executeImmediate(statement, getCurrentTransaction());
    }

    /**
     * Open a handle to a new blob within the current transaction with the given
     * id.
     *
     * @param blob_id
     *         The identifier to be given to the blob
     * @param segmented
     *         If <code>true</code>, the blob will be segmented, otherwise
     *         is will be streamed
     * @throws SQLException
     *         if a Firebird-specific database error occurs
     */
    public FbBlob openBlob(long blob_id, boolean segmented) throws SQLException {
        BlobParameterBuffer blobParameterBuffer = database.createBlobParameterBuffer();

        blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                        : BlobParameterBuffer.TYPE_STREAM);

        FbBlob blob = database.createBlobForInput(getCurrentTransaction(), blobParameterBuffer, blob_id);
        blob.open();

        return blob;
    }

    /**
     * Create a new blob within the current transaction.
     *
     * @param segmented
     *         If <code>true</code> the blob will be segmented, otherwise it will be streamed
     * @throws SQLException
     *         if a Firebird-specific database error occurs
     */
    public FbBlob createBlob(boolean segmented) throws SQLException {
        BlobParameterBuffer blobParameterBuffer = database.createBlobParameterBuffer();

        blobParameterBuffer.addArgument(BlobParameterBuffer.TYPE,
                segmented ? BlobParameterBuffer.TYPE_SEGMENTED
                        : BlobParameterBuffer.TYPE_STREAM);

        FbBlob blob = database.createBlobForOutput(getCurrentTransaction(), blobParameterBuffer);
        blob.open();

        return blob;
    }

    public FbTransaction startTransaction(TransactionParameterBuffer tpb) throws SQLException {
        FbTransaction transaction = database.startTransaction(tpb);
        setCurrentTransaction(transaction);

        return transaction;
    }

    public void prepareTransaction(FbTransaction transaction, byte[] message) throws SQLException {
        transaction.prepare(message);
    }

    public void commitTransaction(FbTransaction transaction) throws SQLException {
        transaction.commit();
    }

    public void rollbackTransaction(FbTransaction transaction) throws SQLException {
        transaction.rollback();
    }

    public void detachDatabase() throws SQLException {
        database.close();
    }

    /**
     * Cancel the currently running operation.
     */
    public void cancelOperation() throws SQLException {
        database.cancelOperation(ISCConstants.fb_cancel_raise);
    }

    /**
     * @deprecated Use {@link VaxEncoding#iscVaxInteger(byte[], int, int)}. This method will be removed in Jaybird 3.1
     */
    @Deprecated
    public int iscVaxInteger(byte[] buffer, int pos, int length) {
        return VaxEncoding.iscVaxInteger(buffer, pos, length);
    }

    /**
     * @deprecated Use {@link VaxEncoding#iscVaxLong(byte[], int, int)}. This method will be removed in Jaybird 3.1
     */
    @Deprecated
    public long iscVaxLong(byte[] buffer, int pos, int length) {
        return VaxEncoding.iscVaxLong(buffer, pos, length);
    }

    // for DatabaseMetaData.

    /**
     * Get the name of the database product that we're connected to.
     *
     * @return The database product name (i.e. Firebird or Interbase)
     */
    public String getDatabaseProductName() {
        return database.getServerVersion().getServerName();
    }

    /**
     * Get the version of the the database that we're connected to
     *
     * @return the database product version
     */
    public String getDatabaseProductVersion() {
        return database.getServerVersion().getFullVersion();
    }

    /**
     * Get the major version number of the database that we're connected to.
     *
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        return database.getServerVersion().getMajorVersion();
    }

    /**
     * Get the minor version number of the database that we're connected to.
     *
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
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
     *         Major version to compare
     * @param minor
     *         Minor version to compare
     * @return a negative integer, zero, or a positive integer as this database version is less than, equal to,
     * or greater than the specified major and minor version
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
        return database.getConnectionProperties().getUser();
    }

    /**
     * Get the buffer length for blobs for this connection.
     *
     * @return The length of blob buffers
     */
    public int getBlobBufferLength() {
        // TODO Add as explicit property on IConnectionProperties
        DatabaseParameterBuffer dpb = database.getConnectionProperties().getExtraDatabaseParameters();
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
        return database.getEncodingFactory().getDefaultEncodingDefinition().getFirebirdEncodingName();
    }

    public String getJavaEncoding() {
        return database.getEncodingFactory().getDefaultEncodingDefinition().getJavaEncodingName();
    }

    public String getMappingPath() {
        // TODO Add as explicit property on IConnectionProperties
        DatabaseParameterBuffer dpb = database.getConnectionProperties().getExtraDatabaseParameters();
        return dpb.getArgumentAsString(DatabaseParameterBufferExtension.MAPPING_PATH);
    }

    @Override
    public Object getSynchronizationObject() {
        return syncObject;
    }
}