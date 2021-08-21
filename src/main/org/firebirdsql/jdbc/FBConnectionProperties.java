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
import org.firebirdsql.gds.ng.FbConnectionProperties;
import org.firebirdsql.gds.ng.IConnectionProperties;
import org.firebirdsql.jaybird.props.PropertyNames;
import org.firebirdsql.jaybird.props.def.ConnectionProperty;
import org.firebirdsql.jaybird.props.def.ConnectionPropertyType;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FBConnectionProperties implements FirebirdConnectionProperties, Serializable, Cloneable {

    private static final long serialVersionUID = 611228437520889118L;

    public static final String DATABASE_PROPERTY = "database";
    public static final String TYPE_PROPERTY = "type";
    // isolation name
    public static final String ISOLATION_PROPERTY = "isolation";
    // isolation name
    public static final String DEFAULT_ISOLATION_PROPERTY = "defaultIsolation";
    // isolation int value
    private static final String DEFAULT_TRANSACTION_ISOLATION_PROPERTY = "defaultTransactionIsolation";

    @Deprecated
    public static final String BLOB_BUFFER_SIZE_PROPERTY = PropertyNames.blobBufferSize;
    /**
     * @deprecated Use {@link PropertyNames#charSet}
     */
    @Deprecated
    public static final String LOCAL_ENCODING_PROPERTY = "localEncoding";
    @Deprecated
    public static final String ENCODING_PROPERTY = PropertyNames.encoding;
    @Deprecated
    public static final String ROLE_NAME_PROPERTY = PropertyNames.roleName;
    @Deprecated
    public static final String SQL_DIALECT_PROPERTY = PropertyNames.sqlDialect;
    @Deprecated
    public static final String USE_STREAM_BLOBS_PROPERTY = PropertyNames.useStreamBlobs;
    @Deprecated
    public static final String SOCKET_BUFFER_SIZE_PROPERTY = PropertyNames.socketBufferSize;
    @Deprecated
    public static final String TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY = PropertyNames.timestampUsesLocalTimezone;
    /**
     * @deprecated Use {@link PropertyNames#user}
     */
    @Deprecated
    public static final String USER_NAME_PROPERTY = "userName";
    @Deprecated
    public static final String PASSWORD_PROPERTY = PropertyNames.password;
    /**
     * @deprecated Use {@link PropertyNames#pageCacheSize}
     */
    @Deprecated
    public static final String BUFFERS_NUMBER_PROPERTY = "buffersNumber";
    /**
     * @deprecated Use {@link PropertyNames#defaultResultSetHoldable}
     */
    @Deprecated
    public static final String DEFAULT_HOLDABLE_RS_PROPERTY = "defaultHoldable";
    @Deprecated
    public static final String SO_TIMEOUT = PropertyNames.soTimeout;
    @Deprecated
    public static final String CONNECT_TIMEOUT = PropertyNames.connectTimeout;
    @Deprecated
    public static final String USE_FIREBIRD_AUTOCOMMIT = PropertyNames.useFirebirdAutocommit;
    @Deprecated
    public static final String WIRE_CRYPT_LEVEL = PropertyNames.wireCrypt;
    @Deprecated
    public static final String DB_CRYPT_CONFIG = PropertyNames.dbCryptConfig;
    @Deprecated
    public static final String AUTH_PLUGINS = PropertyNames.authPlugins;
    @Deprecated
    public static final String GENERATED_KEYS_ENABLED = PropertyNames.generatedKeysEnabled;
    @Deprecated
    public static final String DATA_TYPE_BIND = PropertyNames.dataTypeBind;
    @Deprecated
    public static final String SESSION_TIME_ZONE = PropertyNames.sessionTimeZone;
    @Deprecated
    public static final String IGNORE_PROCEDURE_TYPE = PropertyNames.ignoreProcedureType;
    @Deprecated
    public static final String WIRE_COMPRESSION = PropertyNames.wireCompression;

    private static final int DEFAULT_TRANSACTION_ISOLATION_VALUE = Connection.TRANSACTION_READ_COMMITTED;

    private IConnectionProperties properties = new FbConnectionProperties();
    private String type;
    private String database;

    private int defaultTransactionIsolation = DEFAULT_TRANSACTION_ISOLATION_VALUE;
    private Map<Integer, TransactionParameterBuffer> customMapping = new HashMap<>();
    private FBTpbMapper mapper;

    // TODO See if other properties can also be moved to FbConnectionProperties

    @Override
    public String getProperty(String name) {
        // TODO Can we integrate this in normal property handling?
        switch (name) {
        case DATABASE_PROPERTY:
            return getDatabase();
        case TYPE_PROPERTY:
            return getType();
        case ISOLATION_PROPERTY:
        case DEFAULT_ISOLATION_PROPERTY:
            return getDefaultIsolation();
        case DEFAULT_TRANSACTION_ISOLATION_PROPERTY:
            return String.valueOf(getDefaultTransactionIsolation());
        default:
            return properties.getProperty(name);
        }
    }

    @Override
    public void setProperty(String name, String value) {
        // TODO Can we integrate this in normal property handling?
        switch (name) {
        case DATABASE_PROPERTY:
            setDatabase(value);
            break;
        case TYPE_PROPERTY:
            setType(value);
            break;
        case ISOLATION_PROPERTY:
        case DEFAULT_ISOLATION_PROPERTY:
            setDefaultIsolation(value);
            break;
        case DEFAULT_TRANSACTION_ISOLATION_PROPERTY:
            setDefaultTransactionIsolation(
                    value != null
                            ? (Integer) ConnectionPropertyType.INT.toType(value)
                            : DEFAULT_TRANSACTION_ISOLATION_VALUE);
            break;
        default:
            properties.setProperty(name, value);
            break;
        }
    }

    @Override
    public Integer getIntProperty(String name) {
        // TODO Can we integrate this in normal property handling?
        if (DEFAULT_TRANSACTION_ISOLATION_PROPERTY.equals(name)) {
            return getDefaultTransactionIsolation();
        }
        return properties.getIntProperty(name);
    }

    @Override
    public void setIntProperty(String name, Integer value) {
        // TODO Can we integrate this in normal property handling?
        if (DEFAULT_TRANSACTION_ISOLATION_PROPERTY.equals(name)) {
            setDefaultTransactionIsolation(value != null ? value : DEFAULT_TRANSACTION_ISOLATION_VALUE);
        } else {
            properties.setIntProperty(name, value);
        }
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
        return Objects.hash(type, database);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof FBConnectionProperties)) {
            return false;
        }

        FBConnectionProperties that = (FBConnectionProperties) obj;

        boolean result = this.properties.equals(that.properties);
        result &= Objects.equals(this.type, that.type);
        result &= Objects.equals(this.database, that.database);
        result &= this.defaultTransactionIsolation == that.defaultTransactionIsolation;
        result &= this.customMapping.equals(that.customMapping);
        // If one or both are null we are identical (see also JDBC-249)
        result &= (this.mapper == null || that.mapper == null) || this.mapper.equals(that.mapper);

        return result;
    }

    public Object clone() {
        try {
            FBConnectionProperties clone = (FBConnectionProperties) super.clone();

            clone.properties = properties.asNewMutable();
            clone.customMapping = new HashMap<>(customMapping);
            clone.mapper = mapper != null ? (FBTpbMapper) mapper.clone() : null;

            return clone;
        } catch (CloneNotSupportedException ex) {
            throw new Error("Assertion failure: clone not supported"); // Can't happen
        }
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
                    + "Expected format: propertyName[=propertyValue], was: '" +propertyMapping + "'");
        }
    }

    @Override
    public void setTpbMapping(String tpbMapping) {
        if (mapper != null) {
            throw new IllegalStateException("Properties are already initialized.");
        }
        FirebirdConnectionProperties.super.setTpbMapping(tpbMapping);
    }

    public int getDefaultTransactionIsolation() {
        if (mapper != null) {
            return mapper.getDefaultTransactionIsolation();
        }
        return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        defaultTransactionIsolation = defaultIsolationLevel;
        if (mapper != null) {
            mapper.setDefaultTransactionIsolation(defaultIsolationLevel);
        }
    }

    public String getDefaultIsolation() {
        return FBTpbMapper.getTransactionIsolationName(getDefaultTransactionIsolation());
    }

    public void setDefaultIsolation(String isolation) {
        setDefaultTransactionIsolation(FBTpbMapper.getTransactionIsolationLevel(isolation));
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

        mapper.setDefaultTransactionIsolation(defaultTransactionIsolation);

        for (Map.Entry<Integer, TransactionParameterBuffer> entry : customMapping.entrySet()) {
            Integer isolation = entry.getKey();
            TransactionParameterBuffer tpb = entry.getValue();

            mapper.setMapping(isolation, tpb);
        }

        return mapper;
    }

    /**
     * @return A mutable view of these connection properties as an implementation of {@link IConnectionProperties}
     * @since 5
     */
    public IConnectionProperties asIConnectionProperties() {
        return properties;
    }

}
