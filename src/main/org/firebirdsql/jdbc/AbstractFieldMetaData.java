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

import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;

import java.sql.SQLException;
import java.sql.Types;
import java.sql.Wrapper;
import java.util.Map;
import java.util.Objects;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Base class for {@link org.firebirdsql.jdbc.FBResultSetMetaData} and
 * {@link org.firebirdsql.jdbc.FBParameterMetaData} for methods common to both implementations.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:skidder@users.sourceforge.net">Nickolay Samofatov</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public abstract class AbstractFieldMetaData implements Wrapper {

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private final RowDescriptor rowDescriptor;
    private final FBConnection connection;
    private Map<FieldKey, ExtendedFieldInfo> extendedInfo;

    protected AbstractFieldMetaData(RowDescriptor rowDescriptor, FBConnection connection) {
        assert rowDescriptor != null : "rowDescriptor is required";
        this.rowDescriptor = rowDescriptor;
        this.connection = connection;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface))
            throw new SQLException("Unable to unwrap to class " + iface.getName());

        return iface.cast(this);
    }

    /**
     * @return The row descriptor.
     */
    protected final RowDescriptor getRowDescriptor() {
        return rowDescriptor;
    }

    /**
     * Retrieves the number of fields in the object for which this <code>AbstractFieldMetaData</code> object contains
     * information.
     *
     * @return the number of fields
     */
    protected final int getFieldCount() {
        return rowDescriptor.getCount();
    }

    /**
     * The {@link org.firebirdsql.gds.ng.fields.FieldDescriptor} of the field with index <code>fieldIndex</code>.
     *
     * @param fieldIndex
     *         1-based index of a field in this metadata object
     * @return field descriptor
     */
    protected final FieldDescriptor getFieldDescriptor(int fieldIndex) {
        return rowDescriptor.getFieldDescriptor(fieldIndex - 1);
    }

    /**
     * Retrieves whether values for the designated field can be signed numbers.
     *
     * @param field
     *         the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     */
    protected final boolean isSignedInternal(int field) {
        switch (getFieldDescriptor(field).getType() & ~1) {
        case ISCConstants.SQL_SHORT:
        case ISCConstants.SQL_LONG:
        case ISCConstants.SQL_FLOAT:
        case ISCConstants.SQL_DOUBLE:
        case ISCConstants.SQL_D_FLOAT:
        case ISCConstants.SQL_INT64:
        case ISCConstants.SQL_DEC16:
        case ISCConstants.SQL_DEC34:
        case ISCConstants.SQL_INT128:
            return true;
        default:
            return false;
        }
    }

    /**
     * Retrieves the designated field's number of digits to right of the decimal point.
     * 0 is returned for data types where the scale is not applicable.
     *
     * @param field
     *         the first field is 1, the second is 2, ...
     * @return scale
     */
    protected final int getScaleInternal(int field) {
        return getFieldDescriptor(field).getScale() * (-1);
    }

    protected final String getFieldClassName(int field) throws SQLException {
        switch (getFieldType(field)) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return STRING_CLASS_NAME;

        case Types.SMALLINT:
        case Types.INTEGER:
            return INTEGER_CLASS_NAME;

        case Types.FLOAT:
        case Types.DOUBLE:
            return DOUBLE_CLASS_NAME;

        case Types.TIMESTAMP:
            return TIMESTAMP_CLASS_NAME;

        case Types.BLOB:
            return BLOB_CLASS_NAME;

        case Types.CLOB:
            return CLOB_CLASS_NAME;

        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return BYTE_ARRAY_CLASS_NAME;

        case Types.ARRAY:
            return ARRAY_CLASS_NAME;

        case Types.BIGINT:
            return LONG_CLASS_NAME;

        case Types.TIME:
            return TIME_CLASS_NAME;

        case Types.DATE:
            return SQL_DATE_CLASS_NAME;

        case Types.TIME_WITH_TIMEZONE:
            return OFFSET_TIME_CLASS_NAME;

        case Types.TIMESTAMP_WITH_TIMEZONE:
            return OFFSET_DATE_TIME_CLASS_NAME;

        case Types.NUMERIC:
        case Types.DECIMAL:
        case JaybirdTypeCodes.DECFLOAT:
            return BIG_DECIMAL_CLASS_NAME;

        case Types.BOOLEAN:
            return BOOLEAN_CLASS_NAME;

        case Types.NULL:
        case Types.OTHER:
            return OBJECT_CLASS_NAME;

        case Types.ROWID:
            return ROW_ID_CLASS_NAME;

        default:
            throw new FBSQLException("Unknown SQL type.", SQLStateConstants.SQL_STATE_INVALID_PARAM_TYPE);
        }
    }

    protected final String getFieldTypeName(int field) {
        // Must return the same value as DatabaseMetaData getColumns Type_Name
        // TODO Reduce duplication with FBDatabaseMetaData
        int sqlType = getFieldDescriptor(field).getType() & ~1;
        int sqlScale = getFieldDescriptor(field).getScale();
        int sqlSubtype = getFieldDescriptor(field).getSubType();

        switch (sqlType) {
        case ISCConstants.SQL_SHORT:
            if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubtype == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                return "SMALLINT";
            }
        case ISCConstants.SQL_LONG:
            if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubtype == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                return "INTEGER";
            }
        case ISCConstants.SQL_INT64:
            if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubtype == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                return "BIGINT";
            }
        case ISCConstants.SQL_DOUBLE:
        case ISCConstants.SQL_D_FLOAT:
            if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubtype == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                return "DOUBLE PRECISION";
            }
        case ISCConstants.SQL_DEC16:
        case ISCConstants.SQL_DEC34:
            return "DECFLOAT";
        case ISCConstants.SQL_INT128:
            if (sqlSubtype == SUBTYPE_NUMERIC || (sqlSubtype == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubtype == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                return "INT128";
            }
        case ISCConstants.SQL_FLOAT:
            return "FLOAT";
        case ISCConstants.SQL_TEXT:
            return "CHAR";
        case ISCConstants.SQL_VARYING:
            return "VARCHAR";
        case ISCConstants.SQL_TIMESTAMP:
            return "TIMESTAMP";
        case ISCConstants.SQL_TYPE_TIME:
            return "TIME";
        case ISCConstants.SQL_TYPE_DATE:
            return "DATE";
        case ISCConstants.SQL_TIMESTAMP_TZ:
        case ISCConstants.SQL_TIMESTAMP_TZ_EX:
            return "TIMESTAMP WITH TIME ZONE";
        case ISCConstants.SQL_TIME_TZ:
        case ISCConstants.SQL_TIME_TZ_EX:
            return "TIME WITH TIME ZONE";
        case ISCConstants.SQL_BLOB:
            if (sqlSubtype < 0) {
                return "BLOB SUB_TYPE <0"; // TODO report actual subtype
            } else if (sqlSubtype == ISCConstants.BLOB_SUB_TYPE_BINARY) {
                return "BLOB SUB_TYPE 0";
            } else if (sqlSubtype == ISCConstants.BLOB_SUB_TYPE_TEXT) {
                return "BLOB SUB_TYPE 1";
            } else {
                return "BLOB SUB_TYPE " + sqlSubtype;
            }
        case ISCConstants.SQL_QUAD:
            return "ARRAY"; // TODO Inconsistent with getFieldType
        case ISCConstants.SQL_BOOLEAN:
            return "BOOLEAN";
        default:
            return "NULL";
        }
    }

    protected int getFieldType(int field) {
        return JdbcTypeConverter.toJdbcType(getFieldDescriptor(field));
    }

    /**
     * Retrieves the designated parameter's specified column size.
     * <p/>
     * <P>The returned value represents the maximum column size for the given parameter.
     * For numeric data, this is the maximum precision.  For character data, this is the length in characters.
     * For datetime datatypes, this is the length in characters of the String representation (assuming the
     * maximum allowed precision of the fractional seconds component). For binary data, this is the length in bytes.
     * For the ROWID datatype, this is the length in bytes. 0 is returned for data types where the column size is not
     * applicable.
     *
     * @param field
     *         the first field is 1, the second is 2, ...
     * @return precision
     * @throws SQLException
     *         if a database access error occurs
     */
    protected final int getPrecisionInternal(int field) throws SQLException {
        final int colType = getFieldType(field);

        switch (colType) {
        case Types.DECIMAL:
        case Types.NUMERIC: {
            final ExtendedFieldInfo fieldInfo = getExtFieldInfo(field);
            return fieldInfo == null || fieldInfo.fieldPrecision == 0
                    ? estimateFixedPrecision(field)
                    : fieldInfo.fieldPrecision;
        }

        case JaybirdTypeCodes.DECFLOAT: {
            final FieldDescriptor var = getFieldDescriptor(field);
            switch (var.getType() & ~1) {
            case ISCConstants.SQL_DEC16:
                return 16;
            case ISCConstants.SQL_DEC34:
                return 34;
            default:
                return 0;
            }
        }

        case Types.CHAR:
        case Types.VARCHAR: {
            final FieldDescriptor var = getFieldDescriptor(field);
            final EncodingDefinition encodingDefinition =
                    var.getEncodingFactory().getEncodingDefinitionByCharacterSetId(var.getSubType());
            final int charSetSize = encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
            return var.getLength() / charSetSize;
        }

        case Types.BINARY:
        case Types.VARBINARY: {
            final FieldDescriptor var = getFieldDescriptor(field);
            return var.getLength();
        }

        case Types.FLOAT: {
            if (supportInfoFor(connection).supportsFloatBinaryPrecision()) {
                return 24;
            } else {
                return 7;
            }
        }
        case Types.DOUBLE: {
            if (supportInfoFor(connection).supportsFloatBinaryPrecision()) {
                return 53;
            } else {
                return 15;
            }
        }
        case Types.INTEGER:
            return 10;
        case Types.BIGINT:
            return 19;
        case Types.SMALLINT:
            return 5;
        case Types.DATE:
            return 10;
        case Types.TIME:
            return 8;
        case Types.TIMESTAMP:
            return 19;
        case Types.TIMESTAMP_WITH_TIMEZONE:
            return 30;
        case Types.TIME_WITH_TIMEZONE:
            return 19;
        case Types.BOOLEAN:
            return 1;
        default:
            return 0;
        }
    }

    protected final int estimateFixedPrecision(int fieldIndex) {
        final int sqltype = getFieldDescriptor(fieldIndex).getType() & ~1;
        switch (sqltype) {
        case ISCConstants.SQL_SHORT:
            return 4;
        case ISCConstants.SQL_LONG:
            return 9;
        case ISCConstants.SQL_INT64:
            return 18;
        case ISCConstants.SQL_DOUBLE:
            return 18;
        case ISCConstants.SQL_DEC16:
            return 16;
        case ISCConstants.SQL_DEC34:
            return 34;
        case ISCConstants.SQL_INT128:
            return 38;
        default:
            return 0;
        }
    }

    protected final ExtendedFieldInfo getExtFieldInfo(int columnIndex) throws SQLException {
        if (extendedInfo == null) {
            extendedInfo = getExtendedFieldInfo(connection);
        }

        FieldKey key = new FieldKey(
                getFieldDescriptor(columnIndex).getOriginalTableName(),
                getFieldDescriptor(columnIndex).getOriginalName());

        return extendedInfo.get(key);
    }

    /**
     * This method retrieves extended information from the system tables in
     * a database. Since this method is expensive, use it with care.
     *
     * @return mapping between {@link FieldKey} instances and {@link ExtendedFieldInfo} instances,
     * or an empty Map if the metadata implementation does not support extended info.
     * @throws SQLException
     *         if a database error occurs while obtaining extended field information.
     */
    protected abstract Map<FieldKey, ExtendedFieldInfo> getExtendedFieldInfo(FBConnection connection) throws SQLException;

    /**
     * This class is an old-fashion data structure that stores additional
     * information about fields in a database.
     */
    protected static class ExtendedFieldInfo {
        final FieldKey fieldKey;
        final int fieldPrecision;

        public ExtendedFieldInfo(String relationName, String fieldName, int precision) {
            fieldKey = new FieldKey(relationName, fieldName);
            fieldPrecision = precision;
        }
    }

    /**
     * This class should be used as a composite key in an internal field
     * mapping structures.
     */
    protected static final class FieldKey {
        private final String relationName;
        private final String fieldName;

        /**
         * Create instance of this class for the specified relation and field
         * names.
         *
         * @param relationName
         *         relation name.
         * @param fieldName
         *         field name.
         */
        FieldKey(String relationName, String fieldName) {
            this.relationName = relationName;
            this.fieldName = fieldName;
        }

        /**
         * Check if <code>obj</code> is equal to this object.
         *
         * @param obj
         *         object to check.
         * @return <code>true</code> if <code>obj</code> is instance of this
         * class and has equal relation and field names.
         */
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof FieldKey)) return false;

            FieldKey that = (FieldKey) obj;

            return Objects.equals(relationName, that.relationName) && Objects.equals(fieldName, that.fieldName);
        }

        /**
         * Get hash code of this instance.
         *
         * @return combination of hash codes of <code>relationName</code> field
         * and <code>fieldName</code> field.
         */
        public int hashCode() {
            int result = 971;
            result = 23 * result + (relationName != null ? relationName.hashCode() : 0);
            result = 23 * result + (fieldName != null ? fieldName.hashCode() : 0);
            return result;
        }
    }
}
