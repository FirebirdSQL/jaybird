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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import javax.sql.PooledConnection;

import org.firebirdsql.jdbc.FBDriver;
import org.firebirdsql.jdbc.FirebirdConnection;

/**
 * Connection pool for Firebird JDBC driver.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnectionPoolDataSource extends AbstractConnectionPoolDataSource
    implements PooledConnectionManager 
{
    public static final String USER_NAME = FBDriver.USER;
    public static final String PASSWORD = FBDriver.PASSWORD;
    
    private static final Logger LOG =
        LoggerFactory.getLogger(FBConnectionPoolDataSource.class, false);
    
    private ConnectionPoolConfiguration config;

    /**
     * Create instance of this class.
     * 
     * @param config configuration for this connection pool.
     * 
     * @throws SQLException if something went wrong.
     */
    public FBConnectionPoolDataSource(FBConnectionPoolConfiguration config) 
        throws SQLException
    {
        super();
        
        this.config = config;
        
        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");
        } catch (ClassNotFoundException cnfex) {
            throw new SQLException(
                "Firebird JDBC driver not found.");
        }
    }

    protected Logger getLogger() {
        return LOG;
    }
    
    public ConnectionPoolConfiguration getConfiguration() {
        return config;
    }
    
    protected PooledConnectionManager getConnectionManager() {
        return this;
    }

    /**
     * Allocate new physical connection. This implementation uses 
     * {@link DriverManager} to allocate connection. Other implementations might
     * consider overriding this method to provide more optimized implementation.
     * 
     * @return instance of {@link PooledConnection}
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    public PooledConnection allocateConnection() throws SQLException {
        FBConnectionPoolConfiguration config = 
            (FBConnectionPoolConfiguration)getConfiguration();
            
        String userName = config.getProperty(USER_NAME);
        String password = config.getProperty(PASSWORD);
        
        return allocateConnection(userName, password);
        
    }
    
    /**
     * Allocate new physical connection for the specified user name and 
     * password.
     * 
     * @param userName user name.
     * @param password password.
     *  
     * @return instance of {@link PooledConnection}.
     * 
     * @throws SQLException if connection cannot be allocated.
     */
    public PooledConnection allocateConnection(String userName, String password)
        throws SQLException
    {

        FBConnectionPoolConfiguration config = 
            (FBConnectionPoolConfiguration)getConfiguration();
            
        Properties props = new Properties();
        props.putAll(config.getProperties());
        
        props.setProperty(USER_NAME, userName);
        props.setProperty(PASSWORD, password);

        Connection connection = 
            DriverManager.getConnection(config.getUrl(), props);
            
        PingablePooledConnection pooledConnection = null;

        if (config.isPingable())
            pooledConnection =
                new FBPooledConnection(
                    connection,
                    config.getPingStatement(),
                    config.getPingInterval());
        else
            pooledConnection = new FBPooledConnection(connection);
            
        return pooledConnection;
    }
    
    
    /** 
     * Get name of the connection queue.
     * 
     * @see AbstractConnectionPoolDataSource#getQueueName()
     */
    protected String getPoolName() {
        FBConnectionPoolConfiguration config = 
            (FBConnectionPoolConfiguration)getConfiguration();

        return config.getUrl();
    }
    
    private static class FBPooledConnection extends PingablePooledConnection {

        public FBPooledConnection(Connection connection) throws SQLException {
            super(connection);
        }

        protected FBPooledConnection(Connection connection, 
            String pingStatement, int pingInterval) throws SQLException 
        {
            super(connection, pingStatement, pingInterval);
        }
        
        public Class[] getImplementedInterfaces() {
            return new Class[] {
                FirebirdConnection.class
            };
        }
        
    }

}
