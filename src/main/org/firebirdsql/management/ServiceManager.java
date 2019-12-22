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
package org.firebirdsql.management;

import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.WireCrypt;

import java.io.OutputStream;
import java.sql.SQLException;

/**
 * The base Firebird Service API functionality.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steven Jardine</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface ServiceManager {

    /**
     * Sets the encoding used for encoding or decoding string values.
     * <p>
     * If not set (or null), defaults to the value of system property {@code file.encoding}/
     * </p>
     *
     * @param charSet Java charset name.
     */
    void setCharSet(String charSet);

    String getCharSet();

    /**
     * Sets the username for the connection to the service manager.
     *
     * @param user
     *         for the connection to the service manager.
     */
    void setUser(String user);

    /**
     * Returns the username for the connection to the service manager.
     *
     * @return the username for the connection to the service manager.
     */
    String getUser();

    /**
     * Sets the password for the connection to the service manager.
     *
     * @param password
     *         for the connection to the service manager.
     */
    void setPassword(String password);

    /**
     * Returns the password for the connection to the service manager.
     *
     * @return the password for the connection to the service manager.
     */
    String getPassword();

    /**
     * Sets the database path for the connection to the service manager.
     *
     * @param database
     *         path for the connection to the service manager.
     */
    void setDatabase(String database);

    /**
     * Returns the database path for the connection to the service manager.
     *
     * @return the database path for the connection to the service manager.
     */
    String getDatabase();

    /**
     * Returns the host for the connection to the service manager.
     *
     * @return the host for the connection to the service manager.
     */
    String getHost();

    /**
     * Sets the host for the connection to the service manager.
     *
     * @param host
     *         for the connection to the service manager.
     */
    void setHost(String host);

    /**
     * Returns the port for the connection to the service manager.
     *
     * @return the port for the connection to the service manager.
     */
    int getPort();

    /**
     * Sets the port for the connection to the service manager.
     *
     * @param port
     *         for the connection to the service manager.
     */
    void setPort(int port);

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 4.0
     */
    WireCrypt getWireCrypt();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt Wire encryption level ({@code null} not allowed)
     * @since 4.0
     */
    void setWireCrypt(WireCrypt wireCrypt);

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
     * @param dbCryptConfig Database encryption plugin configuration, meaning plugin specific
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

    /**
     * Returns the logger for the connection to the service manager.
     *
     * @return the logger for the connection to the service manager.
     */
    OutputStream getLogger();

    /**
     * Sets the logger for the connection to the service manager.
     *
     * @param logger
     *         for the connection to the service manager.
     */
    void setLogger(OutputStream logger);

    /**
     * Obtains the server version through a service call.
     *
     * @return Parsed server version, or {@link org.firebirdsql.gds.impl.GDSServerVersion#INVALID_VERSION} if parsing
     * failed.
     * @throws SQLException
     *         For errors connecting to the service manager.
     */
    GDSServerVersion getServerVersion() throws SQLException;
}
