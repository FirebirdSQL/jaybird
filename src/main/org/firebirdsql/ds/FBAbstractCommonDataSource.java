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
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jdbc.FBConnectionProperties;

import javax.naming.BinaryRefAddr;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.util.Map;

/**
 * Abstract class for properties and behaviour common to DataSources, XADataSources and ConnectionPoolDataSources
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public abstract class FBAbstractCommonDataSource extends AbstractConnectionPropertiesDataSource {

    protected static final String REF_DESCRIPTION = "description";
    protected static final String REF_PROPERTIES = "properties";

    private String description;
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
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setNonStandardProperty(propertyMapping);
        }
    }

    @Override
    public String getProperty(String name) {
        synchronized (lock) {
            return connectionProperties.getProperty(name);
        }
    }

    @Override
    public void setProperty(String name, String value) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setProperty(name, value);
        }
    }

    @Override
    public Integer getIntProperty(String name) {
        synchronized (lock) {
            return connectionProperties.getIntProperty(name);
        }
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setIntProperty(name, value);
        }
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        synchronized (lock) {
            return connectionProperties.getBooleanProperty(name);
        }
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        synchronized (lock) {
            checkNotStarted();
            connectionProperties.setBooleanProperty(name, value);
        }
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        synchronized (lock) {
            return connectionProperties.connectionPropertyValues();
        }
    }

    protected final void setConnectionProperties(FBConnectionProperties connectionProperties) {
        if (connectionProperties == null) {
            throw new NullPointerException("null value not allowed for connectionProperties");
        }
        synchronized (lock) {
            checkNotStarted();
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
     * @param ref
     *         Reference to update
     * @param instance
     *         Instance of this class to obtain values
     */
    protected static void updateReference(Reference ref, FBAbstractCommonDataSource instance) throws NamingException {
        synchronized (instance.lock) {
            ref.add(new StringRefAddr(REF_DESCRIPTION, instance.getDescription()));
            byte[] data = DataSourceFactory.serialize(instance.connectionProperties);
            ref.add(new BinaryRefAddr(REF_PROPERTIES, data));
        }
    }
}
