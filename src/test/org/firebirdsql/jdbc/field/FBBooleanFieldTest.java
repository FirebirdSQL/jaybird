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

import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for boolean fields. Note that boolean fields are only supported in Firebird 3.0 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
class FBBooleanFieldTest extends BaseJUnit5TestFBField<FBBooleanField, Boolean> {

    @BeforeEach
    @Override
    void setUp() throws Exception{
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_BOOLEAN);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBBooleanField(fieldDescriptor, fieldData, Types.BOOLEAN);
    }

    @Test
    @Override
    void getBigDecimalNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(BigDecimal.ONE, field.getBigDecimal(), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void getObject_BigDecimal() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(BigDecimal.ONE, field.getObject(BigDecimal.class),
                "Unexpected value for getObject(BigDecimal.class)");
    }

    @Test
    void getBigDecimalNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals( BigDecimal.ZERO, field.getBigDecimal(), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void setBigDecimalNonNull() throws SQLException {
        field.setBigDecimal(BigDecimal.ZERO);

        verifySetBoolean(false);
    }

    @Test
    @Override
    void getBigDecimalIntNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(BigDecimal.ONE, field.getBigDecimal(1), "Unexpected value for getBigDecimal");
    }

    @Test
    @Override
    void getBooleanNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertTrue(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    @Override
    void getObject_Boolean() throws SQLException {
        toReturnBooleanExpectations(true);

        assertTrue(field.getObject(Boolean.class), "Unexpected value for getObject(Boolean.class)");
    }

    @Test
    void getBooleanNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertFalse(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getBooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertFalse(field.getBoolean(), "Unexpected value for getBoolean");
    }

    @Test
    void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getObject(Boolean.class), "Expected null for getObject(Boolean.class)");
    }

    @Test
    @Override
    void setBoolean() throws SQLException {
        field.setBoolean(true);

        verifySetBoolean(true);
    }

    @Test
    @Override
    void getByteNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(0, field.getByte(), "Unexpected value for getByte");
    }

    @Test
    @Override
    void getObject_Byte() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1, (byte) field.getObject(Byte.class), "Unexpected value for getObject(Byte.class)");
    }

    @Test
    @Override
    void setByte() throws SQLException {
        field.setByte((byte) 127);

        verifySetBoolean(true);
    }

    @Test
    @Override
    void getDoubleNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1.0, field.getDouble(), 0.0, "Unexpected value for getDouble");
    }

    @Test
    @Override
    void getObject_Double() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1.0, field.getObject(Double.class), 0.0, "Unexpected value for getObject(Double.class)");
    }

    @Test
    @Override
    void setDouble() throws SQLException {
        field.setDouble(0.0);

        verifySetBoolean(false);
    }

    @Test
    @Override
    void getFloatNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(0.0, field.getFloat(), 0.0, "Unexpected value for getFloat");
    }

    @Test
    @Override
    void getObject_Float() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(0.0, field.getObject(Float.class), 0.0, "Unexpected value for getObject(Float.class)");
    }

    @Test
    @Override
    void setFloat() throws SQLException {
        field.setFloat(Float.MIN_VALUE);

        verifySetBoolean(true);
    }

    @Test
    @Override
    void getIntNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1, field.getInt(), "Unexpected value for getInt");
    }

    @Test
    @Override
    void getObject_Integer() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1, (int) field.getObject(Integer.class), "Unexpected value for getInt");
    }

    @Test
    @Override
    void setInteger() throws SQLException {
        field.setInteger(0);

        verifySetBoolean(false);
    }

    @Test
    @Override
    void getLongNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(0L, field.getLong(), "Unexpected value for getLong");
    }

    @Test
    @Override
    void getObject_Long() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(0L, (long) field.getObject(Long.class), "Unexpected value for getLong");
    }

    @Test
    @Override
    void setLong() throws SQLException {
        field.setLong(1L);

        verifySetBoolean(true);
    }

    @Test
    @Override
    void getObjectNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(Boolean.TRUE, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    void getObjectNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals(Boolean.FALSE, field.getObject(), "Unexpected value for getObject");
    }

    @Test
    @Override
    void setObjectNonNull() throws SQLException {
        field.setObject(Boolean.FALSE);

        verifySetBoolean(false);
    }

    @Test
    @Override
    void getShortNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1, field.getShort(), "Unexpected value for getShort");
    }

    @Test
    @Override
    void getObject_Short() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals(1, (short) field.getObject(Short.class), "Unexpected value for getShort");
    }

    @Test
    @Override
    void setShort() throws SQLException {
        field.setShort((short) 0);

        verifySetBoolean(false);
    }

    @Test
    @Override
    void getStringNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("false", field.getString(), "Unexpected value for getString");
    }

    @Test
    void getStringNonNull_true() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("true", field.getString(), "Unexpected value for getString");
    }

    @Test
    @Override
    void getObject_String() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("false", field.getObject(String.class), "Unexpected value for getString");
    }

     @Test
     @Override
     void setStringNonNull() throws SQLException {
        field.setString("false");

        verifySetBoolean(false);
    }

    /**
     * Test that non 'boolean' string values set false.
     */
    @Test
    void setStringNonNull_someString() throws SQLException {
        field.setString("xyz");

        verifySetBoolean(false);
    }

    /**
     * Test that 'T' sets to true (note: non-standard, subject to change)
     */
    @Test
    void setStringNonNull_T() throws SQLException {
        field.setString("T");

        verifySetBoolean(true);
    }

    /**
     * Test that 't' sets to true (note: non-standard, subject to change)
     */
    @Test
    void setStringNonNull_t() throws SQLException {
        field.setString("t");

        verifySetBoolean(true);
    }

    /**
     * Test that 'Y' sets to true (note: non-standard, subject to change)
     */
    @Test
    void setStringNonNull_Y() throws SQLException {
        field.setString("Y");

        verifySetBoolean(true);
    }

    /**
     * Test that 'y' sets to true (note: non-standard, subject to change)
     */
    @Test
    void setStringNonNull_y() throws SQLException {
        field.setString("y");

        verifySetBoolean(true);
    }

    /**
     * Test that '1' sets to true.
     */
    @Test
    void setStringNonNull_1() throws SQLException {
        field.setString("1");

        verifySetBoolean(true);
    }

    /**
     * Test that 'true' sets to true.
     */
    @Test
    void setStringNonNull_true() throws SQLException {
        field.setString("true");

        verifySetBoolean(true);
    }

    /**
     * Test that 'TRUE' sets to true.
     */
    @Test
    void setStringNonNull_TRUE() throws SQLException {
        field.setString("TRUE");

        verifySetBoolean(true);
    }

    @Test
    @Override
    void getDecimalNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        Decimal128 expectedValue = Decimal128.valueOf("1");
        assertEquals(expectedValue, field.getDecimal(), "Unexpected value for getDecimal");
    }

    @Test
    void getDecimal_null() throws SQLException {
        toReturnNullExpectations();

        assertNull(field.getDecimal(), "expected null for getDecimal");
    }

    @Test
    @Override
    void setDecimalNonNull() throws SQLException {
        field.setDecimal(Decimal128.valueOf("1"));

        verifySetBoolean(true);
    }

    @Test
    void setDecimalNull() throws SQLException {
        field.setDecimal(null);

        verifySetNull();
    }

    @Override
    Boolean getNonNullObject() {
        return Boolean.TRUE;
    }
}
