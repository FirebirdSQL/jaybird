// SPDX-FileCopyrightText: Copyright 2011-2024 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.ds;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.jdbc.FBConnection;

/**
 * XAConnection implementation for {@link FBXADataSource}
 * 
 * @author Mark Rotteveel
 * @since 2.2
 */
public final class FBXAConnection extends FBPooledConnection implements XAConnection {
    
    private final WeakReference<FBManagedConnection> mc;
    
    FBXAConnection(FBConnection connection) throws SQLException {
        super(connection);
        mc = new WeakReference<>(connection.getManagedConnection());
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return getManagedConnection().getXAResource();
    }

    @Override
    void resetConnection(Connection connection) throws SQLException {
        if (!inDistributedTransaction()) {
            super.resetConnection(connection);
        }
    }

    @Override
    PooledConnectionHandler createConnectionHandler(Connection connection) {
        return new XAConnectionHandler(connection, this);
    }
    
    boolean inDistributedTransaction() throws SQLException {
        return getManagedConnection().inDistributedTransaction();
    }
    
    private FBManagedConnection getManagedConnection() throws SQLException {
        FBManagedConnection managedConnection = mc.get();
        if (managedConnection == null) {
            throw FbExceptionBuilder.toNonTransientConnectionException(JaybirdErrorCodes.jb_noManagedConnection);
        }
        return managedConnection;
    }

}
