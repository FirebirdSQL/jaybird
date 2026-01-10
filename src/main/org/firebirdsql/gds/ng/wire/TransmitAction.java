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
 * @since 6.0.4
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
