// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Provides access to the {@link XdrInputStream} and {@link XdrOutputStream} of a connection.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface XdrStreamAccess {

    /**
     * Gets the XDR input stream.
     *
     * @return instance of {@link XdrInputStream}
     * @throws SQLException
     *         if no connection is opened or when exceptions occur retrieving the InputStream
     */
    XdrInputStream getXdrIn() throws SQLException;

    /**
     * Gets the XDR output stream.
     * <p>
     * Writes to the XDR output stream should be done while holding the transmit lock. In general, use of
     * {@link #withTransmitLock(TransmitAction)} should be preferred over calling {@code getXdrOut()} directly.
     * </p>
     *
     * @return instance of {@link XdrOutputStream}
     * @throws SQLException
     *         if no connection is opened or when exceptions occur retrieving the OutputStream
     * @see #withTransmitLock(TransmitAction)
     */
    XdrOutputStream getXdrOut() throws SQLException;

    /**
     * Runs {@link TransmitAction#transmit(XdrOutputStream)} with {@link #getXdrOut()} on {@code transmitAction} under
     * the transmit lock.
     * <p>
     * The transmit lock should only cover sending messages to the server. It should be held for the duration of the
     * entire message. It <strong>must</strong> be released <em>before</em> reading (receiving) messages from the
     * server. If possible, do not do anything other than writing to the XDR output stream while holding the lock.
     * </p>
     * <p>
     * Normal operations <strong>must</strong> obtain the lock while holding the connection lock (i.e. the various
     * {@code withLock()} methods). Out-of-band operations (e.g. cancellation) <strong>must not</strong> take out the
     * connection lock, otherwise they can't be out-of-band.
     * </p>
     * <p>
     * Note for implementations: the lock used must be reentrant.
     * </p>
     *
     * @param transmitAction
     *         the transmit action to run under lock
     * @throws IOException
     *         for errors writing to the XDR output stream
     * @throws SQLException
     *         for other database access errors
     * @see TransmitAction
     * @since 7
     */
    void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException;

}
