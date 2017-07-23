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
package org.firebirdsql.gds.ng;

/**
 * Common properties for database and service attach.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface IAttachProperties<T extends IAttachProperties> {
    int DEFAULT_PORT = 3050;
    String DEFAULT_SERVER_NAME = "localhost";
    int DEFAULT_SOCKET_BUFFER_SIZE = -1;
    int DEFAULT_SO_TIMEOUT = -1;
    int DEFAULT_CONNECT_TIMEOUT = -1;

    /**
     * @return The name of the object to attach to (either a database or service name).
     */
    String getAttachObjectName();

    /**
     * Get the hostname or IP address of the Firebird server.
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_SERVER_NAME}
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
     * NOTE: Implementer should take care to return {@link #DEFAULT_PORT} if
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
     * @return SQL role to use.
     */
    String getRoleName();

    /**
     * @param roleName
     *         SQL role to use.
     */
    void setRoleName(String roleName);

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
     * Get the socket buffer size.
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_SOCKET_BUFFER_SIZE} if the
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
     * Get the initial Socket blocking timeout (SoTimeout).
     * <p>
     * NOTE: Implementer should take care to return {@link #DEFAULT_SO_TIMEOUT} if the
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
     * NOTE: Implementer should take care to return {@link #DEFAULT_CONNECT_TIMEOUT} if the
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
     * Get the wire encryption level.
     * <p>
     * NOTE: Implementer should take care to return {@link WireCrypt#DEFAULT} if
     * the value hasn't been set yet.
     * </p>
     *
     * @return Wire encryption level
     * @since 4.0
     */
    WireCrypt getWireCrypt();

    /**
     * Set the wire encryption level.
     * <p>
     * NOTE: Implementer should take care to use {@link WireCrypt#DEFAULT} if
     * the value hasn't been set yet.
     * </p>
     *
     * @param wireCrypt Wire encryption level ({@code null} not allowed)
     * @since 4.0
     */
    void setWireCrypt(WireCrypt wireCrypt);

    /**
     * @return An immutable version of this instance as an implementation of {@link IAttachProperties}
     */
    T asImmutable();

    /**
     * @return A new, mutable, instance as an implementation of {@link IAttachProperties} with all properties copied.
     */
    T asNewMutable();

}
