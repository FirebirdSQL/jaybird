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
package org.firebirdsql.jdbc;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.ng.AbstractAttachProperties;
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;

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
    private FBTpbMapper mapper;

    public FBConnectionProperties() {
        properties = new FbConnectionProperties();
        properties.registerPropertyUpdateListener(createPropertyUpdateListener());
    }

    @Override
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    @Override
    public void setProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    @Override
    public Integer getIntProperty(String name) {
        return properties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        properties.setIntProperty(name, value);
    }

    @Override
    public Boolean getBooleanProperty(String name) {
        return properties.getBooleanProperty(name);
    }

    @Override
    public void setBooleanProperty(String name, Boolean value) {
        properties.setBooleanProperty(name, value);
    }

    @Override
    public Map<ConnectionProperty, Object> connectionPropertyValues() {
        return properties.connectionPropertyValues();
    }

    public int hashCode() {
        return Objects.hash(getType(), getServerName(), getPortNumber(), getDatabaseName());
    }

    public boolean equals(Object obj) {
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
            clone.mapper = mapper != null ? (FBTpbMapper) mapper.clone() : null;

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new Error("Assertion failure: clone not supported"); // Can't happen
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
        if (key.length() > 0) {
            setProperty(key, value);
        } else {
            throw new IllegalArgumentException("Invalid non-standard property. "
                    + "Expected format: propertyName[=propertyValue], was: '" + propertyMapping + "'");
        }
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        if (mapper != null) {
            return mapper.getMapping(isolation);
        }
        return customMapping.get(isolation);
    }

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
            public void beforeUpdate(ConnectionProperty connectionProperty, Object newValue) {
                if (PropertyNames.tpbMapping.equals(connectionProperty.name()) && mapper != null) {
                    throw new IllegalStateException("Cannot update tpbMapping, properties are already initialized.");
                }
            }

            @Override
            public void afterUpdate(ConnectionProperty connectionProperty, Object newValue) {
                if (PropertyNames.defaultIsolation.equals(connectionProperty.name()) && mapper != null) {
                    mapper.setDefaultTransactionIsolation(getDefaultTransactionIsolation());
                }
            }
        };
    }

}
