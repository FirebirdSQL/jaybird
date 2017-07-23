/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
 * can be obtained from a source control history command.
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
     *            Database name (filepath or alias)
     */
    public final void setDatabaseName(String databaseName) {
        synchronized (lock) {
            checkNotStarted();
            this.databaseName = databaseName;
            setDatabase();
        }
    }

    @Override
    public final String getType() {
        synchronized (lock) {
            return connectionProperties.getType();
        }
    }

    @Override
    public final void setType(String type) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setType(type);
        }
    }

    public String getUser() {
        synchronized (lock) {
            return connectionProperties.getUserName();
        }
    }

    public void setUser(String user) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setUserName(user);
        }
    }

    @Override
    public String getPassword() {
        synchronized (lock) {
            return connectionProperties.getPassword();
        }
    }

    @Override
    public void setPassword(String password) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setPassword(password);
        }
    }

    @Override
    public String getRoleName() {
        synchronized (lock) {
            return connectionProperties.getRoleName();
        }
    }

    @Override
    public void setRoleName(String roleName) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setRoleName(roleName);
        }
    }

    @Override
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
    @Override
    public final void setCharSet(String charSet) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setCharSet(charSet);
        }
    }

    @Override
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
    @Override
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
    @Override
    public int getLoginTimeout() throws SQLException {
        return getConnectTimeout();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This property is an alias for the connectTimeout property.
     * </p>
     */
    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        setConnectTimeout(seconds);
    }

    @Override
    public int getConnectTimeout() {
        synchronized (lock) {
            return connectionProperties.getConnectTimeout();
        }
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setConnectTimeout(connectTimeout);
        }
    }
    
    @Deprecated
    @Override
    public String getDatabase() {
        synchronized(lock) {
            return connectionProperties.getDatabase();
        }
    }

    @Deprecated
    @Override
    public void setDatabase(String database) {
        synchronized(lock) {
            checkNotStarted();
            connectionProperties.setDatabase(database);
        }
    }

    @Override
    public int getBlobBufferSize() {
        synchronized (lock) {
            return connectionProperties.getBlobBufferSize();
        }
    }

    @Override
    public void setBlobBufferSize(int bufferSize) {
        synchronized (lock) {
            connectionProperties.setBlobBufferSize(bufferSize);
        }
    }

    @Override
    public String getSqlDialect() {
        synchronized (lock) {
            return connectionProperties.getSqlDialect();
        }
    }

    @Override
    public void setSqlDialect(String sqlDialect) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setSqlDialect(sqlDialect);
        }
    }

    @Override
    public boolean isUseStreamBlobs() {
        synchronized (lock) {
            return connectionProperties.isUseStreamBlobs();
        }
    }

    @Override
    public void setUseStreamBlobs(boolean useStreamBlobs) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setUseStreamBlobs(useStreamBlobs);
        }
    }

    @Override
    public boolean isUseStandardUdf() {
        synchronized (lock) {
            return connectionProperties.isUseStandardUdf();
        }
    }

    @Override
    public void setUseStandardUdf(boolean useStandardUdf) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setUseStandardUdf(useStandardUdf);
        }
    }

    @Override
    public int getSocketBufferSize() {
        synchronized (lock) {
            return connectionProperties.getSocketBufferSize();
        }
    }

    @Override
    public void setSocketBufferSize(int socketBufferSize) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setSocketBufferSize(socketBufferSize);
        }
    }

    @Override
    public boolean isTimestampUsesLocalTimezone() {
        synchronized (lock) {
            return connectionProperties.isTimestampUsesLocalTimezone();
        }
    }

    @Override
    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setTimestampUsesLocalTimezone(timestampUsesLocalTimezone);
        }
    }

    @Deprecated
    @Override
    public String getUserName() {
        return getUser();
    }

    @Deprecated
    @Override
    public void setUserName(String userName) {
        setUser(userName);
    }

    @Override
    public int getBuffersNumber() {
        synchronized (lock) {
            return connectionProperties.getBuffersNumber();
        }
    }

    @Override
    public void setBuffersNumber(int buffersNumber) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setBuffersNumber(buffersNumber);
        }
    }

    @Override
    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        synchronized (lock) {
            return connectionProperties.getDatabaseParameterBuffer();
        }
    }

    @Override
    public String getTpbMapping() {
        synchronized (lock) {
            return connectionProperties.getTpbMapping();
        }
    }

    @Override
    public void setTpbMapping(String tpbMapping) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setTpbMapping(tpbMapping);
        }
    }

    @Override
    public int getDefaultTransactionIsolation() {
        synchronized (lock) {
            return connectionProperties.getDefaultTransactionIsolation();
        }
    }

    @Override
    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setDefaultTransactionIsolation(defaultIsolationLevel);
        }
    }

    @Override
    public String getDefaultIsolation() {
        synchronized (lock) {
            return connectionProperties.getDefaultIsolation();
        }
    }

    @Override
    public void setDefaultIsolation(String isolation) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setDefaultIsolation(isolation);
        }
    }

    @Override
    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        synchronized (lock) {
            return connectionProperties.getTransactionParameters(isolation);
        }
    }

    @Override
    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setTransactionParameters(isolation, tpb);
        }
    }

    @Override
    public boolean isDefaultResultSetHoldable() {
        synchronized (lock) {
            return connectionProperties.isDefaultResultSetHoldable();
        }
    }

    @Override
    public void setDefaultResultSetHoldable(boolean isHoldable) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setDefaultResultSetHoldable(isHoldable);
        }
    }

    @Override
    public int getSoTimeout() {
        synchronized (lock) {
            return connectionProperties.getSoTimeout();
        }
    }

    @Override
    public void setSoTimeout(int soTimeout) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setSoTimeout(soTimeout);
        }
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        synchronized (lock) {
            return connectionProperties.isUseFirebirdAutocommit();
        }
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setUseFirebirdAutocommit(useFirebirdAutocommit);
        }
    }

    @Override
    public String getWireCrypt() {
        synchronized (lock) {
            return connectionProperties.getWireCrypt();
        }
    }

    @Override
    public void setWireCrypt(String wireCrypt) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setWireCrypt(wireCrypt);
        }
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
    @Override
    public final void setNonStandardProperty(String propertyMapping) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setNonStandardProperty(propertyMapping);
        }
    }
    
    /**
     * Method to set properties which are not exposed through JavaBeans-style setters.
     * 
     * @param key Name of the property (see Jaybird releasenotes)
     * @param value Value of the property
     * @see #setNonStandardProperty(String)
     */
    @Override
    public final void setNonStandardProperty(String key, String value) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setNonStandardProperty(key, value);
        }
    }
    
    /**
     * Method to get the value of properties which are not exposed through JavaBeans-style setters.
     * 
     * @param key Name of the property (see Jaybird releasenotes)
     * @return Value of the property
     * @see #setNonStandardProperty(String)
     * @see #setNonStandardProperty(String, String)
     */
    @Override
    public final String getNonStandardProperty(String key) {
        synchronized (lock) {
            return connectionProperties.getNonStandardProperty(key);
        }
    }

    /**
     * Sets the database property of connectionProperties.
     */
    protected final void setDatabase() {
        synchronized (lock) {
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
    }
    
    protected final void setConnectionProperties(FBConnectionProperties connectionProperties) {
        synchronized (lock) {
            if (connectionProperties == null) {
                throw new NullPointerException("null value not allowed for connectionProperties");
            }
            this.connectionProperties = connectionProperties;
        }
    }
    
    protected final FBConnectionProperties getConnectionProperties() {
        synchronized (lock) {
            return connectionProperties;
        }
    }
    
    /**
     * Updates the supplied reference with RefAddr properties relevant to this class.
     * 
     * @param ref Reference to update
     * @param instance Instance of this class to obtain values
     */
    protected static void updateReference(Reference ref, FBAbstractCommonDataSource instance) {
        synchronized (instance.lock) {
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
}
