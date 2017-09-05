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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Datatype coder wrapping another datatype coder and applying a specific encoding definition, while delegating other
 * methods to the wrapped datatype coder.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 4.0
 */
public final class EncodingSpecificDatatypeCoder implements DatatypeCoder {

    private final DatatypeCoder parentCoder;
    private final EncodingDefinition encodingDefinition;
    private final Encoding encoding;

    /**
     * Creates an encoding datatype coder.
     *
     * @param parentCoder Parent datatype coder
     * @param encodingDefinition Encoding definition to apply for string conversions
     */
    EncodingSpecificDatatypeCoder(DatatypeCoder parentCoder, EncodingDefinition encodingDefinition) {
        this.parentCoder = requireNonNull(parentCoder, "parentCoder");
        this.encodingDefinition = requireNonNull(encodingDefinition, "encodingDefinition");
        encoding = requireNonNull(encodingDefinition.getEncoding(), "encodingDefinition.encoding");
    }

    @Override
    public byte[] encodeString(String value) {
        return encoding.encodeToCharset(value);
    }

    @Override
    public Writer createWriter(OutputStream outputStream) {
        return encoding.createWriter(outputStream);
    }

    @Override
    public String decodeString(byte[] value) {
        return encoding.decodeFromCharset(value);
    }

    @Override
    public Reader createReader(InputStream inputStream) {
        return encoding.createReader(inputStream);
    }

    @Override
    public DatatypeCoder forEncodingDefinition(EncodingDefinition encodingDefinition) {
        if (this.encodingDefinition.equals(encodingDefinition)) {
            return this;
        }
        return parentCoder.forEncodingDefinition(encodingDefinition);
    }

    @Override
    public EncodingDefinition getEncodingDefinition() {
        return encodingDefinition;
    }

    @Override
    public Encoding getEncoding() {
        return encoding;
    }

