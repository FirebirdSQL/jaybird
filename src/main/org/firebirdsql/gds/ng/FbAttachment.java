// SPDX-FileCopyrightText: Copyright 2013-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.gds.impl.GDSServerVersion;
import org.firebirdsql.gds.ng.listeners.ExceptionListenable;

import java.sql.SQLException;

/**
 * Connection to a Firebird server (to a database or service).
 * <p>
 * All methods defined in this interface and the direct descendants {@link FbDatabase} and {@link FbService} are
 * required to notify all {@code SQLException} thrown from the methods defined in this interface, and those exceptions
 * notified by all {@link ExceptionListenable} implementations created from them.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbAttachment extends AutoCloseable, ExceptionListenable {

    /**
     * Attach to the attachment type.
     */
    void attach() throws SQLException;

    /**
     * Detaches and closes the connection.
     *
     * @throws SQLException
     *         If not currently connected, or another problem occurred detaching.
     */
    @Override
    void close() throws SQLException;

    /**
     * Forces the connection to close without proper detach or cleanup.
     * <p>
     * If a given implementation does not support this, then this method should call {@link #close()}.
     * </p>
     *
     * @throws SQLException For problems closing the connection.
     */
    void forceClose() throws SQLException;

    /**
     * @return The attachment handle value
     */
    int getHandle();

    /**
     * @return Firebird version string
     */
    GDSServerVersion getServerVersion();

    /**
     * Current attachment status.
     *
     * @return {@code true} if connected to the server and attached to a database or service, {@code false} otherwise.
     */
    boolean isAttached();

    /**
     * @return The {@link IEncodingFactory} for this connection
     */
    IEncodingFactory getEncodingFactory();

    /**
     * @return The connection encoding (should be the same as returned from calling
     * {@link IEncodingFactory#getDefaultEncoding()} on the result of {@link #getEncodingFactory()}).
     */
    Encoding getEncoding();

    /**
     * @return The {@link DatatypeCoder} for this database implementation.
     */
    DatatypeCoder getDatatypeCoder();

    /**
     * Sets the network timeout for this attachment.
     *
     * @param milliseconds
     *         Timeout in milliseconds; 0 means no timeout. If the attachment doesn't support milliseconds, it should
     *         round up to the nearest second.
     * @throws SQLException
     *         If this attachment is closed, the value of {@code milliseconds} is smaller than 0, or if setting the
     *         timeout fails.
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If this attachment doesn't support (changing) the network timeout.
     */
    void setNetworkTimeout(int milliseconds) throws SQLException;

    /**
     * Gets the current network timeout for this attachment.
     *
     * @return Timeout in milliseconds, 0 means no timeout
     * @throws SQLException
     *         If this attachment is closed
     * @throws java.sql.SQLFeatureNotSupportedException
     *         If this attachment doesn't support network timeout
     */
    int getNetworkTimeout() throws SQLException;

    /**
     * Locks the lock with {@link java.util.concurrent.locks.Lock#lock()} (or equivalent).
     * <p>
     * The returned {@code LockClosable} can be used to unlock, preferably for use in a try-with-resources.
     * </p>
     *
     * @return lock closeable which unlocks the lock on close
     */
    LockCloseable withLock();

    /**
     * Queries if the lock is held by the current thread.
     *
     * @return {@code true} if current thread holds this lock and {@code false} otherwise
     * @see java.util.concurrent.locks.ReentrantLock#isHeldByCurrentThread()
     */
    boolean isLockedByCurrentThread();

}
