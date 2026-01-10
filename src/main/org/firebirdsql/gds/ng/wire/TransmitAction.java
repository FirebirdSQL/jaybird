// SPDX-FileCopyrightText: Copyright 2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Action for transmitting messages to Firebird.
 * <p>
 * This action is primarily intended for transmitting data while the transmit lock is held.
 * </p>
 *
 * @see XdrStreamAccess#withTransmitLock(TransmitAction)
 * @since 7
 */
@FunctionalInterface
@NullMarked
public interface TransmitAction {

    /**
     * Transmits a message (or messages) to Firebird.
     * <p>
     * Implementations <strong>should not</strong> obtain (additional) locks, and <strong>must not</strong> attempt to
     * read (receive) data from the server. Preferably, the only operations done are writes to {@code xdrOut}. In
     * general, an action should write the whole message. If that is not possible, make sure that the full message is
     * written while the transmit lock is held.
     * </p>
     *
     * @param xdrOut
     *         XDR output stream to Firebird server
     * @throws IOException
     *         for errors writing into {@code xdrOut}
     * @throws SQLException
     *         for connection state errors
     * @see XdrStreamAccess#withTransmitLock(TransmitAction)
     */
    void transmit(XdrOutputStream xdrOut) throws IOException, SQLException;

}
