/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.WireCrypt;

import java.sql.SQLException;

/**
 * Connection properties for the Firebird connection. Main part of this
 * interface corresponds to the Database Parameter Buffer, but also contains
 * properties to specify default transaction parameters.
 */
public interface FirebirdConnectionProperties {

    /**
     * @return path to the database including the server name and the port,
     * if needed.
     */
    String getDatabase();

    /**
     * @param database
     *         path to the database including the server name and the
     *         port, if needed.
     */
    void setDatabase(String database);

    /**
     * @return type of the connection, for example, "PURE_JAVA", "LOCAL",
     * "EMBEDDED", depends on the GDS implementations installed in the system.
     */
    String getType();

    /**
     * @param type
     *         type of the connection, for example, "PURE_JAVA", "LOCAL",
     *         "EMBEDDED", depends on the GDS implementations installed in the system.
     */
    void setType(String type);

    /**
     * @return BLOB buffer size in bytes.
     */
    int getBlobBufferSize();

    /**
     * @param bufferSize
     *         size of the BLOB buffer in bytes.
     */
    void setBlobBufferSize(int bufferSize);

    /**
     * @return Character set for the connection.
     * @see #setCharSet(String)
     */
    String getCharSet();

    /**
     * @param charSet
     *         Character set for the connection. Similar to
     *         <code>encoding</code> property, but accepts Java names
     *         instead of Firebird ones.
     */
    void setCharSet(String charSet);

    /**
     * @return Character encoding for the connection.
     * @see #setEncoding(String)
     */
    String getEncoding();

    /**
     * @param encoding
     *         Character encoding for the connection. See Firebird
     *         documentation for more information.
     */
    void setEncoding(String encoding);

    /**
     * @return SQL role to use.
     */
    String getRoleName();

    /**
     * @param roleName
     *         SQL role to use.
     */
    void setRoleName(String roleName);

    /**
     * @return SQL dialect of the client.
     */
    String getSqlDialect();

    /**
     * @param sqlDialect
     *         SQL dialect of the client.
     */
    void setSqlDialect(String sqlDialect);

    /**
     * @return <code>true</code> if stream blobs should be created, otherwise
     * <code>false</code>.
     */
    boolean isUseStreamBlobs();

    /**
     * @param useStreamBlobs
     *         <code>true</code> if stream blobs should be created,
     *         otherwise <code>false</code>.
     */
    void setUseStreamBlobs(boolean useStreamBlobs);

    /**
     * @return <code>true</code> if driver should assume that standard UDF are
     * installed.
     */
    boolean isUseStandardUdf();

    /**
     * @param useStandardUdf
     *         <code>true</code> if driver should assume that standard UDF
     *         are installed.
     */
    void setUseStandardUdf(boolean useStandardUdf);

    /**
     * @return socket buffer size in bytes, or -1 is not specified.
     */
    int getSocketBufferSize();

    /**
     * @param socketBufferSize
     *         socket buffer size in bytes.
     */
    void setSocketBufferSize(int socketBufferSize);

    /**
     * @return <code>true</code> if the Jaybird 1.0 handling of the calendar
     * in corresponding setters. This is also compatible with MySQL
     * calendar treatment.
     */
    boolean isTimestampUsesLocalTimezone();

