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

import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jca.FBPoolingConnectionManager;
import org.firebirdsql.jca.ManagedConnectionPool;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnectionFactory;

import org.firebirdsql.jca.FBTpb;
import org.firebirdsql.jca.FBTpbMapper;
import org.firebirdsql.jca.FBResourceException;
import javax.naming.NamingException;

/**
 * This is a simple implementation of {@link DataSource} interface. Connections
 * are physically opened in {@link DataSource#getConnection()} method and
 * physically closed in {@link Connection#close()} method. If you need connection
 * pooling, use {@link FBWrappingDataSource} instead.
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class FBSimpleDataSource implements DataSource, Serializable, Referenceable {
    
    transient protected FBManagedConnectionFactory mcf;
    transient protected FBDataSource ds;
    transient protected PrintWriter log;
    
    protected Reference jndiReference;
    protected String description;
    protected int loginTimeout;
    protected String tpbMapping;
    
    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource() {
        mcf = new FBManagedConnectionFactory();
    }
    
    /**
     * Get buffer length for the BLOB fields. 
     * 
     * @return length of BLOB buffer.
     */
    public Integer getBlobBufferLength() {
        return mcf.getBlobBufferLength();
    }
    
    /**
     * Set BLOB buffer length. This value influences the performance when 
     * working with BLOB fields.
     * 
     * @param length new length of the BLOB buffer.
     */
    public void setBlobBufferLength(Integer length) {
        mcf.setBlobBufferLength(length);
    }
    
    /**
     * Get name of the database. 
     * 
     * @return database name, value is equal to the part of full JDBC URL without
     * the <code>jdbc:firebirdsql:</code> part.
     * 
     * @deprecated use {@link #getDatabase} instead for the sake of naming
     * compatibility.
     */
    public String getDatabaseName() {
        return getDatabase();
    }
    
    /**
     * Set database name.
     * 
     * @param name connection URL without <code>"jdbc:firebirdsql:"</code>
     * prefix (<code>"//localhost:3050/c:/database/employee.gdb"</code>) for
     * example).
     * 
     * @deprecated use {@link #setDatabase(String)} instead for the sake of 
     * naming compatibility.
     */
    public void setDatabaseName(String name) throws ResourceException {
        setDatabase(name);
    }

    /**
     * Get name of the database. 
     * 
     * @return database name, value is equal to the part of full JDBC URL without
     * the <code>jdbc:firebirdsql:</code> part.
     */
    public String getDatabase() {
        return mcf.getDatabase();
    }

    /**
     * Set database name.
     * 
     * @param name connection URL without <code>"jdbc:firebirdsql:"</code>
     * prefix (<code>"//localhost:3050/c:/database/employee.gdb"</code>) for
     * example).
     * 
     * @throws SQLException if database name cannot be set.
     */
    public void setDatabase(String name)
    {
        mcf.setDatabase(name);
    }
    
    /**
     * Get user name that is used in {@link #getConnection()} method.
     * 
     * @return default user name.
     * 
     * @deprecated use {@link #getUserName()} instead for the sake of naming
     * compatibility.
     */
    public String getUser() {
        return getUserName();
    }
    
    /**
     * Set user name that will be used in {@link #getConnection()} method.
     * 
     * @param user default user name.
     * 
     * @deprecated use {@link setUserName(String)} instead for the sake of
     * naming compatibility.
     */
    public void setUser(String user) {
        setUserName(user);
    }
    
    /**
     * Get user name that is used in {@link #getConnection()} method.
     * 
     * @return default user name.
     */
    public String getUserName() {
        return mcf.getUserName();
    }
    
    /**
     * Set user name that will be used in {@link #getConnection()} method.
     * 
     * @param userName default user name.
     */
    public void setUserName(String userName) {
        mcf.setUserName(userName);
    }
    
    /**
     * Get password used in {@link #getConnection()} method.
     * 
     * @return password corresponding to the user name returned by 
     * {@link #getUserName()}.
     */
    public String getPassword() {
        return mcf.getPassword();
    }
    
    /**
     * Set password that will be used in the {@link #getConnection()} method.
     * 
     * @param password password corresponding to the user name set in 
     * {@link #setUserName(String)}.
     */
    public void setPassword(String password) {
        mcf.setPassword(password);
    }
    
    /**
     * Get encoding for connections produced by this data source.
     * 
     * @return encoding for the connection.
     */
    public String getEncoding() {
        return mcf.getEncoding();
    }
    
    /**
     * Set encoding for connections produced by this data source.
     * 
     * @param encoding encoding for the connection.
     */
    public void setEncoding(String encoding) {
        mcf.setEncoding(encoding);
    }
    
    public String getTpbMapping() {
        return tpbMapping;
    }
    
    public void setTpbMapping(String tpbMapping) throws FBResourceException {
        mcf.setTpbMapper(new FBTpbMapper(tpbMapping, getClass().getClassLoader()));
        this.tpbMapping = tpbMapping;
    }
    
    /*
     * INTERFACES IMPLEMENTATION
     */


    /**
     * Get previously set JNDI reference.
     * 
     * @return instance of {@link Reference} set previously.
     * 
     * @throws NamingException if something went wrong.
     */
    public Reference getReference() throws NamingException {
        return jndiReference;
    }

    /**
     * Set JNDI reference for this data source.
     * 
     * @param reference reference to set.
     */
    public void setReference(Reference reference) {
        jndiReference = reference;
    }

    /**
     * Get JDBC connection with default credentials.
     * 
     * @return new JDBC connection.
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Get JDBC connection with the specified credentials.
     * 
     * @param username user name for the connection.
     * @param password password for the connection.
     * 
     * @return new JDBC connection.
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection(String username, String password) throws SQLException {
        return getDataSource().getConnection(username, password);
    }

    /**
     * Get log for this datasource.
     * 
     * @return log associated with this datasource.
     *
     * @throws SQLException if something went wrong.
     */
    public PrintWriter getLogWriter() throws SQLException {
        return log;
    }

    /**
     * Set log for this datasource.
     * 
     * @param log instance of {@link PrintWriter} that should be associated 
     * with this datasource.
     * 
     * @throws SQLException if something went wrong.
     */
    public void setLogWriter(PrintWriter log) throws SQLException {
        this.log = log;
    }

    /**
     * Get login timeout specified for this datasource.
     * 
     * @return login timeout of this datasource in seconds.
     * 
     * @throws SQLException if something went wrong.
     */
    public int getLoginTimeout() throws SQLException {
        return loginTimeout;
    }

    /**
     * Set login timeout for this datasource.
     * 
     * @param loginTimeout login timeout in seconds.
     * @throws SQLException
     */
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        this.loginTimeout = loginTimeout;
    }
    
    /**
     * Get description of this datasource.
     * 
     * @return description of this datasource.
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set description of this datasource.
     * 
     * @param description description of this datasource.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get underlying connection factory (in our case instance of 
     * {@link FBDataSource} class) that will provide JDBC connections.
     * 
     * @return JDBC connection factory.
     * 
     * @throws SQLException if something went wrong.
     */
    protected synchronized DataSource getDataSource() throws SQLException {
        if (ds != null)
            return ds;
            
        if (mcf.getDatabase() == null || "".equals(mcf.getDatabase().trim()))
            throw new SQLException(
                "Database was not specified. Cannot provide connections.");
                
        try {
            ds = (FBDataSource)mcf.createConnectionFactory();
            
            return ds;
        } catch(ResourceException rex) {
            
            throw new SQLException(rex.getMessage());
            
        }
    }
}
