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
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EncodingSpecificDatatypeCoder}.
 *
 * @author Mark Rotteveel
 */
@ExtendWith(MockitoExtension.class)
class EncodingSpecificDatatypeCoderTest {

    @Mock private EncodingDefinition encodingDefinition;
    @Mock private Encoding encoding;
    @Mock private DatatypeCoder parentCoder;
    private EncodingSpecificDatatypeCoder coder;

    @BeforeEach
    void setup() {
        when(encodingDefinition.getEncoding()).thenReturn(encoding);
        coder = new EncodingSpecificDatatypeCoder(parentCoder, encodingDefinition);
    }

    @Test
    void encodeString_delegatesToEncoding() {
        final String inputValue = "result value";
        final byte[] resultValue = { 1, 2, 3, 4};
        when(encoding.encodeToCharset(inputValue)).thenReturn(resultValue);

        byte[] result = coder.encodeString(inputValue);

        assertArrayEquals(resultValue, result);
    }

    @Test
    void createWriter_delegatesToEncoding(@Mock OutputStream outputStream) {
        final Writer writer = new StringWriter();
        when(encoding.createWriter(outputStream)).thenReturn(writer);

        Writer result = coder.createWriter(outputStream);

        assertSame(writer, result);
    }

    @Test
    void decodeString_delegatesToEncoding() {
        final byte[] inputValue = { 1, 2, 3, 4};
        final String resultValue = "result value";
        when(encoding.decodeFromCharset(inputValue)).thenReturn(resultValue);

        String result = coder.decodeString(inputValue);

        assertEquals(resultValue, result);
    }

    @Test
    void createReader_delegatesToEncoding(@Mock InputStream inputStream) {
        final Reader reader = new StringReader("test");
        when(encoding.createReader(inputStream)).thenReturn(reader);

        Reader result = coder.createReader(inputStream);

        assertSame(reader, result);
    }

    @Test
    void withEncodingDefinition_sameEncodingDefinitionReturnsCurrentInstance() {
        DatatypeCoder result = coder.forEncodingDefinition(encodingDefinition);

        assertSame(coder, result);
    }

    @Test
    void withEncodingDefinition_differentEncodingDefinitionDelegatesToParent(
            @Mock DatatypeCoder newCoder, @Mock EncodingDefinition otherEncodingDefinition) {
        when(parentCoder.forEncodingDefinition(otherEncodingDefinition)).thenReturn(newCoder);

        DatatypeCoder result = coder.forEncodingDefinition(otherEncodingDefinition);

        assertSame(newCoder, result);
    }

    @Test
    void getEncodingDefinition() {
        assertSame(encodingDefinition, coder.getEncodingDefinition());
    }

    @Test
    void unwrap() {
        assertSame(parentCoder, coder.unwrap());
    }

    @Test
    void encodeShort_short() {
        final short value = 23;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeShort(value)).thenReturn(response);

        byte[] result = coder.encodeShort(value);

