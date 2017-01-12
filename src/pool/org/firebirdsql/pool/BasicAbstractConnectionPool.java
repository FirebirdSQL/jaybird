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

import java.io.*;
import java.sql.SQLException;

import javax.naming.*;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

/**
 * Base class for connection pool implementations. Main feature of this class is
 * that it implements {@link org.firebirdsql.pool.ConnectionPoolConfiguration}
 * interface and releives developers from creating getters and setters for 
 * pool configuration parameters. Additionally this class provides basic 
 * functionality for JNDI-enabled connection pools.
 * 
 * No other functionality is available.
 * 
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 */
@Deprecated
public abstract class BasicAbstractConnectionPool 
    extends AbstractConnectionPool 
    implements ConnectionPoolConfiguration, ConnectionPoolDataSource,
    Serializable, Referenceable
{

    /*
     * Following fields contain information about the pool characteristics.
     */
    private int minPoolSize = FBPoolingDefaults.DEFAULT_MIN_SIZE;
    private int maxPoolSize = FBPoolingDefaults.DEFAULT_MAX_SIZE;

    private int blockingTimeout = FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT;
    private int retryInterval = FBPoolingDefaults.DEFAULT_RETRY_INTERVAL;
    private int maxIdleTime = FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT;

    private int pingInterval = FBPoolingDefaults.DEFAULT_PING_INTERVAL;
    private String pingStatement;

    private boolean pooling = true;
    private boolean statementPooling = true;
    private boolean keepStatements = true;
    private int maxStatements = FBPoolingDefaults.DEFAULT_MAX_STATEMENTS;
    
    private Reference reference;
    
    /**
     * Create instance of this class. Default constructor introduced to make
     * it available to subclasses.
     */
    protected BasicAbstractConnectionPool() {
        super();
    }

    public abstract int getLoginTimeout() throws SQLException;
    public abstract void setLoginTimeout(int seconds) throws SQLException;

    public abstract PrintWriter getLogWriter() throws SQLException;
    public abstract void setLogWriter(PrintWriter printWriter) throws SQLException;
    
    public abstract PooledConnection getPooledConnection() throws SQLException;
    public abstract PooledConnection getPooledConnection(
        String user, String password) throws SQLException;
    
    public ConnectionPoolConfiguration getConfiguration() {
        return this;
    }
    
    public int getBlockingTimeout() {
        return blockingTimeout;
    }

    public void setBlockingTimeout(int blockingTimeout) {
        this.blockingTimeout = blockingTimeout;
    }

    /**
     * @deprecated non-standard name, use {@link #getMaxIdleTime()}
     */
    public int getIdleTimeout() {
        return getMaxIdleTime();
    }

    /**
     * @deprecated non-standard name, use {@link #setMaxIdleTime(int)}
     */
    public void setIdleTimeout(int idleTimeout) {
        setMaxIdleTime(idleTimeout);
    }
    
    public int getMaxIdleTime() {
        return maxIdleTime;
    }
    
    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * @deprecated non-standard name, use {@link #getMaxPoolSize()}
     */
    public int getMaxConnections() {
        return getMaxPoolSize();
    }

    /**
     * @deprecated non-standard name, use {@link #setMaxPoolSize(int)}
     */
    public void setMaxConnections(int maxConnections) {
        setMaxPoolSize(maxConnections);
    }
    
    public int getMaxPoolSize() {
        return maxPoolSize;
    }
    
    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * @deprecated non-standard name, use {@link #getMinPoolSize()}
     */
    public int getMinConnections() {
        return getMinPoolSize();
    }

    /**
     * @deprecated non-standard name, use {@link #setMinPoolSize(int)}
     */
    public void setMinConnections(int minConnections) {
        setMinPoolSize(minConnections);
    }
    
    public int getMinPoolSize() {
        return minPoolSize;
    }
    
    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    public String getPingStatement() {
        return pingStatement;
    }

    public void setPingStatement(String pingStatement) {
        this.pingStatement = pingStatement;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isPingable() {
        return getPingInterval() > 0 && getPingStatement() != null;
    }

    public boolean isPooling() {
        return pooling;
    }

    public void setPooling(boolean pooling) {
        this.pooling = pooling;
    }

    public boolean isStatementPooling() {
        return statementPooling;
    }

    public void setStatementPooling(boolean statementPooling) {
        
        // no statement pooling if max allowed statements is 0
        if (getMaxStatements() == 0)
            statementPooling = false;
        
        this.statementPooling = statementPooling;
    }
    
    public boolean isKeepStatements() {
        return keepStatements;
    }
    
    public void setKeepStatements(boolean keepStatements) {
        this.keepStatements = keepStatements;
    }
    
    public int getMaxStatements() {
        return maxStatements;
    }
    
    public void setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
        
        if (maxStatements > 0 && !isStatementPooling())
            setStatementPooling(true);
        else
        if (maxStatements == 0)
            setStatementPooling(false);
    }
    
    private static final String REF_BLOCKING_TIMEOUT = "blockingTimeout";
    private static final String REF_RETRY_INTERVAL = "retryInterval";
    private static final String REF_LOGIN_TIMEOUT = "loginTimeout";

    private static final String REF_PING_INTERVAL = "pingInterval";
    private static final String REF_PING_STATEMENT = "pingStatement";
    
    private static final String REF_POOLING = "pooling";
    private static final String REF_STATEMENT_POOLING = "statementPooling";
    
    private static final String REF_MAX_STATEMENTS = "maxStatements";
    private static final String REF_MAX_POOL_SIZE = "maxPoolSize";
    private static final String REF_MIN_POOL_SIZE = "minPoolSize";
    private static final String REF_MAX_IDLE_TIME = "maxIdleTime";
    
    private static final String REF_MAX_CONNECTIONS = "maxConnections";
    private static final String REF_MIN_CONNECTIONS = "minConnections";
    private static final String REF_IDLE_TIMEOUT = "idleTimeout";

    protected abstract BasicAbstractConnectionPool createObjectInstance();
    /**
     * Get object instance for the specified name in the specified context.
     * This method constructs new datasource if <code>obj</code> represents
     * {@link Reference}, whose factory class is equal to this class.
     */
    
    public Object getObjectInstance(Object obj) throws Exception 
    {
        
        if (!(obj instanceof Reference)) return null;
    
        Reference ref = (Reference)obj;
    
        if (!getClass().getName().equals(ref.getClassName()))
            return null;
    
        BasicAbstractConnectionPool ds = createObjectInstance();
        
        int[] addressesToRemove = new int[ref.size()];
        
        for (int i = 0; i < ref.size(); i++) {
            RefAddr element = ref.get(i);
            
            String type = element.getType();
            String addr = element.getContent().toString();
            
            if (REF_BLOCKING_TIMEOUT.equals(type))
                ds.setBlockingTimeout(Integer.parseInt(addr));
            else
            if (REF_MAX_IDLE_TIME.equals(type))
                ds.setMaxIdleTime(Integer.parseInt(addr));
            else
            if (REF_IDLE_TIMEOUT.equals(type))
                ds.setMaxIdleTime(Integer.parseInt(addr));
            else
            if (REF_LOGIN_TIMEOUT.equals(type))
                ds.setLoginTimeout(Integer.parseInt(addr));
            else
            if (REF_MAX_POOL_SIZE.equals(type))
                ds.setMaxPoolSize(Integer.parseInt(addr));
            else
            if (REF_MIN_POOL_SIZE.equals(type))
                ds.setMinPoolSize(Integer.parseInt(addr));
            else
            if (REF_MAX_CONNECTIONS.equals(type))
                ds.setMaxPoolSize(Integer.parseInt(addr));
            else
            if (REF_MIN_CONNECTIONS.equals(type))
                ds.setMinPoolSize(Integer.parseInt(addr));
            else
            if (REF_PING_INTERVAL.equals(type))
                ds.setPingInterval(Integer.parseInt(addr));
            else
            if (REF_RETRY_INTERVAL.equals(type))
                ds.setRetryInterval(Integer.parseInt(addr));
            else
            if (REF_PING_STATEMENT.equals(type)) 
                ds.setPingStatement(addr);
            else
            if (REF_POOLING.equals(type))
                ds.setPooling(Boolean.valueOf(addr).booleanValue());
            else
            if (REF_STATEMENT_POOLING.equals(type))
                ds.setStatementPooling(Boolean.valueOf(addr).booleanValue());
            else
            if (REF_MAX_STATEMENTS.equals(type))
                ds.setMaxStatements(Integer.parseInt(addr));
            else
                continue;
            
            // set flag that we matched the address, so it has to be removed
            // we rely on fact that if none address was matched, last else
            // clause will be executed and next code will not be executed.
            addressesToRemove[i] = 1;
        }
        
        // removed matched addresses
        for (int i = addressesToRemove.length - 1; i >= 0 ; i--) {
            if (addressesToRemove[i] == 1)
                ref.remove(i);
        }
        
        return ds;
    }
    

    protected String getRefAddr(Reference ref, String type) {
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
        Reference ref = new Reference(getClass().getName(), 
                getClass().getName(), null);
        
        if (getBlockingTimeout() != FBPoolingDefaults.DEFAULT_BLOCKING_TIMEOUT)
            ref.add(new StringRefAddr(REF_BLOCKING_TIMEOUT, 
                String.valueOf(getBlockingTimeout())));
    
        if (getMaxIdleTime() != FBPoolingDefaults.DEFAULT_IDLE_TIMEOUT)
            ref.add(new StringRefAddr(REF_MAX_IDLE_TIME,
                String.valueOf(getMaxIdleTime())));
    
        if (getMaxPoolSize() != FBPoolingDefaults.DEFAULT_MAX_SIZE)
            ref.add(new StringRefAddr(REF_MAX_POOL_SIZE, 
                String.valueOf(getMaxPoolSize())));
    
        if (getMinPoolSize() != FBPoolingDefaults.DEFAULT_MIN_SIZE)
            ref.add(new StringRefAddr(REF_MIN_POOL_SIZE,
                String.valueOf(getMinPoolSize())));
            
        if (getPingInterval() != FBPoolingDefaults.DEFAULT_PING_INTERVAL)
            ref.add(new StringRefAddr(REF_PING_INTERVAL, 
                String.valueOf(getPingInterval())));
        
        if (getRetryInterval() != FBPoolingDefaults.DEFAULT_RETRY_INTERVAL)
            ref.add(new StringRefAddr(REF_RETRY_INTERVAL, 
                    String.valueOf(getRetryInterval())));
        
        if (getPingStatement() != null)
            ref.add(new StringRefAddr(REF_PING_STATEMENT,
                    String.valueOf(getPingStatement())));
        
        if (!isPooling())
            ref.add(new StringRefAddr(REF_POOLING,
                    String.valueOf(isPooling())));
        
        if (!isStatementPooling())
            ref.add(new StringRefAddr(REF_STATEMENT_POOLING,
                    String.valueOf(isStatementPooling())));
        
        if (getMaxStatements() != FBPoolingDefaults.DEFAULT_MAX_STATEMENTS)
            ref.add(new StringRefAddr(REF_MAX_STATEMENTS,
                    String.valueOf(getMaxStatements())));
            
        return ref;
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(obj);
            out.flush();
        } catch(IOException ex) {
            return null;
        }
        
        return bout.toByteArray();
    }

    public static Object deserialize(byte[] data) {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        
        try {
            ObjectInputStream in = new ObjectInputStream(bin);
            return in.readObject();
        } catch(IOException ex) {
            return null;
        } catch(ClassNotFoundException ex) {
            return null;
        }
    }

    
}
