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
package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.DatabaseParameterBuffer;

/**
 * Connection properties for the Firebird connection.
 * <p>
 * TODO Do refactor to remove overlap/duplication with {@link org.firebirdsql.jdbc.FirebirdConnectionProperties}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface IConnectionProperties {

    final int DEFAULT_PORT = 3050;
    final short DEFAULT_DIALECT = 3;
    final String DEFAULT_SERVER_NAME = "localhost";
    final int DEFAULT_SOCKET_BUFFER_SIZE = -1;
    final int DEFAULT_BUFFERS_NUMBER = 0;
    final int DEFAULT_SO_TIMEOUT = -1;
    final int DEFAULT_CONNECT_TIMEOUT = -1;

    /**
     * @return Name or alias of the database
     */
    String getDatabaseName();

    /**
     * @param databaseName
     *         Name or alias of the database
     */
    void setDatabaseName(String databaseName);

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
     * Set the hostname or IP address of the Firebird server.
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_SERVER_NAME} if
     * value hasn't been set yet.
     * </p>
     *
     * @param serverName
     *         Hostname or IP address of the server
     */
    void setServerName(String serverName);

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
     * Set the port number of the server.
     * <p>
     * NOTE: Implementer should take care to use the {@link #DEFAULT_PORT} if
     * this method hasn't been called yet.
     * </p>
     *
     * @param portNumber
     *         Port number of the server
     */
    void setPortNumber(int portNumber);

    /**
     * @return Name of the user to authenticate to the server.
     */
    String getUser();

    /**
     * @param user
     *         Name of the user to authenticate to the server.
     */
    void setUser(String user);

    /**
     * @return Password to authenticate to the server.
     */
    String getPassword();

    /**
     * @param password
     *         Password to authenticate to the server.
     */
    void setPassword(String password);

    /**
     * @return Java character set for the connection.
     */
    String getCharSet();

    /**
     * Set the Java character set for the connection.
     * <p>
     * Contrary to other parts of the codebase, the value of
     * <code>encoding</code> should not be changed when <code>charSet</code> is
     * set.
     * </p>
     *
     * @param charSet
     *         Character set for the connection. Similar to
     *         <code>encoding</code> property, but accepts Java names instead
     *         of Firebird ones.
     * @see #setEncoding(String)
     */
    void setCharSet(String charSet);

    /**
     * @return Firebird character encoding for the connection.
     */
    String getEncoding();

    /**
     * Set the Firebird character set for the connection.
     * <p>
     * Contrary to other parts of the codebase, the value of
     * <code>charSet</code> should not be changed when <code>encoding</code> is
     * set.
     * </p>
     *
     * @param encoding
     *         Firebird character encoding for the connection. See Firebird
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
     * Set the dialect of the client connection
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_DIALECT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @param connectionDialect
     *         SQL dialect of the client.
     */
    void setConnectionDialect(short connectionDialect);

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
     * Set the socket buffer size.
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_SOCKET_BUFFER_SIZE} if the
     * value hasn't been set yet.
     * </p>
     *
     * @param socketBufferSize
     *         socket buffer size in bytes.
     */
    void setSocketBufferSize(int socketBufferSize);

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
     * Set the page cache size.
     * <p>
     * A value of <code>0</code> indicates that the value is not set, and that
     * the server default is used.
     * </p>
     * <p>
     * This option is only relevant for Firebird implementations with per connection cache (eg Classic)
     * </p>
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_BUFFERS_NUMBER} if
     * the value hasn't been set yet.
     * </p>
     *
     * @param pageCacheSize
     *         number of cache buffers that should be allocated for this
     *         connection, should be specified for ClassicServer instances,
     *         SuperServer has a server-wide configuration parameter.
     */
    void setPageCacheSize(int pageCacheSize);

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
     * Set the initial Socket blocking timeout (SoTimeout).
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_SO_TIMEOUT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @param soTimeout
     *         Timeout in milliseconds (0 is 'infinite')
     */
    void setSoTimeout(int soTimeout);

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
     * Set the connect timeout in seconds.
     * <p>
     * NOTE: Implementer should take care to use {@link #DEFAULT_CONNECT_TIMEOUT} if the
     * value hasn't been set yet.
     * </p>
     *
     * @param connectTimeout
     *         Connect timeout in seconds (0 is 'infinite', or better: OS
     *         specific timeout)
     */
    void setConnectTimeout(int connectTimeout);

    /**
     * Set if {@link java.sql.ResultSet} should be {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT} by default.
     *
     * @param holdable
     *         <code>true</code> ResultSets are holdable, <code>false</code> (default) ResultSets are {@link
     *         java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    void setResultSetDefaultHoldable(boolean holdable);

    /**
     * Get whether ResultSets are holdable by default.
     *
     * @return <code>true</code> ResultSets by default are {@link java.sql.ResultSet#HOLD_CURSORS_OVER_COMMIT},
     *         <code>false</code> (default), ResultSets
     *         are {@link java.sql.ResultSet#CLOSE_CURSORS_AT_COMMIT}
     */
    boolean isResultSetDefaultHoldable();

    /**
     * Set if {@link java.sql.ResultSetMetaData#getColumnName(int)} returns the <code>columnLabel</code> instead of the
     * <code>columnName</code>.
     * <p>
     * The default behaviour (with <code>columnLabelForName=false</code> is JDBC-compliant. The behavior for value
     * <code>true</code> is
     * to provide compatibility with tools with a wrong expectation.
     * </p>
     *
     * @param columnLabelForName
     *         <code>false</code> JDBC compliant behavior (<code>columnName</code> is returned), <code>true</code>
     *         compatibility option (<code>columnLabel</code> is returned)
     */
    void setColumnLabelForName(boolean columnLabelForName);

    /**
     * Gets the current setting of <code>columnLabelForName</code>
     *
     * @return <code>false</code> JDBC compliant behavior (<code>columnName</code> is returned), <code>true</code>
     *         compatibility option (<code>columnLabel</code> is returned)
     * @see #setColumnLabelForName(boolean)
     */
    boolean isColumnLabelForName();

    /**
     * Gets the extra database parameters. This can be used to pass extra database parameters that are not directly
     * supported.
     * <p>
     * An immutable instance of <code>IConnectionProperties</code> <b>must</b> return a copy.
     * </p>
     *
     * @return DatabaseParameterBuffer instance.
     */
    DatabaseParameterBuffer getExtraDatabaseParameters();

    /**
     * @return An immutable version of this instance as an implementation of {@link IConnectionProperties}
     */
    IConnectionProperties asImmutable();
}
