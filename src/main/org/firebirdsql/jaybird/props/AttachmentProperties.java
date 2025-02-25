// SPDX-FileCopyrightText: Copyright 2020-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.jaybird.props;

import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.WireCrypt;

import static org.firebirdsql.jaybird.props.PropertyConstants.DEFAULT_WIRE_COMPRESSION;

/**
 * Attachment properties shared by database and service connections.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public interface AttachmentProperties extends BaseProperties {

    /**
     * Get the hostname or IP address of the Firebird server.
     *
     * @return Hostname or IP address of the server
     * @see #setServerName(String)
     */
    default String getServerName() {
        return getProperty(PropertyNames.serverName);
    }

    /**
     * Set the hostname or IP address of the Firebird server.
     * <p>
     * When set to {@code null} (the default), the {@code databaseName} or {@code serviceName} is used as the full
     * identification of the database host, port and database path/alias. Protocol implementations, for example
     * {@code PURE_JAVA}, may default to {@code localhost} when this property is {@code null}, but
     * {@code databaseName}/{@code serviceName} does not (seem to) contain a host name.
     * </p>
     *
     * @param serverName
     *         Hostname or IP address of the server
     */
    default void setServerName(String serverName) {
        setProperty(PropertyNames.serverName, serverName);
    }

    /**
     * Get the port number of the server.
     *
     * @return Port number of the server
     * @see #setPortNumber(int)
     */
    default int getPortNumber() {
        return getIntProperty(PropertyNames.portNumber, PropertyConstants.DEFAULT_PORT);
    }

    /**
     * Set the port number of the server.
     * <p>
     * Defaults to {@code 3050}. This property value will be ignored if {@code serverName} is {@code null}, unless the
     * protocol implementation needs a hostname, but cannot find a hostname in {@code databaseName}/{@code serviceName}.
     * </p>
     *
     * @param portNumber
     *         Port number of the server
     * @see #setServerName(String)
     */
    default void setPortNumber(int portNumber) {
        setIntProperty(PropertyNames.portNumber, portNumber);
    }

    /**
     * @return type of the connection, for example, "PURE_JAVA", "NATIVE", "EMBEDDED", depends on the GDS
     * implementations installed in the system.
     */
    default String getType() {
        return getProperty(PropertyNames.type);
    }

    /**
     * @param type
     *         type of the connection, for example, "PURE_JAVA", "NATIVE", "EMBEDDED", depends on the GDS
     *         implementations installed in the system.
     * @throws IllegalStateException
     *         may be thrown when type cannot or can no longer be changed
     */
    default void setType(String type) {
        setProperty(PropertyNames.type, type);
    }

    /**
     * @return Name of the user to authenticate to the server.
     */
    default String getUser() {
        return getProperty(PropertyNames.user);
    }

    /**
     * @param user
     *         Name of the user to authenticate to the server.
     */
    default void setUser(String user) {
        setProperty(PropertyNames.user, user);
    }

    /**
     * @return Password to authenticate to the server.
     */
    default String getPassword() {
        return getProperty(PropertyNames.password);
    }

    /**
     * @param password
     *         Password to authenticate to the server.
     */
    default void setPassword(String password) {
        setProperty(PropertyNames.password, password);
    }

    /**
     * @return SQL role to use.
     */
    default String getRoleName() {
        return getProperty(PropertyNames.roleName);
    }

    /**
     * @param roleName
     *         SQL role to use.
     */
    default void setRoleName(String roleName) {
        setProperty(PropertyNames.roleName, roleName);
    }

    /**
     * Java character set configured for the connection.
     * <p>
     * After connect, the actual Java character set applied can be obtained from
     * {@link FbAttachment#getEncoding()} (property {@code charsetName}), or {@link FbAttachment#getEncodingFactory()}
     * (properties {@code defaultEncoding.charsetName} or {code defaultEncodingDefinition.javaCharset}).
     * </p>
     *
     * @return Java character set for the connection ({@code null} when not explicitly configured).
     */
    default String getCharSet() {
        return getProperty(PropertyNames.charSet);
    }

    /**
     * Set the Java character set for the connection.
     * <p>
     * It is possible to set both the {@code charSet} and {@code encoding} to achieve a character set conversion effect,
     * but in general only one of both properties should be set.
     * </p>
     *
     * @param charSet
     *         Character set for the connection. Similar to {@code encoding} property, but accepts Java names instead
     *         of Firebird ones.
     * @see #setEncoding(String)
     */
    default void setCharSet(String charSet) {
        setProperty(PropertyNames.charSet, charSet);
    }

    /**
     * Firebird character set configured for the connection.
     * <p>
     * After connect, the actual Firebird character set applied can be obtained from
     * {@link FbAttachment#getEncodingFactory()}, property {@code defaultEncodingDefinition.firebirdEncodingName}.
     * </p>
     *
     * @return Firebird character encoding for the connection ({@code null} when not explicitly configured).
     */
    default String getEncoding() {
        return getProperty(PropertyNames.encoding);
    }

    /**
     * Set the Firebird character set for the connection.
     * <p>
     * It is possible to set both the {@code charSet} and {@code encoding} to achieve a character set conversion effect,
     * but in general only one of both properties should be set.
     * </p>
     *
     * @param encoding
     *         Firebird character encoding for the connection. See Firebird documentation for more information.
     * @see #setCharSet(String)
     */
    default void setEncoding(String encoding) {
        setProperty(PropertyNames.encoding, encoding);
    }

    /**
     * @return Custom process id sent to Firebird on attach; {@code null} means the default is applied (read from
     * system property {@code org.firebirdsql.jdbc.pid}, future versions may also determine the actual process id)
     */
    default Integer getProcessId() {
        return getIntProperty(PropertyNames.processId);
    }

    /**
     * Sets a custom process id to send to Firebird on attach.
     *
     * @param processId
     *         The process id to send; {@code null} to apply the default behaviour (see {@link #getProcessId()})
     */
    default void setProcessId(Integer processId) {
        setIntProperty(PropertyNames.processId, processId);
    }

    /**
     * @return Custom process name sent to Firebird on attach; {@code null} means the default is applied (read from
     * system property {@code org.firebirdsql.jdbc.processName})
     */
    default String getProcessName() {
        return getProperty(PropertyNames.processName);
    }

    /**
     * Sets a custom process name to send to Firebird on attach
     *
     * @param processName
     *         The process name to send; {@code null} to apply the default behaviour (see {@link #getProcessName()})
     */
    default void setProcessName(String processName) {
        setProperty(PropertyNames.processName, processName);
    }

    /**
     * Get the socket buffer size.
     *
     * @return socket buffer size in bytes, or {@code -1} if not set
     */
    default int getSocketBufferSize() {
        return getIntProperty(PropertyNames.socketBufferSize, PropertyConstants.BUFFER_SIZE_NOT_SET);
    }

    /**
     * Set the socket buffer size.
     *
     * @param socketBufferSize
     *         socket buffer size in bytes.
     */
    default void setSocketBufferSize(int socketBufferSize) {
        setIntProperty(PropertyNames.socketBufferSize, socketBufferSize);
    }

    /**
     * Get the initial Socket blocking timeout (SoTimeout).
     *
     * @return The initial socket blocking timeout in milliseconds (0 is 'infinite'), or {@code -1} if not set
     */
    default int getSoTimeout() {
        return getIntProperty(PropertyNames.soTimeout, PropertyConstants.TIMEOUT_NOT_SET);
    }

    /**
     * Set the initial Socket blocking timeout (SoTimeout).
     *
     * @param soTimeout
     *         Timeout in milliseconds (0 is 'infinite')
     */
    default void setSoTimeout(int soTimeout) {
        setIntProperty(PropertyNames.soTimeout, soTimeout);
    }

    /**
     * Get the <em>connect timeout</em> in seconds.
     *
     * @return Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout), or {@code -1} if not set
     */
    default int getConnectTimeout() {
        return getIntProperty(PropertyNames.connectTimeout, PropertyConstants.TIMEOUT_NOT_SET);
    }

    /**
     * Set the <em>connect timeout</em> in seconds.
     *
     * @param connectTimeout
     *         Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout)
     */
    default void setConnectTimeout(int connectTimeout) {
        setIntProperty(PropertyNames.connectTimeout, connectTimeout);
    }

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 4.0
     */
    default String getWireCrypt() {
        return getProperty(PropertyNames.wireCrypt, PropertyConstants.WIRE_CRYPT_DEFAULT);
    }

    /**
     * Set the wire encryption level.
     * <p>
     * Values are defined by {@link WireCrypt}, values are handled case insensitive. Invalid values will throw an
     * exception.
     * </p>
     *
     * @param wireCrypt
     *         Wire encryption level ({@code null} not allowed)
     * @throws InvalidPropertyValueException
     *         If the value is not one of the allowed values
     * @since 4.0
     */
    default void setWireCrypt(String wireCrypt) {
        setProperty(PropertyNames.wireCrypt, wireCrypt);
    }

    /**
     * Get the database encryption plugin configuration.
     *
     * @return Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    default String getDbCryptConfig() {
        return getProperty(PropertyNames.dbCryptConfig);
    }

    /**
     * Sets the database encryption plugin configuration.
     *
     * @param dbCryptConfig
     *         Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    default void setDbCryptConfig(String dbCryptConfig) {
        setProperty(PropertyNames.dbCryptConfig, dbCryptConfig);
    }

    /**
     * Get the list of authentication plugins to try.
     *
     * @return comma-separated list of authentication plugins
     * @since 4.0
     */
    default String getAuthPlugins() {
        return getProperty(PropertyNames.authPlugins, PropertyConstants.DEFAULT_AUTH_PLUGINS);
    }

    /**
     * Sets the authentication plugins to try.
     * <p>
     * Invalid names are skipped during authentication.
     * </p>
     *
     * @param authPlugins
     *         comma-separated list of authentication plugins
     * @since 4.0
     */
    default void setAuthPlugins(String authPlugins) {
        setProperty(PropertyNames.authPlugins, authPlugins);
    }

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
    default boolean isWireCompression() {
        return getBooleanProperty(PropertyNames.wireCompression, DEFAULT_WIRE_COMPRESSION);
    }

    /**
     * Sets if the connection should try to enable wire compression.
     *
     * @param wireCompression
     *         {@code true} enable wire compression, {@code false} disable wire compression (the default)
     * @see #isWireCompression()
     * @since 4.0
     */
    default void setWireCompression(boolean wireCompression) {
        setBooleanProperty(PropertyNames.wireCompression, wireCompression);
    }

    /**
     * Comma-separated list of additionally enabled protocols.
     * <p>
     * By default, pure Java connections of Jaybird only supports the protocol versions of supported Firebird versions.
     * This property lists the additionally enabled unsupported protocol versions. If Jaybird does not have a listed
     * protocol, it is silently ignored.
     * </p>
     * <p>
     * This property is ignored for native connections.
     * </p>
     *
     * @return List of unsupported protocol versions to try in addition to the supported protocols. Comma-separated
     * using only the version number (e.g. {@code "10,11"}). Both the unmasked and masked version are supported (e.g.
     * {@code 32780} for protocol {@code 12}), but we recommend to use the unmasked version. The value {@code "*"} will
     * try all available protocols.
     * @since 6
     */
    default String getEnableProtocol() {
        return getProperty(PropertyNames.enableProtocol);
    }

    /**
     * Comma-separated list of additionally enabled protocols.
     *
     * @param enableProtocol
     *         List of unsupported protocol versions to try in addition to the supported protocols.
     * @see #getEnableProtocol()
     * @since 6
     */
    default void setEnableProtocol(String enableProtocol) {
        setProperty(PropertyNames.enableProtocol, enableProtocol);
    }

    /**
     * @return number of parallel workers, {@code -1} means no value was set (or it was explicitly set to {@code -1})
     * @since 5.0.2
     */
    default int getParallelWorkers() {
        return getIntProperty(PropertyNames.parallelWorkers, PropertyConstants.PARALLEL_WORKERS_NOT_SET);
    }

    /**
     * Sets the number of parallel workers of the connection.
     * <p>
     * Requires Firebird 5.0 or higher, and a Firebird server configured with {@code MaxParallelWorkers} higher than
     * specified by {@code parallelWorkers}.
     * </p>
     * <p>
     * NOTE: For service attachments, this property controls behaviour only for specific operations, and requires
     * Jaybird to explicitly set the parallel workers for that operation.
     * </p>
     *
     * @param parallelWorkers
     *         number of parallel workers
     * @since 5.0.2
     */
    default void setParallelWorkers(int parallelWorkers) {
        setIntProperty(PropertyNames.parallelWorkers, parallelWorkers);
    }

    /**
     * The class name of a custom socket factory to be used for pure Java connections.
     *
     * @return fully-qualified class name of a {@link javax.net.SocketFactory} implementation, or (default) {@code null}
     * for the default socket factory
     * @since 6
     * @see #setSocketFactory(String)
     */
    default String getSocketFactory() {
        return getProperty(PropertyNames.socketFactory);
    }

    /**
     * Sets the class name of a custom socket factory to be used for pure Java connections.
     * <p>
     * The class must extend {@link javax.net.SocketFactory} and have a public single-arg constructor accepting
     * a {@link java.util.Properties}, or a public no-arg constructor. The {@code Properties} object passed in the first
     * case contains custom connection properties with the suffix {@code @socketFactory}, and &mdash; possibly &mdash;
     * other selected properties.
     * </p>
     *
     * @param socketFactory
     *         fully-qualified class name of a {@link javax.net.SocketFactory} implementation, or {@code null} for
     *         the default socket factory
     * @since 6
     */
    default void setSocketFactory(String socketFactory) {
        setProperty(PropertyNames.socketFactory, socketFactory);
    }

}
