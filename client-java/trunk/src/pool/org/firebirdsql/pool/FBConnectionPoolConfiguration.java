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

import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.sql.ConnectionPoolDataSource;

/**
 * This is default implementation of {@link ConnectionPoolConfiguration} 
 * interface.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBConnectionPoolConfiguration extends RefAddr
    implements ConnectionPoolConfiguration, Referenceable {

    /**
     * Type that should be used when creating {@link javax.naming.RefAddr}
     * instances of this configuration.
     */
    public static final String REF_TYPE = "firebird_connection_pool";  
    
    private static final String PING_STATEMENT = ""
        + "SELECT cast(1 AS INTEGER) FROM rdb$database" 
        ;
        
    /**
     * Default ping interval of 5 seconds.
     */
    public static final int DEFAULT_PING_INTERVAL = 5000;
    
    /**
     * Default blocking timeout, value is equal to {@link Integer#MAX_VALUE}.
     */
    public static final int DEFAULT_BLOCKING_TIMEOUT = Integer.MAX_VALUE;
    
    /**
     * Default retry interval of 1 sec.
     */
    public static final int DEFAULT_RETRY_INTERVAL = 1 * 1000;
    
    private String jdbcUrl;
    
    private int minConnections;
    private int maxConnections;
    
    private int blockingTimeout = DEFAULT_BLOCKING_TIMEOUT;
    private int retryInterval = DEFAULT_RETRY_INTERVAL;
    
    private Properties properties = new Properties();
    
    private int pingInterval = DEFAULT_PING_INTERVAL;
        
    /**
     * Create instance of this class. This constuctor creates an empty
     * configuration that can be used to initialize 
     * {@link AbstractConnectionPoolDataSource} connection pool.
     */
    public FBConnectionPoolConfiguration() {
        super(REF_TYPE);
    }

    /**
     * Get JDBC URL for this connection pool.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getJdbcUrl()
     */
    public String getUrl() {
        return jdbcUrl;
    }

    /**
     * Get minimum number of open physical connections that are kept in pool. 
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getMinConnections()
     */
    public int getMinConnections() {
        return minConnections;
    }

    /**
     * Get maximum number of physical connections in the pool.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getMaxConnections()
     */
    public int getMaxConnections() {
        return maxConnections;
    }

    /**
     * Get JDBC connection properties.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getProperties()
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Check if this configuation defines a pingable connection JDBC pool.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#isPingable()
     */
    public boolean isPingable() {
        return true;
    }

    /**
     * Get SQL statement that will be used to "ping" the connection.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getPingStatement()
     */
    public String getPingStatement() {
        return PING_STATEMENT;
    }

    /**
     * Get time interval, after which connection is marked to be pinged on 
     * first request to it.
     * 
     * @see org.firebirdsql.pool.ConnectionPoolConfiguration#getPingInterval()
     */
    public int getPingInterval() {
        return pingInterval;
    }

    /** 
     * Get reference on this configuration.
     * 
     * @see javax.naming.Referenceable#getReference()
     */
    public Reference getReference() throws NamingException {
        return new Reference(REF_TYPE, this);
    }

    /** 
     * Get content of this ref address.
     * 
     * @see javax.naming.RefAddr#getContent()
     */
    public Object getContent() {
        return this;
    }

    /**
     * Set JDBC URL that will be used to connect to the database.
     * 
     * @param jdbcUrl JDBC URL describing a database to connect.
     * 
     * @see #getJdbcUrl()
     */
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Set maximum number of open physical connections in the pool.
     * 
     * @param maxConnections maximum allowed number of open connections in the 
     * pool.
     * 
     * @see #getMaxConnections()
     */
    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * Set minimum number of open physical connections in the pool.
     * 
     * @param minConnections minimum number of open connections in the pool.
     * 
     * @see #getMinConnections()
     */
    public void setMinConnections(int minConnections) {
        this.minConnections = minConnections;
    }

    /**
     * Set ping interval for the connections.
     * 
     * @param pingInterval number of milliseconds after which connection is 
     * marked to "ping" before getting it from the pool or <code>0</code> to
     * remove "pingable" property of this pool.
     * 
     * @see #getPingInterval()
     */
    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    /**
     * Set JDBC properties that will be passed when opening a connection.
     * 
     * @param properties instance of {@link Properties} containing properties
     * of a connection to open.
     * 
     * @see #getProperties()
     */
    public void setProperties(Properties properties) {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    /**
     * Get type of the pool that should be created by a JNDI object factory
     * ({@link FBConnectionPoolObjectFactory}).
     */
    public Class getJNDIType() {
        return ConnectionPoolDataSource.class;
    }


	/**
     * Get pool blocking timeout.
     * 
	 * @see ConnectionPoolConfiguration#getBlockingTimeout()
	 */
	public int getBlockingTimeout() {
		return blockingTimeout;
	}
    
    /**
     * Set blocking timeout.
     * 
     * @param blockingTimeout blocking timeout to set.
     * 
     * @see ConnectionPoolConfiguration#getBlockingTimeout()
     */
    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

	/** 
     * Get retry interval.
     * 
	 * @see ConnectionPoolConfiguration#getRetryInterval()
	 */
	public int getRetryInterval() {
		return retryInterval;
	}

    /**
     * Set retry interval.
     * 
     * @param retryInterval retry interval in milliseconds
     * 
     * @see ConnectionPoolConfiguration#getBlockingTimeout()
     */
    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }
}
