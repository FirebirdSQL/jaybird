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
package org.firebirdsql.jaybird.xca;

import org.firebirdsql.jdbc.FirebirdConnection;

import java.sql.SQLException;

/**
 * The {@code XcaConnectionManager} can be used to modify the configuration of a connection, provide pooling or other
 * behaviour when allocating a new connection.
 */
public interface XcaConnectionManager extends java.io.Serializable {

    /**
     * Allocates a new {@link FirebirdConnection} backed by a {@link FBManagedConnection} from the specified factory.
     * <p>
     * The returned connection should behave as a new connection, but may be backed by an already established (eg
     * pooled) managed connection.
     * </p>
     *
     * @param managedConnectionFactory
     *         Managed connection factory
     * @param connectionRequestInfo
     *         Specific connection request info
     * @return A new {@code FirebirdConnection} instance
     * @throws SQLException
     *         for generic exceptions
     */
    FirebirdConnection allocateConnection(FBManagedConnectionFactory managedConnectionFactory,
            FBConnectionRequestInfo connectionRequestInfo) throws SQLException;

}
