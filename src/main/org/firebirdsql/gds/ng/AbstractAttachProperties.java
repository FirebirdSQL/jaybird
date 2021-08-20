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

import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.internal.ConnectionPropertyRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

/**
 * Abstract mutable implementation of {@link IAttachProperties}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractAttachProperties<T extends IAttachProperties<T>> implements IAttachProperties<T> {

    private String serverName = IAttachProperties.DEFAULT_SERVER_NAME;
    private int portNumber = IAttachProperties.DEFAULT_PORT;
    private final Map<ConnectionProperty, Object> propertyValues;

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
            serverName = src.getServerName();
            portNumber = src.getPortNumber();
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
    AbstractAttachProperties(String serverName, int portNumber, HashMap<ConnectionProperty, Object> propertyValues) {
        this.serverName = serverName;
        this.portNumber = portNumber;
        this.propertyValues = propertyValues;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public void setServerName(String serverName) {
        this.serverName = serverName;
        dirtied();
    }

    @Override
    public int getPortNumber() {
        return portNumber;
    }

    @Override
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
        dirtied();
    }

    @Override
    public void setWireCrypt(WireCrypt wireCrypt) {
        setWireCrypt(requireNonNull(wireCrypt, "wireCrypt").name());
    }

    @Override
    public WireCrypt getWireCryptAsEnum() {
        return WireCrypt.fromString(getWireCrypt());
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
        if (value != null) {
            propertyValues.put(property, property.validate(value));
        } else {
            propertyValues.remove(property);
        }
        dirtied();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractAttachProperties)) return false;

        AbstractAttachProperties<?> that = (AbstractAttachProperties<?>) o;

        return portNumber == that.portNumber
                && Objects.equals(serverName, that.serverName)
                && propertyValues.equals(that.propertyValues);
    }

    @Override
    public int hashCode() {
        int result = serverName != null ? serverName.hashCode() : 0;
        result = 31 * result + portNumber;
        result = 31 * result + propertyValues.hashCode();
        return result;
    }

    /**
     * Called by setters if they have been called.
     */
    protected abstract void dirtied();
}
