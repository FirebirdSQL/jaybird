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
package org.firebirdsql.pool;

import java.sql.Connection;
import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jdbc.FBSQLException;


/**
 * Firebird pooled connection implementing {@link XAConnection} interface.
 *  
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class FBPooledConnection extends PingablePooledConnection
    implements XAConnection 
{

    private FBManagedConnection managedConnection;
    private FBConnectionRequestInfo cri;

    /**
     * Create instance of this class for the specified managed connection.
     * 
     * @param managedConnection instance of {@link FBManagedConnection}
     * @param cri connection request information.
     * @param statementPooling <code>true</code> if statement pooling is enabled.
     * @param transactionIsolation transaction isolation for connection.
     * 
     * @throws SQLException if some SQL error happened.
     * @throws ResourceException if resource management error happened.
     */
    public FBPooledConnection(FBManagedConnection managedConnection, 
        FBConnectionRequestInfo cri, boolean statementPooling, 
        int transactionIsolation, int maxStatements) 
        throws SQLException, ResourceException 
    {
        super((Connection)managedConnection.getConnection(null, cri), 
            statementPooling, transactionIsolation, maxStatements);
        
        this.managedConnection = managedConnection;
        this.cri = cri;
    }

    /**
     * Create instance of this class for the specified managed connection
     * and ping statement.
     * 
     * @param managedConnection instance of {@link FBManagedConnection}
     * @param cri connection request information.
     * @param pingStatement ping statement.
     * @param pingInterval interval after which connection will be pinged. 
     * @param statementPooling <code>true</code> if statement pooling is enabled.
     * @param transactionIsolation transaction isolation for connection.
     * 
     * @throws SQLException if SQL error happened.
     * @throws ResourceException if resource management error happened.
     */
    protected FBPooledConnection(FBManagedConnection managedConnection, 
        FBConnectionRequestInfo cri, String pingStatement, int pingInterval, 
        boolean statementPooling, int transactionIsolation, int maxStatements) 
        throws SQLException, ResourceException 
    {
        super((Connection)managedConnection.getConnection(null, cri), 
            pingStatement, pingInterval, statementPooling, transactionIsolation, 
            maxStatements);
        
        this.managedConnection = managedConnection;
        this.cri = cri;
    }
    
    /**
     * Get XA resource for this connection.
     * 
     * @return instance of {@link XAResource}.
     * 
     * @throws SQLException if SQL error happened.
     */
    public XAResource getXAResource() throws SQLException {
        return managedConnection.getXAResource();
    }
    
    /**
     * Close this statement. This method also destroys managed connection
     * and closes physical connection to the database.
     * 
     * @throws SQLException if SQL error happened.
     */
    public void close() throws SQLException {
        super.close();
        
        try {
            managedConnection.destroy();
        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }
    
}