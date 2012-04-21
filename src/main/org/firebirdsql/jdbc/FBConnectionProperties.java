/*
 * $Id$
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.GDS;
import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.gds.impl.GDSFactory;
import org.firebirdsql.gds.impl.GDSType;
import org.firebirdsql.jca.FBResourceException;

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

    private HashMap properties = new HashMap();
    private String type;
    private String database;

    private String tpbMapping;
    private int defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
    private HashMap customMapping = new HashMap();
    private FBTpbMapper mapper;

    private int getIntProperty(String name) {
        Integer value = (Integer) properties.get(getCanonicalName(name));

        if (value == null)
            return 0;

        return value.intValue();
    }

    private String getCanonicalName(String propertyName) {
        return FBDriverPropertyManager.getCanonicalName(propertyName);
    }

    private String getStringProperty(String name) {
        Object value = properties.get(getCanonicalName(name));

        if (value != null) {
            return value.toString();
        }
        return null;
    }

    private boolean getBooleanProperty(String name) {
        return properties.containsKey(getCanonicalName(name));
    }

    private void setIntProperty(String name, int value) {
        properties.put(getCanonicalName(name), Integer.valueOf(value));
    }

    private void setStringProperty(String name, String value) {
        if (DATABASE_PROPERTY.equals(name))
            setDatabase(value);
        else if (TYPE_PROPERTY.equals(name))
            setType(value);

        name = getCanonicalName(name);
        Object objValue = FBConnectionHelper.parseDpbString(name, value);

        properties.put(name, objValue);
    }

    private void setBooleanProperty(String name) {
        properties.put(getCanonicalName(name), null);
    }

    private void removeProperty(String name) {
        properties.remove(name);
    }

    public int hashCode() {
        int result = 17;
        
        // Hash is built only from fields that are least likely to change (see JDBC-249)
        result = result * 151 + (type != null ? type.hashCode() : 0);
        result = result * 151 + (database != null ? database.hashCode() : 0);

        return result;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof FBConnectionProperties))
            return false;

        FBConnectionProperties that = (FBConnectionProperties) obj;

        boolean result = true;

        result &= this.properties.equals(that.properties);
        result &= this.type != null ? this.type.equals(that.type) : that.type == null;
        result &= this.database != null ? this.database.equals(that.database) : that.database == null;
        result &= this.tpbMapping != null ? this.tpbMapping.equals(that.tpbMapping) : that.tpbMapping == null;
        result &= this.defaultTransactionIsolation == that.defaultTransactionIsolation;
        result &= this.customMapping.equals(that.customMapping);
        // If one or both are null we are identical (see also JDBC-249)
        result &= (this.mapper == null || that.mapper == null) ? true : this.mapper.equals(that.mapper);

        return result;
    }

    public Object clone() {
        try {
            FBConnectionProperties clone = (FBConnectionProperties) super.clone();

            clone.properties = (HashMap) properties.clone();
            clone.customMapping = (HashMap) customMapping.clone();
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
        if (charSet == null)
            return;

        setStringProperty(LOCAL_ENCODING_PROPERTY, charSet);

        String encoding = getStringProperty(LOCAL_ENCODING_PROPERTY);
        if (encoding != null)
            return;

        encoding = EncodingFactory.getIscEncoding(charSet);

        if (encoding != null)
            setStringProperty(ENCODING_PROPERTY, encoding);
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
        if (roleName == null)
            return;

        setStringProperty(ROLE_NAME_PROPERTY, roleName);
    }

    public String getSqlDialect() {
        return getStringProperty(SQL_DIALECT_PROPERTY);
    }

    public void setSqlDialect(String sqlDialect) {
        if (sqlDialect == null)
            return;

        setStringProperty(SQL_DIALECT_PROPERTY, sqlDialect);
    }

    public String getUseTranslation() {
        return getStringProperty(USE_TRANSLATION_PROPERTY);
    }

    public void setUseTranslation(String translationPath) {
        if (translationPath == null)
            return;

        setStringProperty(USE_TRANSLATION_PROPERTY, translationPath);
    }

    public boolean isUseStreamBlobs() {
        return getBooleanProperty(USE_STREAM_BLOBS_PROPERTY);
    }

    public void setUseStreamBlobs(boolean useStreamBlobs) {
        if (useStreamBlobs)
            setBooleanProperty(USE_STREAM_BLOBS_PROPERTY);
        else
            removeProperty(USE_STREAM_BLOBS_PROPERTY);
    }

    public boolean isUseStandardUdf() {
        return getBooleanProperty(USE_STANDARD_UDF_PROPERTY);
    }

    public void setUseStandardUdf(boolean useStandardUdf) {
        if (useStandardUdf)
            setBooleanProperty(USE_STANDARD_UDF_PROPERTY);
        else
            removeProperty(USE_STANDARD_UDF_PROPERTY);
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
        if (timestampUsesLocalTimezone)
            setBooleanProperty(TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY);
        else
            removeProperty(TIMESTAMP_USES_LOCAL_TIMEZONE_PROPERTY);
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
        if (ISOLATION_PROPERTY.equals(key) || DEFAULT_ISOLATION_PROPERTY.equals(key))
            setDefaultIsolation(value);
        else
            setStringProperty(key, value);
    }

    public boolean isDefaultResultSetHoldable() {
        return getBooleanProperty(DEFAULT_HOLDABLE_RS_PROPERTY);
    }

    public void setDefaultResultSetHoldable(boolean isHoldable) {
        if (isHoldable)
            setBooleanProperty(DEFAULT_HOLDABLE_RS_PROPERTY);
        else
            removeProperty(DEFAULT_HOLDABLE_RS_PROPERTY);
    }

    public int getSoTimeout() {
        return getIntProperty(SO_TIMEOUT);
    }

    public void setSoTimeout(int soTimeout) {
        setIntProperty(SO_TIMEOUT, soTimeout);
    }

    public void setNonStandardProperty(String propertyMapping) {
        char[] chars = propertyMapping.toCharArray();
        StringBuffer key = new StringBuffer();
        StringBuffer value = new StringBuffer();

        boolean keyProcessed = false;
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];

            boolean isSeparator = Character.isWhitespace(ch) || ch == '=' || ch == ':';

            // if no key was processed, ignore white spaces
            if (key.length() == 0 && isSeparator)
                continue;

            if (!keyProcessed && !isSeparator) {
                key.append(ch);
            } else if (!keyProcessed && isSeparator) {
                keyProcessed = true;
            } else if (keyProcessed && value.length() == 0 && isSeparator) {
                continue;
            } else if (keyProcessed) {
                value.append(ch);
            }
        }

        String keyStr = key.toString().trim();
        String valueStr = value.length() > 0 ? value.toString().trim() : null;

        setNonStandardProperty(keyStr, valueStr);
    }

    public DatabaseParameterBuffer getDatabaseParameterBuffer() throws SQLException {
        GDS gds = getGds();
        DatabaseParameterBuffer dpb = gds.createDatabaseParameterBuffer();
        for (Iterator iter = properties.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();

            String propertyName = (String) entry.getKey();
            Object value = entry.getValue();

            Integer dpbType = FBConnectionHelper.getDpbKey(propertyName);

            if (dpbType == null)
                continue;

            if (value instanceof Boolean) {
                if (((Boolean) value).booleanValue())
                    dpb.addArgument(dpbType.intValue());
            } else if (value instanceof Byte) {
                dpb.addArgument(dpbType.intValue(), new byte[] { ((Byte) value).byteValue() });
            } else if (value instanceof Integer) {
                dpb.addArgument(dpbType.intValue(), ((Integer) value).intValue());
            } else if (value instanceof String) {
                dpb.addArgument(dpbType.intValue(), (String) value);
            } else if (value == null)
                dpb.addArgument(dpbType.intValue());
        }
        return dpb;
    }

    public String getTpbMapping() {
        return tpbMapping;
    }

    public void setTpbMapping(String tpbMapping) {
        if (mapper != null)
            throw new IllegalStateException("Properties are already initialized.");
        else
            this.tpbMapping = tpbMapping;
    }

    public int getDefaultTransactionIsolation() {
        if (mapper != null)
            return mapper.getDefaultTransactionIsolation();
        else
            return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(int defaultIsolationLevel) {
        defaultTransactionIsolation = defaultIsolationLevel;
        if (mapper != null)
            mapper.setDefaultTransactionIsolation(defaultIsolationLevel);
    }

    public String getDefaultIsolation() {
        return FBTpbMapper.getTransactionIsolationName(getDefaultTransactionIsolation());
    }

    public void setDefaultIsolation(String isolation) {
        setDefaultTransactionIsolation(FBTpbMapper.getTransactionIsolationLevel(isolation));
    }

    public TransactionParameterBuffer getTransactionParameters(int isolation) {
        if (mapper != null)
            return mapper.getMapping(isolation);
        else
            return (TransactionParameterBuffer) customMapping.get(Integer.valueOf(isolation));
    }

    public void setTransactionParameters(int isolation, TransactionParameterBuffer tpb) {
        customMapping.put(Integer.valueOf(isolation), tpb);
        if (mapper != null)
            mapper.setMapping(isolation, tpb);
    }

    public FBTpbMapper getMapper() throws FBResourceException {
        if (mapper != null)
            return mapper;

        GDS gds = getGds();
        if (tpbMapping == null)
            mapper = FBTpbMapper.getDefaultMapper(gds);
        else
            mapper = new FBTpbMapper(gds, tpbMapping, getClass().getClassLoader());

        mapper.setDefaultTransactionIsolation(defaultTransactionIsolation);

        for (Iterator iter = customMapping.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();

            Integer isolation = (Integer) entry.getKey();
            TransactionParameterBuffer tpb = (TransactionParameterBuffer) entry.getValue();

            mapper.setMapping(isolation.intValue(), tpb);
        }

        return mapper;
    }
    
    /**
     * Returns the GDS instance for value of field type.
     * <p>
     * Will return the default GDS if type is not set.
     * </p>
     * @return GDS instance
     * @throws IllegalArgumentException if the value of type does not match a loaded GDSType
     */
    private GDS getGds() {
        GDSType gdsType = GDSType.getType(type);
        if (gdsType == null && type != null)
            throw new IllegalArgumentException("Unknown GDS type " + type);
        return GDSFactory.getGDSForType(gdsType);
    }

}
