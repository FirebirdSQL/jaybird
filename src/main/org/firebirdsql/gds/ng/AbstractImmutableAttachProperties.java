// SPDX-FileCopyrightText: Copyright 2013-2023 Mark Rotteveel
// SPDX-FileCopyrightText: Copyright 2015 Hajime Nakagami
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Abstract immutable implementation of {@link org.firebirdsql.gds.ng.IAttachProperties}.
 * <p>
 * NOTE: This class relies on the default implementation provided in
 * {@link org.firebirdsql.jaybird.props.AttachmentProperties}, so in itself, immutability is not guaranteed by this
 * class: subclasses need to be {@code final} and guard against mutation (that is, they do not override setters, unless
 * they call {@link #immutable()}(.
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
public abstract class AbstractImmutableAttachProperties<T extends IAttachProperties<T>> implements IAttachProperties<T> {

    private final Map<ConnectionProperty, Object> propValues;

    /**
     * Copy constructor for IAttachProperties.
     * <p>
     * All properties defined in {@link org.firebirdsql.gds.ng.IAttachProperties} are copied
     * from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    protected AbstractImmutableAttachProperties(IAttachProperties<T> src) {
        // Though the default implementation doesn't have null keys or values, there is no such requirement on the API
        //noinspection Java9CollectionFactory
        propValues = src instanceof AbstractImmutableAttachProperties
                ? ((AbstractImmutableAttachProperties<T>) src).propValues
                : unmodifiableMap(new HashMap<>(src.connectionPropertyValues()));
    }

    @Override
    public final String getProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asString(propValues.get(property));
    }

    @Override
    public final void setProperty(String name, String value) {
        immutable();
    }

    @Override
    public final Integer getIntProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asInteger(propValues.get(property));
    }

    @Override
    public final void setIntProperty(String name, Integer value) {
        immutable();
    }

    @Override
    public final Boolean getBooleanProperty(String name) {
        ConnectionProperty property = property(name);
        return property.type().asBoolean(propValues.get(property));
    }

    @Override
    public final void setBooleanProperty(String name, Boolean value) {
        immutable();
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
        return propValues;
    }

    @Override
    public final boolean isImmutable() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractImmutableAttachProperties)) return false;

        AbstractImmutableAttachProperties<?> that = (AbstractImmutableAttachProperties<?>) o;

        return propValues.equals(that.propValues);
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