    /**
     * @return the datatype coder wrapped by this instance
     */
    DatatypeCoder unwrap() {
        return parentCoder;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DatatypeCoder)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof EncodingSpecificDatatypeCoder) {
            EncodingSpecificDatatypeCoder other = (EncodingSpecificDatatypeCoder) o;
            return encodingDefinition.equals(other.encodingDefinition)
                    && parentCoder.getClass() == other.parentCoder.getClass();
        } else {
            DatatypeCoder other = (DatatypeCoder) o;
            return encodingDefinition.equals(other.getEncodingDefinition())
                    && parentCoder.getClass() == other.getClass();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentCoder.getClass(), encodingDefinition);
    }

    // Methods delegating to parent coder

    @Override
    public byte[] encodeShort(short value) {
        return parentCoder.encodeShort(value);
    }

    @Override
    public byte[] encodeShort(int value) {
        return parentCoder.encodeShort(value);
    }

    @Override
    public short decodeShort(byte[] byte_int) {
        return parentCoder.decodeShort(byte_int);
    }

    @Override
    public byte[] encodeInt(int value) {
        return parentCoder.encodeInt(value);
    }

    @Override
    public int decodeInt(byte[] byte_int) {
        return parentCoder.decodeInt(byte_int);
    }

    @Override
    public byte[] encodeLong(long value) {
        return parentCoder.encodeLong(value);
    }

    @Override
    public long decodeLong(byte[] byte_int) {
        return parentCoder.decodeLong(byte_int);
    }

    @Override
    public byte[] encodeFloat(float value) {
        return parentCoder.encodeFloat(value);
    }

    @Override
    public float decodeFloat(byte[] byte_int) {
        return parentCoder.decodeFloat(byte_int);
    }

    @Override
    public byte[] encodeDouble(double value) {
        return parentCoder.encodeDouble(value);
    }

    @Override
    public double decodeDouble(byte[] byte_int) {
        return parentCoder.decodeDouble(byte_int);
    }

    @Override
    @Deprecated
    public byte[] encodeString(String value, Encoding encoding) throws SQLException {
        return parentCoder.encodeString(value, encoding);
    }

    @Override
    @Deprecated
    public String decodeString(byte[] value, Encoding encoding) throws SQLException {
        return parentCoder.decodeString(value, encoding);
    }

    @Override
    public Timestamp encodeTimestamp(Timestamp value, Calendar cal) {
        return parentCoder.encodeTimestamp(value, cal);
    }

    @Override
    public Timestamp encodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone) {
        return parentCoder.encodeTimestamp(value, cal, invertTimeZone);
    }

    @Override
    public byte[] encodeTimestamp(Timestamp value) {
        return parentCoder.encodeTimestamp(value);
    }

    @Override
    public byte[] encodeTimestampRaw(RawDateTimeStruct raw) {
        return parentCoder.encodeTimestampRaw(raw);
    }

    @Override
    public byte[] encodeTimestampCalendar(Timestamp value, Calendar c) {
        return parentCoder.encodeTimestampCalendar(value, c);
    }

    @Override
    public Timestamp decodeTimestamp(Timestamp value, Calendar cal) {
        return parentCoder.decodeTimestamp(value, cal);
    }

    @Override
    public Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone) {
        return parentCoder.decodeTimestamp(value, cal, invertTimeZone);
    }

    @Override
    public Timestamp decodeTimestamp(byte[] byte_long) {
        return parentCoder.decodeTimestamp(byte_long);
    }

    @Override
    public RawDateTimeStruct decodeTimestampRaw(byte[] byte_long) {
        return parentCoder.decodeTimestampRaw(byte_long);
    }

    @Override
    public Timestamp decodeTimestampCalendar(byte[] byte_long, Calendar c) {
        return parentCoder.decodeTimestampCalendar(byte_long, c);
    }

    @Override
    public Time encodeTime(Time d, Calendar cal, boolean invertTimeZone) {
        return parentCoder.encodeTime(d, cal, invertTimeZone);
    }

    @Override
    public byte[] encodeTime(Time d) {
        return parentCoder.encodeTime(d);
    }

    @Override
    public byte[] encodeTimeRaw(RawDateTimeStruct raw) {
        return parentCoder.encodeTimeRaw(raw);
    }

    @Override
    public byte[] encodeTimeCalendar(Time d, Calendar c) {
        return parentCoder.encodeTimeCalendar(d, c);
    }

    @Override
    public Time decodeTime(Time d, Calendar cal, boolean invertTimeZone) {
        return parentCoder.decodeTime(d, cal, invertTimeZone);
    }

    @Override
    public Time decodeTime(byte[] int_byte) {
        return parentCoder.decodeTime(int_byte);
    }

    @Override
    public RawDateTimeStruct decodeTimeRaw(byte[] int_byte) {
        return parentCoder.decodeTimeRaw(int_byte);
    }

    @Override
    public Time decodeTimeCalendar(byte[] int_byte, Calendar c) {
        return parentCoder.decodeTimeCalendar(int_byte, c);
    }

    @Override
    public Date encodeDate(Date d, Calendar cal) {
        return parentCoder.encodeDate(d, cal);
    }

    @Override
    public byte[] encodeDate(Date d) {
        return parentCoder.encodeDate(d);
    }

    @Override
    public byte[] encodeDateRaw(RawDateTimeStruct raw) {
        return parentCoder.encodeDateRaw(raw);
    }

    @Override
    public byte[] encodeDateCalendar(Date d, Calendar c) {
        return parentCoder.encodeDateCalendar(d, c);
    }

    @Override
    public Date decodeDate(Date d, Calendar cal) {
        return parentCoder.decodeDate(d, cal);
    }

    @Override
    public Date decodeDate(byte[] byte_int) {
        return parentCoder.decodeDate(byte_int);
    }

    @Override
    public RawDateTimeStruct decodeDateRaw(byte[] byte_int) {
        return parentCoder.decodeDateRaw(byte_int);
    }

    @Override
    public Date decodeDateCalendar(byte[] byte_int, Calendar c) {
        return parentCoder.decodeDateCalendar(byte_int, c);
    }

    @Override
    public boolean decodeBoolean(byte[] data) {
        return parentCoder.decodeBoolean(data);
    }

    @Override
    public byte[] encodeBoolean(boolean value) {
        return parentCoder.encodeBoolean(value);
    }

    @Override
    public byte[] encodeLocalTime(int hour, int minute, int second, int nanos) {
        return parentCoder.encodeLocalTime(hour, minute, second, nanos);
    }

    @Override
    public byte[] encodeLocalDate(int year, int month, int day) {
        return parentCoder.encodeLocalDate(year, month, day);
    }

    @Override
    public byte[] encodeLocalDateTime(int year, int month, int day, int hour, int minute, int second, int nanos) {
        return parentCoder.encodeLocalDateTime(year, month, day, hour, minute, second, nanos);
    }

    @Override
    public IEncodingFactory getEncodingFactory() {
        return parentCoder.getEncodingFactory();
    }

}
