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

import org.firebirdsql.gds.ng.WireCrypt;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyDefinition;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyApplicability.DATABASE;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyApplicability.SERVICE;
import static org.firebirdsql.jaybird.props.def.ConnectionPropertyType.*;

/**
 * Attachment properties shared by database and service connections.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 5
 */
public interface AttachmentProperties extends BaseProperties {

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
    @ConnectionPropertyDefinition(
            name = PropertyNames.user,
            aliases = { "userName", "user_name", "isc_dpb_user_name" },
            type = STRING,
            applicability = { DATABASE, SERVICE },
            description = "Name of the user connecting to Firebird",
            dpbItem = isc_dpb_user_name,
            spbItem = isc_spb_user_name)
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
    @ConnectionPropertyDefinition(
            name = PropertyNames.password,
            aliases = { "isc_dpb_password" },
            type = STRING,
            applicability = { DATABASE, SERVICE },
            description = "Password corresponding to the specified user name",
            dpbItem = isc_dpb_password,
            spbItem = isc_spb_password)
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
    @ConnectionPropertyDefinition(
            name = PropertyNames.roleName,
            aliases = { "sqlRole", "role_name", "sql_role_name", "isc_dpb_sql_role_name" },
            type = STRING,
            applicability = { DATABASE, SERVICE },
            description = "Name of the SQL role",
            dpbItem = isc_dpb_sql_role_name,
            spbItem = isc_spb_sql_role_name)
    default void setRoleName(String roleName) {
        setProperty(PropertyNames.roleName, roleName);
    }

    // TODO Should charSet and/or encoding be moved to DatabaseConnectionProperties?

    /**
     * @return Java character set for the connection.
     */
    default String getCharSet() {
        return getProperty(PropertyNames.charSet);
    }

