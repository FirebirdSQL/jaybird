// SPDX-FileCopyrightText: Copyright 2005-2010 Roman Rokytskyy
// SPDX-FileCopyrightText: Copyright 2005 Steven Jardine
// SPDX-FileCopyrightText: Copyright 2011-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.AbstractAttachProperties;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class FBConnectionProperties implements FirebirdConnectionProperties, Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 611228437520889118L;

    private FbConnectionProperties properties;

    private Map<Integer, TransactionParameterBuffer> customMapping = new HashMap<>();
    private @Nullable FBTpbMapper mapper;

    public FBConnectionProperties() {
        properties = new FbConnectionProperties();
        properties.registerPropertyUpdateListener(createPropertyUpdateListener());
    }

    @Override
    public @Nullable String getProperty(String name) {
        return properties.getProperty(name);
    }

    @Override
    public void setProperty(String name, @Nullable String value) {
        properties.setProperty(name, value);
    }

    @Override
    public @Nullable Integer getIntProperty(String name) {
        return properties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, @Nullable Integer value) {
        properties.setIntProperty(name, value);
    }

    @Override
    public @Nullable Boolean getBooleanProperty(String name) {
        return properties.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, @Nullable Boolean value) {
        properties.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return properties.connectionPropertyValues();
    }

    public int hashCode() {
        return Objects.hash(getType(), getServerName(), getPortNumber(), getDatabaseName());
    }

    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof FBConnectionProperties that)) {
            return false;
        }

        return this.properties.equals(that.properties)
                && this.customMapping.equals(that.customMapping)
                // If one or both are null we are identical (see also JDBC-249)
                && (this.mapper == null || that.mapper == null || this.mapper.equals(that.mapper));
    }

    public Object clone() {
        try {
            FBConnectionProperties clone = (FBConnectionProperties) super.clone();

            clone.properties = (FbConnectionProperties) properties.asNewMutable();
            clone.properties.registerPropertyUpdateListener(clone.createPropertyUpdateListener());
            clone.customMapping = new HashMap<>(customMapping);
            clone.mapper = mapper != null ? FBTpbMapper.copyOf(mapper) : null;

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Assertion failure: clone not supported"); // Can't happen
        }
    }

    @Override
    public void setNonStandardProperty(String propertyMapping) {
        String key;
        String value;
        int equalsIndex = propertyMapping.indexOf('=');
        if (equalsIndex == -1) {
            key = propertyMapping.trim();
            value = "";
        } else {
            key = propertyMapping.substring(0, equalsIndex).trim();
            value = propertyMapping.substring(equalsIndex + 1).trim();
        }
        if (!key.isEmpty()) {
            setProperty(key, value);
        } else {
            throw new IllegalArgumentException("Invalid non-standard property. "
                    + "Expected format: propertyName[=propertyValue], was: '" + propertyMapping + "'");
        }
    }

    @Override
    public @Nullable TransactionParameterBuffer getTransactionParameters(int isolation) {
        if (mapper != null) {
            return mapper.getMapping(isolation);
        }
        return customMapping.get(isolation);
    }

    @Override
    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        customMapping.put(isolation, tpb);
        if (mapper != null) {
            mapper.setMapping(isolation, tpb);
        }
    }

    public FBTpbMapper getMapper() throws SQLException {
        if (mapper != null) {
            return mapper;
        }

        String tpbMapping = getTpbMapping();
        if (tpbMapping == null) {
            mapper = FBTpbMapper.getDefaultMapper();
        } else {
            mapper = new FBTpbMapper(tpbMapping, getClass().getClassLoader());
        }

        mapper.setDefaultTransactionIsolation(getDefaultTransactionIsolation());

        customMapping.forEach(mapper::setMapping);

        return mapper;
    }

    /**
     * @return A mutable view of these connection properties as an implementation of {@link IConnectionProperties}
     * @since 5
     */
    public IConnectionProperties asIConnectionProperties() {
        return properties;
    }

    private AbstractAttachProperties.PropertyUpdateListener createPropertyUpdateListener() {
        return new AbstractAttachProperties.PropertyUpdateListener() {
            @Override
            public void beforeUpdate(ConnectionProperty connectionProperty, @Nullable Object newValue) {
                if (PropertyNames.tpbMapping.equals(connectionProperty.name()) && mapper != null) {
                    throw new IllegalStateException("Cannot update tpbMapping, properties are already initialized.");
                }
            }

            @Override
            public void afterUpdate(ConnectionProperty connectionProperty, @Nullable Object newValue) {
                if (PropertyNames.defaultIsolation.equals(connectionProperty.name()) && mapper != null) {
                    mapper.setDefaultTransactionIsolation(getDefaultTransactionIsolation());
                }
            }
        };
    }

}
