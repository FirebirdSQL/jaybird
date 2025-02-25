// SPDX-FileCopyrightText: Copyright 2019-2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version16;

import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version15.V15Database;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 16 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
public class V16Database extends V15Database {

    /**
     * Creates a V16Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is used for creating further dependent
     *         objects).
     */
    protected V16Database(WireDatabaseConnection connection,
            ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }
}
