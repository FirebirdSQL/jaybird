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
package org.firebirdsql.event;

import org.firebirdsql.gds.ng.WireCrypt;

import java.sql.SQLException;

/**
 * An interface for registering {@link EventListener} instances to listen for database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public interface EventManager {

    /**
     * Make a connection with a database to listen for events.
     *
     * @throws SQLException
     *         If a database communication error occurs
     */
    void connect() throws SQLException;

    /**
     * Close the connection to the database.
     *
     * @throws SQLException
     *         If a database communication error occurs
     */
    void disconnect() throws SQLException;

    /**
     * Sets the username for the connection to the database .
     *
     * @param user
     *         for the connection to the database.
     */
    void setUser(String user);

    /**
     * @return the username for the connection to the database.
     */
    String getUser();

    /**
     * Sets the password for the connection to the database.
     *
     * @param password
     *         for the connection to the database.
     */
    void setPassword(String password);

    /**
     * @return the password for the connection to the database.
     */
    String getPassword();

    /**
     * Sets the database path for the connection to the database.
     *
     * @param database
     *         path for the connection to the database.
     */
    void setDatabase(String database);

    /**
     * @return the database path for the connection to the database.
     */
    String getDatabase();

    /**
     * @return the host for the connection to the database.
     */
    String getHost();

    /**
     * Sets the host for the connection to the database.
     *
     * @param host
     *         for the connection to the database.
     */
    void setHost(String host);

    /**
     * @return the port for the connection to the database.
     */
    int getPort();

    /**
     * Sets the port for the connection to the database.
     *
     * @param port
     *         for the connection to the database.
     */
    void setPort(int port);

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 3.0.4
     */
    WireCrypt getWireCrypt();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt Wire encryption level ({@code null} not allowed)
     * @since 3.0.4
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
     * Register an EventListener that will be called when an event occurs.
     *
     * @param eventName
     *         The name of the event for which the listener will be notified
     * @param listener
     *         The EventListener that will be called when the given event occurs
     * @throws SQLException
     *         If a database access error occurs
     */
    void addEventListener(String eventName, EventListener listener) throws SQLException;

    /**
     * Remove an EventListener for a given event.
     *
     * @param eventName
     *         The name of the event for which the listener will be unregistered.
     * @param listener
     *         The EventListener that is to be unregistered
     * @throws SQLException
     *         If a database access error occurs
     */
    void removeEventListener(String eventName, EventListener listener) throws SQLException;

    /**
     * Wait for the one-time occurrence of an event.
     * <p>
     * This method blocks indefinitely until the event identified by the value of {@code eventName} occurs. The return
     * value is the number of occurrences of the requested event.
     * </p>
     *
     * @param eventName
     *         The name of the event to wait for
     * @return The number of occurences of the requested event
     * @throws InterruptedException
     *         If interrupted while waiting
     * @throws SQLException
     *         If a database access error occurs
     */
    int waitForEvent(String eventName) throws InterruptedException, SQLException;

    /**
     * Wait for the one-time occurrence of an event.
     * <p>
     * This method blocks for a maximum of {@code timeout} milliseconds, waiting for the event identified by
     * {@code eventName} to occur. A timeout value of {@code 0} means wait indefinitely.
     * </p>
     * <p>
     * The return value is the number of occurences of the event in question, or {@code -1} if the call timed out.
     * </p>
     *
     * @param eventName
     *         The name of the event to wait for
     * @param timeout
     *         The maximum number of milliseconds to wait
     * @return The number of occurrences of the requested event, or {@code 1} if the call timed out
     * @throws InterruptedException
     *         If interrupted while waiting
     * @throws SQLException
     *         If a database access error occurs
     */
    int waitForEvent(String eventName, int timeout) throws InterruptedException, SQLException;
    
}
