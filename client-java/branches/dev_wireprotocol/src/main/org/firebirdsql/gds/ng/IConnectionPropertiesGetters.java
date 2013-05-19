/*
 * $Id$
 *
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a source repository history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng;

/**
 * Interface defining the getters of the connection properties.
 *
 * @see IConnectionProperties
 * @author @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface IConnectionPropertiesGetters {
    /**
     * @return Name or alias of the database
     */
    String getDatabaseName();

    /**
     * Get the hostname or IP address of the Firebird server.
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_SERVER_NAME}
     * if value hasn't been set yet.
     * </p>
     *
     * @return Hostname or IP address of the server
     */
    String getServerName();

    /**
     * Get the portnumber of the server.
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_PORT} if
     * value hasn't been set yet.
     * </p>
     *
     * @return Portnumber of the server
     */
    int getPortNumber();

    /**
     * @return Name of the user to authenticate to the server.
     */
    String getUser();

    /**
     * @return Password to authenticate to the server.
     */
    String getPassword();

    /**
     * @return Java character set for the connection.
     */
    String getCharSet();

    /**
     * @return Firebird character encoding for the connection.
     */
    String getEncoding();

    /**
     * @return SQL role to use.
     */
    String getRoleName();

    /**
     * Get the dialect of the client connection
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_DIALECT} if
     * the value hasn't been set yet.
     * </p>
     *
     * @return SQL dialect of the client.
     */
    short getConnectionDialect();

    /**
     * Get the socket buffer size.
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_SOCKET_BUFFER_SIZE} if the
     * value hasn't been set yet.
     * </p>
     *
     * @return socket buffer size in bytes, or -1 if not specified.
     */
    int getSocketBufferSize();

    /**
     * Get the page cache size.
     * <p>
     * A value of <code>0</code> indicates that the value is not set, and that
     * the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic)
     * </p>
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_BUFFERS_NUMBER} if
     * the value hasn't been set yet.
     * </p>
     *
     * @return number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    int getPageCacheSize();

    /**
     * Get the initial Socket blocking timeout (SoTimeout).
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_SO_TIMEOUT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @return The initial socket blocking timeout in milliseconds (0 is
     *         'infinite')
     */
    int getSoTimeout();

    /**
     * Get the connect timeout in seconds.
     * <p>
     * NOTE: Implementer should take care to return {@link IConnectionProperties#DEFAULT_CONNECT_TIMEOUT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @return Connect timeout in seconds (0 is 'infinite', or better: OS
     *         specific timeout)
     */
    int getConnectTimeout();

    /**
     * @return An immutable version of this instance as an implementation of {@link IConnectionPropertiesGetters}
     */
    IConnectionPropertiesGetters asImmutable();
}
