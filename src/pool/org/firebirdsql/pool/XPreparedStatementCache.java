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

import java.sql.*;

import EDU.oswego.cs.dl.util.concurrent.*;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * This class represents a cache of prepared statements corresponding to 
 * one SQL statement. Instance of this class automatically prepares new 
 * {@link java.sql.PreparedStatement} instance if no free statements are
 * available.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
class XPreparedStatementCache {
    
    private static final boolean CACHE_PREPARED_STATEMENTS = true;

    private static final boolean LOG_PREPARE_STATEMENT = false;
    private static final boolean LOG_STATEMENT_IN_POOL = false;
    private static final boolean LOG_POOL_CLEANING = false;

    private static Logger logChannel = 
        LoggerFactory.getLogger(XPreparedStatementCache.class, false);

    private XStatementManager owner;
    private LinkedQueue freeReferences = new LinkedQueue();
    private String sql;
    private int resultSetType;
    private int resultSetConcurrency;
    
    /**
     * Create instance of this class for the specified SQL statement and
     * result set type and concurrency.
     * 
     * @param owner instance of {@link XStatementManager} that will prepare
     * new statements.
     * 
     * @param sql SQL statement to prepare.
     * 
     * @param resultSetType type of result set.
     * 
     * @param resultSetConcurrency result set concurrency.
     */
    public XPreparedStatementCache(XStatementManager owner, String sql,
        int resultSetType, int resultSetConcurrency) 
    {
        this.owner = owner;
        
        if (sql == null)
            throw new NullPointerException(
                "Null objects cannot be guarded.");

        this.sql = sql;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
    }

    /**
     * Reference prepared statement. This method checks prepared statement out
     * of the pool. If no free statement is available, it prepares new one.
     * 
     * @param connection instance of {@link Connection} to associate
     * with prepared statement.
     * 
     * @return free instance of {@link PreparedStatement}.
     * 
     * @throws SQLException if prepared statement cannot be obtained from the 
     * pool.
     */
    synchronized XCachablePreparedStatement take(Connection connection) 
        throws SQLException 
    {

        if (sql == null)
            throw new IllegalStateException(
                "This reference guard was already destroyed.");

        try {
            XCachablePreparedStatement result;

            if (!CACHE_PREPARED_STATEMENTS)
                result = owner.prepareStatement(
                    sql, resultSetType, resultSetConcurrency);
            else {
                if (freeReferences.isEmpty()) {

                    if (LOG_STATEMENT_IN_POOL)
                        logChannel.info(
                            "Found no free prepared statements, " + 
                            "preparing new one.");

                    result = owner.prepareStatement(
                        sql, resultSetType, resultSetConcurrency);
                } else {
                    if (LOG_STATEMENT_IN_POOL)
                        logChannel.info(
                            "Found free prepared statement in pool.");    

                    result = (XCachablePreparedStatement)freeReferences.take();
                }
            }
            
            result.setConnection(connection);

            return result;
        } catch(InterruptedException iex) {
            throw new SQLException("Cannot prepare SQL statement in pool");
        }
    }

    /**
     * Dereference object. If <code>reference</code> is the same as guarded
     * object, counter is simply decreased, otherwise exception is thrown.
     * 
     * @param reference object to dereference.
     * 
     * @throws IllegalArgumentException if <code>reference</code> is not the
     * same as guarded object.
     * 
     * @throws SQLException if prepared statement cannot be returned into the 
     * pool.
     */
    synchronized void put(Object reference) throws SQLException {

        if (reference == null)
            throw new NullPointerException();

        try {
            if (CACHE_PREPARED_STATEMENTS) {
                
                // clean reference
                ((XCachablePreparedStatement)reference).setConnection(null);
                
                freeReferences.put(reference);

                if (LOG_STATEMENT_IN_POOL)
                    logChannel.info("Returned prepared statement to pool.");

            } else
                ((XCachablePreparedStatement)reference).forceClose();
                
        } catch(InterruptedException ex) {
            logChannel.warn("Could not put statement back to pool.", ex);
        }
    }

    /**
     * Invalidate this reference guard. After invoking this method no other
     * method can be invoked.
     */
    synchronized void invalidate() throws SQLException {
        sql = null;
        
        while(!freeReferences.isEmpty()) {
            try {
                XCachablePreparedStatement result =
                    (XCachablePreparedStatement)freeReferences.take();

                result.forceClose();
            } catch(InterruptedException ex) {
                // ignore
            }
        }
    }

}