    /**
     * Set the Java character set for the connection.
     * <p>
     * Contrary to other parts of the codebase, the value of {@code encoding} should not be changed when
     * {@code charSet} is set.
     * </p>
     *
     * @param charSet
     *         Character set for the connection. Similar to {@code encoding} property, but accepts Java names instead
     *         of Firebird ones.
     * @see #setEncoding(String)
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.charSet,
            aliases = { "localEncoding", "local_encoding" },
            type = STRING,
            applicability = { DATABASE, SERVICE /* TODO not really? */ },
            description = "Java encoding for the client")
    default void setCharSet(String charSet) {
        setProperty(PropertyNames.charSet, charSet);
    }

    /**
     * @return Firebird character encoding for the connection.
     */
    default String getEncoding() {
        return getProperty(PropertyNames.encoding);
    }

    /**
     * Set the Firebird character set for the connection.
     * <p>
     * Contrary to other parts of the codebase, the value of {@code charSet} should not be changed when
     * {@code encoding} is set.
     * </p>
     *
     * @param encoding
     *         Firebird character encoding for the connection. See Firebird documentation for more information.
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.encoding,
            aliases = { "lc_ctype", "isc_dpb_lc_ctype" },
            type = STRING,
            applicability = { DATABASE, SERVICE /* TODO not really? */ },
            description = "Client encoding for the database",
            dpbItem = isc_dpb_lc_ctype)
    default void setEncoding(String encoding) {
        setProperty(PropertyNames.encoding, encoding);
    }

    /**
     * Get the socket buffer size.
     *
     * @return socket buffer size in bytes, or -1 if not specified.
     */
    default int getSocketBufferSize() {
        return getIntProperty(PropertyNames.socketBufferSize, -1);
    }

    /**
     * Set the socket buffer size.
     *
     * @param socketBufferSize
     *         socket buffer size in bytes.
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.socketBufferSize,
            aliases = { "socket_buffer_size" },
            type = INT,
            defaultValue = "-1",
            applicability = { DATABASE, SERVICE },
            description = "Size of the TCP/IP socket buffer")
    default void setSocketBufferSize(int socketBufferSize) {
        setIntProperty(PropertyNames.socketBufferSize, socketBufferSize);
    }

    /**
     * Get the initial Socket blocking timeout (SoTimeout).
     *
     * @return The initial socket blocking timeout in milliseconds (0 is 'infinite')
     */
    default int getSoTimeout() {
        return getIntProperty(PropertyNames.soTimeout, -1);
    }

    /**
     * Set the initial Socket blocking timeout (SoTimeout).
     *
     * @param soTimeout
     *         Timeout in milliseconds (0 is 'infinite')
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.soTimeout,
            aliases = { "so_timeout" },
            type = INT,
            defaultValue = "-1",
            applicability = { DATABASE, SERVICE },
            description = "Socket blocking timeout (in milliseconds)")
    default void setSoTimeout(int soTimeout) {
        setIntProperty(PropertyNames.soTimeout, soTimeout);
    }

    /**
     * Get the connect timeout in seconds.
     *
     * @return Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout)
     */
    default int getConnectTimeout() {
        return getIntProperty(PropertyNames.connectTimeout, -1);
    }

    /**
     * Set the connect timeout in seconds.
     *
     * @param connectTimeout
     *         Connect timeout in seconds (0 is 'infinite', or better: OS specific timeout)
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.connectTimeout,
            aliases = { "connect_timeout", "isc_dpb_connect_timeout" },
            type = INT,
            defaultValue = "-1",
            applicability = { DATABASE, SERVICE },
            description = "Connect timeout (in seconds)",
            dpbItem = isc_dpb_connect_timeout,
            spbItem = isc_spb_connect_timeout)
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
        return getProperty(PropertyNames.wireCrypt);
    }

    /**
     * Set the wire encryption level.
     * <p>
     * Values are defined by {@link WireCrypt}, values are handled case insensitive.
     * Invalid values are accepted, but will cause an error when a connection is established. TODO: Verify if still correct
     * </p>
     *
     * @param wireCrypt
     *         Wire encryption level ({@code null} not allowed)
     * @since 4.0
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.wireCrypt,
            aliases = { "wire_crypt_level" },
            type = STRING,
            choices = { "DEFAULT", "REQUIRED", "ENABLED", "DISABLED" },
            defaultValue = "DEFAULT",
            applicability = { DATABASE, SERVICE },
            description = "FB3+ wire crypt level (disabled, enabled, required, default)")
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
    @ConnectionPropertyDefinition(
            name = PropertyNames.dbCryptConfig,
            aliases = { "db_crypt_config" },
            type = STRING,
            applicability = { DATABASE, SERVICE },
            description = "FB3+ database encryption config (format is plugin specific)")
    default void setDbCryptConfig(String dbCryptConfig) {
        setProperty(PropertyNames.dbCryptConfig, dbCryptConfig);
    }

    /**
     * Get the list of authentication plugins to try.
     *
     * @return comma-separated list of authentication plugins, or {@code null} for driver default
     * @since 4.0
     */
    default String getAuthPlugins() {
        return getProperty(PropertyNames.authPlugins);
    }

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
    @ConnectionPropertyDefinition(
            name = PropertyNames.authPlugins,
            aliases = { "auth_plugin_list", "isc_dpb_auth_plugin_list" },
            type = STRING,
            applicability = { DATABASE, SERVICE },
            description = "FB3+ database authentication plugins to try",
            dpbItem = isc_dpb_auth_plugin_list,
            spbItem = isc_spb_auth_plugin_list)
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
        return getBooleanProperty(PropertyNames.wireCompression, false);
    }

    /**
     * Sets if the connection should try to enable wire compression.
     *
     * @param wireCompression
     *         {@code true} enable wire compression, {@code false} disable wire compression (the default)
     * @see #isWireCompression()
     * @since 4.0
     */
    @ConnectionPropertyDefinition(
            name = PropertyNames.wireCompression,
            aliases = { "wire_compression" },
            type = BOOLEAN,
            defaultValue = "false",
            applicability = { DATABASE, SERVICE },
            description = "FB3+ Enable wire compression (default is disabled)")
    default void setWireCompression(boolean wireCompression) {
        setBooleanProperty(PropertyNames.wireCompression, wireCompression);
    }

}
