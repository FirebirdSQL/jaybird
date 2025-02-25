// SPDX-FileCopyrightText: Copyright 2020 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
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
