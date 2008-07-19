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

import java.lang.reflect.Proxy;
import java.sql.*;

import javax.sql.*;

import org.firebirdsql.jdbc.FBSQLException;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.*;

/**
 * This class implements {@link javax.sql.PooledConnection} interface.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public abstract class AbstractPingablePooledConnection implements PooledConnection,
    PooledObject, XConnectionManager,
    XPingableConnection, XStatementManager {

    private static final boolean LOG_PREPARE_STATEMENT = PoolDebugConfiguration.DEBUG_STMT_POOL;
    private static final boolean LOG_POOL_CLEANING = PoolDebugConfiguration.DEBUG_STMT_POOL;
    
    private static final boolean LOG_META_DATA = PoolDebugConfiguration.LOG_DEBUG_INFO;

    private static Logger log =
        LoggerFactory.getLogger(PingablePooledConnection.class, false);

    protected Connection jdbcConnection;
    private HashSet connectionEventListeners = new HashSet();

    private boolean invalid;
    private boolean inPool;

    private PooledConnectionHandler currentConnection;

    private String pingStatement;
    private long lastPingTime = System.currentTimeMillis();
    private int pingInterval = 0;
    
    private int maxStatements;
    private boolean keepStatements;

    private boolean supportsStatementsAccrossCommit;
    private boolean supportsStatementsAccrossRollback;
    private boolean statementPooling;
    
    private int transactionIsolation = -1;

    private HashMap statements = new HashMap();

    protected Logger getLogChannel() {
        return log;
    }

    protected AbstractPingablePooledConnection(Connection connection, 
                                       boolean statementPooling, 
                                       /*int transactionIsolation,*/
                                       int maxStatements, boolean keepStatements) 
        throws SQLException 
    {
        this.jdbcConnection = connection;
        this.statementPooling = statementPooling;
        //this.transactionIsolation = transactionIsolation;
        this.maxStatements = maxStatements;
        this.keepStatements = keepStatements;

        this.supportsStatementsAccrossCommit =
            connection.getMetaData().supportsOpenStatementsAcrossCommit();

        if (LOG_META_DATA && getLogChannel() != null) {
            getLogChannel().info(
                "Pool supports open statements across commit : " +
                supportsStatementsAccrossCommit);

        }
        this.supportsStatementsAccrossRollback =
            connection.getMetaData().supportsOpenStatementsAcrossRollback();

        if (LOG_META_DATA && getLogChannel() != null) {
            getLogChannel().info(
                "Pool supports open statements across rollback : " +
                supportsStatementsAccrossRollback);

        }
    }

    protected AbstractPingablePooledConnection(Connection connection,
        String pingStatement, int pingInterval, boolean statementPooling, 
        /*int transactionIsolation,*/ int maxStatements, boolean keepStatements) 
        throws SQLException 
    {
        this(connection, statementPooling, /*transactionIsolation,*/ maxStatements, keepStatements);
        this.pingStatement = pingStatement;
        this.pingInterval = pingInterval;
    }
    
    public void setDefaultTransactionIsolation(int isolation) {
        this.transactionIsolation = isolation;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }
    
    public boolean isStatementPooling() {
        return statementPooling;
    }

    public boolean isKeepStatements() {
        return keepStatements;
    }
    
    /**
     * Ping connection by executing a ping statement.
     */
    public synchronized boolean ping() {
        if (pingStatement == null) {
            return false;
        }

        try {
            Statement stmt = null;
            try {
                stmt = jdbcConnection.createStatement();
                ResultSet rs = stmt.executeQuery(pingStatement);
                return rs.next();
            } finally {
                if (stmt != null) {
                    stmt.close();

                }
                lastPingTime = System.currentTimeMillis();
            }
        } catch (SQLException sqlex) {
            return false;
        }
    }

    /**
     * Invalidate this instance. After invalidating, no operation can be
     * executed on this instance.
     */
    private void invalidate() {
        invalid = true;
    }

    /**
     * Check if instance has correct state.
     *
     * @throws IllegalStateException if instance has illegal state.
     */
    private void checkValidity() {
        if (invalid) {
            throw new IllegalStateException(
                "Cannot execute desired operation " +
                "because pooled connection has invalid state.");
        }
    }

    /**
     * Check if this pooled connection is still valid.
     *
     * @return <code>true</code> if this pooled connection is still valid.
     */
    public boolean isValid() {

        if (invalid) {
            return false;
        }

        if (pingInterval > 0 &&
            (System.currentTimeMillis() - lastPingTime) > pingInterval &&
            pingStatement != null) {
            return ping();
        } else {
            return true;
        }
    }
    
    /**
     * Check whether this object is currently in pool or had been released
     * to the application.
     * 
     * @return <code>true</code> if the object is currently in pool. 
     */
    public boolean isInPool() {
        return inPool;
    }
    
    /**
     * Set the "inPool" flag to this object. This method should be called only
     * by the pool implementation.
     * 
     * @param inPool <code>true</code> if object is in pool, otherwise 
     * <code>false</code>.
     */
    public void setInPool(boolean inPool) {
        this.inPool = inPool;
    }

    private void checkInPool() throws SQLException {
        if (inPool)
            throw new FBSQLException(
                "Physical connection is currently in pool, " +
                "you cannot allocate logical connections now.");
    }
    
    /**
     * Add connection listener to be notified about connection events.
     *
     * @param listener listener to add.
     */
    public synchronized
        void addConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.add(listener);
    }

    /**
     * Remove connection listener from this pooled connection.
     *
     * @param listener listener to remove.
     */
    public synchronized
        void removeConnectionEventListener(ConnectionEventListener listener) {
        connectionEventListeners.remove(listener);
    }

    /**
     * Close this pooled connection. This operation closes physical
     * connection to the database. Should not be called by applications
     * directly.
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        internalClose();
        
        ConnectionEvent event = new ConnectionEvent(this);
        
        List tempListeners = new ArrayList(connectionEventListeners);
        
        Iterator iter = tempListeners.iterator();
        while (iter.hasNext()) {
            ConnectionEventListener listener = 
                (ConnectionEventListener)iter.next();
            
            if (!(listener instanceof PooledConnectionEventListener))
                continue;
            
            PooledConnectionEventListener pooledEventListener = 
                (PooledConnectionEventListener)listener;
            
            pooledEventListener.physicalConnectionClosed(event);
        }
    }
    
    /**
     * Close this connection.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void internalClose() throws SQLException {
        checkValidity();

        if (currentConnection != null) 
            currentConnection.deallocate();
        
        jdbcConnection.close();

        statements.clear();
        
        invalidate();
    }
    
    /**
     * Deallocate this object.
	 */
	public void deallocate() {
        try {
            internalClose();
        } catch(SQLException ex) {
            if (log != null)
                log.warn("Could not cleanly deallocate connection.", ex);
            
        } finally {
            // and finally notify about the event
            ConnectionEvent event = new ConnectionEvent(this);
            
            List tempListeners = new ArrayList(connectionEventListeners);
            
            Iterator iter = tempListeners.iterator();
            while (iter.hasNext()) {
                ConnectionEventListener listener = 
                    (ConnectionEventListener)iter.next();
                
                if (!(listener instanceof PooledConnectionEventListener))
                    continue;
                
                PooledConnectionEventListener pooledEventListener = 
                    (PooledConnectionEventListener)listener;
                
                pooledEventListener.physicalConnectionDeallocated(event);
            }
        }
	}

    /**
     * Get JDBC connection corresponding to this pooled connection instance.
     *
     * @return instance of {@link Connection}
     *
     * @throws SQLException if some error happened.
     */
    public
        Connection getConnection() throws SQLException {

        checkValidity();
        
        checkInPool();

        if (currentConnection != null) {

            throw new IllegalStateException(
                "Cannot provide new connection while old one is still in use.");

            //currentConnection.close();
        }

        currentConnection = new PooledConnectionHandler(jdbcConnection, this);

        Connection result = currentConnection.getProxy();
        
        configureConnectionDefaults(result);

        return result;
    }

    /**
     * Configure default values for this connection.
     * 
     * @param connection instance of {@link Connection} to configure.
     * 
     * @throws SQLException if something went wrong.
     */
    protected void configureConnectionDefaults(Connection connection) throws SQLException {
        connection.setAutoCommit(true);
        connection.setReadOnly(false);
        
        if (transactionIsolation != -1)
            connection.setTransactionIsolation(transactionIsolation);
    }

    /**
     * Handle {@link Connection#prepareStatement(String)} method call. This
     * method check internal cache first and returns prepared statement if found.
     * Otherwise, it prepares statement and caches it.
     *
     * @param statement statement to prepare.
     *
     * @return instance of {@link PreparedStatement} corresponding to the
     * <code>statement</code>.
     *
     * @throws SQLException if there was problem preparing statement.
     */
    public PreparedStatement getPreparedStatement(String statement,
        int resultSetType, int resultSetConcurrency) throws SQLException {
        
        if (!isStatementPooling())
            return jdbcConnection.prepareStatement(
                statement, resultSetType, resultSetConcurrency);
        
        PreparedStatement stmt;

        synchronized (statements) {
            XPreparedStatementCache stmtCache =
                (XPreparedStatementCache)statements.get(statement);

            if (stmtCache == null) {
                stmtCache = new XPreparedStatementCache(
                    this, statement, resultSetType, resultSetConcurrency, 
                    maxStatements);

                statements.put(statement, stmtCache);
            }

            stmt = stmtCache.take(currentConnection.getProxy());
        }

        return stmt;
    }

    /**
     * Prepare the specified statement and wrap it with cache notification
     * wrapper.
     *
     * @param statement sattement to prepare.
     *
     * @return prepared and wrapped statement.
     *
     * @throws SQLException if underlying connection threw this exception.
     */
    public XCachablePreparedStatement prepareStatement(String statement,
        int resultSetType, int resultSetConcurrency, boolean cached) throws SQLException {
        if (LOG_PREPARE_STATEMENT && getLogChannel() != null) {
            getLogChannel().info("Prepared statement for SQL '" + statement +
                "'");

        }
        PreparedStatement stmt = jdbcConnection.prepareStatement(
            statement, resultSetType, resultSetConcurrency);
            
        Class[] implementedInterfaces = 
            PooledConnectionHandler.getAllInterfaces(stmt.getClass());

        PooledPreparedStatementHandler handler =
            new PooledPreparedStatementHandler(statement, stmt, this, cached);

        // copy all implemented interfaces from the original prepared statement
        // and add XCachablePreparedStatement interface
        Class[] interfacesToImplement = 
            new Class[implementedInterfaces.length + 1];
            
        System.arraycopy(
            implementedInterfaces, 0, 
            interfacesToImplement, 0, 
            implementedInterfaces.length);
            
        interfacesToImplement[implementedInterfaces.length] = 
            XCachablePreparedStatement.class;     
        
        // create a dynamic proxy for the specified handler
        return (XCachablePreparedStatement)Proxy.newProxyInstance(
            getClass().getClassLoader(),
            interfacesToImplement,
            handler);
    }

    /**
     * Clean the cache.
     *
     * @throws SQLException if at least one of the cached statement could not
     * be closed.
     */
    private void cleanCache() throws SQLException {

        if (LOG_POOL_CLEANING && getLogChannel() != null) {
            getLogChannel().info("Prepared statement cache cleaned.");

        }
        synchronized (statements) {
            Iterator iter = statements.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry item = (Map.Entry)iter.next();

                XPreparedStatementCache stmtCache =
                    (XPreparedStatementCache)item.getValue();

                iter.remove();
                
                stmtCache.invalidate();
            }
        }
    }

    /**
     * Handle {@link java.sql.PreparedStatement#close()} method. This implementation
     * dereferences proxy in cache.
     *
     * @param statement SQL statement corresponding to the proxy.
     * @param proxy proxy wrapping the connection.
     *
     * @throws SQLException if prepared statement cannot be added to the pool.
     */
    public void statementClosed(String statement, Object proxy) 
        throws SQLException 
    {
        synchronized (statements) {
            XPreparedStatementCache stmtCache =
                (XPreparedStatementCache)statements.get(statement);

            if (stmtCache == null) {
                if (getLogChannel() != null) {
                    getLogChannel().error(
                        "Cannot find statement cache for SQL \"" + statement +
                        "\". Trying to close statement to release resources."
                        );

                }
                if (proxy instanceof XCachablePreparedStatement) {
                    ((XCachablePreparedStatement)proxy).forceClose();
                }
            } else {
                stmtCache.put(proxy);
            }
        }
    }

    public void connectionClosed(PooledConnectionHandler connection) throws SQLException {

        if (connection != currentConnection) {
            throw new IllegalArgumentException(
                "Notified about a connection that is not under my control.");
        }

        if (!keepStatements)
            cleanCache();

        currentConnection = null;

        ConnectionEvent event = new ConnectionEvent(this);
        
        List tempListeners = new ArrayList(connectionEventListeners);
        
        Iterator iter = tempListeners.iterator();
        while (iter.hasNext()) {
            ((ConnectionEventListener)iter.next()).connectionClosed(event);
        }
    }

    public void connectionErrorOccured(PooledConnectionHandler connection, SQLException ex) {
        ConnectionEvent event = new ConnectionEvent(this, ex);

        List tempListeners = new ArrayList(connectionEventListeners);
        
        Iterator iter = tempListeners.iterator();
        while (iter.hasNext()) {
            ((ConnectionEventListener)iter.next()).connectionErrorOccurred(
                event);
        }
    }

    public boolean isValid(PooledConnectionHandler connection) {
        return connection == currentConnection;
    }

    /**
     * Notify this class that transaction was committed.
     *
     * @param connection connection that was commited.
     *
     * @see XConnectionManager#connectionCommitted(PooledConnectionHandler)
     */
    public void connectionCommitted(PooledConnectionHandler connection) throws SQLException {

        if (connection != currentConnection) {
            throw new IllegalArgumentException(
                "Specified connection does not correspond " +
                "current physical connection");
        }

        if (!supportsStatementsAccrossCommit) {
            cleanCache();
        }
    }

    /**
     * Notify this class that transaction was rolled back.
     *
     * @param connection connection that was commited.
     *
     * @see XConnectionManager#connectionRolledBack(PooledConnectionHandler)
     */
    public void connectionRolledBack(PooledConnectionHandler connection) throws
        SQLException {
        if (connection != currentConnection) {
            throw new IllegalArgumentException(
                "Specified connection does not correspond " +
                "current physical connection");
        }

        if (!supportsStatementsAccrossRollback) {
            cleanCache();
        }
    }

}