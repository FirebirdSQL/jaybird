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
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Mutable implementation of {@link IConnectionProperties}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbImmutableConnectionProperties
 * @since 3.0
 */
public final class FbConnectionProperties extends AbstractAttachProperties<IConnectionProperties>
        implements IConnectionProperties, Serializable {

    private String databaseName;

    private FbImmutableConnectionProperties immutableConnectionPropertiesCache;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link IConnectionProperties} are
     * copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbConnectionProperties(IConnectionProperties src) {
        super(src);
        if (src != null) {
            databaseName = src.getDatabaseName();
        }
    }

    /**
     * Default constructor for FbConnectionProperties
     */
    public FbConnectionProperties() {
        setSessionTimeZone(TimeZone.getDefault().getID());
        setSqlDialect(PropertyConstants.DEFAULT_DIALECT);
    }

    // For internal use, to provide serialization support
    private FbConnectionProperties(String serverName, int portNumber, String databaseName,
            HashMap<ConnectionProperty, Object> propValues) {
        super(serverName, portNumber, propValues);
        this.databaseName = databaseName;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        dirtied();
    }

    @Override
    public String getAttachObjectName() {
        return getDatabaseName();
    }

    @Override
    public void setSessionTimeZone(String sessionTimeZone) {
        setProperty(PropertyNames.sessionTimeZone,
                sessionTimeZone != null ? sessionTimeZone : TimeZone.getDefault().getID());
    }

    @Override
    public IConnectionProperties asImmutable() {
        if (immutableConnectionPropertiesCache == null) {
            immutableConnectionPropertiesCache = new FbImmutableConnectionProperties(this);
        }
        return immutableConnectionPropertiesCache;
    }

    @Override
    public IConnectionProperties asNewMutable() {
        return new FbConnectionProperties(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FbConnectionProperties that = (FbConnectionProperties) o;

        return Objects.equals(databaseName, that.databaseName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        return result;
    }

    @Override
    protected void dirtied() {
        immutableConnectionPropertiesCache = null;
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String serverName;
        private final int portNumber;
        private final String databaseName;
        private final Map<String, Serializable> propValues;

        private SerializationProxy(FbConnectionProperties fbConnectionProperties) {
            serverName = fbConnectionProperties.getServerName();
            portNumber = fbConnectionProperties.getPortNumber();
            databaseName = fbConnectionProperties.databaseName;
            Map<ConnectionProperty, Object> srcProps = fbConnectionProperties.connectionPropertyValues();
            propValues = new HashMap<>(srcProps.size());
            srcProps.forEach((k, v) -> propValues.put(k.name(), (Serializable) v));
        }

        protected Object readResolve() {
            HashMap<ConnectionProperty, Object> targetProps = new HashMap<>(propValues.size());
            ConnectionPropertyRegistry propertyRegistry = ConnectionPropertyRegistry.getInstance();
            propValues.forEach((k, v) -> targetProps.put(propertyRegistry.getOrUnknown(k), v));

            return new FbConnectionProperties(serverName, portNumber, databaseName, targetProps);
        }

    }
}
