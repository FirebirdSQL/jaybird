/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.ParameterBufferHelper;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.jca.FBResourceException;
import org.firebirdsql.util.ObjectUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class FBConnectionProperties implements FirebirdConnectionProperties, Serializable, Cloneable {

    private static final long serialVersionUID = 611228437520889118L;

    public static final String DATABASE_PROPERTY = "database";
    public static final String TYPE_PROPERTY = "type";
    public static final String ISOLATION_PROPERTY = "isolation";
    public static final String DEFAULT_ISOLATION_PROPERTY = "defaultIsolation";

    public static final String BLOB_BUFFER_SIZE_PROPERTY = "blobBufferSize";
    public static final String LOCAL_ENCODING_PROPERTY = "localEncoding";
    public static final String ENCODING_PROPERTY = "encoding";
    public static final String ROLE_NAME_PROPERTY = "roleName";
    public static final String SQL_DIALECT_PROPERTY = "sqlDialect";
    public static final String USE_TRANSLATION_PROPERTY = "useTranslation";
    public static final String USE_STREAM_BLOBS_PROPERTY = "useStreamBlobs";
    public static final String USE_STANDARD_UDF_PROPERTY = "useStandardUdf";
    public static final String SOCKET_BUFFER_SIZE_PROPERTY = "socketBufferSize";
    public static final String TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY = "timestampUsesLocalTimezone";
    public static final String USER_NAME_PROPERTY = "userName";
    public static final String PASSWORD_PROPERTY = "password";
    public static final String BUFFERS_NUMBER_PROPERTY = "buffersNumber";
    public static final String DEFAULT_HOLDABLE_RS_PROPERTY = "defaultHoldable";
    public static final String SO_TIMEOUT = "soTimeout";
    public static final String CONNECT_TIMEOUT = "connectTimeout";
    public static final String USE_FIREBIRD_AUTOCOMMIT = "useFirebirdAutocommit";

    private Map<String, Object> properties = new HashMap<>();
    private String type;
    private String database;

    private String tpbMapping;
    private int defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
    private Map<Integer, TransactionParameterBuffer> customMapping = new HashMap<>();
    private FBTpbMapper mapper;

    private int getIntProperty(String name) {
        Integer value = (Integer) properties.get(getCanonicalName(name));
        return value != null ? value : 0;
    }

    private String getCanonicalName(String propertyName) {
        return FBDriverPropertyManager.getCanonicalName(propertyName);
    }

    private String getStringProperty(String name) {
        Object value = properties.get(getCanonicalName(name));
        return value != null ? value.toString() : null;
    }

    private boolean getBooleanProperty(String name) {
        String canonicalName = getCanonicalName(name);
        return properties.containsKey(canonicalName) && (Boolean) properties.get(canonicalName);
    }

    private void setIntProperty(String name, int value) {
        properties.put(getCanonicalName(name), value);
    }

    private void setStringProperty(String name, String value) {
        if (DATABASE_PROPERTY.equals(name)) {
            setDatabase(value);
        } else if (TYPE_PROPERTY.equals(name)) {
            setType(value);
        }

        name = getCanonicalName(name);
        Object objValue = ParameterBufferHelper.parseDpbString(name, value);

        properties.put(name, objValue);
    }

    private void setBooleanProperty(String name, boolean value) {
        if (value) {
            properties.put(getCanonicalName(name), Boolean.TRUE);
        } else {
            properties.remove(name);
        }
    }

    public int hashCode() {
        return ObjectUtils.hash(type, database);
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
        result &= ObjectUtils.equals(this.type, that.type);
        result &= ObjectUtils.equals(this.database, that.database);
        result &= ObjectUtils.equals(this.tpbMapping, that.tpbMapping);
        result &= this.defaultTransactionIsolation == that.defaultTransactionIsolation;
        result &= this.customMapping.equals(that.customMapping);
        // If one or both are null we are identical (see also JDBC-249)
        result &= (this.mapper == null || that.mapper == null) || this.mapper.equals(that.mapper);

        return result;
    }

    public Object clone() {
        try {
            FBConnectionProperties clone = (FBConnectionProperties) super.clone();

            clone.properties = new HashMap<>(properties);
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

    public int getBlobBufferSize() {
        return getIntProperty(BLOB_BUFFER_SIZE_PROPERTY);
    }

    public void setBlobBufferSize(int bufferSize) {
        setIntProperty(BLOB_BUFFER_SIZE_PROPERTY, bufferSize);
    }

    public String getCharSet() {
        return getStringProperty(LOCAL_ENCODING_PROPERTY);
    }

    public void setCharSet(String charSet) {
        if (charSet == null) {
            return;
        }

        // Normalize the name of the encoding
        String normalizedCharSet = EncodingFactory.getJavaEncodingForAlias(charSet);
        if (normalizedCharSet != null) {
            setStringProperty(LOCAL_ENCODING_PROPERTY, normalizedCharSet);
        }

        String encoding = getStringProperty(ENCODING_PROPERTY);
        if (encoding != null) {
            return;
        }

        encoding = EncodingFactory.getIscEncoding(charSet);

        if (encoding != null) {
            setStringProperty(ENCODING_PROPERTY, encoding);
        }
    }

    public String getEncoding() {
        return getStringProperty(ENCODING_PROPERTY);
    }

    public void setEncoding(String encoding) {
        if (encoding == null)
            return;

        setStringProperty(ENCODING_PROPERTY, encoding);

        String charSet = getStringProperty(LOCAL_ENCODING_PROPERTY);
        if (charSet != null)
            return;

        charSet = EncodingFactory.getJavaEncoding(encoding);

        if (charSet != null)
            setStringProperty(LOCAL_ENCODING_PROPERTY, charSet);
    }

    public String getRoleName() {
        return getStringProperty(ROLE_NAME_PROPERTY);
    }

    public void setRoleName(String roleName) {
        if (roleName != null) {
            setStringProperty(ROLE_NAME_PROPERTY, roleName);
        }
    }

    public String getSqlDialect() {
        return getStringProperty(SQL_DIALECT_PROPERTY);
    }

    public void setSqlDialect(String sqlDialect) {
        if (sqlDialect != null) {
            setStringProperty(SQL_DIALECT_PROPERTY, sqlDialect);
        }
    }

    public String getUseTranslation() {
        return getStringProperty(USE_TRANSLATION_PROPERTY);
    }

    public void setUseTranslation(String translationPath) {
        if (translationPath != null) {
            setStringProperty(USE_TRANSLATION_PROPERTY, translationPath);
        }
    }

    public boolean isUseStreamBlobs() {
        return getBooleanProperty(USE_STREAM_BLOBS_PROPERTY);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        setBooleanProperty(USE_STREAM_BLOBS_PROPERTY, useStreamBlobs);
    }

    public boolean isUseStandardUdf() {
        return getBooleanProperty(USE_STANDARD_UDF_PROPERTY);
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        setBooleanProperty(USE_STANDARD_UDF_PROPERTY, useStandardUdf);
    }

    public int getSocketBufferSize() {
        return getIntProperty(SOCKET_BUFFER_SIZE_PROPERTY);
    }

    public void setSocketBufferSize(int socketBufferSize) {
        setIntProperty(SOCKET_BUFFER_SIZE_PROPERTY, socketBufferSize);
    }

    public boolean isTimestampUsesLocalTimezone() {
        return getBooleanProperty(TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY);
    }

    public void setTimestampUsesLocalTimezone(boolean timestampUsesLocalTimezone) {
        setBooleanProperty(TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY, timestampUsesLocalTimezone);
    }

    public String getUserName() {
        return getStringProperty(USER_NAME_PROPERTY);
    }

    public void setUserName(String userName) {
        setStringProperty(USER_NAME_PROPERTY, userName);
    }

    public String getPassword() {
        return getStringProperty(PASSWORD_PROPERTY);
    }

    public void setPassword(String password) {
        setStringProperty(PASSWORD_PROPERTY, password);
    }

    public int getBuffersNumber() {
        return getIntProperty(BUFFERS_NUMBER_PROPERTY);
    }

    public void setBuffersNumber(int buffersNumber) {
        setIntProperty(BUFFERS_NUMBER_PROPERTY, buffersNumber);
    }

    public String getNonStandardProperty(String key) {
        return getStringProperty(key);
    }

    public void setNonStandardProperty(String key, String value) {
        if (ISOLATION_PROPERTY.equals(key) || DEFAULT_ISOLATION_PROPERTY.equals(key)) {
            setDefaultIsolation(value);
        } else {
            setStringProperty(key, value);
        }
    }

    public boolean isDefaultResultSetHoldable() {
        return getBooleanProperty(DEFAULT_HOLDABLE_RS_PROPERTY);
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        setBooleanProperty(DEFAULT_HOLDABLE_RS_PROPERTY, isHoldable);
    }

    public int getSoTimeout() {
        return getIntProperty(SO_TIMEOUT);
    }

    public void setSoTimeout(int soTimeout) {
        setIntProperty(SO_TIMEOUT, soTimeout);
    }

    @Override
    public int getConnectTimeout() {
        return getIntProperty(CONNECT_TIMEOUT);
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        setIntProperty(CONNECT_TIMEOUT, connectTimeout);
    }

    @Override
    public boolean isUseFirebirdAutocommit() {
        return getBooleanProperty(USE_FIREBIRD_AUTOCOMMIT);
    }

    @Override
    public void setUseFirebirdAutocommit(boolean useFirebirdAutocommit) {
        setBooleanProperty(USE_FIREBIRD_AUTOCOMMIT, useFirebirdAutocommit);
    }

    public void setNonStandardProperty(String propertyMapping) {
        char[] chars = propertyMapping.toCharArray();
        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        boolean keyProcessed = false;
        for (char ch : chars) {
            boolean isSeparator = Character.isWhitespace(ch) || ch == '=' || ch == ':';

            // if no key was processed, ignore white spaces
            if (key.length() == 0 && isSeparator)
                continue;

            if (!keyProcessed && !isSeparator) {
                key.append(ch);
            } else if (!keyProcessed) {
                keyProcessed = true;
            } else if (value.length() != 0 || !isSeparator) {
                value.append(ch);
            }
        }

        String keyStr = key.toString().trim();
        String valueStr = value.length() > 0 ? value.toString().trim() : null;

        setNonStandardProperty(keyStr, valueStr);
    }

    /**
     * @deprecated TODO Usage of this method should be removed or revised as current use of default encoding is not correct.
     */
    @Deprecated
    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        // TODO Instance creation should be done through FbDatabase or database factory?
        DatabaseParameterBuffer dpb = new DatabaseParameterBufferImp(
                DatabaseParameterBufferImp.DpbMetaData.DPB_VERSION_1,
                EncodingFactory.getDefaultInstance().getDefaultEncoding());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            Object value = entry.getValue();

            Integer dpbType = ParameterBufferHelper.getDpbKey(propertyName);

            if (dpbType == null)
                continue;

            if (value instanceof Boolean) {
                if ((Boolean) value)
                    dpb.addArgument(dpbType);
            } else if (value instanceof Byte) {
                dpb.addArgument(dpbType, new byte[] { (Byte) value });
            } else if (value instanceof Integer) {
                dpb.addArgument(dpbType, (Integer) value);
            } else if (value instanceof String) {
                dpb.addArgument(dpbType, (String) value);
            } else if (value == null)
                dpb.addArgument(dpbType);
        }
        return dpb;
    }

    public String getTpbMapping() {
        return tpbMapping;
    }

    public void setTpbMapping(String tpbMapping) {
        if (mapper != null) {
            throw new IllegalStateException("Properties are already initialized.");
        }
        this.tpbMapping = tpbMapping;
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

    public FBTpbMapper getMapper() throws FBResourceException {
        if (mapper != null) {
            return mapper;
        }

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
}
