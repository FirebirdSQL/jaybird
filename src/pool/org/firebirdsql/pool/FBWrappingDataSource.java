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
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.resource.Referenceable;
import javax.sql.DataSource;

import org.firebirdsql.jdbc.FBConnectionDefaults;
import org.firebirdsql.jdbc.FBConnectionHelper;

/**
 * Implementation of {@link javax.sql.DataSource} including connection pooling.
 * Following properties are supported:
 * <ul>
 * <li><code>blobBufferSize</code> size of the buffer used to transfer BLOB data.
 * <li><code>blockingTimeout</code> time in milliseconds during which 
 * {@link #getConnection()} method will block if no free connection is in pool.
 * <li><code>database</code> path to a database including the server name;
 * for example <code>localhost/3050:c:/path/to/database.gdb</code>.
 * <li><code>encoding</code> character encoding for the JDBC connection.
 * <li><code>idleTimeout</code> time in milliseconds after which 
 * idle physical connection in the pool is closed.
 * <li><code>loginTimeout</code> property from {@link javax.sql.DataSource},
 * in this context is a synonym for <code>blockingTimeout</code> (however
 * value is specified in seconds).
 * <li><code>maxSize</code> maximum number of physical connections that can
 * be opened by this data source.
 * <li><code>minSize</code> minimum number of connections that will remain open
 * by this data source.
 * <li><code>password</code> password that is used to connect to database.
 * <li><code>pingInterval</code> time interval during which connection will
 * be proved for aliveness.
 * <li><code>socketBufferSize</code> size of the socket buffer in bytes. In some 
 * cases values used by JVM by default are not optimal. This results in 
 * performance degradation (especially when you transfer big BLOBs). Usually 
 * 8192 bytes provides good results.
 * <li><code>sqlRole</code> SQL role name.
 * <li><code>tpbMapping</code> mapping of the TPB parameters to JDBC transaction
 * isolation levels.
 * <li><code>type</code> type of connection that will be created. There are 
 * three possible types: pure Java (or type 4), type 2 that will use Firebird
 * client library to connect to the database, and embedded that will use 
 * embedded engine (access to local databases). Possible values are (case 
 * insensitive):
 * <ul> 
 * <li><code>"PURE_JAVA"</code> or <code>"TYPE4"</code> for pure Java (type 4) 
 * JDBC connections;
 * <li><code>"NATIVE"</code> or <code>"TYPE2"</code> to use Firebird client
 * library;
 * <li><code>"EMBEDDED"</code> to use embedded engine.
 * </ul>
 * <li><code>userName</code> name of the user that will be used to access the 
 * database.
 * </ul>
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
public class FBWrappingDataSource implements DataSource, 
    ObjectFactory, Referenceable, Serializable 
{

    private FBConnectionPoolDataSource pool;
    
    private Reference reference;

    private String description;
    
    /**
     * Create instance of this class.
     */
    public FBWrappingDataSource() throws SQLException {
        // empty
    }
    
    private synchronized FBConnectionPoolDataSource getPool() {
        if (pool == null)
            pool = new FBConnectionPoolDataSource();
            
        return pool;
    }

    /**
     * Finalize this instance. This method will shut the pool down.
     * 
     * @throws Throwable if something went wrong.
     */
    protected void finalize() throws Throwable {
        if (pool != null) {
            pool.shutdown();

        }
        super.finalize();
    }

    /**
     * Get JDBC connection from this data source.
     * 
     * @return instance of {@link Connection}.
     * 
     * @throws SQLException if connection cannot be obtained due to some reason.
     */
    public Connection getConnection() throws SQLException {
        return getPool().getPooledConnection().getConnection();
    }

    /**
     * Get JDBC connection for the specified user name and password.
     * 
     * @link instance of {@link Connection}
     * 
     * @throws SQLException if something went wrong.
     */
    public Connection getConnection(String user, String password) 
        throws SQLException 
    {
        return getPool().getPooledConnection(user, password).getConnection();
    }

    /**
     * Get login timeout.
     * 
     * @return login timeout.
     */
    public int getLoginTimeout() {
        return getBlockingTimeout() * 1000;
    }

    /**
     * Get log writer.
     * 
     * @return instance of {@link PrintWriter}.
     */
    public PrintWriter getLogWriter() {
        return getPool().getLogWriter();
    }

    /**
     * Set login timeout.
     * 
     * @param seconds login timeout.
     */
    public void setLoginTimeout(int seconds) {
        setBlockingTimeout(seconds * 1000);
    }

    /**
     * Set log writer.
     * 
     * @param printWriter instance of {@link PrintWriter}.
     */
    public void setLogWriter(PrintWriter printWriter) {
        getPool().setLogWriter(printWriter);
    }

    /*
     * Properties of this datasource.
     */

    public int getBlockingTimeout() {
        return getPool().getBlockingTimeout();
    }

    public void setBlockingTimeout(int blockingTimeoutValue) {
        getPool().setBlockingTimeout(blockingTimeoutValue);
    }

    public String getDatabase() {
        return getPool().getDatabase();
    }

    public void setDatabase(String databaseValue) {
        getPool().setDatabase(databaseValue);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionValue) {
        this.description = descriptionValue;
    }

    public String getEncoding() {
        return getPool().getEncoding();
    }

    public void setEncoding(String encodingValue) {
        getPool().setEncoding(encodingValue);
    }
    
    public String getCharSet() {
    	return FBConnectionHelper.getJavaEncoding(getEncoding());
    }
    
    public void setCharSet(String charSet) throws SQLException {
        String iscEncoding = FBConnectionHelper.getIscEncoding(charSet);
        if (iscEncoding == null)
            throw new SQLException("Unknown character set " + charSet);
        
    	setEncoding(iscEncoding);
    }

    public int getIdleTimeout() {
        return getPool().getIdleTimeout();
    }

    public void setIdleTimeout(int idleTimeoutValue) {
        getPool().setIdleTimeout(idleTimeoutValue);
    }

    public int getMaxSize() {
        return getPool().getMaxConnections();
    }

    public void setMaxSize(int maxSizeValue) {
        getPool().setMaxConnections(maxSizeValue);
    }

    public int getMinSize() {
        return getPool().getMinConnections();
    }

    public void setMinSize(int minSizeValue) {
        getPool().setMinConnections(minSizeValue);
    }

    public String getPassword() {
        return getPool().getPassword();
    }

    public void setPassword(String passwordValue) {
        getPool().setPassword(passwordValue);
    }

    public String getTpbMapping() {
        return getPool().getTpbMapping();
    }

    public void setTpbMapping(String tpbMappingValue) {
        getPool().setTpbMapping(tpbMappingValue);
    }

    public String getUserName() {
        return getPool().getUserName();
    }

    public void setUserName(String userNameValue) {
        getPool().setUserName(userNameValue);
    }

    public int getBlobBufferSize() {
        return getPool().getBlobBufferSize();
    }

    public void setBlobBufferSize(int blobBufferSizeValue) {
        getPool().setBlobBufferSize(blobBufferSizeValue);
    }

    public String getType() {
        return getPool().getType();
    }

    public void setType(String typeValue) throws SQLException {
        getPool().setType(typeValue);
    }

    public int getPingInterval() {
        return getPool().getPingInterval();
    }

    public void setPingInterval(int pingIntervalValue) {
        getPool().setPingInterval(pingIntervalValue);
    }

    public int getSocketBufferSize() {
        return getPool().getSocketBufferSize();
    }

    public String getSqlRole() {
        return getPool().getSqlRole();
    }

    public void setSocketBufferSize(int socketBufferSize) {
        getPool().setSocketBufferSize(socketBufferSize);
    }

    public void setSqlRole(String sqlRole) {
        getPool().setSqlRole(sqlRole);
    }
    
    public String getNonStandardProperty(String key) {
        return getPool().getNonStandardProperty(key);
    }
    
    public void setNonStandardProperty(String key, String value) {
        if (key == null)
            throw new NullPointerException("Key is null");
            
        if (value == null)
            value = "";
            
        getPool().setNonStandardProperty(key, value);
    }
    
    /**
     * @deprecated use {@link #isPooling()} method.
     */
    public boolean getPooling() {
        return getPool().isPooling();
    }
    
    public boolean isPooling() {
        return getPool().isPooling();
    }
    
    public void setPooling(boolean pooling) {
        getPool().setPooling(pooling);
    }
    
    public boolean isStatementPooling() {
        return getPool().isStatementPooling();
    }
    
    public void setStatementPooling(boolean statementPooling) {
        getPool().setStatementPooling(statementPooling);
    }
    
    public int getIdleTimeoutMinutes() {
        return getIdleTimeout() / 60 / 1000;
    }
    
    public void setIdleTimeoutMinutes(int timeout) {
        setIdleTimeout(timeout * 60 * 1000);
    }
    
    public int getConnectionCount() throws SQLException {
        return getPool().getFreeSize();
    }
    
    /*
     * JNDI-related code. 
     */

    private static final String REF_BLOB_BUFFER_SIZE = "blobBufferSize";
    private static final String REF_BLOCKING_TIMEOUT = "blockingTimeout";
    private static final String REF_DATABASE = "database";
    private static final String REF_DESCRIPTION = "description";
    private static final String REF_ENCODING = "encoding";
    private static final String REF_IDLE_TIMEOUT = "idleTimeout";
    private static final String REF_LOGIN_TIMEOUT = "loginTimeout";
    private static final String REF_MAX_SIZE = "maxSize";
    private static final String REF_MIN_SIZE = "minSize";
    private static final String REF_PASSWORD = "password";
    private static final String REF_PING_INTERVAL = "pingInterval";
    private static final String REF_SOCKET_BUFFER_SIZE = "socketBufferSize";
    private static final String REF_SQL_ROLE = "sqlRole";
    private static final String REF_TPB_MAPPING = "tpbMapping";
    private static final String REF_TYPE = "type";
    private static final String REF_USER_NAME = "userName";

    /**
     * Get object instance for the specified name in the specified context.
     * This method constructs new datasource if <code>obj</code> represents
     * {@link Reference}, whose factory class is equal to this class.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, 
        Hashtable environment) throws Exception 
    {
        if (!(obj instanceof Reference)) return null;

        Reference ref = (Reference)obj;

        if (!getClass().getName().equals(ref.getClassName()))
            return null;

        FBWrappingDataSource ds = new FBWrappingDataSource();
        
        String addr = getRefAddr(ref, REF_BLOB_BUFFER_SIZE);
        if (addr != null)
            ds.setBlobBufferSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_BLOCKING_TIMEOUT);
        if (addr != null)
            ds.setBlockingTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_DATABASE);
        if (addr != null)
            ds.setDatabase(addr);
            
        addr = getRefAddr(ref, REF_DESCRIPTION);
        if (addr != null)
            ds.setDescription(addr);
            
        addr = getRefAddr(ref, REF_ENCODING);
        if (addr != null)
            ds.setEncoding(addr);
            
        addr = getRefAddr(ref, REF_IDLE_TIMEOUT);
        if (addr != null)
            ds.setIdleTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_LOGIN_TIMEOUT);
        if (addr != null)
            ds.setLoginTimeout(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MAX_SIZE);
        if (addr != null)
            ds.setMaxSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_MIN_SIZE);
        if (addr != null)
            ds.setMinSize(Integer.parseInt(addr));

        addr = getRefAddr(ref, REF_PASSWORD);
        if (addr != null)
            ds.setPassword(addr);
            
        addr = getRefAddr(ref, REF_PING_INTERVAL);
        if (addr != null)
            ds.setPingInterval(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_SOCKET_BUFFER_SIZE);
        if (addr != null)
            ds.setSocketBufferSize(Integer.parseInt(addr));
            
        addr = getRefAddr(ref, REF_SQL_ROLE);
        if (addr != null)
            ds.setSqlRole(addr);
            
        addr = getRefAddr(ref, REF_TPB_MAPPING);
        if (addr != null)
            ds.setTpbMapping(addr);
            
        addr = getRefAddr(ref, REF_TYPE);
        if (addr != null)
            ds.setType(addr);
            
        addr = getRefAddr(ref, REF_USER_NAME);
        if (addr != null)
            ds.setUserName(addr);
            
        return ds;
    }
    
    private String getRefAddr(Reference ref, String type) {
        RefAddr addr = ref.get(type);
        if (addr == null)
            return null;
        else
            return addr.getContent().toString();
    }
    
    /**
     * Get JDNI reference.
     * 
     * @return instance of {@link Reference}.
     */
    public Reference getReference() {
        if (reference == null)
            return getDefaultReference();
        else
            return reference;
    }
    
    /**
     * Set JNDI reference for this data source.
     * 
     * @param reference JNDI reference.
     */
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    /**
     * Get default JNDI reference for this datasource. This method is called if
     * datasource is used in non-JCA environment.
     * 
     * @return instance of {@link Reference} containing all information 
     * that allows to reconstruct the datasource.
     */
    public Reference getDefaultReference() {
        Reference ref = new Reference(getClass().getName());
        
        if (getBlobBufferSize() != FBConnectionDefaults.DEFAULT_BLOB_BUFFER_SIZE)
            ref.add(new StringRefAddr(REF_BLOB_BUFFER_SIZE, 
                String.valueOf(getBlobBufferSize())));
            
        if (getBlockingTimeout() != FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT)
            ref.add(new StringRefAddr(REF_BLOCKING_TIMEOUT, 
                String.valueOf(getBlockingTimeout())));

        if (getDatabase() != null)            
            ref.add(new StringRefAddr(REF_DATABASE, getDatabase()));
            
        if (getDescription() != null)
            ref.add(new StringRefAddr(REF_DESCRIPTION, getDescription()));
            
        if (getEncoding() != null)
            ref.add(new StringRefAddr(REF_ENCODING, getEncoding()));

        if (getIdleTimeout() != FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT)
            ref.add(new StringRefAddr(REF_IDLE_TIMEOUT,
                String.valueOf(getIdleTimeout())));
            
        if (getLoginTimeout() != FBPoolingDefaults.DEFAULT_LOGIN_TIMEOUT)
            ref.add(new StringRefAddr(REF_LOGIN_TIMEOUT,
                String.valueOf(getLoginTimeout())));

        if (getMaxSize() != FBPoolingDefaults.DEFAULT_MAX_SIZE)
            ref.add(new StringRefAddr(REF_MAX_SIZE, 
                String.valueOf(getMaxSize())));

        if (getMinSize() != FBPoolingDefaults.DEFAULT_MIN_SIZE)
            ref.add(new StringRefAddr(REF_MIN_SIZE,
                String.valueOf(getMinSize())));
            
        if (getPassword() != null)
            ref.add(new StringRefAddr(REF_PASSWORD, getPassword()));

        if (getPingInterval() != FBPoolingDefaults.DEFAULT_PING_INTERVAL)
            ref.add(new StringRefAddr(REF_PING_INTERVAL, 
                String.valueOf(getPingInterval())));
        
        if (getSocketBufferSize() != FBConnectionDefaults.DEFAULT_SOCKET_BUFFER_SIZE)
            ref.add(new StringRefAddr(REF_SOCKET_BUFFER_SIZE, 
            	String.valueOf(getSocketBufferSize())));
        	
        if (getSqlRole() != null)
       	    ref.add(new StringRefAddr(REF_SQL_ROLE, getSqlRole()));
       	
        if (getTpbMapping() != null)
       	    ref.add(new StringRefAddr(REF_TPB_MAPPING, getTpbMapping()));
            
        if (getType() != null)
       	    ref.add(new StringRefAddr(REF_TYPE, getType()));
        
        if (getUserName() != null)
       	    ref.add(new StringRefAddr(REF_USER_NAME, getUserName()));
        
        return ref;
    }

}
