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

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.CommonDataSource;

import org.firebirdsql.jdbc.FBConnectionProperties;

/**
 * Abstract class for properties and behaviour common to DataSources,
 * XADataSources and ConnectionPoolDataSources
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public abstract class FBAbstractCommonDataSource implements CommonDataSource {

    protected static final String REF_DATABASE_NAME = "databaseName";
    protected static final String REF_PORT_NUMBER = "portNumber";
    protected static final String REF_SERVER_NAME = "serverName";
    protected static final String REF_DESCRIPTION = "description";
    protected static final String REF_PROPERTIES = "properties";
    
    protected PrintWriter logWriter;
    protected String description;
    protected String serverName;
    protected int portNumber;
    protected String databaseName;
    protected final Object lock = new Object();
    protected FBConnectionProperties connectionProperties = new FBConnectionProperties();

    /**
     * Method to check if this DataSource has not yet started.
     * <p>
     * Implementations should throw IllegalStateException when the DataSource is
     * already in use and modifying properties would be ignored.
     * </p>
     * 
     * @throws IllegalStateException
     *             When the DataSource is already in use
     */
    protected abstract void checkNotStarted() throws IllegalStateException;

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
        synchronized (lock) {
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
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setEncoding(encoding);
        }
    }

    public PrintWriter getLogWriter() throws SQLException {
        return logWriter;
    }

    public void setLogWriter(PrintWriter out) throws SQLException {
        logWriter = out;
    }

    public int getLoginTimeout() throws SQLException {
        return connectionProperties.getSoTimeout() / 1000;
    }

    public void setLoginTimeout(int seconds) throws SQLException {
        connectionProperties.setSoTimeout(seconds * 1000);
    }
    
    /**
     * Method that allows setting non-standard property in the form "key=value"
     * form. This method is needed by some containers to specify properties
     * in the configuration.
     * 
     * @param propertyMapping mapping between property name (key) and its value.
     * Name and value are separated with "=", ":" or whitespace character. 
     * Whitespace characters on the beginning of the string and between key and
     * value are ignored. No escaping is possible: "\n" is backslash-en, not
     * a new line mark.
     */
    public void setNonStandardProperty(String propertyMapping) {
        checkNotStarted();
        connectionProperties.setNonStandardProperty(propertyMapping);
    }

    /**
     * Sets the database property of connectionProperties.
     */
    protected void setDatabase() {
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
    
    protected void setConnectionProperties(FBConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
    
    protected static void updateReference(Reference ref, FBAbstractCommonDataSource instance) throws NamingException {
        ref.add(new StringRefAddr(REF_DESCRIPTION, instance.getDescription()));
        ref.add(new StringRefAddr(REF_SERVER_NAME, instance.getServerName()));
        if (instance.getPortNumber() != 0) {
            ref.add(new StringRefAddr(REF_PORT_NUMBER, Integer.toString(instance.getPortNumber())));
        }
        ref.add(new StringRefAddr(REF_DATABASE_NAME, instance.getDatabaseName()));
        byte[] data = DataSourceFactory.serialize(instance.connectionProperties);
        ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
    }

}
