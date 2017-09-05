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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 * Tests for {@link EncodingSpecificDatatypeCoder}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 */
public class EncodingSpecificDatatypeCoderTest {

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    {
        context.setImposteriser(ClassImposteriser.INSTANCE);
    }

    @Mock private EncodingDefinition encodingDefinition;
    @Mock private Encoding encoding;
    @Mock private DatatypeCoder parentCoder;
    private EncodingSpecificDatatypeCoder coder;

    @Before
    public void setup() {
        context.checking(new Expectations() {{
            allowing(encodingDefinition).getEncoding(); will(returnValue(encoding));
        }});
        coder = new EncodingSpecificDatatypeCoder(parentCoder, encodingDefinition);
    }

    @Test
    public void encodeString_delegatesToEncoding() throws Exception {
        final String inputValue = "result value";
        final byte[] resultValue = { 1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(encoding).encodeToCharset(inputValue); will(returnValue(resultValue));
        }});

        byte[] result = coder.encodeString(inputValue);

        assertArrayEquals(resultValue, result);
    }

    @Test
    public void createWriter_delegatesToEncoding() throws Exception {
        final OutputStream outputStream = context.mock(OutputStream.class);
        final Writer writer = context.mock(Writer.class);

        context.checking(new Expectations() {{
            oneOf(encoding).createWriter(outputStream); will(returnValue(writer));
        }});

        Writer result = coder.createWriter(outputStream);

        assertSame(writer, result);
    }

    @Test
    public void decodeString_delegatesToEncoding() throws Exception {
        final byte[] inputValue = { 1, 2, 3, 4};
        final String resultValue = "result value";

        context.checking(new Expectations() {{
            oneOf(encoding).decodeFromCharset(inputValue); will(returnValue(resultValue));
        }});

        String result = coder.decodeString(inputValue);

        assertEquals(resultValue, result);
    }

    @Test
    public void createReader_delegatesToEncoding() throws Exception {
        final InputStream inputStream = context.mock(InputStream.class);
        final Reader reader = context.mock(Reader.class);

        context.checking(new Expectations() {{
            oneOf(encoding).createReader(inputStream); will(returnValue(reader));
        }});

        Reader result = coder.createReader(inputStream);

        assertSame(reader, result);
    }

    @Test
    public void withEncodingDefinition_sameEncodingDefinitionReturnsCurrentInstance() throws Exception {
        DatatypeCoder result = coder.forEncodingDefinition(encodingDefinition);

        assertSame(coder, result);
    }

    @Test
    public void withEncodingDefinition_differentEncodingDefinitionDelegatesToParent() throws Exception {
        final DatatypeCoder newCoder = context.mock(DatatypeCoder.class, "newCoder");
        final EncodingDefinition otherEncodingDefinition = context.mock(EncodingDefinition.class, "otherEncodingDefinition");

        context.checking(new Expectations() {{
            oneOf(parentCoder).forEncodingDefinition(otherEncodingDefinition); will(returnValue(newCoder));
        }});

        DatatypeCoder result = coder.forEncodingDefinition(otherEncodingDefinition);

        assertSame(newCoder, result);
    }

    @Test
    public void getEncodingDefinition() throws Exception {
        assertSame(encodingDefinition, coder.getEncodingDefinition());
    }

    @Test
    public void unwrap() throws Exception {
        assertSame(parentCoder, coder.unwrap());
    }

