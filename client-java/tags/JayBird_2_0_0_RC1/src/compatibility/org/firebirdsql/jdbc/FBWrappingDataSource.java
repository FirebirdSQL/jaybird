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
package org.firebirdsql.jdbc;

import java.sql.SQLException;


/**
 * Wrapping data source. This implementation is available only to provide easy
 * migration from JayBird 1.0.x to a new connection pooling framework.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * 
 * @deprecated Use {@link org.firebirdsql.pool.FBWrappingDataSource} instead.
 */
public class FBWrappingDataSource extends
        org.firebirdsql.pool.FBWrappingDataSource 
{
    
    /**
     * Create instanec of this class.
     * 
     * @throws SQLException if pool cannot be instantiated.
     */
    public FBWrappingDataSource() throws SQLException {
        super();
    }
    
    public int getBlobBufferLength() {
        return getBlobBufferSize();
    }
    
    public void setBlobBufferLength(int length) {
        setBlobBufferSize(length);
    }
    
    public String getDatabaseName() {
        return getDatabase();
    }
    
    public void setDatabaseName(String databaseName) {
        setDatabase(databaseName);
    }
    
    public String getUser() {
        return getUserName();
    }
    
    public void setUser(String user) {
        setUserName(user);
    }
    
    public int getMaxSize() {
        return getMaxConnections();
    }
    
    public void setMaxSize(int maxSize) {
        setMaxConnections(maxSize);
    }
    
    public int getMinSize() {
        return getMinConnections();
    }
    
    public void setMinSize(int minSize) {
        setMinConnections(minSize);
    }
    
    public int getIdleTimeoutMinutes() {
        return getIdleTimeout() / 60 / 1000;
    }
    
    public void setIdleTimeoutMinutes(int timeout) {
        setIdleTimeout(timeout * 60 * 1000);
    }    
    
    public Integer getTransactionIsolation() {
        return new Integer(getTransactionIsolationLevel());
    }
    
    public void setTransactionIsolation(Integer isolation) {
        setTransactionIsolationLevel(isolation.intValue());
    }
}
