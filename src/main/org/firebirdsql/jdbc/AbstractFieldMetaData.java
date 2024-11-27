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
import org.firebirdsql.gds.JaybirdErrorCodes;
import org.firebirdsql.gds.ng.FbExceptionBuilder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.firebirdsql.util.InternalApi;

import java.sql.SQLException;
import java.sql.Types;
import java.sql.Wrapper;
import java.util.Map;

import static org.firebirdsql.jdbc.JavaTypeNameConstants.*;
import static org.firebirdsql.jdbc.SQLStateConstants.SQL_STATE_INVALID_PARAM_TYPE;
import static org.firebirdsql.util.FirebirdSupportInfo.supportInfoFor;

/**
 * Base class for {@link org.firebirdsql.jdbc.FBResultSetMetaData} and
 * {@link org.firebirdsql.jdbc.FBParameterMetaData} for methods common to both implementations.
 * <p>
 * This class is internal API of Jaybird. Future versions may radically change, move, or make inaccessible this type.
 * </p>
 *
 * @author David Jencks
 * @author Nickolay Samofatov
 * @author Mark Rotteveel
 * @since 3.0
 */
@InternalApi
public abstract class AbstractFieldMetaData implements Wrapper {

    private final RowDescriptor rowDescriptor;
    private final FBConnection connection;
    private Map<FieldKey, ExtendedFieldInfo> extendedInfo;

    protected AbstractFieldMetaData(RowDescriptor rowDescriptor, FBConnection connection) {
        assert rowDescriptor != null : "rowDescriptor is required";
        this.rowDescriptor = rowDescriptor;
        this.connection = connection;
    }

