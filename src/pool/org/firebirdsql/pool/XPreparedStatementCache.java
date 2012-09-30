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
import java.util.*;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.util.SQLExceptionChainBuilder;

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

    private static final boolean LOG_STATEMENT_IN_POOL = PoolDebugConfiguration.DEBUG_STMT_POOL;

    private static Logger logChannel = 
        LoggerFactory.getLogger(XPreparedStatementCache.class, false);

    private XStatementManager owner;
    private BlockingStack freeReferences = new BlockingStack();
    private HashMap workingReferences = new HashMap();
    
    private XPreparedStatementModel key;
    private int maxSize;
    
    /**
     * Create instance of this class for the specified SQL statement and
     * result set type and concurrency.
     * 
     * @param owner instance of {@link XStatementManager} that will prepare
     * new statements.
     * 
     * @param key key of the prepared statement.
     * 
     * @param maxSize maximum pool size.
     */
    public XPreparedStatementCache(XStatementManager owner,
            XPreparedStatementModel key, int maxSize) {
        
        this.owner = owner;
        this.maxSize = maxSize;
        
        if (key == null)
            throw new NullPointerException("Null objects cannot be guarded.");

        this.key = key;
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

        if (key == null)
            throw new IllegalStateException(
                "This reference guard was already destroyed.");

        try {
            XCachablePreparedStatement result;

            if (!CACHE_PREPARED_STATEMENTS)
                result = prepareStatement(key, false);
            else {
                if (freeReferences.isEmpty()) {

                    if (LOG_STATEMENT_IN_POOL && logChannel != null)
                        logChannel.info(
                            "Found no free prepared statements, " + 
                            "preparing new one.");

                    result = prepareStatement(key,
                        workingReferences.size() < maxSize);
                } else {
                    if (LOG_STATEMENT_IN_POOL && logChannel != null)
                        logChannel.info(
                            "Found free prepared statement in pool.");    

                    result = (XCachablePreparedStatement)freeReferences.pop();
                }
            }
            
            result.setConnection(connection);
            
            workingReferences.put(result.getOriginal(), result);

            return result;
        } catch(InterruptedException iex) {
            throw new SQLException("Cannot prepare SQL statement in pool");
        }
    }
    
    /**
     * Prepare the SQL statement. This method checks the type of the statement
     * (i.e. whether generated keys should be returned or not).
     * 
     * @param key instance of {@link XPreparedStatementModel} that will be used
     * to prepare the statement.
     * 
     * @param cached <code>true</code> if statement should be cached, otherwise
     * <code>false</code>
     * 
     * @return instance of {@link XCachablePreparedStatement}
     * 
     * @throws SQLException if statement cannot be prepared.
     */
    protected XCachablePreparedStatement prepareStatement(
            XPreparedStatementModel key, boolean cached) throws SQLException {
        
        return owner.prepareStatement(key, cached);
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
                
                XCachablePreparedStatement statement = 
                    (XCachablePreparedStatement)reference;
                
                if (statement.isCached()) {
                    statement.setConnection(null);
                    freeReferences.push(reference);
                } 
                
                workingReferences.remove(statement.getOriginal());

                // release statement if it is not cached
                if (!statement.isCached())
                    statement.forceClose();
                
                if (LOG_STATEMENT_IN_POOL && logChannel != null)
                    logChannel.info("Returned prepared statement to pool.");

            } else
                ((XCachablePreparedStatement)reference).forceClose();
                
        } catch(InterruptedException ex) {
            logChannel.warn("Could not put statement back to pool.", ex);
        }
    }

    /**
     * Invalidate this pool. After invoking this method no other
     * method can be invoked.
     */
    synchronized void invalidate() throws SQLException {
        key = null;
        
        // clear free references
        SQLExceptionChainBuilder chain = new SQLExceptionChainBuilder();
        while(!freeReferences.isEmpty()) {
            try {
                XCachablePreparedStatement result =
                    (XCachablePreparedStatement)freeReferences.pop();

                result.forceClose();
            } catch(InterruptedException ex) {
                // ignore
            } catch(SQLException ex) {
                chain.append(ex);
            }
        }
        
        // clear working references, even they're currently in use
        // since we are invalidating the pool and there's no way back
        Iterator iter = workingReferences.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            XCachablePreparedStatement item = 
                (XCachablePreparedStatement)entry.getValue();
                
            try {
                item.forceClose();
            } catch(SQLException ex) {
                chain.append(ex);
            }
        }
        workingReferences.clear();
        
        if (chain.hasException())
            throw chain.getException();
    }

}