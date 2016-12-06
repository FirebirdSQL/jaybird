/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jdbc.FBConnection;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.SQLStateConstants;

/**
 * XAConnection implementation for {@link FBXADataSource}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBXAConnection extends FBPooledConnection implements XAConnection {
    
    private final WeakReference<FBManagedConnection> mc;
    
    public FBXAConnection(FBConnection connection) {
        super(connection);
        mc = new WeakReference<>(connection.getManagedConnection());
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return getManagedConnection().getXAResource();
    }

    @Override
    protected void resetConnection(Connection connection) throws SQLException {
        if (!inDistributedTransaction()) {
            connection.setAutoCommit(true);
        }
    }

    @Override
    protected PooledConnectionHandler createConnectionHandler(Connection connection) {
        return new XAConnectionHandler(connection, this);
    }
    
    protected boolean inDistributedTransaction() throws SQLException {
        return getManagedConnection().inDistributedTransaction();
    }
    
    private FBManagedConnection getManagedConnection() throws SQLException {
        FBManagedConnection managedConnection = mc.get();
        if (managedConnection == null) {
            throw new FBSQLException("Managed Connection is null, connection unavailable",
                    SQLStateConstants.SQL_STATE_CONNECTION_CLOSED);
        }
        return managedConnection;
    }
}