    @Override
    public final boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface != null && iface.isAssignableFrom(getClass());
    }

    @Override
    public final <T> T unwrap(Class<T> iface) throws SQLException {
        if (!isWrapperFor(iface)) {
            throw FbExceptionBuilder.forException(JaybirdErrorCodes.jb_unableToUnwrap)
                    .messageParameter(iface != null ? iface.getName() : "(null)")
                    .toSQLException();
        }
        return iface.cast(this);
    }

    /**
     * @return The row descriptor.
     */
    protected final RowDescriptor getRowDescriptor() {
        return rowDescriptor;
    }

    /**
     * Retrieves the number of fields in the object for which this {@code AbstractFieldMetaData} object contains
     * information.
     *
     * @return the number of fields
     */
    protected final int getFieldCount() {
        return rowDescriptor.getCount();
    }

    /**
     * The {@link FieldDescriptor} of the field with index {@code fieldIndex}.
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
     * @return {@code true} if so; {@code false} otherwise
     */
    protected final boolean isSignedInternal(int field) {
        return switch (getFieldDescriptor(field).getType() & ~1) {
            case ISCConstants.SQL_SHORT, ISCConstants.SQL_LONG, ISCConstants.SQL_FLOAT, ISCConstants.SQL_DOUBLE,
                    ISCConstants.SQL_D_FLOAT, ISCConstants.SQL_INT64, ISCConstants.SQL_DEC16, ISCConstants.SQL_DEC34,
                    ISCConstants.SQL_INT128 -> true;
            default -> false;
        };
    }

    /**
     * Retrieves the designated field's number of digits to right of the decimal point.
     * <p>
     * 0 is returned for data types where the scale is not applicable.
     * </p>
     *
     * @param field
     *         the first field is 1, the second is 2, ...
     * @return scale
     */
    protected final int getScaleInternal(int field) {
        return getFieldDescriptor(field).getScale() * (-1);
    }

    protected final String getFieldClassName(int field) throws SQLException {
        return switch (getFieldType(field)) {
            case Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> STRING_CLASS_NAME;
            case Types.SMALLINT, Types.INTEGER -> INTEGER_CLASS_NAME;
            case Types.FLOAT, Types.DOUBLE -> DOUBLE_CLASS_NAME;
            case Types.TIMESTAMP -> TIMESTAMP_CLASS_NAME;
            case Types.BLOB -> BLOB_CLASS_NAME;
            case Types.CLOB -> CLOB_CLASS_NAME;
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> BYTE_ARRAY_CLASS_NAME;
            case Types.ARRAY -> ARRAY_CLASS_NAME;
            case Types.BIGINT -> LONG_CLASS_NAME;
            case Types.TIME -> TIME_CLASS_NAME;
            case Types.DATE -> SQL_DATE_CLASS_NAME;
            case Types.TIME_WITH_TIMEZONE -> OFFSET_TIME_CLASS_NAME;
            case Types.TIMESTAMP_WITH_TIMEZONE -> OFFSET_DATE_TIME_CLASS_NAME;
            case Types.NUMERIC, Types.DECIMAL, JaybirdTypeCodes.DECFLOAT -> BIG_DECIMAL_CLASS_NAME;
            case Types.BOOLEAN -> BOOLEAN_CLASS_NAME;
            case Types.NULL, Types.OTHER -> OBJECT_CLASS_NAME;
            case Types.ROWID -> ROW_ID_CLASS_NAME;
            default -> throw new SQLException("Field %d has unknown JDBC SQL type: %s"
                    .formatted(field, getFieldType(field)), SQL_STATE_INVALID_PARAM_TYPE);
        };
    }

    protected final String getFieldTypeName(int field) {
        // Must return the same value as DatabaseMetaData getColumns Type_Name
        FieldDescriptor fieldDescriptor = getFieldDescriptor(field);
        int sqlType = fieldDescriptor.getType() & ~1;
        int sqlScale = fieldDescriptor.getScale();
        int sqlSubtype = fieldDescriptor.getSubType();
        int jdbcType = JdbcTypeConverter.fromFirebirdToJdbcType(sqlType, sqlSubtype, sqlScale);
        return JdbcTypeConverter.getTypeName(jdbcType, sqlType, sqlSubtype, sqlScale);
    }

    protected final int getFieldType(int field) {
        return JdbcTypeConverter.toJdbcType(getFieldDescriptor(field));
    }

    /**
     * Retrieves the designated parameter's specified column size.
     * <p>
     * The returned value represents the maximum column size for the given parameter. For numeric data, this is
     * the maximum precision.  For character data, this is the length in characters. For datetime datatypes, this is
     * the length in characters of the String representation (assuming the maximum allowed precision of the fractional
     * seconds component). For binary data, this is the length in bytes. For the ROWID datatype, this is the length in
     * bytes. 0 is returned for data types where the column size is not applicable.
     * </p>
     *
     * @param field
     *         the first field is 1, the second is 2, ...
     * @return precision
     * @throws SQLException
     *         if a database access error occurs
     */
    @SuppressWarnings("DuplicateBranchesInSwitch")
    protected final int getPrecisionInternal(int field) throws SQLException {
        return switch (getFieldType(field)) {
        case Types.DECIMAL, Types.NUMERIC -> {
            final ExtendedFieldInfo fieldInfo = getExtFieldInfo(field);
            yield fieldInfo == null || fieldInfo.fieldPrecision == 0
                    ? estimateFixedPrecision(field)
                    : fieldInfo.fieldPrecision;
        }
        case JaybirdTypeCodes.DECFLOAT -> switch (getFieldDescriptor(field).getType() & ~1) {
            case ISCConstants.SQL_DEC16 -> 16;
            case ISCConstants.SQL_DEC34 -> 34;
            default -> 0;
        };
        case Types.CHAR, Types.VARCHAR -> {
            final FieldDescriptor fieldDesc = getFieldDescriptor(field);
            final EncodingDefinition encodingDefinition =
                    fieldDesc.getEncodingFactory().getEncodingDefinitionByCharacterSetId(fieldDesc.getSubType());
            final int charSetSize = encodingDefinition != null ? encodingDefinition.getMaxBytesPerChar() : 1;
            yield fieldDesc.getLength() / charSetSize;
        }
        case Types.BINARY, Types.VARBINARY -> getFieldDescriptor(field).getLength();
        case Types.FLOAT -> {
            if (connection == null || supportInfoFor(connection).supportsFloatBinaryPrecision()) {
                yield 24;
            } else {
                yield 7;
            }
        }
        case Types.DOUBLE -> {
            if (connection == null || supportInfoFor(connection).supportsFloatBinaryPrecision()) {
                yield 53;
            } else {
                yield 15;
            }
        }
        case Types.INTEGER -> 10;
        case Types.BIGINT -> 19;
        case Types.SMALLINT -> 5;
        case Types.DATE -> 10;
        case Types.TIME -> 8;
        case Types.TIMESTAMP -> 19;
        case Types.TIMESTAMP_WITH_TIMEZONE -> 30;
        case Types.TIME_WITH_TIMEZONE -> 19;
        case Types.BOOLEAN -> 1;
        default -> 0;
        };
    }

    protected final int estimateFixedPrecision(int fieldIndex) {
        return switch (getFieldDescriptor(fieldIndex).getType() & ~1) {
            case ISCConstants.SQL_SHORT -> 4;
            case ISCConstants.SQL_LONG -> 9;
            case ISCConstants.SQL_INT64, ISCConstants.SQL_DOUBLE -> 18;
            case ISCConstants.SQL_DEC16 -> 16;
            case ISCConstants.SQL_DEC34 -> 34;
            case ISCConstants.SQL_INT128 -> 38;
            default -> 0;
        };
    }

    protected final ExtendedFieldInfo getExtFieldInfo(int columnIndex) throws SQLException {
        if (extendedInfo == null) {
            extendedInfo = getExtendedFieldInfo(connection);
        }

        return extendedInfo.get(new FieldKey(getFieldDescriptor(columnIndex)));
    }

    /**
     * This method retrieves extended information from the system tables in a database. Since this method is expensive,
     * use it with care.
     *
     * @return mapping between {@link FieldKey} instances and {@link ExtendedFieldInfo} instances, or an empty Map if
     * the metadata implementation does not support extended info.
     * @throws SQLException
     *         if a database error occurs while obtaining extended field information.
     */
    protected abstract Map<FieldKey, ExtendedFieldInfo> getExtendedFieldInfo(FBConnection connection) throws SQLException;

    /**
     * Stores additional information about fields in a database.
     */
    protected record ExtendedFieldInfo(FieldKey fieldKey, int fieldPrecision, boolean autoIncrement) {
        public ExtendedFieldInfo(String relationName, String fieldName, int precision, boolean autoIncrement) {
            this(new FieldKey(relationName, fieldName), precision, autoIncrement);
        }
    }

    /**
     * A composite key for internal field mapping structures.
     *
     * @param relationName
     *         relation name
     * @param fieldName
     *         field name
     */
    protected record FieldKey(String relationName, String fieldName) {
        public FieldKey(FieldDescriptor fieldDescriptor) {
            this(fieldDescriptor.getOriginalTableName(), fieldDescriptor.getOriginalName());
        }
    }
}
