/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.gds.ng.FbTransaction;

/**
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public interface ProtocolDescriptor {

    /**
     * @return The protocol version
     */
    int getVersion();

    /**
     * @return Protocol architecture
     */
    int getArchitecture();

    /**
     * @return Minimum supported protocol type
     */
    int getMinimumType();

    /**
     * @return Maximum supported protocol type
     */
    int getMaximumType();

    /**
     * @return Preference weight
     */
    int getWeight();

    /**
     * Create {@link FbWireDatabase} implementation for this protocol.
     *
     * @param connection
     *         WireConnection to this database
     * @return FbWireDatabase implementation
     */
    FbWireDatabase createDatabase(WireConnection connection);

    /**
     * Create {@link FbTransaction} implementation for this protocol.
     *
     * @param database
     *         FbWireDatabase of the current database
     * @return FbTransaction implementation
     */
    FbTransaction createTransaction(FbWireDatabase database);
}
