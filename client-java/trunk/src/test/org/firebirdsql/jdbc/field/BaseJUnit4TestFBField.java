/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc.field;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import org.firebirdsql.gds.XSQLVAR;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Abstract base class for testing {@link FBField} implementations.
 * <p>
 * Basic naming conventions for tests:
 * <ul>
 * <li>methods returning objects or primitives : get&lt;type&gt;NonNull</li>
 * <li>methods receiving objects : set&lt;type&gt;NonNull</li>
 * <li>methods receiving primitive types : set&lt;type&gt;</li>
 * </ul>
 * </p>
 * <p>
 * The basic idea is that this class tests for the type conversion exceptions thrown by
 * all methods of FBField, while extending tests will override (and add) tests for the
 * specific implementation.
 * </p>
 * 
 * @param <T> FBField implementation under test
 * @param <O> Object type of FBField implementation under test
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(JMock.class)
public abstract class BaseJUnit4TestFBField<T extends FBField, O> {
    protected static final String ALIAS_VALUE = "aliasvalue";
    protected static final String NAME_VALUE = "namevalue";
    protected static final String RELATION_NAME_VALUE = "relationnamevalue";

    protected Mockery context = new JUnit4Mockery();
    
    // TODO Convert exception expectation to @Rule (needs to wait until JMock 2.6 is released)
    
    protected FieldDataProvider fieldData;
    protected XSQLVAR xsqlvar;
    protected T field;
    
    @Before
    public void setUp() throws Exception {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        fieldData = context.mock(FieldDataProvider.class);
        xsqlvar = new XSQLVAR();
        xsqlvar.aliasname = ALIAS_VALUE;
        xsqlvar.sqlname = NAME_VALUE;
        xsqlvar.relname = RELATION_NAME_VALUE;
    }
    
    @Test
    public void getAlias() throws SQLException {
        assertEquals("Unexpected value for getAlias()", ALIAS_VALUE, field.getAlias());
    }
    
    @Test(expected = FBDriverNotCapableException.class)
    public void getArrayNonNull() throws SQLException {
        field.getArray();
    }
    
