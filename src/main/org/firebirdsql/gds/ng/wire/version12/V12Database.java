/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 12 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
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
                try {
                    // In case of abort we forcibly close the connection
                    closeConnection();
                } catch (IOException ioe) {
                    throw new SQLNonTransientConnectionException("Connection abort failed", ioe);
                }
            } else {
                checkConnected();
                try {
                    // We circumvent the normal xdrOut to minimize the chance of interleaved writes
                    // TODO We may still need to do separate write / read synchronization to ensure this works correctly
                    final ByteArrayOutputStream out = new ByteArrayOutputStream(8);
                    final XdrOutputStream xdr = new XdrOutputStream(out, false);
                    xdr.writeInt(WireProtocolConstants.op_cancel);
                    xdr.writeInt(kind);
                    wireOperations.writeDirect(out.toByteArray());
                } catch (IOException ioe) {
                    throw new FbExceptionBuilder().exception(ISCConstants.isc_net_write_err).cause(ioe)
                            .toSQLException();
                }
            }
        } catch (SQLException ex) {
            exceptionListenerDispatcher.errorOccurred(ex);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * For version 12 always returns the UTF8 encoding.
     * </p>
     *
     * @see {@link org.firebirdsql.gds.ng.wire.version12.V12ParameterConverter}
     */
    @Override
    protected Encoding getFilenameEncoding(DatabaseParameterBuffer dpb) {
        return getEncodingFactory().getEncodingForFirebirdName("UTF8");
    }
}
