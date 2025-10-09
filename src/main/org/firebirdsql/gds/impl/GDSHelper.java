// SPDX-FileCopyrightText: Copyright 2005-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Gabriel Reid
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2006 Ludovic Orban
// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.*;
import org.firebirdsql.gds.ng.*;

import java.sql.SQLException;
import java.util.TimeZone;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.firebirdsql.jaybird.props.PropertyConstants.SESSION_TIME_ZONE_SERVER;

/**
 * Helper class for all GDS-related operations.
 */
public final class GDSHelper {

    private static final Predicate<String> OFFSET_ZONE_NAME_PREDICATE =
            Pattern.compile("[+-]\\d{2}:\\d{2}").asMatchPredicate();

    private final FbDatabase database;
    private FbTransaction transaction;
    private TimeZone sessionTimeZone;

    /**
     * Create instance of this class.
     */
    public GDSHelper(FbDatabase database) {
        this.database = requireNonNull(database, "database");
    }

    public FbTransaction getCurrentTransaction() {
        try (LockCloseable ignored = withLock()) {
            return transaction;
        }
    }

    public void setCurrentTransaction(FbTransaction transaction) {
        try (LockCloseable ignored = withLock()) {
            this.transaction = transaction;
        }
    }

    public FbDatabase getCurrentDatabase() {
        return database;
    }

    public IConnectionProperties getConnectionProperties() {
        return database.getConnectionProperties();
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
     * @return {@code true} if this connection is currently in a transaction, {@code false} otherwise.
     */
    public boolean inTransaction() {
        try (LockCloseable ignored = withLock()) {
            return transaction != null && transaction.getState() == TransactionState.ACTIVE;
        }
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
     * Open a handle to a new blob within the current transaction with the given id.
     *
     * @param blobId
     *         the identifier to be given to the blob
     * @param blobConfig
     *         blob configuration
     * @throws SQLException
     *         if a Firebird-specific database error occurs
     */
    @SuppressWarnings("java:S2095")
    public FbBlob openBlob(long blobId, BlobConfig blobConfig) throws SQLException {
        FbBlob blob = database.createBlobForInput(getCurrentTransaction(), blobConfig, blobId);
        blob.open();
        return blob;
    }

    /**
     * Create a new blob within the current transaction.
     *
     * @param blobConfig
     *         blob configuration
     * @throws SQLException
     *         if a Firebird-specific database error occurs
     */
    @SuppressWarnings("java:S2095")
    public FbBlob createBlob(BlobConfig blobConfig) throws SQLException {
        FbBlob blob = database.createBlobForOutput(getCurrentTransaction(), blobConfig);
        blob.open();
        return blob;
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

    // for DatabaseMetaData.

    // TODO Consider removing some of these methods by pushing them down into FBDatabaseMetaData

    /**
     * Get the name of the database product that we're connected to.
     *
     * @return The database product name (i.e. Firebird or Interbase)
     */
    public String getDatabaseProductName() {
        return getServerVersion().getServerName();
    }

    /**
     * Get the version of the database that we're connected to.
     *
     * @return the database product version
     */
    public String getDatabaseProductVersion() {
        return getServerVersion().getFullVersion();
    }

    /**
     * Get the major version number of the database that we're connected to.
     *
     * @return The major version number of the database
     */
    public int getDatabaseProductMajorVersion() {
        return getServerVersion().getMajorVersion();
    }

    /**
     * Get the minor version number of the database that we're connected to.
     *
     * @return The minor version number of the database
     */
    public int getDatabaseProductMinorVersion() {
        return getServerVersion().getMinorVersion();
    }

    /**
     * Compares the version of this database to the specified major and minor version.
     * <p>
     * This method follows the semantics of {@link Comparable}: returns a negative value if the version of this database 
     * connection is smaller than the supplied arguments, 0 if they are equal or positive if its bigger.
     * </p>
     *
     * @param major
     *         Major version to compare
     * @param minor
     *         Minor version to compare
     * @return a negative integer, zero, or a positive integer as this database version is less than, equal to,
     * or greater than the specified major and minor version
     * @see #compareToVersion(int) 
     */
    public int compareToVersion(int major, int minor) {
        int differenceMajor = getDatabaseProductMajorVersion() - major;
        if (differenceMajor == 0) {
            return getDatabaseProductMinorVersion() - minor;
        }
        return differenceMajor;
    }

    /**
     * Get the server version.
     *
     * @return server version
     */
    public GDSServerVersion getServerVersion() {
        return database.getServerVersion();
    }

    /**
     * Compares the version of this database to the specified major version.
     * <p>
     * This method follows the semantics of {@link Comparable}: returns a negative value if the version of this database 
     * connection is smaller than the supplied arguments, 0 if they are equal or positive if its bigger.
     * </p>
     *
     * @param major
     *         Major version to compare
     * @return a negative integer, zero, or a positive integer as this database version is less than, equal to,
     * or greater than the specified major version
     * @see #compareToVersion(int, int) 
     */
    public int compareToVersion(int major) {
        return getDatabaseProductMajorVersion() - major;
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
     * Get the session time zone as configured in the connection property.
     * <p>
     * NOTE: This is not necessarily the actual server time zone.
     * </p>
     *
     * @return Value of connection property {@code sessionTimeZone}
     */
    public TimeZone getSessionTimeZone() {
        if (sessionTimeZone == null) {
            return initSessionTimeZone();
        }
        return sessionTimeZone;
    }

    @SuppressWarnings("java:S1121")
    private TimeZone initSessionTimeZone() {
        String sessionTimeZoneName = database.getConnectionProperties().getSessionTimeZone();
        if (sessionTimeZoneName == null || SESSION_TIME_ZONE_SERVER.equalsIgnoreCase(sessionTimeZoneName)) {
            return sessionTimeZone = TimeZone.getDefault();
        }
        TimeZone timeZone = getTimeZone(sessionTimeZoneName);
        if ("GMT".equals(timeZone.getID()) && !"GMT".equalsIgnoreCase(sessionTimeZoneName)) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                    "TimeZone fallback to GMT from {0}; possible cause: value of sessionTimeZone unknown in Java. Time "
                    + "and Timestamp values may yield unexpected values. Consider setting a different value for "
                    + "sessionTimeZone", sessionTimeZoneName);
        }
        return sessionTimeZone = timeZone;
    }

    private static TimeZone getTimeZone(String timeZoneName) {
        if (OFFSET_ZONE_NAME_PREDICATE.test(timeZoneName)) {
            timeZoneName = "GMT" + timeZoneName;
        }
        return TimeZone.getTimeZone(timeZoneName);
    }

    /**
     * @see FbAttachment#withLock() 
     */
    public LockCloseable withLock() {
        return database.withLock();
    }
}