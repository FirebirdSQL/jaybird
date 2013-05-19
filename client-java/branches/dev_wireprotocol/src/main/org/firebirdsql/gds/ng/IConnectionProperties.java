package org.firebirdsql.gds.ng;

/**
 * Connection properties for the Firebird connection.
 * <p>
 * This interface only defines setters, the getters are defined in {@link IConnectionPropertiesGetters}.
 * </p>
 * <p>
 * TODO Do refactor to remove overlap/duplication with {@link org.firebirdsql.jdbc.FirebirdConnectionProperties}
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface IConnectionProperties extends IConnectionPropertiesGetters {

    final int DEFAULT_PORT = 3050;
    final short DEFAULT_DIALECT = 3;
    final String DEFAULT_SERVER_NAME = "localhost";
    final int DEFAULT_SOCKET_BUFFER_SIZE = -1;
    final int DEFAULT_BUFFERS_NUMBER = 0;
    final int DEFAULT_SO_TIMEOUT = -1;
    final int DEFAULT_CONNECT_TIMEOUT = -1;

    /**
     * @param databaseName
     *         Name or alias of the database
     */
    void setDatabaseName(String databaseName);

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
     * @param user
     *         Name of the user to authenticate to the server.
     */
    void setUser(String user);

    /**
     * @param password
     *         Password to authenticate to the server.
     */
    void setPassword(String password);

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
     * @param roleName
     *         SQL role to use.
     */
    void setRoleName(String roleName);

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
}
