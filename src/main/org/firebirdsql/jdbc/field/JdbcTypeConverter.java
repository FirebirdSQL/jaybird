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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.firebirdsql.util.InternalApi;

import java.sql.Types;

import static org.firebirdsql.gds.ISCConstants.BLOB_SUB_TYPE_BINARY;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;

/**
 * Helper class to convert from Firebird and metadata type information to JDBC type information.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
@InternalApi
public final class JdbcTypeConverter {

    private JdbcTypeConverter() {
        // No instances
    }

    /**
     * Gets the JDBC type value from {@link java.sql.Types} for the field descriptor.
     *
     * @param fieldDescriptor
     *         Field descriptor
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int toJdbcType(final FieldDescriptor fieldDescriptor) {
        if (fieldDescriptor.isDbKey()) {
            return Types.ROWID;
        }
        return fromFirebirdToJdbcType(fieldDescriptor.getType(), fieldDescriptor.getSubType(),
                fieldDescriptor.getScale());
    }

    /**
     * Determines if a field descriptor matches a JDBC type value from {@link java.sql.Types}.
     *
     * @param fieldDescriptor
     *         Field descritpor
     * @param jdbcType
     *         JDBC type
     * @return {@code true} if the field descriptor and JDBC type are equivalent (using {@link #toJdbcType(FieldDescriptor)})
     */
    public static boolean isJdbcType(final FieldDescriptor fieldDescriptor, final int jdbcType) {
        return toJdbcType(fieldDescriptor) == jdbcType;
    }

    /**
     * Converts from the Firebird type, subtype and scale to the JDBC type value from {@link java.sql.Types}.
     * <p>
     * This method is not capable of identifying {@link java.sql.Types#ROWID}; this will be identified
     * as {@link java.sql.Types#BINARY} instead.
     * </p>
     *
     * @param firebirdType
     *         Firebird type value (from {@link ISCConstants} {@code SQL_*} with or without nullable bit set
     * @param subtype
     *         Subtype
     * @param scale
     *         Scale
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int fromFirebirdToJdbcType(int firebirdType, int subtype, int scale) {
        firebirdType = firebirdType & ~1;

        return switch (firebirdType) {
            case ISCConstants.SQL_SHORT, ISCConstants.SQL_LONG, ISCConstants.SQL_INT64, ISCConstants.SQL_DOUBLE,
                    ISCConstants.SQL_D_FLOAT, ISCConstants.SQL_INT128 -> {
                if (subtype == SUBTYPE_DECIMAL) {
                    yield Types.DECIMAL;
                } else if (subtype == SUBTYPE_NUMERIC || scale < 0) {
                    yield Types.NUMERIC;
                }
                yield switch (firebirdType) {
                    case ISCConstants.SQL_SHORT -> Types.SMALLINT;
                    case ISCConstants.SQL_LONG -> Types.INTEGER;
                    case ISCConstants.SQL_INT64 -> Types.BIGINT;
                    case ISCConstants.SQL_DOUBLE, ISCConstants.SQL_D_FLOAT -> Types.DOUBLE;
                    // We map INT128 to JDBC type NUMERIC to simplify usage
                    case ISCConstants.SQL_INT128 -> Types.NUMERIC;
                    // Already covered by parent case, this is to satisfy the compiler
                    default -> throw new IllegalStateException("Unexpected value: " + firebirdType);
                };
            }
            case ISCConstants.SQL_FLOAT -> Types.FLOAT;
            case ISCConstants.SQL_DEC16, ISCConstants.SQL_DEC34 -> JaybirdTypeCodes.DECFLOAT;
            case ISCConstants.SQL_TEXT -> subtype == ISCConstants.CS_BINARY ? Types.BINARY : Types.CHAR;
            case ISCConstants.SQL_VARYING -> subtype == ISCConstants.CS_BINARY ? Types.VARBINARY : Types.VARCHAR;
            case ISCConstants.SQL_TIMESTAMP -> Types.TIMESTAMP;
            case ISCConstants.SQL_TYPE_TIME -> Types.TIME;
            case ISCConstants.SQL_TYPE_DATE -> Types.DATE;
            case ISCConstants.SQL_TIMESTAMP_TZ, ISCConstants.SQL_TIMESTAMP_TZ_EX -> Types.TIMESTAMP_WITH_TIMEZONE;
            case ISCConstants.SQL_TIME_TZ, ISCConstants.SQL_TIME_TZ_EX -> Types.TIME_WITH_TIMEZONE;
            case ISCConstants.SQL_BLOB -> {
                if (subtype < 0) {
                    yield Types.BLOB;
                } else if (subtype == ISCConstants.BLOB_SUB_TYPE_TEXT) {
                    yield Types.LONGVARCHAR;
                }
                yield Types.LONGVARBINARY;
            }
            case ISCConstants.SQL_BOOLEAN -> Types.BOOLEAN;
            case ISCConstants.SQL_NULL -> Types.NULL;
            case ISCConstants.SQL_ARRAY -> Types.ARRAY;
            default -> Types.OTHER;
        };
    }

    /**
     * Converts from the metadata type (as used in the system tables) to JDBC type values from {@link java.sql.Types}.
     *
     * @param metaDataType
     *         Metadata type value
     * @param subtype
     *         Subtype
     * @param scale
     *         Scale
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int fromMetaDataToJdbcType(int metaDataType, int subtype, int scale) {
        return fromFirebirdToJdbcType(fromMetaDataToFirebirdType(metaDataType), subtype, scale);
    }

    /**
     * Converts the metadata type value to the Firebird type value (null bit not set).
     *
     * @param metaDataType
     *         Metadata type value
     * @return Firebird type value
     */
    public static int fromMetaDataToFirebirdType(int metaDataType) {
        return switch (metaDataType) {
            case smallint_type -> ISCConstants.SQL_SHORT;
            case integer_type -> ISCConstants.SQL_LONG;
            case int64_type -> ISCConstants.SQL_INT64;
            case dec16_type -> ISCConstants.SQL_DEC16;
            case dec34_type -> ISCConstants.SQL_DEC34;
            case int128_type -> ISCConstants.SQL_INT128;
            case quad_type -> ISCConstants.SQL_QUAD;
            case float_type -> ISCConstants.SQL_FLOAT;
            case double_type -> ISCConstants.SQL_DOUBLE;
            case d_float_type -> ISCConstants.SQL_D_FLOAT;
            case date_type -> ISCConstants.SQL_TYPE_DATE;
            case time_type -> ISCConstants.SQL_TYPE_TIME;
            case timestamp_type -> ISCConstants.SQL_TIMESTAMP;
            case time_tz_type -> ISCConstants.SQL_TIME_TZ;
            case timestamp_tz_type -> ISCConstants.SQL_TIMESTAMP_TZ;
            // Shouldn't occur as metadata data type, mapping for consistency
            case ex_time_tz_type -> ISCConstants.SQL_TIME_TZ_EX;
            // Shouldn't occur as metadata data type, mapping for consistency
            case ex_timestamp_tz_type -> ISCConstants.SQL_TIMESTAMP_TZ_EX;
            case char_type -> ISCConstants.SQL_TEXT;
            case cstring_type, varchar_type -> ISCConstants.SQL_VARYING;
            case blob_type -> ISCConstants.SQL_BLOB;
            case boolean_type -> ISCConstants.SQL_BOOLEAN;
            // TODO Throw illegal arg / unsupported instead?
            default -> ISCConstants.SQL_NULL;
        };
    }

    public static String getTypeName(int jdbcType, int firebirdType, int subtype, int scale) {
        return switch (jdbcType) {
            case Types.SMALLINT -> "SMALLINT";
            case Types.INTEGER -> "INTEGER";
            case Types.BIGINT -> "BIGINT";
            case Types.DOUBLE -> "DOUBLE PRECISION";
            case JaybirdTypeCodes.DECFLOAT -> "DECFLOAT";
            case Types.FLOAT -> "FLOAT";
            case Types.DECIMAL -> "DECIMAL";
            case Types.NUMERIC -> {
                if (firebirdType == ISCConstants.SQL_INT128 && subtype == 0 && scale == 0) {
                    yield "INT128";
                }
                yield "NUMERIC";
            }
            // Reporting CHAR for Types.BINARY, otherwise need the Firebird version (i.e. 4.0 or higher)
            case Types.CHAR, Types.BINARY -> "CHAR";
            // Reporting VARCHAR for Types.VARBINARY, otherwise need the Firebird version (i.e. 4.0 or higher)
            case Types.VARCHAR, Types.VARBINARY -> "VARCHAR";
            // NOTE In practice CLOB and NCLOB are not used in this way; adding them for completeness
            case Types.LONGVARCHAR, Types.CLOB, Types.NCLOB -> "BLOB SUB_TYPE TEXT";
            case Types.LONGVARBINARY ->
                    subtype == BLOB_SUB_TYPE_BINARY ? "BLOB SUB_TYPE BINARY" : "BLOB SUB_TYPE " + subtype;
            case Types.BLOB -> "BLOB SUB_TYPE " + subtype;
            case Types.TIMESTAMP -> "TIMESTAMP";
            case Types.TIME -> "TIME";
            case Types.DATE -> "DATE";
            case Types.TIMESTAMP_WITH_TIMEZONE -> "TIMESTAMP WITH TIME ZONE";
            case Types.TIME_WITH_TIMEZONE -> "TIME WITH TIME ZONE";
            case Types.BOOLEAN -> "BOOLEAN";
            // Cannot practically occur as a type name
            case Types.NULL -> "NULL";
            // Arrays not supported
            case Types.ARRAY -> "ARRAY";
            // NOTE: Previously reported NULL
            default -> "OTHER";
        };
    }
}