    /**
     * @param timestampUsesLocalTimezone
     *         <code>true</code> if the Jaybird 1.0 handling of the
     *         calendar in corresponding setters. This is also compatible
     *         with MySQL calendar treatment.
     */
    void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone);

    /**
     * @return name of the user that will be used when connecting to the database.
     */
    String getUserName();

    /**
     * @param userName
     *         name of the user that will be used when connecting to the database.
     */
    void setUserName(String userName);

    /**
     * @return password corresponding to the specified user name.
     */
    String getPassword();

    /**
     * @param password
     *         password corresponding to the specified user name.
     */
    void setPassword(String password);

    /**
     * @return number of cache buffers that should be allocated for this
     * connection, should be specified for ClassicServer instances,
     * SuperServer has a server-wide configuration parameter.
     */
    int getBuffersNumber();

    /**
     * @param buffersNumber
     *         number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    void setBuffersNumber(int buffersNumber);

    /**
     * Get the property that does not have corresponding getter method by its
     * name.
     *
     * @param key
     *         name of the property to get.
     * @return value of the property.
     */
    String getNonStandardProperty(String key);

    /**
     * Set the property that does not have corresponding setter method.
     *
     * @param key
     *         name of the property to set.
     * @param value
     *         value of the property.
     */
    void setNonStandardProperty(String key, String value);

    /**
     * Set the property that does not have corresponding setter method.
     *
     * @param propertyMapping
     *         parameter value in the ?propertyName[=propertyValue]? form,
     *         this allows setting non-standard parameters using
     *         configuration files.
     */
    void setNonStandardProperty(String propertyMapping);

    /**
     * Get the database parameter buffer corresponding to the current connection
     * request information.
     *
     * @return instance of {@link DatabaseParameterBuffer}.
     * @throws SQLException
     *         if database parameter buffer cannot be created.
     */
    DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException;

    /**
     * Get the used TPB mapping.
     *
     * @return path to the TPB mapping.
     * @see #setTpbMapping(String)
     */
    String getTpbMapping();

    /**
     * Set path to the properties file with the TPB mapping. The path begins
     * with the protocol specification followed by the path to the resource. A
     * special protocol <code>"res:"</code> should be used to specify resource
     * in the classpath.
     * <p/>
     * For the compatibility reasons, if no protocol is specified, classpath is
     * used by default.
     * <p/>
     * Properties file contains a mapping between the transaction isolation
     * level (name of the constant in the {@link java.sql.Connection} interface
     * and a comma-separated list of TPB parameters.
     *
     * @param tpbMapping
     *         path to the properties file.
     */
    void setTpbMapping(String tpbMapping);

    /**
     * Get the default transaction isolation level. This is the transaction
     * isolation level for the newly created connections.
     *
     * @return default transaction isolation level.
     */
    int getDefaultTransactionIsolation();

    /**
     * Set the default transaction isolation level.
     *
     * @param defaultIsolationLevel
     *         default transaction isolation level.
     */
    void setDefaultTransactionIsolation(int defaultIsolationLevel);

    /**
     * Get the default transaction isolation level as string. This method is
     * complementary to the {@link #getDefaultTransactionIsolation()}, however
     * it takes a string as parameter instead of a numeric constant.
     *
     * @return default transaction isolation as string.
     * @see #setDefaultIsolation(String)
     */
    String getDefaultIsolation();

    /**
     * Set the default transaction isolation level as string. This method is
     * complementary to the {@link #setDefaultTransactionIsolation(int)},
     * however it takes a string as parameter instead of a numeric constant.
     * <p/>
     * Following strings are allowed:
     * <ul>
     * <li><code>"TRANSACTION_READ_COMMITTED"</code> for a READ COMMITTED
     * isolation level.
     * <li><code>"TRANSACTION_REPEATABLE_READ"</code> for a REPEATABLE READ
     * isolation level.
     * <li><code>"TRANSACTION_SERIALIZABLE"</code> for a SERIALIZABLE
     * isolation level.
     * </ul>
     *
     * @param isolation
     *         string constant representing a default isolation level.
     */
    void setDefaultIsolation(String isolation);

    /**
     * Get the transaction parameter buffer corresponding to the current
     * connection request information.
     *
     * @param isolation
     *         transaction isolation level for which TPB should be returned.
     * @return instance of {@link TransactionParameterBuffer}.
     */
    TransactionParameterBuffer getTransactionParameters(int isolation);

    /**
     * Set transaction parameters for the specified transaction isolation level.
     * The specified TPB is used as a default mapping for the specified
     * isolation level.
     *
     * @param isolation
     *         transaction isolation level.
     * @param tpb
     *         instance of {@link TransactionParameterBuffer} containing
     *         transaction parameters.
     */
    void setTransactionParameters(int isolation, TransactionParameterBuffer tpb);

    /**
     * Get the default ResultSet holdability.
     *
     * @return <code>true</code> when ResultSets are holdable by default, <code>false</code> not holdable.
     */
    boolean isDefaultResultSetHoldable();

    /**
     * Sets the default ResultSet holdability.
     *
     * @param isHoldable
     *         <code>true</code> when ResultSets are holdable by default, <code>false</code> not holdable.
     */
    void setDefaultResultSetHoldable(boolean isHoldable);

    /**
     * Get the current Socket blocking timeout (SoTimeout).
     *
     * @return The socket blocking timeout in milliseconds (0 is 'infinite')
     */
    int getSoTimeout();

    /**
     * Set the Socket blocking timeout (SoTimeout).
     *
     * @param soTimeout
     *         Timeout in milliseconds (0 is 'infinite')
     */
    void setSoTimeout(int soTimeout);

    /**
     * Get the current connect timeout.
     *
     * @return Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout)
     */
    int getConnectTimeout();

    /**
     * Set the connect timeout.
     *
     * @param connectTimeout
     *         Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout)
     */
    void setConnectTimeout(int connectTimeout);

    /**
     * Get whether to use Firebird autocommit (experimental).
     *
     * @return {@code true} use Firebird autocommit
     */
    boolean isUseFirebirdAutocommit();

    /**
     * Set whether to use Firebird autocommit (experimental).
     *
     * @param useFirebirdAutocommit
     *         {@code true} Use Firebird autocommit
     */
    void setUseFirebirdAutocommit(boolean useFirebirdAutocommit);

    /**
     * Get the wire encryption level value.
     *
     * @return Wire encryption level ({@code null} implies {@code DEFAULT})
     * @since 4.0
     */
    String getWireCrypt();

    /**
     * Sets the wire encryption level.
     * <p>
     * Values are defined by {@link WireCrypt}, values are handled case insensitive.
     * Invalid values are accepted, but will cause an error when a connection is established.
     * </p>
     *
     * @param wireCrypt
     *         Wire encryption level
     * @since 4.0
     */
    void setWireCrypt(String wireCrypt);

    /**
     * Get the database encryption plugin configuration.
     *
     * @return Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    String getDbCryptConfig();

    /**
     * Sets the database encryption plugin configuration.
     *
     * @param dbCryptConfig
     *         Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    void setDbCryptConfig(String dbCryptConfig);

    /**
     * Get the list of authentication plugins to try.
     *
     * @return comma-separated list of authentication plugins, or {@code null} for driver default
     * @since 4.0
     */
    String getAuthPlugins();

    /**
     * Sets the authentication plugins to try.
     * <p>
     * Invalid names are skipped during authentication.
     * </p>
     *
     * @param authPlugins
     *         comma-separated list of authentication plugins, or {@code null} for driver default
     * @since 4.0
     */
    void setAuthPlugins(String authPlugins);

    /**
     * Get the {@code generatedKeysEnabled} configuration.
     *
     * @return configuration value for {@code generatedKeysEnabled}, or {@code null} for driver default
     * @since 4.0
     */
    String getGeneratedKeysEnabled();

    /**
     * Sets the {@code generatedKeysEnabled} configuration.
     *
     * @param generatedKeysEnabled
     *         Generated keys support configuration: {@code default} (or null/empty), {@code disabled}, {@code ignored},
     *         or a list of statement types to enable (possible values: {@code insert}, {@code update}, {@code delete},
     *         {@code update_or_insert}, {@code merge})
     */
    void setGeneratedKeysEnabled(String generatedKeysEnabled);

    /**
     * Get the {@code dataTypeBind} configuration.
     *
     * @return configuration value for {@code dataTypeBind}, or {@code null} for driver default
     * @since 4.0
     */
    String getDataTypeBind();

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
    void setDataTypeBind(String dataTypeBind);

    /**
     * Get the {@code sessionTimeZone}.
     *
     * @return value for {@code sessionTimeZone}, or {@code null} for driver default (JVM default time zone)
     * @since 4.0
     */
    String getSessionTimeZone();

    /**
     * Sets the {@code sessionTimeZone}.
     *
     * @param sessionTimeZone
     *         Firebird 4+ session time zone name (we strongly suggest to use Java compatible names only),
     *         use {@code "server"} to use server default time zone (note: conversion will use JVM default time zone)
     * @since 4.0
     */
    void setSessionTimeZone(String sessionTimeZone);

    /**
     * Get the value for {@code ignoreProcedureType}.
     *
     * @return value for {@code ignoreProcedureType}
     * @since 3.0.6
     */
    boolean isIgnoreProcedureType();

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
    void setIgnoreProcedureType(boolean ignoreProcedureType);

    /**
     * Get if wire compression should be enabled.
     * <p>
     * Wire compression requires Firebird 3 or higher, and the server must have the zlib library. If compression cannot
     * be negotiated, the connection will be made without wire compression.
     * </p>
     * <p>
     * This property will be ignored for native connections. For native connections, the configuration in
     * {@code firebird.conf} read by the client library will be used.
     * </p>
     *
     * @return {@code true} wire compression enabled
     * @since 4.0
     */
    boolean isWireCompression();

    /**
     * Sets if the connection should try to enable wire compression.
     *
     * @param wireCompression
     *         {@code true} enable wire compression, {@code false} disable wire compression (the default)
     * @see #isWireCompression()
     * @since 4.0
     */
    void setWireCompression(boolean wireCompression);
}
