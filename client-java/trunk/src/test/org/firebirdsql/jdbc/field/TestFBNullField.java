package org.firebirdsql.jdbc.field;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import org.firebirdsql.gds.XSQLVAR;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link FBNullField}
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
@RunWith(JMock.class)
public class TestFBNullField {
    Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    
    private FieldDataProvider fieldData;
    private FBNullField field;
    
    @Before
    public void setUp() throws Exception {
        fieldData = context.mock(FieldDataProvider.class);
        field = new FBNullField(new XSQLVAR(), fieldData, Types.NULL);
    }
    
    // TODO Investigate necessity to test getters, it looks like FBNullField is only used for parameters and never for ResultSet columns
    
    @Test
    public void setAsciiStreamNull() throws SQLException {
        setNullExpectations();
        
        field.setAsciiStream(null, 7);
    }
    
    @Test
    public void setAsciiStreanNonNull() throws SQLException {
        setNonNullExpectations();
        InputStream in = context.mock(InputStream.class);
        // TODO Read and/or close expectation?
        
        field.setAsciiStream(in, 15);
    }
    
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
    
    @Test
    public void setUnicodeStreamNull() throws SQLException {
        setNullExpectations();
        
        field.setUnicodeStream(null, 7);
    }
    
    @Test
    public void setUnicodeStreanNonNull() throws SQLException {
        setNonNullExpectations();
        InputStream in = context.mock(InputStream.class);
        // TODO Read and/or close expectation?
        
        field.setUnicodeStream(in, 15);
    }

    private void setNullExpectations() {
        context.checking(new Expectations() {{
            one(fieldData).setFieldData(null);
        }});
    }
    
    private void setNonNullExpectations() {
        context.checking(new Expectations() {{
            // NOTE: Implementation detail
            one(fieldData).setFieldData(new byte[0]);
        }});
    }
}
