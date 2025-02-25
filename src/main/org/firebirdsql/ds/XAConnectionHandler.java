// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * InvocationHandler for the logical connection returned by FBXAConnection.
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
final class XAConnectionHandler extends PooledConnectionHandler {
    
    private final FBXAConnection xaOwner;

    XAConnectionHandler(Connection connection, FBXAConnection owner) {
        super(connection, owner);
        xaOwner = owner;
    }

    @Override
    boolean isRollbackAllowed() throws SQLException {
        return !(xaOwner.inDistributedTransaction() || connection.getAutoCommit());
    }
}
