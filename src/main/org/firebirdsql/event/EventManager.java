/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
 * 
 * Copyright (C) All Rights Reserved.
 * 
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *  
 *   - Redistributions of source code must retain the above copyright 
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above 
 *     copyright notice, this list of conditions and the following 
 *     disclaimer in the documentation and/or other materials provided 
 *     with the distribution.
 *   - Neither the name of the firebird development team nor the names
 *     of its contributors may be used to endorse or promote products 
 *     derived from this software without specific prior written 
 *     permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF 
 * SUCH DAMAGE.
 */
package org.firebirdsql.event;

import org.firebirdsql.gds.ng.WireCrypt;

import java.sql.SQLException;

/**
 * An interface for registering <code>EventListener</code>s to listen for
 * database events.
 *
 * @author <a href="mailto:gab_reid@users.sourceforge.net">Gabriel Reid</a>
 */
public interface EventManager {

    /**
     * Make a connection with a database to listen for events.
     *
     * @throws SQLException If a database communication error occurs
     */
    public void connect() throws SQLException;

    /**
     * Close the connection to the database.
     *
     * @throws SQLException If a database communication error occurs
     */
    public void disconnect() throws SQLException;

    /**
     * Sets the username for the connection to the database .
     * @param user for the connection to the database.
     */
    public void setUser(String user);

    /**
     * @return the username for the connection to the database.
     */
    public String getUser();

    /**
     * Sets the password for the connection to the database.
     * @param password for the connection to the database.
     */
    public void setPassword(String password);

    /**
     * @return the password for the connection to the database.
     */
    public String getPassword();

    /**
     * Sets the database path for the connection to the database.
     * @param database path for the connection to the database.
     */
    public void setDatabase(String database);

    /**
     * @return the database path for the connection to the database.
     */
    public String getDatabase();

    /**
     * @return the host for the connection to the database.
     */
    public String getHost();

    /**
     * Sets the host for the connection to the database.
     * @param host for the connection to the database.
     */
    public void setHost(String host);

    /**
     * @return the port for the connection to the database.
     */
    public int getPort();

    /**
     * Sets the port for the connection to the database.
     * @param port for the connection to the database.
     */
    public void setPort(int port);

    /**
     * Get the wire encryption level.
     *
     * @return Wire encryption level
     * @since 3.0.4
     */
    public WireCrypt getWireCrypt();

    /**
     * Set the wire encryption level.
     *
     * @param wireCrypt Wire encryption level ({@code null} not allowed)
     * @since 3.0.4
     */
    public void setWireCrypt(WireCrypt wireCrypt);

    /**
     * Get the database encryption plugin configuration.
     *
     * @return Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    public String getDbCryptConfig();

    /**
     * Sets the database encryption plugin configuration.
     *
     * @param dbCryptConfig Database encryption plugin configuration, meaning plugin specific
     * @since 3.0.4
     */
    public void setDbCryptConfig(String dbCryptConfig);

    /**
     * Register an EventListener that will be called when an event occurs.
     *
     * @param eventName The name of the event for which the listener will
     *                  be notified
     * @param listener The EventListener that will be called when the given
     *                 event occurs
     * @throws SQLException If a database access error occurs
     */
    public void addEventListener(String eventName, EventListener listener)
            throws SQLException;

    /**
     * Remove an EventListener for a given event.
     *
     * @param eventName The name of the event for which the listener 
     *                  will be unregistered.
     * @param listener The EventListener that is to be unregistered
     * @throws SQLException If a database access error occurs
     */
    public void removeEventListener(String eventName, EventListener listener)
            throws SQLException;

    /**
     * Wait for the one-time occurence of an event.
     *
     * This method blocks indefinitely until the event identified by the
     * value of <code>eventName</code> occurs. The return value is the
     * number of occurrences of the requested event.
     *
     * @param eventName The name of the event to wait for
     * @return The number of occurences of the requested event
     * @throws InterruptedException If interrupted while waiting
     * @throws SQLException If a database access error occurs
     */
    public int waitForEvent(String eventName) 
            throws InterruptedException, SQLException;

    /**
     * Wait for the one-time occurence of an event.
     *
     * This method blocks for a maximum of <code>timeout</code> milliseconds,
     * waiting for the event identified by <code>eventName</code> to occur.
     * A timeout value of <code>0</code> means wait indefinitely.
     *
     * The return value is the number of occurences of the event in question,
     * or <code>-1</code> if the call timed out.
     *
     * @param eventName The name of the event to wait for
     * @param timeout The maximum number of milliseconds to wait
     * @return The number of occurrences of the requested event, or 
     *         <code>-1</code> if the call timed out
     * @throws InterruptedException If interrupted while waiting
     * @throws SQLException If a database access error occurs
     */
    public int waitForEvent(String eventName, int timeout) 
            throws InterruptedException, SQLException;
}
