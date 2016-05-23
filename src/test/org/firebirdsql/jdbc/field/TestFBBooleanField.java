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
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

import static org.junit.Assert.*;

/**
 * Test for boolean fields. Note that boolean fields are only supported in Firebird 3.0 or higher.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class TestFBBooleanField extends BaseJUnit4TestFBField<FBBooleanField, Boolean> {

    @Before
    @Override
    public void setUp() throws Exception{
        super.setUp();

        rowDescriptorBuilder.setType(ISCConstants.SQL_BOOLEAN);
        fieldDescriptor = rowDescriptorBuilder.toFieldDescriptor();
        field = new FBBooleanField(fieldDescriptor, fieldData, Types.BOOLEAN);
    }

    @Test
    @Override
    public void getBigDecimalNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getBigDecimal",  BigDecimal.ONE, field.getBigDecimal());
    }

    @Test
    @Override
    public void getObject_BigDecimal() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getObject(BigDecimal.class)",
                BigDecimal.ONE, field.getObject(BigDecimal.class));
    }

    @Test
    public void getBigDecimalNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getBigDecimal",  BigDecimal.ZERO, field.getBigDecimal());
    }

    @Test
    @Override
    public void setBigDecimalNonNull() throws SQLException {
        setBooleanExpectations(false);

        field.setBigDecimal(BigDecimal.ZERO);
    }

    @Test
    @Override
    public void getBigDecimalIntNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getBigDecimal", BigDecimal.ONE, field.getBigDecimal(1));
    }

    @Test
    @Override
    public void getBooleanNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertTrue("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    @Override
    public void getObject_Boolean() throws SQLException {
        toReturnBooleanExpectations(true);

        assertTrue("Unexpected value for getObject(Boolean.class)", field.getObject(Boolean.class));
    }

    @Test
    public void getBooleanNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertFalse("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getBooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertFalse("Unexpected value for getBoolean", field.getBoolean());
    }

    @Test
    public void getObject_BooleanNull() throws SQLException {
        toReturnNullExpectations();

        assertNull("Expected null for getObject(Boolean.class)", field.getObject(Boolean.class));
    }

    @Test
    @Override
    public void setBoolean() throws SQLException {
        setBooleanExpectations(true);

        field.setBoolean(true);
    }

    @Test
    @Override
    public void getByteNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getByte", 0, field.getByte());
    }

    @Test
    @Override
    public void getObject_Byte() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getObject(Byte.class)", 1, (byte) field.getObject(Byte.class));
    }

    @Test
    @Override
    public void setByte() throws SQLException {
        setBooleanExpectations(true);

        field.setByte((byte) 127);
    }

    @Test
    @Override
    public void getDoubleNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getDouble", 1.0, field.getDouble(), 0.0);
    }

    @Test
    @Override
    public void getObject_Double() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getObject(Double.class)", 1.0, field.getObject(Double.class), 0.0);
    }

    @Test
    @Override
    public void setDouble() throws SQLException {
        setBooleanExpectations(false);

        field.setDouble(0.0);
    }

    @Test
    @Override
    public void getFloatNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getFloat", 0.0, field.getFloat(), 0.0);
    }

    @Test
    @Override
    public void getObject_Float() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getObject(Float.class)", 0.0, field.getObject(Float.class), 0.0);
    }

    @Test
    @Override
    public void setFloat() throws SQLException {
        setBooleanExpectations(true);

        field.setFloat(Float.MIN_VALUE);
    }

    @Test
    @Override
    public void getIntNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getInt", 1, field.getInt());
    }

    @Test
    @Override
    public void getObject_Integer() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getInt", 1, (int) field.getObject(Integer.class));
    }

    @Test
    @Override
    public void setInteger() throws SQLException {
        setBooleanExpectations(false);

        field.setInteger(0);
    }

    @Test
    @Override
    public void getLongNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getLong", 0L, field.getLong());
    }

    @Test
    @Override
    public void getObject_Long() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getLong", 0L, (long) field.getObject(Long.class));
    }

    @Test
    @Override
    public void setLong() throws SQLException {
        setBooleanExpectations(true);

        field.setLong(1L);
    }

    @Test
    @Override
    public void getObjectNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getObject", Boolean.TRUE, field.getObject());
    }

    @Test
    public void getObjectNonNull_false() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getObject", Boolean.FALSE, field.getObject());
    }

    @Test
    @Override
    public void setObjectNonNull() throws SQLException {
        setBooleanExpectations(false);

        field.setObject(Boolean.FALSE);
    }

    @Test
    @Override
    public void getShortNonNull() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getShort", 1, field.getShort());
    }

    @Test
    @Override
    public void getObject_Short() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getShort", 1, (short) field.getObject(Short.class));
    }

    @Test
    @Override
    public void setShort() throws SQLException {
        setBooleanExpectations(false);

        field.setShort((short) 0);
    }

    @Test
    @Override
    public void getStringNonNull() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getString", "false", field.getString());
    }

    @Test
    public void getStringNonNull_true() throws SQLException {
        toReturnBooleanExpectations(true);

        assertEquals("Unexpected value for getString", "true", field.getString());
    }

    @Test
    @Override
    public void getObject_String() throws SQLException {
        toReturnBooleanExpectations(false);

        assertEquals("Unexpected value for getString", "false", field.getObject(String.class));
    }

     @Test
     @Override
     public void setStringNonNull() throws SQLException {
        setBooleanExpectations(false);

        field.setString("false");
    }

    /**
     * Test that non 'boolean' string values set false.
     */
    @Test
    public void setStringNonNull_someString() throws SQLException {
        setBooleanExpectations(false);

        field.setString("xyz");
    }

    /**
     * Test that 'T' sets to true (note: non-standard, subject to change)
     */
    @Test
    public void setStringNonNull_T() throws SQLException {
        setBooleanExpectations(true);

        field.setString("T");
    }

    /**
     * Test that 't' sets to true (note: non-standard, subject to change)
     */
    @Test
    public void setStringNonNull_t() throws SQLException {
        setBooleanExpectations(true);

        field.setString("t");
    }

    /**
     * Test that 'Y' sets to true (note: non-standard, subject to change)
     */
    @Test
    public void setStringNonNull_Y() throws SQLException {
        setBooleanExpectations(true);

        field.setString("Y");
    }

    /**
     * Test that 'y' sets to true (note: non-standard, subject to change)
     */
    @Test
    public void setStringNonNull_y() throws SQLException {
        setBooleanExpectations(true);

        field.setString("y");
    }

    /**
     * Test that '1' sets to true.
     */
    @Test
    public void setStringNonNull_1() throws SQLException {
        setBooleanExpectations(true);

        field.setString("1");
    }

    /**
     * Test that 'true' sets to true.
     */
    @Test
    public void setStringNonNull_true() throws SQLException {
        setBooleanExpectations(true);

        field.setString("true");
    }

    /**
     * Test that 'TRUE' sets to true.
     */
    @Test
    public void setStringNonNull_TRUE() throws SQLException {
        setBooleanExpectations(true);

        field.setString("true");
    }

    @Override
    protected Boolean getNonNullObject() {
        return Boolean.TRUE;
    }
}
