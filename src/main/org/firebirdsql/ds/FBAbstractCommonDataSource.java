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

import java.sql.SQLException;

import javax.naming.BinaryRefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jdbc.FBConnectionProperties;
import org.firebirdsql.jdbc.FirebirdConnectionProperties;

/**
 * Abstract class for properties and behaviour common to DataSources,
 * XADataSources and ConnectionPoolDataSources
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public abstract class FBAbstractCommonDataSource extends RootCommonDataSource implements FirebirdConnectionProperties {

    protected static final String REF_DATABASE_NAME = "databaseName";
    protected static final String REF_PORT_NUMBER = "portNumber";
    protected static final String REF_SERVER_NAME = "serverName";
    protected static final String REF_DESCRIPTION = "description";
    protected static final String REF_PROPERTIES = "properties";
    
    private String description;
    private String serverName;
    private int portNumber;
    private String databaseName;
    protected final Object lock = new Object();
    private FBConnectionProperties connectionProperties = new FBConnectionProperties();

    /**
     * Method to check if this DataSource has not yet started.
     * <p>
     * Implementations should throw IllegalStateException when the DataSource is
     * already in use and modifying properties is not allowed.
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

    public final String getServerName() {
        synchronized (lock) {
            return serverName;
        }
    }

    public final void setServerName(String serverName) {
        synchronized (lock) {
            checkNotStarted();
            this.serverName = serverName;
            setDatabase();
        }
    }

    public final int getPortNumber() {
        synchronized (lock) {
            return portNumber;
        }
    }

    public final void setPortNumber(int portNumber) {
        synchronized (lock) {
            checkNotStarted();
            this.portNumber = portNumber;
            setDatabase();
        }
    }

    public final String getDatabaseName() {
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
    public final void setDatabaseName(String databaseName) {
        synchronized (lock) {
            checkNotStarted();
            this.databaseName = databaseName;
            setDatabase();
        }
    }

    public final String getType() {
        synchronized (lock) {
            return connectionProperties.getType();
        }
    }

    public final void setType(String type) {
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

    public final String getCharSet() {
        synchronized (lock) {
            return connectionProperties.getCharSet();
        }
    }

    /**
     * @param charSet
     *            Character set for the connection. Similar to
     *            <code>encoding</code> property, but accepts Java names instead
     *            of Firebird ones.
     */
    public final void setCharSet(String charSet) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setCharSet(charSet);
        }
    }

    public final String getEncoding() {
        synchronized (lock) {
            return connectionProperties.getEncoding();
        }
    }

    /**
     * @param encoding
     *            Firebird name of the character encoding for the connection.
     *            See Firebird documentation for more information.
     */
    public final void setEncoding(String encoding) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setEncoding(encoding);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    public int getLoginTimeout() throws SQLException {
    	return getConnectTimeout();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    public void setLoginTimeout(int seconds) throws SQLException {
    	setConnectTimeout(seconds);
    }
    
    public int getConnectTimeout() {
        return connectionProperties.getConnectTimeout();
    }
    
    public void setConnectTimeout(int connectTimeout) {
        connectionProperties.setConnectTimeout(connectTimeout);
    }
    
    @Deprecated
    public String getDatabase() {
        synchronized(lock) {
            return connectionProperties.getDatabase();
        }
    }

    @Deprecated
    public void setDatabase(String database) {
        synchronized(lock) {
            // TODO: Try to set databaseName, portNumber and serverName?
            connectionProperties.setDatabase(database);
        }
    }

    public int getBlobBufferSize() {
        return connectionProperties.getBlobBufferSize();
    }

    public void setBlobBufferSize(int bufferSize) {
        checkNotStarted();
        connectionProperties.setBlobBufferSize(bufferSize);
    }

    public String getSqlDialect() {
        return connectionProperties.getSqlDialect();
    }

    public void setSqlDialect(String sqlDialect) {
        checkNotStarted();
        connectionProperties.setSqlDialect(sqlDialect);
    }

    public String getUseTranslation() {
        synchronized(lock) {
            return connectionProperties.getUseTranslation();
        }
    }

    public void setUseTranslation(String translationPath) {
        synchronized(lock) {
            checkNotStarted();
            connectionProperties.setUseTranslation(translationPath);
        }
    }

    public boolean isUseStreamBlobs() {
        return connectionProperties.isUseStreamBlobs();
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        checkNotStarted();
        connectionProperties.setUseStreamBlobs(useStreamBlobs);
    }

    public boolean isUseStandardUdf() {
        return connectionProperties.isUseStandardUdf();
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        checkNotStarted();
        connectionProperties.setUseStandardUdf(useStandardUdf);
    }

    public int getSocketBufferSize() {
        return connectionProperties.getSocketBufferSize();
    }

    public void setSocketBufferSize(int socketBufferSize) {
        checkNotStarted();
        connectionProperties.setSocketBufferSize(socketBufferSize);
    }

    public boolean isTimestampUsesLocalTimezone() {
        return connectionProperties.isTimestampUsesLocalTimezone();
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        checkNotStarted();
        connectionProperties.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
    }

    @Deprecated
    public String getUserName() {
        return getUser();
    }

    @Deprecated
    public void setUserName(String userName) {
        setUser(userName);
    }

    public int getBuffersNumber() {
        return connectionProperties.getBuffersNumber();
    }

    public void setBuffersNumber(int buffersNumber) {
        checkNotStarted();
        connectionProperties.setBuffersNumber(buffersNumber);
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        return connectionProperties.getDatabaseParameterBuffer();
    }

    public String getTpbMapping() {
        return connectionProperties.getTpbMapping();
    }

    public void setTpbMapping(String tpbMapping) {
        checkNotStarted();
        connectionProperties.setTpbMapping(tpbMapping);
    }

    public int getDefaultTransactionIsolation() {
        return connectionProperties.getDefaultTransactionIsolation();
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        checkNotStarted();
        connectionProperties.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public String getDefaultIsolation() {
        return connectionProperties.getDefaultIsolation();
    }

    public void setDefaultIsolation(String isolation) {
        checkNotStarted();
        connectionProperties.setDefaultIsolation(isolation);
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        return connectionProperties.getTransactionParameters(isolation);
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        checkNotStarted();
        connectionProperties.setTransactionParameters(isolation, tpb);
    }

    public boolean isDefaultResultSetHoldable() {
        return connectionProperties.isDefaultResultSetHoldable();
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        checkNotStarted();
        connectionProperties.setDefaultResultSetHoldable(isHoldable);
    }

    public int getSoTimeout() {
        return connectionProperties.getSoTimeout();
    }

    public void setSoTimeout(int soTimeout) {
        checkNotStarted();
        connectionProperties.setSoTimeout(soTimeout);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return connectionProperties.isUseFirebirdAutocommit();
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        checkNotStarted();
        connectionProperties.setUseFirebirdAutocommit(useFirebirdAutocommit);
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
     * @see #setNonStandardProperty(String, String)
     */
    public final void setNonStandardProperty(String propertyMapping) {
        checkNotStarted();
        connectionProperties.setNonStandardProperty(propertyMapping);
    }
    
    /**
     * Method to set properties which are not exposed through JavaBeans-style setters.
     * 
     * @param key Name of the property (see Jaybird releasenotes)
     * @param value Value of the property
     * @see #setNonStandardProperty(String)
     */
    public final void setNonStandardProperty(String key, String value) {
        checkNotStarted();
        connectionProperties.setNonStandardProperty(key, value);
    }
    
    /**
     * Method to get the value of properties which are not exposed through JavaBeans-style setters.
     * 
     * @param key Name of the property (see Jaybird releasenotes)
     * @return Value of the property
     * @see #setNonStandardProperty(String)
     * @see #setNonStandardProperty(String, String)
     */
    public final String getNonStandardProperty(String key) {
        return connectionProperties.getNonStandardProperty(key);
    }

    /**
     * Sets the database property of connectionProperties.
     */
    protected final void setDatabase() {
        // TODO: Not 100% sure if this works for all GDSTypes, may need to defer
        // to getDatabasePath of the relevant GDSFactoryPlugin
        StringBuilder sb = new StringBuilder();
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
    
    protected final void setConnectionProperties(FBConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }
    
    protected final FBConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }
    
    /**
     * Updates the supplied reference with RefAddr properties relevant to this class.
     * 
     * @param ref Reference to update
     * @param instance Instance of this class to obtain values
     */
    protected static void updateReference(Reference ref, FBAbstractCommonDataSource instance) {
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
