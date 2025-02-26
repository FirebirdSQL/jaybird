// SPDX-FileCopyrightText: Copyright 2015-2022 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.jdbc.field;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.stream.Stream;

import static org.firebirdsql.common.matchers.MatcherAssume.assumeThat;
import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.metadata.FbMetadataConstants.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Parametrized test for {@link JdbcTypeConverter}.
 *
 * @author Mark Rotteveel
 * @since 3.0
 */
class JdbcTypeConverterTest {

    private static final RowDescriptorBuilder rowDescriptorBuilder = new RowDescriptorBuilder(1,
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8)));

    static Stream<Arguments> testData() {
        // Note: only use non-nullable test cases, nullable is tested through testFromFirebirdToJdbcTypeNullableBitSet
        return Stream.of(
                create(SQL_TEXT, CS_NONE, 0, char_type, Types.CHAR),
                create(SQL_TEXT, CS_BINARY, 0, char_type, Types.BINARY),
                create(SQL_VARYING, CS_NONE, 0, varchar_type, Types.VARCHAR),
                create(SQL_VARYING, CS_NONE, 0, cstring_type, Types.VARCHAR),
                create(SQL_VARYING, CS_BINARY, 0, varchar_type, Types.VARBINARY),
                create(SQL_VARYING, CS_BINARY, 0, cstring_type, Types.VARBINARY),
                create(SQL_SHORT, 0, 0, smallint_type, Types.SMALLINT),
                create(SQL_SHORT, 0, -1, smallint_type, Types.NUMERIC),
                create(SQL_SHORT, SUBTYPE_NUMERIC, 0, smallint_type, Types.NUMERIC),
                create(SQL_SHORT, SUBTYPE_NUMERIC, -1, smallint_type, Types.NUMERIC),
                create(SQL_SHORT, SUBTYPE_DECIMAL, 0, smallint_type, Types.DECIMAL),
                create(SQL_SHORT, SUBTYPE_DECIMAL, -1, smallint_type, Types.DECIMAL),
                create(SQL_LONG, 0, 0, integer_type, Types.INTEGER),
                create(SQL_LONG, 0, -1, integer_type, Types.NUMERIC),
                create(SQL_LONG, SUBTYPE_NUMERIC, 0, integer_type, Types.NUMERIC),
                create(SQL_LONG, SUBTYPE_NUMERIC, -1, integer_type, Types.NUMERIC),
                create(SQL_LONG, SUBTYPE_DECIMAL, 0, integer_type, Types.DECIMAL),
                create(SQL_LONG, SUBTYPE_DECIMAL, -1, integer_type, Types.DECIMAL),
                create(SQL_INT64, 0, 0, int64_type, Types.BIGINT),
                create(SQL_INT64, 0, -1, int64_type, Types.NUMERIC),
                create(SQL_INT64, SUBTYPE_NUMERIC, 0, int64_type, Types.NUMERIC),
                create(SQL_INT64, SUBTYPE_NUMERIC, -1, int64_type, Types.NUMERIC),
                create(SQL_INT64, SUBTYPE_DECIMAL, 0, int64_type, Types.DECIMAL),
                create(SQL_INT64, SUBTYPE_DECIMAL, -1, int64_type, Types.DECIMAL),
                create(SQL_FLOAT, 0, 0, float_type, Types.FLOAT),
                create(SQL_D_FLOAT, 0, 0, d_float_type, Types.DOUBLE),
                create(SQL_DOUBLE, 0, 0, double_type, Types.DOUBLE),
                create(SQL_TYPE_DATE, 0, 0, date_type, Types.DATE),
                create(SQL_TYPE_TIME, 0, 0, time_type, Types.TIME),
                create(SQL_TIMESTAMP, 0, 0, timestamp_type, Types.TIMESTAMP),
                create(SQL_TIME_TZ, 0, 0, time_tz_type, Types.TIME_WITH_TIMEZONE),
                create(SQL_TIMESTAMP_TZ, 0, 0, timestamp_tz_type, Types.TIMESTAMP_WITH_TIMEZONE),
                create(SQL_BLOB, 0, 0, blob_type, Types.LONGVARBINARY),
                create(SQL_BLOB, 1, 0, blob_type, Types.LONGVARCHAR),
                create(SQL_BLOB, 2, 0, blob_type, Types.LONGVARBINARY),
                create(SQL_BLOB, -1, 0, blob_type, Types.BLOB),
                create(SQL_BOOLEAN, 0, 0, boolean_type, Types.BOOLEAN),
                create(SQL_NULL, 0, 0, -1, Types.NULL), // TODO Check if there is a metadata type
                create(SQL_ARRAY, 0, 0, -1, Types.ARRAY), // TODO Check if there is a metadata type
                create(SQL_QUAD, 0, 0, quad_type, Types.OTHER),
                createDbKey(Types.ROWID)
        );
    }

    @ParameterizedTest(name = "{index}: Field: {0}; expected: {2}")
    @MethodSource("testData")
    void testToJdbcType(FieldDescriptor fieldDescriptor, Integer ignored, Integer expectedJdbcType) {
        assertEquals(expectedJdbcType, JdbcTypeConverter.toJdbcType(fieldDescriptor));
    }

    @ParameterizedTest(name = "{index}: Field: {0}; expected: {2}")
    @MethodSource("testData")
    void testFromFirebirdToJdbcTypeNullableBitSet(
            FieldDescriptor fieldDescriptor, Integer ignored, Integer expectedJdbcType) {
        assumeThat("ROWID detection not possible for fromFirebirdToJdbcType",
                expectedJdbcType, not(equalTo(Types.ROWID)));

        // fromFirebirdToJdbcType with non-nullable is tested through testToJdbcType
        int nullableFirebirdType = fieldDescriptor.getType() | 1;
        int subType = fieldDescriptor.getSubType();
        int scale = fieldDescriptor.getScale();

        assertEquals(expectedJdbcType, JdbcTypeConverter.fromFirebirdToJdbcType(nullableFirebirdType, subType, scale));
    }

    @ParameterizedTest(name = "{index}: Field: {0}, metadata: {1}; expected: {2}")
    @MethodSource("testData")
    void testFromMetaDataToJdbcType(FieldDescriptor fieldDescriptor, Integer metadataType, Integer expectedJdbcType) {
        assumeThat("Metadata type unknown for Firebird type " + fieldDescriptor.getType(),
                metadataType, not(equalTo(-1)));
        int subType = fieldDescriptor.getSubType();
        int scale = fieldDescriptor.getScale();

        assertEquals(expectedJdbcType, JdbcTypeConverter.fromMetaDataToJdbcType(metadataType, subType, scale));
    }

    @ParameterizedTest(name = "{index}: Field: {0}, expected: {1}")
    @MethodSource("testData")
    void testFromMetaDataToFirebirdType(FieldDescriptor fieldDescriptor, Integer metadataType, Integer ignored) {
        assumeThat("Metadata type unknown for Firebird type " + fieldDescriptor.getType(),
                metadataType, not(equalTo(-1)));

        assertEquals(fieldDescriptor.getType(), JdbcTypeConverter.fromMetaDataToFirebirdType(metadataType));
    }

    /**
     * Creates test data.
     * @param type Firebird type
     * @param subtype Subtype
     * @param scale Scale
     * @param metadataType Metadata type, or {@code -1} to signal unsupported/unknown
     * @param jdbcType Expected JDBC type
     * @return Test data
     */
    private static Arguments create(int type, int subtype, int scale, int metadataType, int jdbcType) {
        assertEquals(0, (type & 1), "Only use non-nullable types");
        FieldDescriptor fieldDescriptor = rowDescriptorBuilder
                .setType(type)
                .setSubType(subtype)
                .setScale(scale)
                .setOriginalName("DUMMY")
                .toFieldDescriptor();
        return Arguments.of(fieldDescriptor, metadataType, jdbcType);
    }

    @SuppressWarnings("SameParameterValue")
    private static Arguments createDbKey(int jdbcType) {
        FieldDescriptor fieldDescriptor = rowDescriptorBuilder
                .setType(ISCConstants.SQL_TEXT)
                .setSubType(ISCConstants.CS_BINARY)
                .setOriginalName("DB_KEY")
                .toFieldDescriptor();
        return Arguments.of(fieldDescriptor, -1, jdbcType);
    }

}
