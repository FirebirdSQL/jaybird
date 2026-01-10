// SPDX-FileCopyrightText: Copyright 2014-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version12;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.impl.wire.WireProtocolConstants;
import org.firebirdsql.gds.impl.wire.XdrOutputStream;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version11.V11Database;

import java.io.IOException;
import java.sql.SQLException;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 12 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public class V12Database extends V11Database {

    /**
     * Creates a V12Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected V12Database(WireDatabaseConnection connection,
            ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    public void cancelOperation(int kind) throws SQLException {
        try {
            if (kind == ISCConstants.fb_cancel_abort) {
                // In case of abort we forcibly close the connection
                forceClose();
            } else {
                checkConnected();
                sendCancel(kind);
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    private void sendCancel(int kind) throws SQLException {
        try {
            withTransmitLock(xdrOut -> {
                sendCancelMsg(xdrOut, kind);
                xdrOut.flush();
            });
        } catch (IOException e) {
            throw FbExceptionBuilder.ioWriteError(e);
        }
    }

    /**
     * Sends the cancel message (struct {@code p_cancel_op}) to the server, without flushing.
     *
     * @param xdrOut
     *         XDR output stream
     * @param kind
     *         cancellation kind
     * @throws IOException
     *         for errors writing to the output stream
     * @since 7
     */
    protected void sendCancelMsg(XdrOutputStream xdrOut, int kind) throws IOException {
        xdrOut.writeInt(WireProtocolConstants.op_cancel); // p_operation
        xdrOut.writeInt(kind); // p_co_kind
    }

    /**
     * {@inheritDoc}
     * <p>
     * For version 12 always returns the UTF8 encoding.
     * </p>
     *
     * @see V12ParameterConverter
     */
    @Override
    protected Encoding getFilenameEncoding(DatabaseParameterBuffer dpb) {
        return getEncodingFactory().getEncodingForFirebirdName("UTF8");
    }
}
