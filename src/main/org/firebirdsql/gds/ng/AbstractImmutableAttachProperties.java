// SPDX-FileCopyrightText: Copyright 2013-2026 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Abstract immutable implementation of {@link org.firebirdsql.gds.ng.IAttachProperties}.
 * <p>
 * NOTE: This class relies on the default implementation provided in
 * {@link org.firebirdsql.jaybird.props.AttachmentProperties}, so in itself, immutability is not guaranteed by this
 * class: subclasses need to be {@code final} and guard against mutation (that is, they do not override setters, unless
 * they call {@link #immutable()}).
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractImmutableAttachProperties<T extends IAttachProperties<T>>
        implements IAttachProperties<T> {

    private final Map<ConnectionProperty, Object> propValues;

    /**
     * Copy constructor for IAttachProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IAttachProperties} are copied from {@code src} to the new
     * instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    protected AbstractImmutableAttachProperties(IAttachProperties<T> src) {
        // TODO Revisit this after we have more robust null-marking in place
        // Though the default implementation doesn't have null keys or values, there is no such requirement on the API
        //noinspection Java9CollectionFactory
        propValues = src instanceof AbstractImmutableAttachProperties
                ? ((AbstractImmutableAttachProperties<T>) src).propValues
                : unmodifiableMap(new HashMap<>(src.connectionPropertyValues()));
    }

    @Override
    public final @Nullable String getProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asString(propValues.get(property));
    }

    @Override
    public final void setProperty(String name, @Nullable String value) {
        immutable();
    }

    @Override
    public final @Nullable Integer getIntProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asInteger(propValues.get(property));
    }

    @Override
    public final void setIntProperty(String name, @Nullable Integer value) {
        immutable();
    }

    @Override
    public final @Nullable Boolean getBooleanProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asBoolean(propValues.get(property));
    }

    @Override
    public final void setBooleanProperty(String name, @Nullable Boolean value) {
        immutable();
    }

    /**
     * Returns the property of the specified name.
     * <p>
     * When the property is not a known property, an unknown variant is returned.
     * </p>
     *
     * @param name
     *         property name (cannot be {@code null})
     * @return a connection property instance, never {@code null}
     */
    protected final ConnectionProperty property(String name) {
        return ConnectionPropertyRegistry.getInstance().getOrUnknown(name);
    }

    @Override
    public final Map<ConnectionProperty, Object> connectionPropertyValues() {
        return propValues;
    }

    @Override
    public final boolean isImmutable() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        return o instanceof AbstractImmutableAttachProperties<?> that && propValues.equals(that.propValues);
    }

    @Override
    public int hashCode() {
        return propValues.hashCode();
    }

    /**
     * Throws an UnsupportedOperationException
     */
    protected final void immutable() {
        throw new UnsupportedOperationException("this object is immutable");
    }
}
