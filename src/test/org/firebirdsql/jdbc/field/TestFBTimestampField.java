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
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * Describe class <code>TestFBTimestampField</code> here.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @version 1.0
 */
public class TestFBTimestampField extends BaseTestFBField {

    @Before
    public void setUp() throws SQLException {
        RowDescriptor rowDescriptor = new RowDescriptorBuilder(1, datatypeCoder)
                .setFieldIndex(0)
                .setType(ISCConstants.SQL_TIMESTAMP)
                .addField()
                .toRowDescriptor();
        field = FBField.createField(rowDescriptor.getFieldDescriptor(0), createDataProvider(rowDescriptor), null, false);
    }

    @Test
    public void testShort() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testShort();
    }

    @Test
    public void testDouble() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testDouble();
    }

    @Test
    public void testLong() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testLong();
    }

    @Test
    public void testByte() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testByte();
    }

    @Test
    public void testBoolean() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testBoolean();
    }

    @Test
    public void testBinaryStream() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testBinaryStream();
    }

    @Test
    public void testFloat() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testFloat();
    }

    @Test
    public void testBytes() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testBytes();
    }

    @Test
    public void testAsciiStream() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testAsciiStream();
    }

    @Test
    public void testInteger() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        super.testInteger();
    }

    @Test
    public void testBigDecimal() throws SQLException {
        expectedException.expect(TypeConversionException.class);
        field.setBigDecimal(new BigDecimal(TEST_DOUBLE));
    }

    //--- real test methods

    @Test
    public void testString() throws SQLException {
        field.setString(TEST_TIMESTAMP.toString());
        assertEquals("String value test failure:", TEST_TIMESTAMP, field.getTimestamp());
    }

    @Test
    public void testObject() throws SQLException {
        field.setObject(TEST_TIMESTAMP);
        assertEquals("Object value test failure:", TEST_TIMESTAMP, field.getTimestamp());
    }
}
