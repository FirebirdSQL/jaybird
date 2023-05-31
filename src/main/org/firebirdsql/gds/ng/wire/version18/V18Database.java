/*
 * Firebird Open Source JDBC Driver
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
