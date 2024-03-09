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

import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.firebirdsql.util.InternalApi;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.unmodifiableMap;

/**
 * Abstract mutable implementation of {@link IAttachProperties}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractAttachProperties<T extends IAttachProperties<T>> implements IAttachProperties<T> {

    private static final PropertyUpdateListener NULL_LISTENER = new PropertyUpdateListener() {
        @Override
        public void beforeUpdate(ConnectionProperty connectionProperty, Object newValue) {
            // do nothing
        }

        @Override
        public void afterUpdate(ConnectionProperty connectionProperty, Object newValue) {
            // do nothing
        }
    };

    private static final Pattern GMT_WITH_OFFSET = Pattern.compile("^GMT([+-]\\d{2}:\\d{2})$");

    private final Map<ConnectionProperty, Object> propertyValues;
    private PropertyUpdateListener propertyUpdateListener = NULL_LISTENER;

    /**
     * Copy constructor for IAttachProperties.
     * <p>
     * All properties defined in {@link IAttachProperties} are copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    protected AbstractAttachProperties(IAttachProperties<T> src) {
        this();
        if (src != null) {
            propertyValues.putAll(src.connectionPropertyValues());
        }
    }

    /**
     * Default constructor for AbstractAttachProperties
     */
    protected AbstractAttachProperties() {
        propertyValues = new HashMap<>();
    }

    // For internal use, to provide serialization support in FbConnectionProperties
    AbstractAttachProperties(HashMap<ConnectionProperty, Object> propertyValues) {
        this.propertyValues = propertyValues;
    }

    @Override
    public final void setProperty(String name, String value) {
        ConnectionProperty property = property(name);
        setProperty(property, property.type().toType(value));
    }

    @Override
    public final String getProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asString(propertyValues.get(property));
    }

    @Override
    public final void setIntProperty(String name, Integer value) {
        ConnectionProperty property = property(name);
        setProperty(property, property.type().toType(value));
    }

    @Override
    public final Integer getIntProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asInteger(propertyValues.get(property));
    }

    @Override
    public final void setBooleanProperty(String name, Boolean value) {
        ConnectionProperty property = property(name);
        setProperty(property, property.type().toType(value));
    }

    @Override
    public final Boolean getBooleanProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asBoolean(propertyValues.get(property));
    }

    private void setProperty(ConnectionProperty property, Object value) {
        if (value == null) {
            value = resolveStoredDefaultValue(property);
        }
        // TODO Maybe this should be pushed down into property#validate(String)?
        if (PropertyNames.sessionTimeZone.equals(property.name())) {
            value = normalizeTimezone(String.valueOf(value));
        }
        // Exceptions thrown from the listener will prevent update from property
        propertyUpdateListener.beforeUpdate(property, value);
        if (value != null) {
            propertyValues.put(property, property.validate(value));
        } else {
            propertyValues.remove(property);
        }
        dirtied();
        try {
            propertyUpdateListener.afterUpdate(property, value);
        } catch (Exception e) {
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING,
                    "Ignored exception calling propertyUpdateListener.afterUpdate", e);
        }
    }

    /**
     * Normalizes timezone name, specifically converts Java's {@code GMT[+-]HH:MM} to Firebird's {@code [+-]HH:MM}.
     *
     * @param timezone
     *         timezone name
     * @return original or modified timezone name
     */
    private static String normalizeTimezone(String timezone) {
        if (timezone == null) return null;
        Matcher matcher = GMT_WITH_OFFSET.matcher(timezone);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return timezone;
    }

    /**
     * Resolve the default value for the specified connection property.
     * <p>
     * This method is only used for properties that must have a stored default value to function correctly.
     * </p>
     *
     * @param property Connection property
     * @return Default value to apply (usually {@code null})
     */
    protected Object resolveStoredDefaultValue(ConnectionProperty property) {
        return null;
    }

    /**
     * Returns the property of the specified name.
     * <p>
     * When the property is not a known property, an unknown variant is returned.
     * </p>
     *
     * @param name
     *         Property name
     * @return A connection property instance, never {@code null}
     */
    protected final ConnectionProperty property(String name) {
        return ConnectionPropertyRegistry.getInstance().getOrUnknown(name);
    }

    @Override
    public final Map<ConnectionProperty, Object> connectionPropertyValues() {
        return unmodifiableMap(propertyValues);
    }

    @Override
    public final boolean isImmutable() {
        return false;
    }

    /**
     * Registers an update listener that is notified when a connection property is modified.
     * <p>
     * This method is only for internal use within Jaybird.
     * </p>
     *
     * @param listener Listener to register or {@code null} to unregister
     * @throws IllegalStateException When a property update listener was already registered
     */
    @InternalApi
    public void registerPropertyUpdateListener(PropertyUpdateListener listener) {
        if (listener == null) {
            propertyUpdateListener = NULL_LISTENER;
        } else if (propertyUpdateListener == NULL_LISTENER) {
            propertyUpdateListener = listener;
        } else {
            throw new IllegalStateException("A listener is already registered");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractAttachProperties<?> that)) return false;

        return propertyValues.equals(that.propertyValues);
    }

    @Override
    public int hashCode() {
        return propertyValues.hashCode();
    }

    /**
     * Called by setters if they have been called.
     */
    protected abstract void dirtied();

    /**
     * Property update listener. This interface is only for internal use within Jaybird.
     */
    @InternalApi
    public interface PropertyUpdateListener {

        void beforeUpdate(ConnectionProperty connectionProperty, Object newValue);

        void afterUpdate(ConnectionProperty connectionProperty, Object newValue);

    }
}
