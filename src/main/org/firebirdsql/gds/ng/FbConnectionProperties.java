// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.PropertyConstants;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.firebirdsql.jaybird.util.CollectionUtils;
import org.jspecify.annotations.Nullable;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.firebirdsql.gds.JaybirdSystemProperties.getDefaultAsyncFetch;
import static org.firebirdsql.gds.JaybirdSystemProperties.getDefaultMaxBlobCacheSize;
import static org.firebirdsql.gds.JaybirdSystemProperties.getDefaultMaxInlineBlobSize;
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
    private @Nullable FbImmutableConnectionProperties immutableConnectionPropertiesCache;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link IConnectionProperties} are copied from {@code src} to the new instance.
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
        Integer maxInlineBlobSize = getDefaultMaxInlineBlobSize();
        if (maxInlineBlobSize != null) {
            setMaxInlineBlobSize(maxInlineBlobSize);
        }
        Integer maxBlobCacheSize = getDefaultMaxBlobCacheSize();
        if (maxBlobCacheSize != null) {
            setMaxBlobCacheSize(maxBlobCacheSize);
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
    protected @Nullable Object resolveStoredDefaultValue(ConnectionProperty property) {
        return switch (property.name()) {
            case PropertyNames.sessionTimeZone -> defaultTimeZone();
            case PropertyNames.sqlDialect -> PropertyConstants.DEFAULT_DIALECT;
            case PropertyNames.asyncFetch -> getDefaultAsyncFetch();
            case PropertyNames.maxInlineBlobSize -> negativeToZero(getDefaultMaxInlineBlobSize());
            case PropertyNames.maxBlobCacheSize -> negativeToZero(getDefaultMaxBlobCacheSize());
            default -> super.resolveStoredDefaultValue(property);
        };
    }

    private static @Nullable Integer negativeToZero(@Nullable Integer value) {
        if (value != null && value < 0) return 0;
        return value;
    }

    private static String defaultTimeZone() {
        return TimeZone.getDefault().getID();
    }

    @Override
    @SuppressWarnings("java:S1206")
    public boolean equals(@Nullable Object o) {
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
            propValues = new HashMap<>(CollectionUtils.mapCapacity(srcProps.size()));
            srcProps.forEach((k, v) -> propValues.put(k.name(), (Serializable) v));
        }

        @Serial
        protected Object readResolve() {
            HashMap<ConnectionProperty, Object> targetProps;
            // In theory, propValues might be null due to serialization
            if (propValues != null) {
                targetProps = new HashMap<>(CollectionUtils.mapCapacity(propValues.size()));
                ConnectionPropertyRegistry propertyRegistry = ConnectionPropertyRegistry.getInstance();
                propValues.forEach((k, v) -> targetProps.put(propertyRegistry.getOrUnknown(k), v));
            } else {
                targetProps = new HashMap<>();
            }

            return new FbConnectionProperties(targetProps);
        }

    }
}
