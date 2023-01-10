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

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.ng.DefaultDatatypeCoder;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptorBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link FBNullField}
 * 
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class FBNullFieldTest {

    private static final DefaultDatatypeCoder defaultDatatypeCoder =
            DefaultDatatypeCoder.forEncodingFactory(EncodingFactory.createInstance(StandardCharsets.UTF_8));

    @Mock
    private FieldDataProvider fieldData;
    private FBNullField field;
    
    @BeforeEach
    void setUp() throws Exception {
        FieldDescriptor fieldDescriptor = new RowDescriptorBuilder(1, defaultDatatypeCoder).toFieldDescriptor();
        field = new FBNullField(fieldDescriptor, fieldData, Types.NULL);
    }
    
    // TODO Investigate necessity to test getters, it looks like FBNullField is only used for parameters and never for ResultSet columns
    
    @Test
    void setBigDecimalNull() throws SQLException {
        field.setBigDecimal(null);

        verifySetNull();
    }
    
    @Test
    void setBigDecimalNonNull() throws SQLException {
        field.setBigDecimal(BigDecimal.TEN);

        verifySetNonNull();
    }
    
    @Test
    void setBinaryStreamNull() throws SQLException {
        field.setBinaryStream(null, 7);

        verifySetNull();
    }
    
    @Test
    void setBinaryStreanNonNull(@Mock InputStream in) throws SQLException {
        // TODO Read and/or close expectation?

        field.setBinaryStream(in, 15);
        
        verifySetNonNull();
    }
    
    @Test
    void setBoolean() throws SQLException {
        field.setBoolean(false);

        verifySetNonNull();
    }
    
    @Test
    void setByte() throws SQLException {
        field.setByte((byte) 6);

        verifySetNonNull();
    }
    
    @Test
    void setBytesNull() throws SQLException {
        field.setBytes(null);

        verifySetNull();
    }
    
    @Test
    void setBytesNonNull() throws SQLException {
        field.setBytes(new byte[] { 3, 4, 5 });

        verifySetNonNull();
    }
    
    @Test
    void setCharacterStreamNull() throws SQLException {
        field.setCharacterStream(null, 7);

        verifySetNull();
    }
    
    @Test
    void setCharacterStreamNonNull() throws SQLException {
        Reader in = new StringReader("test");
        // TODO Read and/or close expectation?

        field.setCharacterStream(in, 15);
        
        verifySetNonNull();
    }
    
    @Test
    void setDateNull() throws SQLException {
        field.setDate(null);

        verifySetNull();
    }
    
    @Test
    void setDateNonNull() throws SQLException {
        field.setDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()));

        verifySetNonNull();
    }
    
    @Test
    void setDateWithCalendarNull() throws SQLException {
        field.setDate(null, Calendar.getInstance());

        verifySetNull();
    }
    
    @Test
    void setDateWithCalendarNonNull() throws SQLException {
        field.setDate(new java.sql.Date(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance());

        verifySetNonNull();
    }
    
    @Test
    void setDouble() throws SQLException {
        field.setDouble(18.3);

        verifySetNonNull();
    }
    
    @Test
    void setFloat() throws SQLException {
        field.setFloat(18.3F);

        verifySetNonNull();
    }
    
    @Test
    void setInteger() throws SQLException {
        field.setInteger(513);

        verifySetNonNull();
    }
    
    @Test
    void setLong() throws SQLException {
        field.setLong(759745987234958L);

        verifySetNonNull();
    }
    
    @Test
    void setNull() {
        field.setNull();

        verifySetNull();
    }
    
    @Test
    void setObjectNull() throws SQLException {
        field.setObject(null);

        verifySetNull();
    }
    
    @Test
    void setObjectNoNull() throws SQLException {
        field.setObject(new Object());

        verifySetNonNull();
    }
    
    @Test
    void setShort() throws SQLException {
        field.setShort((short) 132);

        verifySetNonNull();
    }
    
    @Test
    void setStringNull() throws SQLException {
        field.setString(null);

        verifySetNull();
    }
    
    @Test
    void setStringNonNull() throws SQLException {
        field.setString("test value");

        verifySetNonNull();
    }
    
    @Test
    void setTimeNull() throws SQLException {
        field.setTime(null);

        verifySetNull();
    }
    
    @Test
    void setTimeNonNull() throws SQLException {
        field.setTime(new java.sql.Time(Calendar.getInstance().getTimeInMillis()));

        verifySetNonNull();
    }
    
    @Test
    void setTimeWithCalendarNull() throws SQLException {
        field.setTime(null, Calendar.getInstance());

        verifySetNull();
    }
    
    @Test
    void setTimeWithCalendarNonNull() throws SQLException {
        field.setTime(new java.sql.Time(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance());

        verifySetNonNull();
    }
    
    @Test
    void setTimeStampNull() throws SQLException {
        field.setTimestamp(null);

        verifySetNull();
    }
    
    @Test
    void setTimeStampNonNull() throws SQLException {
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));

        verifySetNonNull();
    }
    
    @Test
    void setTimeStampWithCalendarNull() throws SQLException {
        field.setTimestamp(null, Calendar.getInstance());

        verifySetNull();
    }
    
    @Test
    void setTimeStampWithCalendarNonNull() throws SQLException {
        field.setTimestamp(new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()), Calendar.getInstance());

        verifySetNonNull();
    }

    private void verifySetNull() {
        verify(fieldData).setFieldData(null);
    }
    
    private void verifySetNonNull() {
        // NOTE: Implementation detail
        verify(fieldData).setFieldData(new byte[0]);
    }
}
