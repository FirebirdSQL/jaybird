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

import org.firebirdsql.gds.impl.wire.XdrInputStream;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Provides access to the {@link XdrInputStream} and {@link XdrOutputStream}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
     * For Jaybird 5 and 6, the transmit lock only needs to be used for statement operations and cancellation. See also
     * <a href="https://github.com/FirebirdSQL/jaybird/blob/master/devdoc/jdp/jdp-2026-02-cancellation-thread-safety-backport.adoc">jdp-2026-02: Cancellation thread-safety backport</a>
     * </p>
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
     * @since 5.0.11
     */
    void withTransmitLock(TransmitAction transmitAction) throws IOException, SQLException;

}
