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
package org.firebirdsql.jdbc.field;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.sql.Types;

/**
 * Helper class to convert from Firebird and metadata type information to JDBC type information.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public final class JdbcTypeConverter {

    static final int SUBTYPE_NUMERIC = 1;
    static final int SUBTYPE_DECIMAL = 2;

    private JdbcTypeConverter() {
        // No instances
    }

    /**
     * Gets the JDBC type value from {@link java.sql.Types} for the field descriptor.
     *
     * @param fieldDescriptor Field descriptor
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int toJdbcType(final FieldDescriptor fieldDescriptor) {
        return fromFirebirdToJdbcType(fieldDescriptor.getType(), fieldDescriptor.getSubType(),
                fieldDescriptor.getScale());
    }

    /**
     * Converts from the Firebird type, subtype and scale to the JDBC type value from {@link java.sql.Types}.
     *
     * @param firebirdType Firebird type value (from {@link ISCConstants} {@code SQL_*} with or without nullable bit set
     * @param subtype Subtype
     * @param scale Scale
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int fromFirebirdToJdbcType(int firebirdType, int subtype, int scale) {
        firebirdType = firebirdType & ~1;

        switch (firebirdType) {
        case ISCConstants.SQL_SHORT:
            if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                return Types.NUMERIC;
            else if (subtype == SUBTYPE_DECIMAL)
                return Types.DECIMAL;
            else
                return Types.SMALLINT;
        case ISCConstants.SQL_LONG:
            if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                return Types.NUMERIC;
            else if (subtype == SUBTYPE_DECIMAL)
                return Types.DECIMAL;
            else
                return Types.INTEGER;
        case ISCConstants.SQL_INT64:
            if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                return Types.NUMERIC;
            else if (subtype == SUBTYPE_DECIMAL)
                return Types.DECIMAL;
            else
                return Types.BIGINT;
        case ISCConstants.SQL_DOUBLE:
        case ISCConstants.SQL_D_FLOAT:
            if (subtype == SUBTYPE_NUMERIC || (subtype == 0 && scale < 0))
                return Types.NUMERIC;
            else if (subtype == SUBTYPE_DECIMAL)
                return Types.DECIMAL;
            else
                return Types.DOUBLE;
        case ISCConstants.SQL_FLOAT:
            return Types.FLOAT;
        case ISCConstants.SQL_TEXT:
            if (subtype == ISCConstants.CS_BINARY){
                return Types.BINARY;
            } else {
                return Types.CHAR;
            }
        case ISCConstants.SQL_VARYING:
            if (subtype == ISCConstants.CS_BINARY){
                return Types.VARBINARY;
            } else {
                return Types.VARCHAR;
            }
        case ISCConstants.SQL_TIMESTAMP:
            return Types.TIMESTAMP;
        case ISCConstants.SQL_TYPE_TIME:
            return Types.TIME;
        case ISCConstants.SQL_TYPE_DATE:
            return Types.DATE;
        case ISCConstants.SQL_BLOB:
            if (subtype < 0)
                return Types.BLOB;
            else if (subtype == 1)
                return Types.LONGVARCHAR;
            else // if (subtype == 0 || subtype > 1)
                return Types.LONGVARBINARY;
        case ISCConstants.SQL_BOOLEAN:
            return Types.BOOLEAN;
        case ISCConstants.SQL_NULL:
            return Types.NULL;
        case ISCConstants.SQL_ARRAY:
            return Types.ARRAY;
        case ISCConstants.SQL_QUAD:
        default:
            return Types.OTHER;
        }
    }

    /**
     * Converts from the metadata type (as used in the system tables) to JDBC type values from {@link java.sql.Types}.
     * @param metaDataType Metadata type value
     * @param subtype Subtype
     * @param scale Scale
     * @return JDBC type, or {@link Types#OTHER} for unknown types
     */
    public static int fromMetaDataToJdbcType(int metaDataType, int subtype, int scale) {
        return fromFirebirdToJdbcType(fromMetaDataToFirebirdType(metaDataType), subtype, scale);
    }

    // TODO Double check if these are the same as the blr constants or not.
    // TODO And if they are the same, check missing types (like text2 = 15, varying2 = 38)
    static final int smallint_type = 7;
    static final int integer_type = 8;
    static final int quad_type = 9;
    static final int float_type = 10;
    static final int d_float_type = 11;
    static final int date_type = 12;
    static final int time_type = 13;
    static final int char_type = 14;
    static final int int64_type = 16;
    static final int double_type = 27;
    static final int timestamp_type = 35;
    static final int varchar_type = 37;
    // static final int cstring_type = 40;
    static final int blob_type = 261;
    static final short boolean_type = 23;

    /**
     * Converts the metadata type value to the Firebird type value (null bit not set).
     *
     * @param metaDataType Metadata type value
     * @return Firebird type value
     */
    public static int fromMetaDataToFirebirdType(int metaDataType) {
        switch (metaDataType) {
        case smallint_type:
            return ISCConstants.SQL_SHORT;
        case integer_type:
            return ISCConstants.SQL_LONG;
        case int64_type:
            return ISCConstants.SQL_INT64;
        case quad_type:
            return ISCConstants.SQL_QUAD;
        case float_type:
            return ISCConstants.SQL_FLOAT;
        case double_type:
            return ISCConstants.SQL_DOUBLE;
        case d_float_type:
            return ISCConstants.SQL_D_FLOAT;
        case date_type:
            return ISCConstants.SQL_TYPE_DATE;
        case time_type:
            return ISCConstants.SQL_TYPE_TIME;
        case timestamp_type:
            return ISCConstants.SQL_TIMESTAMP;
        case char_type:
            return ISCConstants.SQL_TEXT;
        case varchar_type:
            return ISCConstants.SQL_VARYING;
        case blob_type:
            return ISCConstants.SQL_BLOB;
        case boolean_type:
            return ISCConstants.SQL_BOOLEAN;
        default:
            // TODO Throw illegal arg / unsupported instead?
            return ISCConstants.SQL_NULL;
        }
    }
}
