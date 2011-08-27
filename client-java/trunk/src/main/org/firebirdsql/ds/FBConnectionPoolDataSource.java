/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import javax.resource.ResourceException;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBManagedConnectionFactory;
import org.firebirdsql.jdbc.FBConnectionProperties;
import org.firebirdsql.jdbc.FBDataSource;
import org.firebirdsql.jdbc.FBSQLException;

/**
 * Bare-bones implementation of {@link javax.sql.ConnectionPoolDataSource}.
 * <p>
 * Use this class instead of the broken implementation of
 * {@link org.firebirdsql.pool.FBConnectionPoolDataSource}.
 * </p>
 * <p>
 * Please be aware that this is not a connectionpool. This class provides
 * PooledConnection objects for connection pool implementations (eg as provided
 * by a JEE application server). If you need a standalone connectionpool,
 * consider using a connectionpool implementation like c3p0, BoneCP or BDCP.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public class FBConnectionPoolDataSource implements ConnectionPoolDataSource, Referenceable {
    
    // TODO Implement Referenceable

    private PrintWriter logWriter;
    private String description;
    private String serverName;
    private int portNumber;
    private String databaseName;
    private final Object lock = new Object();

    private volatile transient FBDataSource internalDs;

    private FBConnectionProperties connectionProperties = new FBConnectionProperties();

    public PooledConnection getPooledConnection() throws SQLException {
        return getPooledConnection(connectionProperties.getUserName(),
                connectionProperties.getPassword());
    }

    public PooledConnection getPooledConnection(String user, String password) throws SQLException {
        if (internalDs == null) {
            initialize();
        }
        return new FBPooledConnection(internalDs.getConnection(user, password));
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServerName() {
        synchronized (lock) {
            return serverName;
        }
    }

    public void setServerName(String serverName) {
        synchronized (lock) {
            checkNotStarted();
            this.serverName = serverName;
            setDatabase();
        }
    }

    public int getPortNumber() {
        synchronized (lock) {
            return portNumber;
        }
    }

    public void setPortNumber(int portNumber) {
        synchronized (lock) {
            checkNotStarted();
            this.portNumber = portNumber;
            setDatabase();
        }
    }

    public String getDatabaseName() {
        synchronized (lock) {
            return databaseName;
        }
    }

    /**
     * Sets the databaseName of this datasource.
     * <p>
     * The databaseName is the filepath or alias of the database only, so it
     * should not include serverName and portNumber.
     * </p>
     * 
     * @param databaseName
     *            Databasename (filepath or alias)
     */
    public void setDatabaseName(String databaseName) {
        synchronized (lock) {
            checkNotStarted();
            this.databaseName = databaseName;
            setDatabase();
        }
    }

    public String getType() {
        synchronized (lock) {
            return connectionProperties.getType();
        }
    }

    public void setType(String type) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setType(type);
        }
    }

    public String getUser() {
        return connectionProperties.getUserName();
    }

    public void setUser(String user) {
        connectionProperties.setUserName(user);
    }

    public String getPassword() {
        return connectionProperties.getPassword();
    }

    public String getRoleName() {
        return connectionProperties.getRoleName();
    }

    public void setRoleName(String roleName) {
        connectionProperties.setRoleName(roleName);
    }

    public void setPassword(String password) {
        connectionProperties.setPassword(password);
    }

    public String getCharSet() {
        return connectionProperties.getCharSet();
    }

    /**
     * @param charSet
     *            Character set for the connection. Similar to
     *            <code>encoding</code> property, but accepts Java names instead
     *            of Firebird ones.
     */
    public void setCharSet(String charSet) {
        synchronized(lock) {
            checkNotStarted();
            connectionProperties.setCharSet(charSet);
        }
    }

    public String getEncoding() {
        return connectionProperties.getEncoding();
    }

    /**
     * @param encoding
     *            Firebird name of the character encoding for the connection.
     *            See Firebird documentation for more information.
     */
    public void setEncoding(String encoding) {
        synchronized(lock) {
            checkNotStarted();
            connectionProperties.setEncoding(encoding);
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        logWriter = out;
        if (internalDs != null) {
            internalDs.setLogWriter(out);
        }
    }

    public int getLoginTimeout() throws SQLException {
        return connectionProperties.getSoTimeout() / 1000;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        connectionProperties.setSoTimeout(seconds * 1000);
    }

    private void initialize() throws SQLException {
        synchronized (lock) {
            if (internalDs != null) {
                return;
            }
            try {
                GDSType gdsType = GDSType.getType(getType());
                if (gdsType == null) {
                    gdsType = GDSFactory.getDefaultGDSType();
                }
                FBManagedConnectionFactory mcf = new FBManagedConnectionFactory(
                        gdsType, connectionProperties);
                internalDs = (FBDataSource) mcf.createConnectionFactory();
                internalDs.setLogWriter(logWriter);
            } catch (ResourceException e) {
                throw new FBSQLException(e);
            }
        }
    }

    private void checkNotStarted() {
        if (internalDs != null) {
            throw new IllegalStateException("DataSource already in use. Change of this property is not allowed");
        }
    }

    /**
     * Sets the database property of connectionProperties.
     */
    private void setDatabase() {
        // TODO: Not 100% sure if this works for all GDSTypes, may need to defer
        // to getDatabasePath of the relevant GDSFactoryPlugin
        StringBuffer sb = new StringBuffer();
        if (serverName != null && serverName.length() > 0) {
            sb.append("//").append(serverName);
            if (portNumber > 0) {
                sb.append(':').append(portNumber);
            }
            sb.append('/');
        }

        if (databaseName != null) {
            sb.append(databaseName);
        }

        if (sb.length() > 0) {
            connectionProperties.setDatabase(sb.toString());
        } else {
            connectionProperties.setDatabase(null);
        }
    }

    public Reference getReference() throws NamingException {
        Reference ref = new Reference(getClass().getName(), DataSourceFactory.class.getName(), null);
        
        ref.add(new StringRefAddr("description", getDescription()));
        ref.add(new StringRefAddr("serverName", getServerName()));
        if (getPortNumber() != 0) {
            ref.add(new StringRefAddr("portNumber", Integer.toString(getPortNumber())));
        }
        ref.add(new StringRefAddr("databaseName", getDatabaseName()));
        ref.add(new StringRefAddr("user", getUser()));
        ref.add(new StringRefAddr("password", getPassword()));
        ref.add(new StringRefAddr("charSet", getCharSet()));
        try {
            if (getLoginTimeout() != 0) {
                ref.add(new StringRefAddr("loginTimeout", Integer.toString(getLoginTimeout())));
            }
        } catch (SQLException ex) {
            NamingException ne = new NamingException();
            ne.setRootCause(ex);
            throw ne;
        }
        ref.add(new StringRefAddr("roleName", getRoleName()));
        ref.add(new StringRefAddr("type", getType()));

        return ref;
    }
}