//    TODO ?
//    @Test
//    public void equals() throws Exception {
//    }
//
//    @Test
//    public void testHashCode() throws Exception {
//    }

    @Test
    public void encodeShort_short() throws Exception {
        final short value = 23;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeShort(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeShort(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeShort_int() throws Exception {
        final int value = 23;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeShort(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeShort(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void decodeShort() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final short response = 23;

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeShort(value); will(returnValue(response));
        }});

        short result = coder.decodeShort(value);

        assertEquals(response, result);
    }

    @Test
    public void encodeInt() throws Exception {
        final int value = 23;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeInt(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeInt(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void decodeInt() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final int response = 23;

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeInt(value); will(returnValue(response));
        }});

        int result = coder.decodeInt(value);

        assertEquals(response, result);
    }

    @Test
    public void encodeLong() throws Exception {
        final long value = 23;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeLong(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeLong(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void decodeLong() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final long response = 23;

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeLong(value); will(returnValue(response));
        }});

        long result = coder.decodeLong(value);

        assertEquals(response, result);
    }

    @Test
    public void encodeFloat() throws Exception {
        final float value = 23.1f;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeFloat(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeFloat(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void decodeFloat() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final float response = 23.1f;

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeFloat(value); will(returnValue(response));
        }});

        float result = coder.decodeFloat(value);

        assertEquals(response, result, 0.0);
    }

    @Test
    public void encodeDouble() throws Exception {
        final double value = 23.1;
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeDouble(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeDouble(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void decodeDouble() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final double response = 23.1;

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeDouble(value); will(returnValue(response));
        }});

        double result = coder.decodeDouble(value);

        assertEquals(response, result, 0.0);
    }

    @Test
    public void encodeTimestamp_calendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimestamp(value, calendar); will(returnValue(response));
        }});

        Timestamp result = coder.encodeTimestamp(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void encodeTimestamp_calendar_boolean() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response1 = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        final Timestamp response2 = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimestamp(value, calendar, false); will(returnValue(response1));
            oneOf(parentCoder).encodeTimestamp(value, calendar, true); will(returnValue(response2));
        }});

        Timestamp result1 = coder.encodeTimestamp(value, calendar, false);
        Timestamp result2 = coder.encodeTimestamp(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    public void encodeTimestamp() throws Exception {
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimestamp(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeTimestamp(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeTimestampRaw() throws Exception {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimestampRaw(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeTimestampRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeTimestampCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimestampCalendar(value, calendar); will(returnValue(response));
        }});

        byte[] result = coder.encodeTimestampCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeTimestamp_calendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimestamp(value, calendar); will(returnValue(response));
        }});

        Timestamp result = coder.decodeTimestamp(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeTimestamp_calendar_boolean() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response1 = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        final Timestamp response2 = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimestamp(value, calendar, false); will(returnValue(response1));
            oneOf(parentCoder).decodeTimestamp(value, calendar, true); will(returnValue(response2));
        }});

        Timestamp result1 = coder.decodeTimestamp(value, calendar, false);
        Timestamp result2 = coder.decodeTimestamp(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    public void decodeTimestamp() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final Timestamp response = new Timestamp(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimestamp(value); will(returnValue(response));
        }});

        Timestamp result = coder.decodeTimestamp(value);

        assertSame(response, result);
    }

    @Test
    public void decodeTimestampRaw() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimestampRaw(value); will(returnValue(response));
        }});

        DatatypeCoder.RawDateTimeStruct result = coder.decodeTimestampRaw(value);

        assertSame(response, result);
    }

    @Test
    public void decodeTimestampCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Timestamp response = new Timestamp(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimestampCalendar(value, calendar); will(returnValue(response));
        }});

        Timestamp result = coder.decodeTimestampCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void encodeTime_calendar_boolean() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final Time response1 = new Time(System.currentTimeMillis() - 60 * 60 * 1000);
        final Time response2 = new Time(System.currentTimeMillis() + 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTime(value, calendar, false); will(returnValue(response1));
            oneOf(parentCoder).encodeTime(value, calendar, true); will(returnValue(response2));
        }});

        Time result1 = coder.encodeTime(value, calendar, false);
        Time result2 = coder.encodeTime(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    public void encodeTime() throws Exception {
        final Time value = new Time(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTime(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeTime(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeTimeRaw() throws Exception {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimeRaw(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeTimeRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeTimeCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeTimeCalendar(value, calendar); will(returnValue(response));
        }});

        byte[] result = coder.encodeTimeCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeTime_calendar_boolean() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final Time response1 = new Time(System.currentTimeMillis() - 60 * 60 * 1000);
        final Time response2 = new Time(System.currentTimeMillis() + 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTime(value, calendar, false); will(returnValue(response1));
            oneOf(parentCoder).decodeTime(value, calendar, true); will(returnValue(response2));
        }});

        Time result1 = coder.decodeTime(value, calendar, false);
        Time result2 = coder.decodeTime(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    public void decodeTime() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final Time response = new Time(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTime(value); will(returnValue(response));
        }});

        Time result = coder.decodeTime(value);

        assertSame(response, result);
    }

    @Test
    public void decodeTimeRaw() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimeRaw(value); will(returnValue(response));
        }});

        DatatypeCoder.RawDateTimeStruct result = coder.decodeTimeRaw(value);

        assertSame(response, result);
    }

    @Test
    public void decodeTimeCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Time response = new Time(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeTimeCalendar(value, calendar); will(returnValue(response));
        }});

        Time result = coder.decodeTimeCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void encodeDate_calendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final Date response = new Date(System.currentTimeMillis() - 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeDate(value, calendar); will(returnValue(response));
        }});

        Date result = coder.encodeDate(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void encodeDate() throws Exception {
        final Date value = new Date(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeDate(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeDate(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeDateRaw() throws Exception {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeDateRaw(value); will(returnValue(response));
        }});

        byte[] result = coder.encodeDateRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    public void encodeDateCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeDateCalendar(value, calendar); will(returnValue(response));
        }});

        byte[] result = coder.encodeDateCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeDate_calendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final Date response = new Date(System.currentTimeMillis() - 60 * 60 * 1000);

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeDate(value, calendar); will(returnValue(response));
        }});

        Date result = coder.decodeDate(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeDate() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final Date response = new Date(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeDate(value); will(returnValue(response));
        }});

        Date result = coder.decodeDate(value);

        assertSame(response, result);
    }

    @Test
    public void decodeDateRaw() throws Exception {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeDateRaw(value); will(returnValue(response));
        }});

        DatatypeCoder.RawDateTimeStruct result = coder.decodeDateRaw(value);

        assertSame(response, result);
    }

    @Test
    public void decodeDateCalendar() throws Exception {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Date response = new Date(System.currentTimeMillis());

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeDateCalendar(value, calendar); will(returnValue(response));
        }});

        Date result = coder.decodeDateCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeBoolean() throws Exception {
        final byte[] valueTrue = {1, 0};
        final byte[] valueFalse = {0, 0};

        context.checking(new Expectations() {{
            oneOf(parentCoder).decodeBoolean(valueTrue); will(returnValue(true));
            oneOf(parentCoder).decodeBoolean(valueFalse); will(returnValue(false));
        }});

        assertTrue(coder.decodeBoolean(valueTrue));
        assertFalse(coder.decodeBoolean(valueFalse));
    }

    @Test
    public void encodeBoolean() throws Exception {
        final byte[] resultTrue = {1, 0};
        final byte[] resultFalse = {0, 0};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeBoolean(true); will(returnValue(resultTrue));
            oneOf(parentCoder).encodeBoolean(false); will(returnValue(resultFalse));
        }});

        assertSame(resultTrue, coder.encodeBoolean(true));
        assertSame(resultFalse, coder.encodeBoolean(false));
    }

    @Test
    public void encodeLocalTime() throws Exception {
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeLocalTime(1, 2, 3, 4); will(returnValue(response));
        }});

        assertSame(response, coder.encodeLocalTime(1, 2, 3, 4));
    }

    @Test
    public void encodeLocalDate() throws Exception {
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeLocalDate(1, 2, 3); will(returnValue(response));
        }});

        assertSame(response, coder.encodeLocalDate(1, 2, 3));
    }

    @Test
    public void encodeLocalDateTime() throws Exception {
        final byte[] response = {1, 2, 3, 4};

        context.checking(new Expectations() {{
            oneOf(parentCoder).encodeLocalDateTime(1, 2, 3, 4, 5, 6, 7); will(returnValue(response));
        }});

        assertSame(response, coder.encodeLocalDateTime(1, 2, 3, 4, 5, 6, 7));
    }

    @Test
    public void getEncodingFactory() throws Exception {
        final IEncodingFactory response = context.mock(IEncodingFactory.class);

        context.checking(new Expectations() {{
            oneOf(parentCoder).getEncodingFactory(); will(returnValue(response));
        }});

        assertSame(response, coder.getEncodingFactory());
    }

}