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
package org.firebirdsql.jaybird.props;

import org.firebirdsql.jdbc.FirebirdCallableStatement;

/**
 * Properties for database connections.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface DatabaseConnectionProperties extends AttachmentProperties {

    /**
     * Gets the database of the connection.
     *
     * @return database name
     * @see #setDatabaseName(String)
     */
    default String getDatabaseName() {
        return getProperty(PropertyNames.databaseName);
    }

    /**
     * Sets the database of the connection
     * <p>
     * When {@code serverName} is {@code null}, the value is taken as the URL of the database, and exact
     * interpretation depends on the protocol implementation ({@code type}). Basically, the URL would be the JDBC URL,
     * but without the {@code jdbc:firebird[sql]:[subprotocol:]} prefix and without connection properties. Examples:
     * </p>
     * <ul>
     * <li>//localhost/employee &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example)</li>
     * <li>localhost:employee &mdash; NATIVE, PURE_JAVA</li>
     * <li>//localhost:3051/employee &mdash; PURE_JAVA, NATIVE (for NATIVE, this format is parsed and
     * transformed to the next example)</li>
     * <li>localhost/3051:employee &mdash; NATIVE, PURE_JAVA</li>
     * <li>/path/to/your.fdb &mdash; NATIVE, EMBEDDED, PURE_JAVA (PURE_JAVA will use localhost
     * as {@code serverName}, depending on the Firebird version and platform, NATIVE may use Firebird Embedded)</li>
     * <li>C:\path\to\your.fdb &mdash; NATIVE, EMBEDDED (protocols like PURE_JAVA may attempt to connect to a server
     * called {@code C}, depending on the Firebird version and platform, NATIVE may use Firebird Embedded)</li>
     * <li>C:/path/to/your.fdb &mdash; NATIVE, EMBEDDED (protocols like PURE_JAVA may attempt to connect to a server
     * called {@code C}, depending on the Firebird version and platform, NATIVE may use Firebird Embedded)</li>
     * <li>xnet://employee &mdash; NATIVE (EMBEDDED will behave as NATIVE, protocols like PURE_JAVA may
     * attempt to connect to a server called {@code xnet})</li>
     * <li>other Firebird {@code fbclient} connection URLs &mdash; NATIVE (EMBEDDED will behave as NATIVE, protocols
     * like PURE_JAVA may interpret the protocol name as a host name</li>
     * <li>Custom {@code type} implementations may support other URL formats</li>
     * </ul>
     * <p>
     * Some protocols, for example PURE_JAVA, when {@code serverName} is not set, but {@code databaseName} doesn't seem
     * to contain a host name, may default to attempting to connect to localhost with {@code databaseName} as the
     * database path or alias.
     * </p>
     * <p>
     * When {@code serverName} is set, the value is taken as the database path or alias. Examples:
     * </p>
     * <ul>
     * <li>employee</li>
     * <li>/path/to/your.fdb</li>
     * <li>C:\path\to\your.fdb</li>
     * <li>C:/path/to/your.fdb</li>
     * <li>relative/path/to/your.fdb &mdash; not recommended</li>
     * </ul>
     *
     * @param databaseName
     *         database name
     */
    default void setDatabaseName(String databaseName) {
        setProperty(PropertyNames.databaseName, databaseName);
    }

    /**
     * @return SQL dialect of the client connection
     */
    default int getSqlDialect() {
        return getIntProperty(PropertyNames.sqlDialect, PropertyConstants.DEFAULT_DIALECT);
    }

    /**
     * @param sqlDialect
     *         SQL dialect of the client connection.
     */
    default void setSqlDialect(int sqlDialect) {
        setIntProperty(PropertyNames.sqlDialect, sqlDialect);
    }

    /**
     * Get the page cache size.
     * <p>
     * A value of {@code 0} indicates that the value is not set, and that the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic)
     * </p>
     * <p>
     * NOTE: Implementer should take care to return {@code 0} if the value hasn't been set yet.
     * </p>
     *
     * @return size of cache in pages for this connection, can be specified for Classic and SuperClassic instances,
     * ignored for SuperServer as the cache is shared; 0 when not set
     */
    default int getPageCacheSize() {
        return getIntProperty(PropertyNames.pageCacheSize, PropertyConstants.DEFAULT_PAGE_CACHE_SIZE);
    }

    /**
     * Set the page cache size.
     * <p>
     * A value of {@code 0} indicates that the value is not set, and that the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic).
     * </p>
     *
     * @param pageCacheSize
     *         size of cache in pages for this connection, can be specified for Classic and SuperClassic instances
     *         ignored for SuperServer as the cache is shared.
     */
    default void setPageCacheSize(int pageCacheSize) {
        setIntProperty(PropertyNames.pageCacheSize, pageCacheSize);
    }

    /**
     * Get the {@code dataTypeBind} configuration.
     *
     * @return configuration value for {@code dataTypeBind}, or {@code null} for driver default
     * @since 4.0
     */
    default String getDataTypeBind() {
        return getProperty(PropertyNames.dataTypeBind);
    }

    /**
     * Sets the {@code dataTypeBind} configuration.
     * <p>
     * If the value is explicitly set to a non-null value and the connected server is Firebird 4 or higher, this will
     * configure the data type binding with the specified values using {@code isc_dpb_set_bind}, which is equivalent to
     * executing {@code SET BIND} statements with the values.
     * </p>
     * <p>
     * See also Firebird documentation for {@code SET BIND}.
     * </p>
     *
     * @param dataTypeBind
     *         Firebird 4+ data type bind configuration, a semicolon-separated list of {@code <from-type> TO <to-type>}
     * @since 4.0
     */
    default void setDataTypeBind(String dataTypeBind) {
        setProperty(PropertyNames.dataTypeBind, dataTypeBind);
    }

    /**
     * Get the {@code sessionTimeZone}.
     *
     * @return value for {@code sessionTimeZone}, or {@code null} for driver default (JVM default time zone)
     * @since 4.0
     */
    default String getSessionTimeZone() {
        return getProperty(PropertyNames.sessionTimeZone);
    }

    /**
     * Sets the {@code sessionTimeZone}.
     *
     * @param sessionTimeZone
     *         Firebird 4+ session time zone name (we strongly suggest to use Java compatible names only),
     *         use {@code "server"} to use server default time zone (note: conversion will use JVM default time zone)
     * @since 4.0
     */
    default void setSessionTimeZone(String sessionTimeZone) {
        setProperty(PropertyNames.sessionTimeZone, sessionTimeZone);
    }

    /**
     * @return BLOB buffer size in bytes.
     */
    default int getBlobBufferSize() {
        return getIntProperty(PropertyNames.blobBufferSize, PropertyConstants.DEFAULT_BLOB_BUFFER_SIZE);
    }

    /**
     * @param blobBufferSize
     *         size of the BLOB buffer in bytes.
     */
    default void setBlobBufferSize(int blobBufferSize) {
        setIntProperty(PropertyNames.blobBufferSize, blobBufferSize);
    }

    /**
     * @return {@code true} if stream blobs should be created, otherwise {@code false}.
     */
    default boolean isUseStreamBlobs() {
        return getBooleanProperty(PropertyNames.useStreamBlobs, PropertyConstants.DEFAULT_STREAM_BLOBS);
    }

    /**
     * @param useStreamBlobs
     *         {@code true} if stream blobs should be created, otherwise {@code false}.
     */
    default void setUseStreamBlobs(boolean useStreamBlobs) {
        setBooleanProperty(PropertyNames.useStreamBlobs, useStreamBlobs);
    }

    /**
     * Get whether ResultSets are holdable by default.
     *
     * @return {@code true} ResultSets by default are {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT},
     * {@code false} (default), ResultSets are {@link java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    default boolean isDefaultResultSetHoldable() {
        return getBooleanProperty(PropertyNames.defaultResultSetHoldable, PropertyConstants.DEFAULT_RESULT_SET_HOLDABLE);
    }

    /**
     * Set if {@link java.sql.ResultSet} should be {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT} by default.
     *
     * @param defaultResultSetHoldable
     *         {@code true} ResultSets are holdable, {@code false} (default) ResultSets are {@link
     *         java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    default void setDefaultResultSetHoldable(boolean defaultResultSetHoldable) {
        setBooleanProperty(PropertyNames.defaultResultSetHoldable, defaultResultSetHoldable);
    }

    /**
     * Get whether to use Firebird autocommit (experimental).
     *
     * @return {@code true} use Firebird autocommit
     * @since 2.2.9
     */
    default boolean isUseFirebirdAutocommit() {
        return getBooleanProperty(PropertyNames.useFirebirdAutocommit, PropertyConstants.DEFAULT_USE_FIREBIRD_AUTO_COMMIT);
    }

    /**
     * Set whether to use Firebird autocommit (experimental).
     *
     * @param useFirebirdAutocommit
     *         {@code true} Use Firebird autocommit
     * @since 2.2.9
     */
    default void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        setBooleanProperty(PropertyNames.useFirebirdAutocommit, useFirebirdAutocommit);
    }

    /**
     * Gets the current setting of {@code columnLabelForName}
     *
     * @return {@code false} JDBC compliant behavior ({@code columnName} is returned), {@code true} compatibility
     * option ({@code columnLabel} is returned)
     * @see #setColumnLabelForName(boolean)
     * @since 2.2.1
     */
    default boolean isColumnLabelForName() {
        return getBooleanProperty(PropertyNames.columnLabelForName, PropertyConstants.DEFAULT_COLUMN_LABEL_FOR_NAME);
    }

    /**
     * Set if {@link java.sql.ResultSetMetaData#getColumnName(int)} returns the {@code columnLabel} instead of the
     * {@code columnName}.
     * <p>
     * The default behaviour (with {@code columnLabelForName=false} is JDBC-compliant. The behavior for value
     * {@code true} is to provide compatibility with tools with a wrong expectation.
     * </p>
     *
     * @param columnLabelForName
     *         {@code false} JDBC compliant behavior ({@code columnName} is returned), {@code true} compatibility
     *         option ({@code columnLabel} is returned)
     * @since 2.2.1
     */
    default void setColumnLabelForName(boolean columnLabelForName) {
        setBooleanProperty(PropertyNames.columnLabelForName, columnLabelForName);
    }

    /**
     * Get the {@code generatedKeysEnabled} configuration.
     *
     * @return configuration value for {@code generatedKeysEnabled}, or {@code null} for driver default
     * @since 4.0
     */
    default String getGeneratedKeysEnabled() {
        return getProperty(PropertyNames.generatedKeysEnabled);
    }

    /**
     * Sets the {@code generatedKeysEnabled} configuration.
     *
     * @param generatedKeysEnabled
     *         Generated keys support configuration: {@code default} (or null/empty), {@code disabled}, {@code ignored},
     *         or a list of statement types to enable (possible values: {@code insert}, {@code update}, {@code delete},
     *         {@code update_or_insert}, {@code merge})
     * @since 4.0
     */
    default void setGeneratedKeysEnabled(String generatedKeysEnabled) {
        setProperty(PropertyNames.generatedKeysEnabled, generatedKeysEnabled);
    }

    /**
     * Get the value for {@code ignoreProcedureType}.
     *
     * @return value for {@code ignoreProcedureType}
     * @since 3.0.6
     */
    default boolean isIgnoreProcedureType() {
        return getBooleanProperty(PropertyNames.ignoreProcedureType, PropertyConstants.DEFAULT_IGNORE_PROCEDURE_TYPE);
    }

    /**
     * Sets the value {@code ignoreProcedureType}.
     * <p>
     * When set to true, the {@link java.sql.CallableStatement} implementation in Jaybird will ignore metadata
     * information about the stored procedure type and default to using {@code EXECUTE PROCEDURE}, unless the type is
     * explicitly set using {@link FirebirdCallableStatement#setSelectableProcedure(boolean)}. This can be useful in
     * situations where a stored procedure is selectable, but tooling or code expects an executable stored procedure.
     * </p>
     *
     * @param ignoreProcedureType
     *         {@code true} Ignore procedure type
     * @since 3.0.6
     */
    default void setIgnoreProcedureType(boolean ignoreProcedureType) {
        setBooleanProperty(PropertyNames.ignoreProcedureType, ignoreProcedureType);
    }

    /**
     * @return the server-side {@code DECFLOAT} rounding mode, {@code null} applies the Firebird server default
     */
    default String getDecfloatRound() {
        return getProperty(PropertyNames.decfloatRound);
    }

    /**
     * Sets the {@code DECFLOAT} rounding mode
     *
     * @param decfloatRound
     *         Firebird 4+ server-side {@code DECFLOAT} rounding mode ({@code ceiling, up, half_up, half_even,
     *         half_down, down, floor, reround}); {@code null} to apply the Firebird server default ({@code half_up} in
     *         Firebird 4)
     */
    default void setDecfloatRound(String decfloatRound) {
        setProperty(PropertyNames.decfloatRound, decfloatRound);
    }

    /**
     * @return the server-side {@code DECFLOAT} error traps, {@code null} applies the Firebird server default
     */
    default String getDecfloatTraps() {
        return getProperty(PropertyNames.decfloatTraps);
    }

    /**
     * Sets the {@code DECFLOAT} error traps.
     *
     * @param decfloatTraps
     *         Firebird 4+ server-side {@code DECFLOAT} error traps; comma-separated list with options
     *         {@code Division_by_zero, Inexact, Invalid_operation, Overflow, Underflow}; {@code null} to apply
     *         Firebird server default ({@code Division_by_zero,Invalid_operation,Overflow} in Firebird 4)
     */
    default void setDecfloatTraps(String decfloatTraps) {
        setProperty(PropertyNames.decfloatTraps, decfloatTraps);
    }

    /**
     * Get the used TPB mapping.
     *
     * @return resource bundle name of the TPB mapping
     * @see #setTpbMapping(String)
     */
    default String getTpbMapping() {
        return getProperty(PropertyNames.tpbMapping);
    }

    /**
     * Sets a <em>resource bundle</em> name with the TPB mapping.
     * <p>
     * For compatibility reasons, the prefix {@code "res:"} is allowed, but this works exactly the same as without a
     * prefix. We strongly recommend not to use the {@code "res:"} prefix, future versions of Jaybird (Jaybird 7 or
     * later) may stop supporting this.
     * </p>
     * <p>
     * The resource bundle should contain a mapping between the transaction isolation level (name of the constant in
     * the {@link java.sql.Connection} interface and a comma-separated list of TPB parameters).
     * </p>
     * <p>
     *
     * </p>
     *
     * @param tpbMapping
     *         name of the resource bundle
     * @throws IllegalStateException
     *         May be thrown when the mapping has already been initialized (not all implementations do this)
     */
    default void setTpbMapping(String tpbMapping) {
        setProperty(PropertyNames.tpbMapping, tpbMapping);
    }

    /**
     * Get the default transaction isolation level. This is the transaction isolation level for the newly created
     * connections.
     *
     * @return default transaction isolation level.
     */
    default int getDefaultTransactionIsolation() {
        return getIntProperty(PropertyNames.defaultIsolation, PropertyConstants.DEFAULT_TRANSACTION_ISOLATION_VALUE);
    }

    /**
     * Set the default transaction isolation level.
     *
     * @param defaultIsolationLevel
     *         default transaction isolation level.
     */
    default void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        setIntProperty(PropertyNames.defaultIsolation, defaultIsolationLevel);
    }

    /**
     * Get the default transaction isolation level as string. This method is complementary to
     * {@link #getDefaultTransactionIsolation()}, however it returns a string name instead of a numeric constant.
     *
     * @return default transaction isolation as string.
     * @see #setDefaultIsolation(String)
     */
    default String getDefaultIsolation() {
        return getProperty(PropertyNames.defaultIsolation, PropertyConstants.DEFAULT_TRANSACTION_ISOLATION_NAME);
    }

    /**
     * Set the default transaction isolation level as string. This method is complementary to
     * {@link #setDefaultTransactionIsolation(int)}, however it takes a string as parameter instead of a numeric
     * constant.
     * <p>
     * Following strings are allowed:
     * <ul>
     * <li>{@code "TRANSACTION_READ_COMMITTED"} for a READ COMMITTED isolation level.
     * <li>{@code "TRANSACTION_REPEATABLE_READ"} for a REPEATABLE READ isolation level.
     * <li>{@code "TRANSACTION_SERIALIZABLE"} for a SERIALIZABLE isolation level.
     * <li>Integer string values matching the isolation levels</li>
     * </ul>
     * </p>
     *
     * @param isolation
     *         string constant representing a default isolation level.
     */
    default void setDefaultIsolation(String isolation) {
        setProperty(PropertyNames.defaultIsolation, isolation);
    }

    /**
     * @return {@code true} if the Jaybird 1.0 handling of the calendar in corresponding setters. This is also
     * compatible with MySQL calendar treatment.
     * @deprecated This property has unclear semantics and will be removed in a future version (Jaybird 6 or later)
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    default boolean isTimestampUsesLocalTimezone() {
        return getBooleanProperty(
                PropertyNames.timestampUsesLocalTimezone, PropertyConstants.DEFAULT_TIMESTAMP_USES_LOCAL);
    }

    /**
     * Sets the value {@code timestampUsesLocalTimezone}.
     *
     * @param timestampUsesLocalTimezone
     *         {@code true} if the Jaybird 1.0 handling of the calendar in corresponding setters. This is also
     *         compatible with MySQL calendar treatment.
     * @deprecated This property has unclear semantics and will be removed in a future version (Jaybird 6 or later)
     */
    @Deprecated
    default void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        setBooleanProperty(PropertyNames.processName, timestampUsesLocalTimezone);
    }

    /**
     * @return Configuration of scrollable cursors, either {@code EMULATED} (default) or {@code SERVER} (case-insensitive)
     * @see #setScrollableCursor(String)
     */
    default String getScrollableCursor() {
        return getProperty(PropertyNames.scrollableCursor, PropertyConstants.DEFAULT_SCROLLABLE_CURSOR);
    }

    /**
     * Sets the type of scrollable cursor.
     * <p>
     * Possible values are (case-insensitive):
     * </p>
     * <ul>
     * <li>{@code EMULATED} (default) - emulate scrollable cursors in memory by fetching all rows</li>
     * <li>{@code SERVER} - user server-side scrollable cursor (requires Firebird 5.0 and pure-java connection).
     * Falls back to {@code EMULATED} behaviour when server-side support is not available, or when holdable cursors are
     * requested</li>
     * </ul>
     *
     * @param scrollableCursor
     *         Scrollable cursor type, one of {@code EMULATED} or {@code SERVER} (case-insensitive)
     */
    default void setScrollableCursor(String scrollableCursor) {
        setProperty(PropertyNames.scrollableCursor, scrollableCursor);
    }

    /**
     * @return {@code true} (default) use server-side batch if supported by server, {@code false} always use emulated batch
     * @see #setUseServerBatch(boolean)
     */
    default boolean isUseServerBatch() {
        return getBooleanProperty(PropertyNames.useServerBatch, PropertyConstants.DEFAULT_USE_SERVER_BATCH);
    }

    /**
     * Sets whether to use server-side batch support, if available.
     * <p>
     * Currently, server-side batch is only supported with Firebird 4.0 or higher, with a pure Java connection, using
     * a {@link java.sql.PreparedStatement}, but not a {@link java.sql.CallableStatement}, and only when not requesting
     * generated keys.
     * </p>
     * <p>
     * The implementation will fall back to emulated batches if either the server version doesn't support batches, or
     * if the statement cannot be executed using the server-side batch mechanism for other reasons (e.g. requesting
     * generated keys).
     * </p>
     *
     * @param useServerBatch
     *         {@code true}, use server-side batch support if possible, {@code false} always use emulated batch
     */
    default void setUseServerBatch(boolean useServerBatch) {
        setBooleanProperty(PropertyNames.useServerBatch, useServerBatch);
    }

    /**
     * @return batch buffer size in bytes, {@code < 0} to use server-side default (16MB as of Firebird 4.0),
     * {@code 0} (default) to use server-side maximum (256MB as of Firebird 4.0), values exceeding server-side maximum
     * will set server-side maximum
     * @see #setServerBatchBufferSize(int)
     */
    default int getServerBatchBufferSize() {
        return getIntProperty(PropertyNames.serverBatchBufferSize, PropertyConstants.DEFAULT_SERVER_BATCH_BUFFER_SIZE);
    }

    /**
     * Sets the server batch buffer size (if server batch is supported and enabled).
     *
     * @param serverBatchBufferSize
     *         server batch buffer size in bytes, use {@code < 0} to set server-side default (16MB as of Firebird 4.0),
     *         use {@code 0} to use server-side maximum (256MB as of Firebird 4.0), values exceeding server-side maximum
     *         will set server-side maximum
     */
    default void setServerBatchBufferSize(int serverBatchBufferSize) {
        setIntProperty(PropertyNames.serverBatchBufferSize, serverBatchBufferSize);
    }

}
