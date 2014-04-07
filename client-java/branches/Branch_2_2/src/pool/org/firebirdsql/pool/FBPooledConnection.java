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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBManagedConnection;
import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.jdbc.FirebirdStatement;


/**
 * Firebird pooled connection implementing {@link XAConnection} interface.
 *  
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class FBPooledConnection extends PingablePooledConnection
    implements XAConnection 
{
    /**
     * Internal connection event listener. This class is responsible for closing
     * connections when corresponding event comes to the server.
     */
    private static class FBManagedConnectionEvetListener implements ConnectionEventListener {
        public void connectionClosed(ConnectionEvent event) {
            
            PrintWriter externalLog = ((FBManagedConnection)event.getSource()).getLogWriter();
            try {
                ((FBManagedConnection)event.getSource()).destroy();
            }
            catch (ResourceException e) {
                if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
            }

        }
        public void connectionErrorOccurred(ConnectionEvent event) {
            PrintWriter externalLog = ((FBManagedConnection)event.getSource()).getLogWriter();
            try {
                ((FBManagedConnection)event.getSource()).destroy();
            }
            catch (ResourceException e) {
                if (externalLog != null) externalLog.println("Exception closing unmanaged connection: " + e);
            }

        }
        public void localTransactionCommitted(ConnectionEvent event) {

        }
        public void localTransactionRolledback(ConnectionEvent event) {

        }
        public void localTransactionStarted(ConnectionEvent event) {

        }
    }

    private FBManagedConnection managedConnection;
    
    private FBManagedConnectionEvetListener listener = 
        new FBManagedConnectionEvetListener();

    /**
     * Create instance of this class for the specified managed connection.
     * 
     * @param managedConnection instance of {@link FBManagedConnection}
     * @param cri connection request information.
     * @param statementPooling <code>true</code> if statement pooling is enabled.
     * @param maxStatements Maximum number of statements to pools
     * @param keepStatements Keep statements after closing logical connection obtained from this pooled connection
     * 
     * @throws SQLException if some SQL error happened.
     * @throws ResourceException if resource management error happened.
     */
    public FBPooledConnection(FBManagedConnection managedConnection, 
        FBConnectionRequestInfo cri, boolean statementPooling, 
        int maxStatements, boolean keepStatements, PooledConnectionQueue owningQueue)
        throws SQLException, ResourceException 
    {
        super((Connection)managedConnection.getConnection(null, cri), 
            statementPooling, maxStatements, keepStatements, owningQueue);
        
        this.managedConnection = managedConnection;
        
        this.managedConnection.addConnectionEventListener(this.listener);
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
     *
     * @throws SQLException if SQL error happened.
     * @throws ResourceException if resource management error happened.
     */
    protected FBPooledConnection(FBManagedConnection managedConnection, 
        FBConnectionRequestInfo cri, String pingStatement, int pingInterval, 
        boolean statementPooling,int maxStatements, 
        boolean keepStatements, PooledConnectionQueue owningQueue)
        throws SQLException, ResourceException 
    {
        super((Connection)managedConnection.getConnection(null, cri), 
            pingStatement, pingInterval, statementPooling, /*transactionIsolation,*/ 
            maxStatements, keepStatements, owningQueue);
        
        this.managedConnection = managedConnection;
        
        this.managedConnection.addConnectionEventListener(this.listener);
    }
    
    /* (non-Javadoc)
     * @see org.firebirdsql.pool.PingablePooledConnection#configureConnectionDefaults(java.sql.Connection)
     */
    protected void configureConnectionDefaults(Connection connection) throws SQLException {
        if (!managedConnection.isManagedEnvironment()) 
            connection.setAutoCommit(true);

        connection.setReadOnly(false);
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
    
    /**
     * Notify this class that statement was closed. This method closes open 
     * result set if there is one. Note, it works only for statements 
     * implementing {@link FirebirdStatement} interface.
     * 
     * @param key Key to SQL statement that was closed.
     * @param proxy corresponding proxy.
     * 
     * @throws SQLException if something went wrong.
     */
    public void statementClosed(XPreparedStatementModel key, Object proxy)
            throws SQLException {
        
        if (proxy instanceof FirebirdStatement) {
            FirebirdStatement fbStmt = (FirebirdStatement)proxy;
            
            ResultSet rs = fbStmt.getCurrentResultSet();
            
            if (rs != null)
                rs.close();
            
        }
        
        super.statementClosed(key, proxy);
    }

    @Override
    protected boolean isRollbackAllowed() {
        return !managedConnection.inDistributedTransaction();
    }

    void setManagedEnvironment(boolean managedEnvironment) throws SQLException {
        try {
            managedConnection.setManagedEnvironment(managedEnvironment);
        } catch(ResourceException ex) {
            throw new FBSQLException(ex);
        }
    }
}