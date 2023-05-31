/*
 * Firebird Open Source JDBC Driver
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

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.LockCloseable;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jdbc.FBConnectionProperties;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract class for properties and behaviour common to DataSources, XADataSources and ConnectionPoolDataSources
 *
 * @author Mark Rotteveel
 * @since 2.2
 */
public abstract class FBAbstractCommonDataSource extends AbstractConnectionPropertiesDataSource {

    protected static final String REF_DESCRIPTION = "description";
    protected static final String REF_PROPERTIES = "properties";

    private String description;
    private final Lock lock = new ReentrantLock();
    private final LockCloseable unlock = lock::unlock;
    private FBConnectionProperties connectionProperties = new FBConnectionProperties();

    protected final LockCloseable withLock() {
        lock.lock();
        return unlock;
    }

    /**
     * Method to check if this DataSource has not yet started.
     * <p>
     * Implementations should throw IllegalStateException when the DataSource is
     * already in use and modifying properties is not allowed.
     * </p>
     *
     * @throws IllegalStateException
     *         When the DataSource is already in use
     */
    protected abstract void checkNotStarted() throws IllegalStateException;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties.getTransactionParameters(isolation);
        }
    }

    @Override
    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            connectionProperties.setTransactionParameters(isolation, tpb);
        }
    }

    /**
     * Method that allows setting non-standard property in the form "key=value"
     * form. This method is needed by some containers to specify properties
     * in the configuration.
     *
     * @param propertyMapping
     *         mapping between property name (key) and its value. Name and value are separated with "=", ":" or
     *         whitespace character. Whitespace characters on the beginning of the string and between key and value are
     *         ignored. No escaping is possible: "\n" is backslash-en, not a new line mark.
     * @see #setProperty(String, String)
     */
    @Override
    public final void setNonStandardProperty(String propertyMapping) {
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            connectionProperties.setNonStandardProperty(propertyMapping);
        }
    }

    @Override
    public String getProperty(String name) {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties.getProperty(name);
        }
    }

    @Override
    public void setProperty(String name, String value) {
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            connectionProperties.setProperty(name, value);
        }
    }

    @Override
    public Integer getIntProperty(String name) {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties.getIntProperty(name);
        }
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            connectionProperties.setIntProperty(name, value);
        }
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties.getBooleanProperty(name);
        }
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            connectionProperties.setBooleanProperty(name, value);
        }
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties.connectionPropertyValues();
        }
    }

    protected final void setConnectionProperties(FBConnectionProperties connectionProperties) {
        if (connectionProperties == null) {
            throw new NullPointerException("null value not allowed for connectionProperties");
        }
        try (LockCloseable ignored = withLock()) {
            checkNotStarted();
            this.connectionProperties = connectionProperties;
        }
    }

    protected final FBConnectionProperties getConnectionProperties() {
        try (LockCloseable ignored = withLock()) {
            return connectionProperties;
        }
    }

    /**
     * Updates the supplied reference with RefAddr properties relevant to this class.
     *
     * @param ref
     *         Reference to update
     * @param instance
     *         Instance of this class to obtain values
     */
    protected static void updateReference(Reference ref, FBAbstractCommonDataSource instance) throws NamingException {
        try (LockCloseable ignored = instance.withLock()) {
            ref.add(new StringRefAddr(REF_DESCRIPTION, instance.getDescription()));
            byte[] data = DataSourceFactory.serialize(instance.connectionProperties);
            ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
        }
    }
}
