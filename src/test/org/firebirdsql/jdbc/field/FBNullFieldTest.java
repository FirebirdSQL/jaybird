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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests for {@link FBNullField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class FBNullFieldTest {

    private static final DefaultDatatypeCoder defaultDatatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));
    
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        context.setThreadingPolicy(new Synchroniser());
    }

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private FieldDataProvider fieldData;
    private FBNullField field;
    
    @Before
    public void setUp() throws Exception {
        fieldData = context.mock(FieldDataProvider.class);
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(1, defaultDatatypeCoder).toFieldDescriptor();
        field = new FBNullField(fieldDescriptor, fieldData, Types.NULL);
    }
    
    // TODO Investigate necessity to test getters, it looks like FBNullField is only used for parameters and never for ResultSet columns
    
    @Test
    public void setBigDecimalNull() throws SQLException {
        setNullExpectations();
        
        field.setBigDecimal(null);
    }
    
    @Test
    public void setBigDecimalNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setBigDecimal(BigDecimal.TEN);
    }
    
    @Test
    public void setBinaryStreamNull() throws SQLException {
        setNullExpectations();
        
        field.setBinaryStream(null, 7);
    }
    
    @Test
    public void setBinaryStreanNonNull() throws SQLException {
        setNonNullExpectations();
        InputStream in = context.mock(InputStream.class);
        // TODO Read and/or close expectation?
        
        field.setBinaryStream(in, 15);
    }
    
    @Test
    public void setBoolean() throws SQLException {
        setNonNullExpectations();
        
        field.setBoolean(false);
    }
    
    @Test
    public void setByte() throws SQLException {
        setNonNullExpectations();
        
        field.setByte((byte) 6);
    }
    
    @Test
    public void setBytesNull() throws SQLException {
        setNullExpectations();
        
        field.setBytes(null);
    }
    
    @Test
    public void setBytesNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setBytes(new byte[] { 3, 4, 5 });
    }
    
    @Test
    public void setCharacterStreamNull() throws SQLException {
        setNullExpectations();
        
        field.setCharacterStream(null, 7);
    }
    
    @Test
    public void setCharacterStreamNonNull() throws SQLException {
        setNonNullExpectations();
        Reader in = context.mock(Reader.class);
        // TODO Read and/or close expectation?
        
        field.setCharacterStream(in, 15);
    }
    
    @Test
    public void setDateNull() throws SQLException {
        setNullExpectations();
        
        field.setDate(null);
    }
    
    @Test
    public void setDateNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
    }
    
    @Test
    public void setDateWithCalendarNull() throws SQLException {
        setNullExpectations();
        
        field.setDate(null, Calendar.getInstance());
    }
    
    @Test
    public void setDateWithCalendarNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()),
                Calendar.getInstance());
    }
    
    @Test
    public void setDouble() throws SQLException {
        setNonNullExpectations();
        
        field.setDouble(18.3);
    }
    
    @Test
    public void setFloat() throws SQLException {
        setNonNullExpectations();
        
        field.setFloat(18.3F);
    }
    
    @Test
    public void setInteger() throws SQLException {
        setNonNullExpectations();
        
        field.setInteger(513);
    }
    
    @Test
    public void setLong() throws SQLException {
        setNonNullExpectations();
        
        field.setLong(759745987234958L);
    }
    
    @Test
    public void setNull() throws SQLException {
        setNullExpectations();
        
        field.setNull();
    }
    
    @Test
    public void setObjectNull() throws SQLException {
        setNullExpectations();
        
        field.setObject(null);
    }
    
    @Test
    public void setObjectNoNull() throws SQLException {
        setNonNullExpectations();
        
        field.setObject(new Object());
    }
    
    @Test
    public void setShort() throws SQLException {
        setNonNullExpectations();
        
        field.setShort((short) 132);
    }
    
    @Test
    public void setStringNull() throws SQLException {
        setNullExpectations();
        
        field.setString(null);
    }
    
    @Test
    public void setStringNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setString("test value");
    }
    
    @Test
    public void setTimeNull() throws SQLException {
        setNullExpectations();
        
        field.setTime(null);
    }
    
    @Test
    public void setTimeNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setTime(new java.sql.Time(Calendar.getInstance().getTimeInMillis()));
    }
    
    @Test
    public void setTimeWithCalendarNull() throws SQLException {
        setNullExpectations();
        
        field.setTime(null, Calendar.getInstance());
    }
    
    @Test
    public void setTimeWithCalendarNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setTime(new java.sql.Time(Calendar.getInstance().getTimeInMillis()),
                Calendar.getInstance());
    }
    
    @Test
    public void setTimeStampNull() throws SQLException {
        setNullExpectations();
        
        field.setTimestamp(null);
    }
    
    @Test
    public void setTimeStampNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
    }
    
    @Test
    public void setTimeStampWithCalendarNull() throws SQLException {
        setNullExpectations();
        
        field.setTimestamp(null, Calendar.getInstance());
    }
    
    @Test
    public void setTimeStampWithCalendarNonNull() throws SQLException {
        setNonNullExpectations();
        
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()),
                Calendar.getInstance());
    }

    private void setNullExpectations() {
        context.checking(new Expectations() {{
            oneOf(fieldData).setFieldData(null);
        }});
    }
    
    private void setNonNullExpectations() {
        context.checking(new Expectations() {{
            // NOTE: Implementation detail
            oneOf(fieldData).setFieldData(new byte[0]);
        }});
    }
}
