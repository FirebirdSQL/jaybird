/*
 * Firebird Open Source J2ee connector - jdbc driver
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
package org.firebirdsql.jca;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.firebirdsql.jdbc.AbstractConnection;

/**
 * Simple non-pooled XAConnection implementation.
 * 
 * @author <a href="mailto:lorban@bitronix.be">Ludovic Orban</a>
 */
public class FBXAConnection implements XAConnection {
    
    private AbstractConnection connection;
    
    protected FBXAConnection(AbstractConnection connection) {
        this.connection = connection;
    }

    public XAResource getXAResource() throws SQLException {
        return connection.getManagedConnection();
    }

    public void close() throws SQLException {
        connection.close();
    }

    public Connection getConnection() throws SQLException {
        return new FBXAConnectionHandle(connection, this).getProxy();
    }

    public void addConnectionEventListener(ConnectionEventListener listener) {
        //TODO: do something !
    }

    public void removeConnectionEventListener(ConnectionEventListener listener) {
        //TODO: do something !
    }

}
