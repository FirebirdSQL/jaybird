// SPDX-FileCopyrightText: Copyright 2014-2015 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later OR BSD-3-Clause
package org.firebirdsql.gds.ng;

import java.sql.SQLException;

/**
 * Factory for {@link org.firebirdsql.gds.ng.FbDatabase} instances.
 * <p>
 * A <code>FbDatabaseFactory</code> knows how to create connected (but unattached) instance of {@link org.firebirdsql.gds.ng.FbDatabase}
 * for a specific protocol type (eg wire protocol, embedded or native).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public interface FbDatabaseFactory {

    /**
     * Connects to a Firebird server based on the supplied connection properties.
     * <p>
     * The {@link org.firebirdsql.gds.ng.FbDatabase} instance will be connected to the server, but is not yet attached.
     * </p>
     *
     * @param connectionProperties Connection properties
     * @return Database instance
     */
    FbDatabase connect(IConnectionProperties connectionProperties) throws SQLException;

    /**
     * Connects to the service manager of a Firebird server with the supplied service properties.
     *
     * @param serviceProperties Service properties
     * @return Service instance
     */
    FbService serviceConnect(IServiceProperties serviceProperties) throws SQLException;
}
