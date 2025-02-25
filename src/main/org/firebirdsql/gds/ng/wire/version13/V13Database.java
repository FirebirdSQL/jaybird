// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-FileCopyrightText: Copyright 2015-2023 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng.wire.version13;

import org.firebirdsql.gds.ng.wire.ProtocolDescriptor;
import org.firebirdsql.gds.ng.wire.WireDatabaseConnection;
import org.firebirdsql.gds.ng.wire.version12.V12Database;

/**
 * {@link org.firebirdsql.gds.ng.wire.FbWireDatabase} implementation for the version 13 wire protocol.
 *
 * @author Mark Rotteveel
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

}
