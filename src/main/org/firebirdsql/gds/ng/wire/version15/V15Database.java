// SPDX-FileCopyrightText: Copyright 2019-2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version15;

import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version13.V13Database;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 15 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V15Database extends V13Database {

    /**
     * Creates a V15Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is used for creating further dependent
     *         objects).
     */
    protected V15Database(WireDatabaseConnection connection,
            ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }
}
