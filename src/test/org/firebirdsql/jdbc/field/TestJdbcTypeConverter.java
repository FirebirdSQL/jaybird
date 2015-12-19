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

import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;

import static org.firebirdsql.gds.ISCConstants.*;
import static org.firebirdsql.jdbc.field.JdbcTypeConverter.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

/**
 * Parametrized test for {@link JdbcTypeConverter}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
@RunWith(Parameterized.class)
public class TestJdbcTypeConverter {

    private static final RowDescriptorBuilder rowDescriptorBuilder =
            new RowDescriptorBuilder(1, DefaultDatatypeCoder.getDefaultInstance());
    private final FieldDescriptor fieldDescriptor;
    private final int expectedJdbcType;
    private final int metadataType;

    public TestJdbcTypeConverter(FieldDescriptor fieldDescriptor, Integer metadataType, Integer expectedJdbcType) {
        this.metadataType = metadataType;
        assertTrue("Only use non-nullable types", (fieldDescriptor.getType() & 1) == 0);
        this.fieldDescriptor = fieldDescriptor;
        this.expectedJdbcType = expectedJdbcType;
    }

    @Parameterized.Parameters(name = "Field: {0}, metadata: {1}; expected: {2}")
    public static Collection<Object[]> testData() {
        // Note: only use non-nullable test cases, nullable is tested through testFromFirebirdToJdbcTypeNullableBitSet
        return Arrays.asList(
                create(SQL_TEXT, CS_NONE, 0, char_type, Types.CHAR),
                create(SQL_TEXT, CS_BINARY, 0, char_type, Types.BINARY),
                create(SQL_VARYING, CS_NONE, 0, varchar_type, Types.VARCHAR),
                create(SQL_VARYING, CS_BINARY, 0, varchar_type, Types.VARBINARY),
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
                create(SQL_BLOB, 0, 0, blob_type, Types.LONGVARBINARY),
                create(SQL_BLOB, 1, 0, blob_type, Types.LONGVARCHAR),
                create(SQL_BLOB, 2, 0, blob_type, Types.LONGVARBINARY),
                create(SQL_BLOB, -1, 0, blob_type, Types.BLOB),
                create(SQL_BOOLEAN, 0, 0, boolean_type, Types.BOOLEAN),
                create(SQL_NULL, 0, 0, -1, Types.NULL), // TODO Check if there is a metadata type
                create(SQL_ARRAY, 0, 0, -1, Types.ARRAY), // TODO Check if there is a metadata type
                create(SQL_QUAD, 0, 0, quad_type, Types.OTHER)
        );
    }

    @Test
    public void testToJdbcType() {
        assertEquals(expectedJdbcType, JdbcTypeConverter.toJdbcType(fieldDescriptor));
    }

    @Test
    public void testFromFirebirdToJdbcTypeNullableBitSet() {
        // fromFirebirdToJdbcType with non-nullable is tested through testToJdbcType
        int nullableFirebirdType = fieldDescriptor.getType() | 1;
        int subType = fieldDescriptor.getSubType();
        int scale = fieldDescriptor.getScale();

        assertEquals(expectedJdbcType, JdbcTypeConverter.fromFirebirdToJdbcType(nullableFirebirdType, subType, scale));
    }

    @Test
    public void testFromMetaDataToJdbcType() {
        assumeThat("Metadata type unknown for Firebird type " + fieldDescriptor.getType(),
                metadataType, not(equalTo(-1)));
        int subType = fieldDescriptor.getSubType();
        int scale = fieldDescriptor.getScale();

        assertEquals(expectedJdbcType, JdbcTypeConverter.fromMetaDataToJdbcType(metadataType, subType, scale));
    }

    @Test
    public void testFromMetaDataToFirebirdType() {
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
    private static Object[] create(int type, int subtype, int scale, int metadataType, int jdbcType) {
        FieldDescriptor fieldDescriptor = rowDescriptorBuilder
                .setType(type)
                .setSubType(subtype)
                .setScale(scale)
                .toFieldDescriptor();
        return new Object[] { fieldDescriptor, metadataType, jdbcType };
    }

}
