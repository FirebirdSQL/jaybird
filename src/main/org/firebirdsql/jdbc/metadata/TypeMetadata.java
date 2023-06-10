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
package org.firebirdsql.jdbc.metadata;

import org.firebirdsql.jdbc.JaybirdTypeCodes;
import org.firebirdsql.jdbc.field.JdbcTypeConverter;
import org.firebirdsql.util.FirebirdSupportInfo;
import org.firebirdsql.util.InternalApi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;

/**
 * Helper class to determine type metadata conforming to expectations of {@link java.sql.DatabaseMetaData}.
 *
 * @author Mark Rotteveel
 * @since 4.0
 */
@InternalApi
public final class TypeMetadata {

    static final String FIELD_TYPE = "FIELD_TYPE";
    static final String FIELD_SUB_TYPE = "FIELD_SUB_TYPE";
    static final String FIELD_PRECISION = "FIELD_PRECISION";
    static final String FIELD_SCALE = "FIELD_SCALE";
    static final String FIELD_LENGTH = "FIELD_LENGTH";
    // Avoid CHAR_LENGTH and CHARACTER_LENGTH as those are reserved words
    static final String CHAR_LEN = "CHAR_LEN";
    static final String CHARSET_ID = "CHARSET_ID";

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
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(typeBehaviours));
    }

    /**
     * @return Firebird type number
     */
    int getType() {
        return type;
    }

    /**
     * @return The {@link java.sql.Types} or {@link JaybirdTypeCodes} code for this datatype
     */
    int getJdbcType() {
        return jdbcType;
    }

    /**
     * @return The SQL datatype name, returns {@code "NULL"} if the type is unknown
     */
    String getSqlTypeName() {
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
     * This method will also return any non-zero precision information stored for other data types than those listed in
     * the {@code COLUMN_SIZE} definition in the JDBC API.
     * </p>
     *
     * @return The column size as defined in {@link java.sql.DatabaseMetaData}, or {@code null}.
     */
    Integer getColumnSize() {
        return switch (jdbcType) {
            case Types.FLOAT -> isFloatBinaryPrecision() ? FLOAT_BINARY_PRECISION : FLOAT_DECIMAL_PRECISION;
            case Types.DOUBLE -> isFloatBinaryPrecision() ? DOUBLE_BINARY_PRECISION : DOUBLE_DECIMAL_PRECISION;
            case Types.CHAR, Types.VARCHAR, Types.BINARY, Types.VARBINARY -> characterLength;
            case Types.BIGINT -> BIGINT_PRECISION;
            case Types.INTEGER -> INTEGER_PRECISION;
            case Types.SMALLINT -> SMALLINT_PRECISION;
            case Types.BOOLEAN -> BOOLEAN_BINARY_PRECISION;
            case Types.NUMERIC, Types.DECIMAL -> switch (type) {
                case double_type, d_float_type, int64_type -> coalesce(precision, NUMERIC_BIGINT_PRECISION);
                case integer_type -> coalesce(precision, NUMERIC_INTEGER_PRECISION);
                case smallint_type -> coalesce(precision, NUMERIC_SMALLINT_PRECISION);
                case int128_type -> {
                    if (precision == 0) {
                        // INT128 (precision reported as 38, not 39 to avoid issues with tools using type name as NUMERIC)
                        yield NUMERIC_INT128_PRECISION;
                    }
                    yield coalesce(precision, NUMERIC_INT128_PRECISION);
                }
                default -> throw new IllegalStateException(String.format(
                        "Incorrect derivation of NUMERIC/DECIMAL precision for jdbcType %d, type %d, subType %d, scale %d",
                        jdbcType, type, subType, scale));
            };
            case Types.DATE -> DATE_PRECISION;
            case Types.TIME -> TIME_PRECISION;
            case Types.TIMESTAMP -> TIMESTAMP_PRECISION;
            case Types.TIME_WITH_TIMEZONE -> TIME_WITH_TIMEZONE_PRECISION;
            case Types.TIMESTAMP_WITH_TIMEZONE -> TIMESTAMP_WITH_TIMEZONE_PRECISION;
            case JaybirdTypeCodes.DECFLOAT -> switch (type) {
                case dec16_type -> DECFLOAT_16_PRECISION;
                case dec34_type -> DECFLOAT_34_PRECISION;
                default -> throw new IllegalStateException(String.format(
                        "Incorrect derivation of DECFLOAT precision for jdbcType %d, type %d, subType %d, scale %d",
                        jdbcType, type, subType, scale));
            };
            // If we have non-default, non-zero precision, report it
            default -> coalesce(precision, 0) != 0 ? precision : null;
        };
    }

    /**
     * @return The field length in bytes
     */
    Integer getLength() {
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
    Integer getScale() {
        return switch (jdbcType) {
            case Types.BIGINT, Types.INTEGER, Types.SMALLINT -> 0;
            case Types.NUMERIC, Types.DECIMAL -> -1 * coalesce(scale, 0);
            // If we have non-default, non-zero scale, report it
            default -> coalesce(scale, 0) != 0 ? scale : null;
        };
    }

    /**
     * @return The radix of numerical precision (either {@code 2} or {@code 10}; returns {@code 10} for non-numerical,
     * non-boolean types).
     */
    int getRadix() {
        return switch (jdbcType) {
            case Types.FLOAT, Types.DOUBLE -> isFloatBinaryPrecision() ? RADIX_BINARY : RADIX_DECIMAL;
            case Types.BOOLEAN -> RADIX_BINARY;
            default -> RADIX_DECIMAL;
        };
    }

    /**
     * @return The maximum number of bytes for a character type column, {@code null} otherwise
     */
    Integer getCharOctetLength() {
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
    static Builder builder(FirebirdSupportInfo supportInfo) {
        return new Builder(supportInfo);
    }

    /**
     * Derives the JDBC data type from {@link java.sql.Types} or {@link JaybirdTypeCodes} from metadata information.
     *
     * @param sqlType
     *         Firebird type code as used in the metadata tables
     * @param sqlSubType
     *         Firebird subtype code as used in the metadata tables
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
     *         Firebird subtype code as used in the metadata tables
     * @param sqlScale
     *         Firebird scale as used in the metadata tables
     * @return JDBC/SQL type name
     */
    public static String getDataTypeName(int sqlType, int sqlSubType, int sqlScale) {
        int firebirdType = JdbcTypeConverter.fromMetaDataToFirebirdType(sqlType);
        int jdbcType = JdbcTypeConverter.fromFirebirdToJdbcType(firebirdType, sqlSubType, sqlScale);
        return JdbcTypeConverter.getTypeName(jdbcType, firebirdType, sqlSubType, sqlScale);
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

    enum TypeBehaviour {
        FLOAT_DECIMAL_PRECISION,
        FLOAT_BINARY_PRECISION
    }

    static final class Builder {

        private int type;
        private Integer subType;
        private Integer precision;
        private Integer scale;
        private Integer characterSetId;
        private Integer fieldLength;
        private Integer characterLength;
        private final Set<TypeBehaviour> typeBehaviours = EnumSet.noneOf(TypeBehaviour.class);

        Builder(FirebirdSupportInfo supportInfo) {
            typeBehaviours.add(supportInfo.supportsFloatBinaryPrecision()
                    ? TypeBehaviour.FLOAT_BINARY_PRECISION
                    : TypeBehaviour.FLOAT_DECIMAL_PRECISION);
        }

        TypeMetadata build() {
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
        Builder withType(int type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the field sub-type code ({@code RDB$FIELD_SUB_TYPE}).
         *
         * @param subType
         *         Field subtype code
         * @return this builder
         */
        Builder withSubType(Integer subType) {
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
        Builder withPrecision(Integer precision) {
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
        Builder withScale(Integer scale) {
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
        Builder withCharacterSetId(Integer characterSetId) {
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
        Builder withFieldLength(Integer fieldLength) {
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
        Builder withCharacterLength(Integer characterLength) {
            this.characterLength = characterLength;
            return this;
        }

        /**
         * Populate this builder from the current row of a {@code ResultSet}.
         * <p>
         * The result set should have the following column labels:
         * </p>
         * <ul>
         *     <li>{@code FIELD_TYPE} - type</li>
         *     <li>{@code FIELD_SUB_TYPE} - sub-type</li>
         *     <li>{@code FIELD_PRECISION} - precision</li>
         *     <li>{@code FIELD_SCALE} - scale</li>
         *     <li>{@code FIELD_LENGTH} - length (in bytes)</li>
         *     <li>{@code CHAR_LEN} - character length (or {@code null})</li>
         *     <li>{@code CHARSET_ID} - character set id (or {@code null})</li>
         * </ul>
         *
         * @param resultSet
         *         Result set positioned on a row with the required columns
         * @return this builder
         * @throws SQLException
         *         For errors retrieving data, including absence of required columns
         */
        Builder fromCurrentRow(ResultSet resultSet) throws SQLException {
            return withType(resultSet.getObject(FIELD_TYPE, Integer.class))
                    .withSubType(resultSet.getObject(FIELD_SUB_TYPE, Integer.class))
                    .withPrecision(resultSet.getObject(FIELD_PRECISION, Integer.class))
                    .withScale(resultSet.getObject(FIELD_SCALE, Integer.class))
                    .withFieldLength(resultSet.getObject(FIELD_LENGTH, Integer.class))
                    .withCharacterLength(resultSet.getObject(CHAR_LEN, Integer.class))
                    .withCharacterSetId(resultSet.getObject(CHARSET_ID, Integer.class));
        }
    }
}