    @Test(expected = TypeConversionException.class)
    public void getAsciiStreamNonNull() throws SQLException {
        field.getAsciiStream();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setAsciiStreamNonNull() throws SQLException {
        field.setAsciiStream(context.mock(InputStream.class), 100);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBigDecimalNonNull() throws SQLException {
        field.getBigDecimal();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setBigDecimalNonNull() throws SQLException {
        field.setBigDecimal(BigDecimal.ONE);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBigDecimalIntNonNull() throws SQLException {
        field.getBigDecimal(1);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBinaryStreamNonNull() throws SQLException {
        field.getBinaryStream();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setBinaryStreamNonNull() throws SQLException {
        field.setBinaryStream(context.mock(InputStream.class), 100);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBlobNonNull() throws SQLException {
        field.getBlob();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setBlobNonNull() throws SQLException {
        field.setBlob(context.mock(FBBlob.class));
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBooleanNonNull() throws SQLException {
        field.getBoolean();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setBoolean() throws SQLException {
        field.setBoolean(true);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getByteNonNull() throws SQLException {
        field.getByte();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setByte() throws SQLException {
        field.setByte((byte)1);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getBytesNonNull() throws SQLException {
        field.getBytes();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setBytesNonNull() throws SQLException {
        field.setBytes(new byte[] { 1, 2 });
    }
    
    @Test(expected = TypeConversionException.class)
    public void getCharacterStreamNonNull() throws SQLException {
        field.getCharacterStream();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setCharacterStreamNonNull() throws SQLException {
        field.setCharacterStream(context.mock(Reader.class), 100);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getClobNonNull() throws SQLException {
        field.getClob();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setClobNonNull() throws SQLException {
        field.setClob(context.mock(FBClob.class));
    }
    
    @Test(expected = TypeConversionException.class)
    public void getDateNonNull() throws SQLException {
        field.getDate();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setDateNonNull() throws SQLException {
        field.setDate(java.sql.Date.valueOf("2012-03-11"));
    }
    
    @Test(expected = TypeConversionException.class)
    public void getDateCalendarNonNull() throws SQLException {
        field.getDate(Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void setDateCalendarNonNull() throws SQLException {
        field.setDate(java.sql.Date.valueOf("2012-03-11"), Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getDoubleNonNull() throws SQLException {
        field.getDouble();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setDouble() throws SQLException {
        field.setDouble(1.0);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getFloatNonNull() throws SQLException {
        field.getFloat();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setFloat() throws SQLException {
        field.setFloat(1.0f);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getIntNonNull() throws SQLException {
        field.getInt();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setInteger() throws SQLException {
        field.setInteger(1);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getLongNonNull() throws SQLException {
        field.getLong();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setLong() throws SQLException {
        field.setLong(1);
    }
    
    @Test
    public void setNull() throws SQLException {
        setNullExpectations();
        
        field.setNull();
    }
    
    @Test
    public void getName() throws SQLException {
        assertEquals("Unexpected value for getName()", NAME_VALUE, field.getName());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getObjectNonNull() throws SQLException {
        field.getObject();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setObjectNonNull() throws SQLException {
        field.setObject(getNonNullObject());
    }
    
    @Test(expected = TypeConversionException.class)
    public void setObjectUnsupportedType() throws SQLException {
        field.setObject(new Object());
    }
    
    @Test
    public void setObjectNull() throws SQLException {
        setNullExpectations();
        
        field.setObject(null);
    }
    
    @Test(expected = FBDriverNotCapableException.class)
    public void getObjectMapNonNull() throws SQLException {
        field.getObject(new HashMap<String,Class<?>>());
    }
    
    @Test(expected = FBDriverNotCapableException.class)
    public void getRefNonNull() throws SQLException {
        field.getRef();
    }
    
    @Test
    public void getRelationName() throws SQLException {
        assertEquals("Unexpected value for getRelationName()", RELATION_NAME_VALUE, field.getRelationName());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getShortNonNull() throws SQLException {
        field.getShort();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setShort() throws SQLException {
        field.setShort((short)1);
    }
    
    @Test(expected = TypeConversionException.class)
    public void getStringNonNull() throws SQLException {
        field.getString();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setStringNonNull() throws SQLException {
        field.setString("");
    }
    
    @Test(expected = TypeConversionException.class)
    public void getTimeNonNull() throws SQLException {
        field.getTime();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setTimeNonNull() throws SQLException {
        field.setTime(java.sql.Time.valueOf("01:00:01"));
    }
    
    @Test(expected = TypeConversionException.class)
    public void getTimeCalendarNonNull() throws SQLException {
        field.getTime(Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void setTimeCalendarNonNull() throws SQLException {
        field.setTime(java.sql.Time.valueOf("01:00:01"), Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getTimestampNonNull() throws SQLException {
        field.getTimestamp();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setTimestampNonNull() throws SQLException {
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
    }
    
    @Test(expected = TypeConversionException.class)
    public void getTimestampCalendarNonNull() throws SQLException {
        field.getTimestamp(Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void setTimestampCalendarNonNull() throws SQLException {
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance());
    }
    
    @Test(expected = TypeConversionException.class)
    public void getUnicodeStreamNonNull() throws SQLException {
        field.getUnicodeStream();
    }
    
    @Test(expected = TypeConversionException.class)
    public void setUnicodeStreamNonNull() throws SQLException {
        field.setUnicodeStream(context.mock(InputStream.class), 100);
    }
    
    @Test
    public void isNull_nullValue() throws SQLException {
        toReturnNullExpectations();
        
        assertTrue("Expected isNull() to return true for null-field", field.isNull());
    }
    
    @Test
    public void isNull_nonNullValue() throws SQLException {
        // TODO Check if this is sufficient, otherwise we may need to add an abstract toReturnNonNull
        context.checking(new Expectations() {{
            atLeast(1).of(fieldData).getFieldData(); will(returnValue(new byte[0]));
        }});
        
        assertFalse("Expected isNull() to return false for non-null-field", field.isNull());
    }

    /**
     * Expectations for setting field to null
     */
    protected void setNullExpectations() {
        context.checking(new Expectations() {{
            one(fieldData).setFieldData(null);
        }});
    }

    /**
     * Expectations to return null from fieldData.
     */
    protected void toReturnNullExpectations() {
        context.checking(new Expectations() {{
                atLeast(1).of(fieldData).getFieldData(); will(returnValue(null));
        }});
    }
    
    /**
     * @return A non-null object of the right type for the field under test
     */
    protected abstract O getNonNullObject();
}
