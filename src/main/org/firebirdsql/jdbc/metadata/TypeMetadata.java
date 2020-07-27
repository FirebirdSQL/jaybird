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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.firebirdsql.util.FirebirdSupportInfo;

import java.sql.Types;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;

/**
 * Helper class to determine type metadata conforming to expectations of {@link java.sql.DatabaseMetaData}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public class TypeMetadata {

    private final int type;
    private final int subType;
    private final Integer precision;
    private final Integer scale;
    private final Integer jdbcType;
    private final Integer fieldLength;
    private final Integer characterLength;
    private final Set<TypeBehaviour> typeBehaviours;

    private TypeMetadata(int type, Integer subType, Integer precision, Integer scale, Integer characterSetId,
            Integer fieldLength, Integer characterLength, Set<TypeBehaviour> typeBehaviours) {
        this.type = type;
        this.subType = coalesce(subType, 0);
        this.precision = precision;
        this.scale = scale;
        this.jdbcType = getDataType(type, this.subType, coalesce(scale, 0), coalesce(characterSetId, 0));
        this.fieldLength = fieldLength;
        if (isDefault(characterLength) && !isDefault(fieldLength) && isCharacterType(type)) {
            // NOTE: We're not taking the character set length into account as there are situations where the maximum
            // field and character length is the same even if a multi-byte character set is used.
            this.characterLength = fieldLength;
        } else {
            this.characterLength = characterLength;
        }
        this.typeBehaviours = typeBehaviours.isEmpty()
                ? Collections.<TypeBehaviour>emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(typeBehaviours));
    }

    /**
     * @return The {@link java.sql.Types} or {@link JaybirdTypeCodes} code for this datatype
     */
    public int getJdbcType() {
        return jdbcType;
    }

    /**
     * @return The SQL datatype name, returns {@code "NULL"} if the type is unknown
     */
    public String getSqlTypeName() {
        return getDataTypeName(type, subType, coalesce(scale, 0));
    }

    /**
     * Returns the column size (precision) of the type.
     * <p>
     * The value returned follows the definition used in {@link java.sql.DatabaseMetaData}, as established in
     * {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)} for {@code COLUMN_SIZE}. The same
     * definition is used for database metadata columns {@code PRECISION} in, among others,
     * {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}.
     * </p>
     * <p>
     * This method will also return any non-zero precision information stored for other datatypes than those listed in
     * the {@code COLUMN_SIZE} definition in the JDBC API.
     * </p>
     *
     * @return The column size as defined in {@link java.sql.DatabaseMetaData}, or {@code null}.
     */
    public Integer getColumnSize() {
        switch (jdbcType) {
        case Types.FLOAT:
            return isFloatBinaryPrecision() ? FLOAT_BINARY_PRECISION : FLOAT_DECIMAL_PRECISION;
        case Types.DOUBLE:
            return isFloatBinaryPrecision() ? DOUBLE_BINARY_PRECISION : DOUBLE_DECIMAL_PRECISION;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.BINARY:
        case Types.VARBINARY:
            return characterLength;
        case Types.BIGINT:
            return BIGINT_PRECISION;
        case Types.INTEGER:
            return INTEGER_PRECISION;
        case Types.SMALLINT:
            return SMALLINT_PRECISION;
        case Types.BOOLEAN:
            return BOOLEAN_BINARY_PRECISION;
        case Types.NUMERIC:
        case Types.DECIMAL:
            switch (type) {
            case double_type:
            case d_float_type:
            case int64_type:
                return coalesce(precision, NUMERIC_BIGINT_PRECISION);
            case integer_type:
                return coalesce(precision, NUMERIC_INTEGER_PRECISION);
            case smallint_type:
                return coalesce(precision, NUMERIC_SMALLINT_PRECISION);
            case int128_type:
                return coalesce(precision, NUMERIC_INT128_PRECISION);
            default:
                throw new IllegalStateException(String.format(
                        "Incorrect derivation of NUMERIC/DECIMAL precision for jdbcType %d, type %d, subType %d, scale %d",
                        jdbcType, type, subType, scale));
            }
        case Types.DATE:
            return DATE_PRECISION;
        case Types.TIME:
            return TIME_PRECISION;
        case Types.TIMESTAMP:
            return TIMESTAMP_PRECISION;
        case Types.TIME_WITH_TIMEZONE:
            return TIME_WITH_TIMEZONE_PRECISION;
        case Types.TIMESTAMP_WITH_TIMEZONE:
            return TIMESTAMP_WITH_TIMEZONE_PRECISION;
        case JaybirdTypeCodes.DECFLOAT:
            switch (type) {
            case dec16_type:
                return DECFLOAT_16_PRECISION;
            case dec34_type:
                return DECFLOAT_34_PRECISION;
            default:
                throw new IllegalStateException(String.format(
                        "Incorrect derivation of DECFLOAT precision for jdbcType %d, type %d, subType %d, scale %d",
                        jdbcType, type, subType, scale));
            }
        }
        // If we have non-default, non-zero precision, report it
        return coalesce(precision, 0) != 0 ? precision : null;
    }

    /**
     * @return The field length in bytes
     */
    public Integer getLength() {
        return fieldLength;
    }

    /**
     * Returns the scale of the field.
     * <p>
     * For numerical types, returns a zero or positive scale. For types without scale, it returns {@code null},
     * for types that have a non-zero scale in the Firebird metadata, it returns the scale as stored.
     * </p>
     *
     * @return The scale of a field, or {@code null}.
     */
    public Integer getScale() {
        switch (jdbcType) {
        case Types.BIGINT:
        case Types.INTEGER:
        case Types.SMALLINT:
            return 0;
        case Types.NUMERIC:
        case Types.DECIMAL:
            return -1 * coalesce(scale, 0);
        }
        // If we have non-default, non-zero scale, report it
        return coalesce(scale, 0) != 0 ? scale : null;
    }

    /**
     * @return The radix of numerical precision (either {@code 2} or {@code 10}; returns {@code 10} for non-numerical,
     * non-boolean types.
     */
    public int getRadix() {
        switch (jdbcType) {
        case Types.FLOAT:
        case Types.DOUBLE:
            return isFloatBinaryPrecision() ? RADIX_BINARY : RADIX_DECIMAL;
        case Types.BOOLEAN:
            return RADIX_BINARY;
        default:
            return RADIX_DECIMAL;
        }
    }

    /**
     * @return The maximum number of bytes for a character type column, {@code null} otherwise
     */
    public Integer getCharOctetLength() {
        if (isCharacterType(type)) {
            return getLength();
        }
        return null;
    }

    /**
     * Creates type metadata builder.
     *
     * @param supportInfo
     *         Firebird support info
     * @return Builder for type metadata
     */
    public static Builder builder(FirebirdSupportInfo supportInfo) {
        return new Builder(supportInfo);
    }

    /**
     * Derives the JDBC data type from {@link java.sql.Types} or {@link JaybirdTypeCodes} from metadata information.
     *
     * @param sqlType
     *         Firebird type code as used in the metadata tables
     * @param sqlSubType
     *         Firebird sub-type code as used in the metadata tables
     * @param sqlScale
     *         Firebird scale as used in the metadata tables
     * @param characterSetId
     *         Character set id as used in the metadata tables
     * @return JDBC data type code.
     */
    public static int getDataType(int sqlType, int sqlSubType, int sqlScale, int characterSetId) {
        // TODO Preserved for backwards compatibility, is this really necessary?
        if (sqlType == blob_type && sqlSubType > 1) {
            return Types.OTHER;
        }
        final int jdbcType = JdbcTypeConverter.fromMetaDataToJdbcType(sqlType, sqlSubType, sqlScale);
        // Metadata from RDB$ tables does not contain character set in subtype, manual fixup
        if (characterSetId == CS_BINARY) {
            if (jdbcType == Types.CHAR) {
                return Types.BINARY;
            } else if (jdbcType == Types.VARCHAR) {
                return Types.VARBINARY;
            }
        }
        return jdbcType;
    }

    /**
     * Derives the JDBC/SQL type name from metadata information.
     *
     * @param sqlType
     *         Firebird type code as used in the metadata tables
     * @param sqlSubType
     *         Firebird sub-type code as used in the metadata tables
     * @param sqlScale
     *         Firebird scale as used in the metadata tables
     * @return JDBC/SQL type name
     */
    public static String getDataTypeName(int sqlType, int sqlSubType, int sqlScale) {
        // TODO Unify with AbstractFieldMetadata
        // TODO Map using JDBC type code (except maybe blob exceptions)?
        switch (sqlType) {
        case smallint_type:
        case integer_type:
        case int64_type:
        case double_type:
        case d_float_type:
        case int128_type:
            if (sqlSubType == SUBTYPE_NUMERIC || (sqlSubType == 0 && sqlScale < 0)) {
                return "NUMERIC";
            } else if (sqlSubType == SUBTYPE_DECIMAL) {
                return "DECIMAL";
            } else {
                switch (sqlType) {
                case smallint_type:
                    return "SMALLINT";
                case integer_type:
                    return "INTEGER";
                case int64_type:
                    return "BIGINT";
                case double_type:
                case d_float_type:
                    return "DOUBLE PRECISION";
                case int128_type:
                    return "INT128";
                default:
                    throw new IllegalStateException(String.format(
                            "Incorrect derivation of type name in getDataTypeName(%d, %d, %d)",
                            sqlType, sqlSubType, sqlScale));
                }
            }
        case float_type:
            return "FLOAT";
        case char_type:
            return "CHAR";
        case varchar_type:
        case cstring_type:
            return "VARCHAR";
        case timestamp_type:
            return "TIMESTAMP";
        case time_type:
            return "TIME";
        case date_type:
            return "DATE";
        case time_tz_type:
        case ex_time_tz_type:
            return "TIME WITH TIME ZONE";
        case timestamp_tz_type:
        case ex_timestamp_tz_type:
            return "TIMESTAMP WITH TIME ZONE";
        case blob_type:
            if (sqlSubType == BLOB_SUB_TYPE_BINARY) {
                return "BLOB SUB_TYPE BINARY";
            } else if (sqlSubType == BLOB_SUB_TYPE_TEXT) {
                return "BLOB SUB_TYPE TEXT";
            } else {
                // In the past implementations returned BLOB SUB_TYPE < 0 for negative subtypes
                return "BLOB SUB_TYPE " + sqlSubType;
            }
        case quad_type:
            return "ARRAY";
        case boolean_type:
            return "BOOLEAN";
        case dec16_type:
        case dec34_type:
            return "DECFLOAT";
        default:
            return "NULL";
        }
    }

    private boolean isFloatBinaryPrecision() {
        return typeBehaviours.contains(TypeBehaviour.FLOAT_BINARY_PRECISION);
    }

    private static int coalesce(Integer value, int replacement) {
        return value != null ? value : replacement;
    }

    private static boolean isDefault(Integer value) {
        return value == null;
    }

    private static boolean isCharacterType(int sqlType) {
        return sqlType == char_type || sqlType == varchar_type || sqlType == cstring_type;
    }

    public enum TypeBehaviour {
        FLOAT_DECIMAL_PRECISION,
        FLOAT_BINARY_PRECISION
    }

    public static class Builder {

        private int type;
        private Integer subType;
        private Integer precision;
        private Integer scale;
        private Integer characterSetId;
        private Integer fieldLength;
        private Integer characterLength;
        private final Set<TypeBehaviour> typeBehaviours = EnumSet.noneOf(TypeBehaviour.class);

        public Builder(FirebirdSupportInfo supportInfo) {
            typeBehaviours.add(supportInfo.supportsFloatBinaryPrecision()
                    ? TypeBehaviour.FLOAT_BINARY_PRECISION
                    : TypeBehaviour.FLOAT_DECIMAL_PRECISION);
        }

        public TypeMetadata build() {
            if (type == 0) {
                throw new IllegalStateException("type must be set");
            }
            return new TypeMetadata(type, subType, precision, scale, characterSetId, fieldLength, characterLength,
                    typeBehaviours);
        }

        /**
         * Sets the field type code ({@code RDB$FIELD_TYPE}).
         *
         * @param type
         *         Field type code
         * @return this builder
         */
        public Builder withType(int type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the field sub type code ({@code RDB$FIELD_SUB_TYPE}).
         *
         * @param subType
         *         Field sub type code
         * @return this builder
         */
        public Builder withSubType(Integer subType) {
            this.subType = subType;
            return this;
        }

        /**
         * Sets the field precision ({@code RDB$FIELD_PRECISION}).
         *
         * @param precision
         *         Field precision
         * @return this builder
         */
        public Builder withPrecision(Integer precision) {
            this.precision = precision;
            return this;
        }

        /**
         * Sets the field scale ({@code RDB$FIELD_SCALE}).
         *
         * @param scale
         *         Field scale
         * @return this builder
         */
        public Builder withScale(Integer scale) {
            this.scale = scale;
            return this;
        }

        /**
         * Sets the character set id ({@code RDB$CHARACTER_SET_ID}).
         *
         * @param characterSetId
         *         Character set id
         * @return this builder
         */
        public Builder withCharacterSetId(Integer characterSetId) {
            this.characterSetId = characterSetId;
            return this;
        }

        /**
         * Sets the field length ({@code RDB$FIELD_LENGTH}).
         *
         * @param fieldLength
         *         Field length
         * @return this builder
         */
        public Builder withFieldLength(Integer fieldLength) {
            this.fieldLength = fieldLength;
            return this;
        }

        /**
         * Sets the character length ({@code RDB$CHARACTER_LENGTH}).
         *
         * @param characterLength
         *         Character length
         * @return this builder
         */
        public Builder withCharacterLength(Integer characterLength) {
            this.characterLength = characterLength;
            return this;
        }

    }

}
