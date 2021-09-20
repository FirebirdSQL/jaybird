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

import org.firebirdsql.gds.ng.FbAttachment;
import org.firebirdsql.gds.ng.WireCrypt;

import static org.firebirdsql.jaybird.props.PropertyConstants.DEFAULT_WIRE_COMPRESSION;

/**
 * Attachment properties shared by database and service connections.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
     * @return type of the connection, for example, "PURE_JAVA", "LOCAL", "EMBEDDED", depends on the GDS implementations
     * installed in the system.
     */
    default String getType() {
        return getProperty(PropertyNames.type);
    }

    /**
     * @param type
     *         type of the connection, for example, "PURE_JAVA", "LOCAL", "EMBEDDED", depends on the GDS implementations
     *         installed in the system.
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
     * Get the connect timeout in seconds.
     *
     * @return Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout), or {@code -1} if not set
     */
    default int getConnectTimeout() {
        return getIntProperty(PropertyNames.connectTimeout, PropertyConstants.TIMEOUT_NOT_SET);
    }

    /**
     * Set the connect timeout in seconds.
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

}
