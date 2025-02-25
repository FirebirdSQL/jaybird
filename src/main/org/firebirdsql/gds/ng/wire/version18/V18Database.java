// SPDX-FileCopyrightText: Copyright 2021 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version18;

import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version16.V16Database;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 18 wire protocol.
 *
 * @author Mark Rotteveel
 * @since 5
 */
public class V18Database extends V16Database {

    /**
     * Creates a V18Database instance.
     *
     * @param connection
     *         A WireConnection with an established connection to the server.
     * @param descriptor
     *         The ProtocolDescriptor that created this connection (this is used for creating further dependent
     *         objects).
     */
    protected V18Database(WireDatabaseConnection connection, ProtocolDescriptor descriptor) {
        super(connection, descriptor);
    }
}