        assertArrayEquals(response, result);
    }

    @Test
    void encodeShort_int() {
        final int value = 23;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeShort(value)).thenReturn(response);

        byte[] result = coder.encodeShort(value);

        assertArrayEquals(response, result);
    }

    @Test
    void decodeShort() {
        final byte[] value = {1, 2, 3, 4};
        final short response = 23;
        when(parentCoder.decodeShort(value)).thenReturn(response);

        short result = coder.decodeShort(value);

        assertEquals(response, result);
    }

    @Test
    void encodeInt() {
        final int value = 23;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeInt(value)).thenReturn(response);

        byte[] result = coder.encodeInt(value);

        assertArrayEquals(response, result);
    }

    @Test
    void decodeInt() {
        final byte[] value = {1, 2, 3, 4};
        final int response = 23;
        when(parentCoder.decodeInt(value)).thenReturn(response);

        int result = coder.decodeInt(value);

        assertEquals(response, result);
    }

    @Test
    void encodeLong() {
        final long value = 23;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeLong(value)).thenReturn(response);

        byte[] result = coder.encodeLong(value);

        assertArrayEquals(response, result);
    }

    @Test
    void decodeLong() {
        final byte[] value = {1, 2, 3, 4};
        final long response = 23;
        when(parentCoder.decodeLong(value)).thenReturn(response);

        long result = coder.decodeLong(value);

        assertEquals(response, result);
    }

    @Test
    public void encodeFloat() {
        final float value = 23.1f;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeFloat(value)).thenReturn(response);

        byte[] result = coder.encodeFloat(value);

        assertArrayEquals(response, result);
    }

    @Test
    void decodeFloat() {
        final byte[] value = {1, 2, 3, 4};
        final float response = 23.1f;
        when(parentCoder.decodeFloat(value)).thenReturn(response);

        float result = coder.decodeFloat(value);

        assertEquals(response, result, 0.0);
    }

    @Test
    void encodeDouble() {
        final double value = 23.1;
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeDouble(value)).thenReturn(response);

        byte[] result = coder.encodeDouble(value);

        assertArrayEquals(response, result);
    }

    @Test
    void decodeDouble() {
        final byte[] value = {1, 2, 3, 4};
        final double response = 23.1;
        when(parentCoder.decodeDouble(value)).thenReturn(response);

        double result = coder.decodeDouble(value);

        assertEquals(response, result, 0.0);
    }

    @Test
    void encodeTimestamp_calendar_boolean() {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response1 = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        final Timestamp response2 = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000);
        when(parentCoder.encodeTimestamp(value, calendar, false)).thenReturn(response1);
        when(parentCoder.encodeTimestamp(value, calendar, true)).thenReturn(response2);

        Timestamp result1 = coder.encodeTimestamp(value, calendar, false);
        Timestamp result2 = coder.encodeTimestamp(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    void encodeTimestampRaw() {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeTimestampRaw(value)).thenReturn(response);

        byte[] result = coder.encodeTimestampRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    void encodeTimestampCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeTimestampCalendar(value, calendar)).thenReturn(response);

        byte[] result = coder.encodeTimestampCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    void decodeTimestamp_calendar_boolean() {
        final Calendar calendar = Calendar.getInstance();
        final Timestamp value = new Timestamp(System.currentTimeMillis());
        final Timestamp response1 = new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000);
        final Timestamp response2 = new Timestamp(System.currentTimeMillis() + 60 * 60 * 1000);
        when(parentCoder.decodeTimestamp(value, calendar, false)).thenReturn(response1);
        when(parentCoder.decodeTimestamp(value, calendar, true)).thenReturn(response2);

        Timestamp result1 = coder.decodeTimestamp(value, calendar, false);
        Timestamp result2 = coder.decodeTimestamp(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    void decodeTimestampRaw() {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();
        when(parentCoder.decodeTimestampRaw(value)).thenReturn(response);

        DatatypeCoder.RawDateTimeStruct result = coder.decodeTimestampRaw(value);

        assertSame(response, result);
    }

    @Test
    void decodeTimestampCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Timestamp response = new Timestamp(System.currentTimeMillis());
        when(parentCoder.decodeTimestampCalendar(value, calendar)).thenReturn(response);

        Timestamp result = coder.decodeTimestampCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    void encodeTime_calendar_boolean() {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final Time response1 = new Time(System.currentTimeMillis() - 60 * 60 * 1000);
        final Time response2 = new Time(System.currentTimeMillis() + 60 * 60 * 1000);
        when(parentCoder.encodeTime(value, calendar, false)).thenReturn(response1);
        when(parentCoder.encodeTime(value, calendar, true)).thenReturn(response2);

        Time result1 = coder.encodeTime(value, calendar, false);
        Time result2 = coder.encodeTime(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    void encodeTimeRaw() {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeTimeRaw(value)).thenReturn(response);

        byte[] result = coder.encodeTimeRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    void encodeTimeCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeTimeCalendar(value, calendar)).thenReturn(response);

        byte[] result = coder.encodeTimeCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    void decodeTime_calendar_boolean() {
        final Calendar calendar = Calendar.getInstance();
        final Time value = new Time(System.currentTimeMillis());
        final Time response1 = new Time(System.currentTimeMillis() - 60 * 60 * 1000);
        final Time response2 = new Time(System.currentTimeMillis() + 60 * 60 * 1000);
        when(parentCoder.decodeTime(value, calendar, false)).thenReturn(response1);
        when(parentCoder.decodeTime(value, calendar, true)).thenReturn(response2);

        Time result1 = coder.decodeTime(value, calendar, false);
        Time result2 = coder.decodeTime(value, calendar, true);

        assertSame(response1, result1);
        assertSame(response2, result2);
    }

    @Test
    void decodeTimeRaw() {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();
        when(parentCoder.decodeTimeRaw(value)).thenReturn(response);

        DatatypeCoder.RawDateTimeStruct result = coder.decodeTimeRaw(value);

        assertSame(response, result);
    }

    @Test
    void decodeTimeCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Time response = new Time(System.currentTimeMillis());
        when(parentCoder.decodeTimeCalendar(value, calendar)).thenReturn(response);

        Time result = coder.decodeTimeCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    void encodeDate_calendar() {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final Date response = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
        when(parentCoder.encodeDate(value, calendar)).thenReturn(response);

        Date result = coder.encodeDate(value, calendar);

        assertSame(response, result);
    }

    @Test
    void encodeDateRaw() {
        final DatatypeCoder.RawDateTimeStruct value = new DatatypeCoder.RawDateTimeStruct();
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeDateRaw(value)).thenReturn(response);

        byte[] result = coder.encodeDateRaw(value);

        assertArrayEquals(response, result);
    }

    @Test
    void encodeDateCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeDateCalendar(value, calendar)).thenReturn(response);

        byte[] result = coder.encodeDateCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    void decodeDate_calendar() {
        final Calendar calendar = Calendar.getInstance();
        final Date value = new Date(System.currentTimeMillis());
        final Date response = new Date(System.currentTimeMillis() - 60 * 60 * 1000);
        when(parentCoder.decodeDate(value, calendar)).thenReturn(response);

        Date result = coder.decodeDate(value, calendar);

        assertSame(response, result);
    }

    @Test
    void decodeDateRaw() {
        final byte[] value = {1, 2, 3, 4};
        final DatatypeCoder.RawDateTimeStruct response = new DatatypeCoder.RawDateTimeStruct();
        when(parentCoder.decodeDateRaw(value)).thenReturn(response);

        DatatypeCoder.RawDateTimeStruct result = coder.decodeDateRaw(value);

        assertSame(response, result);
    }

    @Test
    void decodeDateCalendar() {
        final Calendar calendar = Calendar.getInstance();
        final byte[] value = {1, 2, 3, 4};
        final Date response = new Date(System.currentTimeMillis());
        when(parentCoder.decodeDateCalendar(value, calendar)).thenReturn(response);

        Date result = coder.decodeDateCalendar(value, calendar);

        assertSame(response, result);
    }

    @Test
    public void decodeBoolean() {
        final byte[] valueTrue = {1, 0};
        final byte[] valueFalse = {0, 0};
        when(parentCoder.decodeBoolean(valueTrue)).thenReturn(true);
        when(parentCoder.decodeBoolean(valueFalse)).thenReturn(false);

        assertTrue(coder.decodeBoolean(valueTrue));
        assertFalse(coder.decodeBoolean(valueFalse));
    }

    @Test
    void encodeBoolean() {
        final byte[] resultTrue = {1, 0};
        final byte[] resultFalse = {0, 0};
        when(parentCoder.encodeBoolean(true)).thenReturn(resultTrue);
        when(parentCoder.encodeBoolean(false)).thenReturn(resultFalse);

        assertSame(resultTrue, coder.encodeBoolean(true));
        assertSame(resultFalse, coder.encodeBoolean(false));
    }

    @Test
    void encodeLocalTime() {
        final LocalTime value = LocalTime.of(1, 2, 3, 4);
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeLocalTime(value)).thenReturn(response);

        assertSame(response, coder.encodeLocalTime(value));
    }

    @Test
    void encodeLocalDate() {
        LocalDate value = LocalDate.of(1, 2, 3);
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeLocalDate(value)).thenReturn(response);

        assertSame(response, coder.encodeLocalDate(value));
    }

    @Test
    void encodeLocalDateTime() {
        LocalDateTime value = LocalDateTime.of(1, 2, 3, 4, 5, 6, 7);
        final byte[] response = {1, 2, 3, 4};
        when(parentCoder.encodeLocalDateTime(value)).thenReturn(response);

        assertSame(response, coder.encodeLocalDateTime(value));
    }

    @Test
    void getEncodingFactory(@Mock IEncodingFactory response) {
        when(parentCoder.getEncodingFactory()).thenReturn(response);

        assertSame(response, coder.getEncodingFactory());
    }

}