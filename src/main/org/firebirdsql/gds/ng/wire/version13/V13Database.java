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
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.gds.VaxEncoding;
import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version12.V12Database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 13 wire protocol.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class V13Database extends V12Database {

    /**
     * Creates a V13Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is
     *         used for creating further dependent objects).
     */
    protected V13Database(WireDatabaseConnection connection,
            ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }

    @Override
    protected byte[] getTransactionIdBuffer(long transactionId) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
        try {
            VaxEncoding.encodeVaxLongWithoutLength(bos, (int) transactionId);
        } catch (IOException e) {
            // ignored: won't happen with a ByteArrayOutputStream
        }
        return bos.toByteArray();
    }

}
