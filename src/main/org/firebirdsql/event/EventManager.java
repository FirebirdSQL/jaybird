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
import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.jaybird.props.DatabaseConnectionProperties;

import java.sql.SQLException;

/**
 * An interface for registering {@link EventListener} instances to listen for database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@SuppressWarnings("unused")
public interface EventManager extends AttachmentProperties, AutoCloseable {

    /**
     * Make a connection with a database to listen for events.
     *
     * @throws SQLException
     *         If a database communication error occurs
     * @throws IllegalStateException
     *         If already connected
     */
    void connect() throws SQLException;

    /**
     * If connected, disconnects, otherwise does nothing.
     * <p>
     * Contrary to {@link #disconnect()}, this method does not throw {@link IllegalStateException} when not connected.
     * </p>
     *
     * @throws SQLException
     *         For errors during disconnect
     * @since 3.0.7
     */
    void close() throws SQLException;

    /**
     * Close the connection to the database.
     *
     * @throws SQLException
     *         If a database communication error occurs
     * @throws IllegalStateException
     *         If not currently connected
     * @see #close()
     */
    void disconnect() throws SQLException;

    /**
     * @return {@code true} when connected and able to listen for events
     * @see #connect()
     * @see #disconnect()
     */
    boolean isConnected();

    /**
     * Get the database name.
     * <p>
     * See {@link DatabaseConnectionProperties#getDatabaseName()} for details.
     * </p>
     *
     * @return database name
     * @since 5
     */
    String getDatabaseName();

    /**
     * Set the database name.
     * <p>
     * See {@link DatabaseConnectionProperties#setDatabaseName(String)} for details.
     * </p>
     *
     * @param databaseName database name
     * @since 5
     */
    void setDatabaseName(String databaseName);

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 5
     */
    WireCrypt getWireCryptAsEnum();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt
     *         Wire encryption level ({@code null} not allowed)
     * @since 5
     */
    void setWireCryptAsEnum(WireCrypt wireCrypt);

    /**
     * Get the poll timeout in milliseconds of the async thread to check whether it was stopped or not.
     * <p>
     * Default value is 1000 (1 second).
     * </p>
     *
     * @return wait timeout in milliseconds
     */
    long getWaitTimeout();

    /**
     * Set the poll timeout in milliseconds of the async thread to check whether it was stopped or not.
     * <p>
     * Default value is 1000 (1 second).
     * </p>
     *
     * @param waitTimeout
     *         wait timeout in milliseconds
     */
    void setWaitTimeout(long waitTimeout);

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
