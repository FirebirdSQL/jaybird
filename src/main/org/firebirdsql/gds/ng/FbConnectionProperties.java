// SPDX-FileCopyrightText: Copyright 2013-2024 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.firebirdsql.gds.JaybirdSystemProperties.getDefaultAsyncFetch;
import static org.firebirdsql.gds.JaybirdSystemProperties.getDefaultReportSQLWarnings;

/**
 * Mutable implementation of {@link IConnectionProperties}
 *
 * @author Mark Rotteveel
 * @see FbImmutableConnectionProperties
 * @since 3.0
 */
public final class FbConnectionProperties extends AbstractAttachProperties<IConnectionProperties>
        implements IConnectionProperties, Serializable {

    @SuppressWarnings("java:S1948")
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
    }

    /**
     * Default constructor for FbConnectionProperties
     */
    public FbConnectionProperties() {
        setSessionTimeZone(defaultTimeZone());
        setSqlDialect(PropertyConstants.DEFAULT_DIALECT);
        try {
            setReportSQLWarnings(getDefaultReportSQLWarnings());
        } catch (IllegalArgumentException ignored) {
            // Incorrect value, ignore
        }
        Boolean asyncFetch = getDefaultAsyncFetch();
        if (asyncFetch != null) {
            setAsyncFetch(asyncFetch);
        }
    }

    // For internal use, to provide serialization support
    private FbConnectionProperties(HashMap<ConnectionProperty, Object> propValues) {
        super(propValues);
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
    protected Object resolveStoredDefaultValue(ConnectionProperty property) {
        return switch (property.name()) {
            case PropertyNames.sessionTimeZone -> defaultTimeZone();
            case PropertyNames.sqlDialect -> PropertyConstants.DEFAULT_DIALECT;
            case PropertyNames.asyncFetch -> getDefaultAsyncFetch();
            default -> super.resolveStoredDefaultValue(property);
        };
    }

    private static String defaultTimeZone() {
        return TimeZone.getDefault().getID();
    }

    @Override
    @SuppressWarnings("java:S1206")
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return super.equals(o);
    }

    @Override
    protected void dirtied() {
        immutableConnectionPropertiesCache = null;
    }

    @Serial
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    @Serial
    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private static class SerializationProxy implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private final Map<String, Serializable> propValues;

        private SerializationProxy(FbConnectionProperties fbConnectionProperties) {
            Map<ConnectionProperty, Object> srcProps = fbConnectionProperties.connectionPropertyValues();
            propValues = new HashMap<>(srcProps.size());
            srcProps.forEach((k, v) -> propValues.put(k.name(), (Serializable) v));
        }

        @Serial
        protected Object readResolve() {
            HashMap<ConnectionProperty, Object> targetProps = new HashMap<>(propValues.size());
            ConnectionPropertyRegistry propertyRegistry = ConnectionPropertyRegistry.getInstance();
            propValues.forEach((k, v) -> targetProps.put(propertyRegistry.getOrUnknown(k), v));

            return new FbConnectionProperties(targetProps);
        }

    }
}
