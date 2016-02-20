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
import org.firebirdsql.encodings.IEncodingFactory;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Interface defining the encoding and decoding for Firebird (numerical) data types.
 * <p>
 * TODO: access the connection encoding factory through this interface?
 * </p>
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface DatatypeCoder {

    /**
     * Encode a <code>short</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     * @see #encodeShort(int)
     */
    byte[] encodeShort(short value);

    /**
     * Encode a <code>short</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    byte[] encodeShort(int value);

    /**
     * Decode a <code>byte</code> array into a <code>short</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>short</code> value of the decoded
     *         <code>byte</code> array
     */
    short decodeShort(byte[] byte_int);

    /**
     * Encode an <code>int</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    byte[] encodeInt(int value);

    /**
     * Decode a <code>byte</code> array into an <code>int</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>int</code> value of the decoded
     *         <code>byte</code> array
     */
    int decodeInt(byte[] byte_int);

    /**
     * Encode a <code>long</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    byte[] encodeLong(long value);

    /**
     * Decode a <code>byte</code> array into a <code>long</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>long</code> value of the decoded
     *         <code>byte</code> array
     */
    long decodeLong(byte[] byte_int);

    /**
     * Encode a <code>float</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    byte[] encodeFloat(float value);

    /**
     * Decode a <code>byte</code> array into a <code>float</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>float</code> value of the decoded
     *         <code>byte</code> array
     */
    float decodeFloat(byte[] byte_int);

    /**
     * Encode a <code>double</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    byte[] encodeDouble(double value);

    /**
     * Decode a <code>byte</code> array into a <code>double</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>double</code> value of the decoded
     *         <code>byte</code> array
     */
    double decodeDouble(byte[] byte_int);

// TODO String encoding/decoding might need to be done differently

    /**
     * Encode a <code>String</code> value into a <code>byte</code> array using
     * a given encoding.
     *
     * @param value The <code>String</code> to be encoded
     * @param javaEncoding The java encoding to use in the encoding process
     * @param mappingPath The character mapping path to be used in the encoding
     * @return The value of <code>value</code> as a <code>byte</code> array
     * @throws java.sql.SQLException if the given encoding cannot be found, or an error
     *         occurs during the encoding
     */
    byte[] encodeString(String value, String javaEncoding, String mappingPath) throws SQLException;

    byte[] encodeString(String value, Encoding encoding, String mappingPath) throws SQLException;

    // TODO Is below method needed?
//    /**
//     * Encode a <code>byte</code> array using a given encoding.
//     *
//     * @param value The <code>byte</code> array to be encoded
//     * @param encoding The encoding to use in the encoding process
//     * @param mappingPath The character mapping path to be used in the encoding
//     * @return The value of <code>value</code> encoded using the given encoding
//     * @throws java.sql.SQLException if the given encoding cannot be found, or an error
//     *         occurs during the encoding
//     */
//    byte[] encodeString(byte[] value, String encoding, String mappingPath)throws SQLException;

    /**
     * Decode an encoded <code>byte</code> array into a <code>String</code>
     * using a given encoding.
     *
     * @param value The value to be decoded
     * @param javaEncoding The java encoding to be used in the decoding process
     * @param mappingPath The character mapping path to be used in the decoding
     * @return The decoded <code>String</code>
     * @throws java.sql.SQLException if the given encoding cannot be found, or an
     *         error occurs during the decoding
     */
    String decodeString(byte[] value, String javaEncoding, String mappingPath) throws SQLException;

    String decodeString(byte[] value, Encoding encoding, String mappingPath) throws SQLException;

    /**
     * Encode a <code>Timestamp</code> using a given <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @param cal The <code>Calendar</code> to be used for encoding,
     *        may be <code>null</code>
     */
    Timestamp encodeTimestamp(Timestamp value, Calendar cal);

    /**
     * Encode a <code>Timestamp</code> using a given <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @param cal The <code>Calendar</code> to be used for encoding,
     *        may be <code>null</code>
     * @param invertTimeZone If <code>true</code>, the timezone offset value
     *        will be subtracted from the encoded value, otherwise it will
     *        be added
     * @return The encoded <code>Timestamp</code>
     */
    Timestamp encodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone);

    /**
     * Encode a <code>Timestamp</code> as a <code>byte</code> array.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @return The array of <code>byte</code>s that represents the given
     *         <code>Timestamp</code> value
     */
    byte[] encodeTimestamp(Timestamp value);

    byte[] encodeTimestampCalendar(Timestamp value, Calendar c);

    /**
     * Decode a <code>Timestamp</code> value using a given
     * <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in decoding,
     *        may be <code>null</code>
     * @return The decoded <code>Timestamp</code>
     */
    Timestamp decodeTimestamp(Timestamp value, Calendar cal);

    /**
     * Decode a <code>Timestamp</code> value using a given
     * <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in decoding,
     *        may be <code>null</code>
     * @param invertTimeZone If <code>true</code>, the timezone offset value
     *        will be subtracted from the decoded value, otherwise it will
     *        be added
     * @return The encoded <code>Timestamp</code>
     */
    Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone);

    /**
     * Decode a <code>byte</code> array into a <code>Timestamp</code>.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return A <code>Timestamp</code> value from the decoded
     *         <code>byte</code>s
     */
    Timestamp decodeTimestamp(byte[] byte_int);

    Timestamp decodeTimestampCalendar(byte[] byte_int, Calendar c);

    /**
     * Encode a given <code>Time</code> value using a given
     * <code>Calendar</code>.
     *
     * @param d The <code>Time</code> to be encoded
     * @param cal The <code>Calendar</code> to be used in the encoding,
     *        may be <code>null</code>
     * @return The encoded <code>Time</code>
     */
    java.sql.Time encodeTime(Time d, Calendar cal, boolean invertTimeZone);

    /**
     * Encode a <code>Time</code> value into a <code>byte</code> array.
     *
     * @param d The <code>Time</code> to be encoded
     * @return The array of <code>byte</code>s representing the given
     *         <code>Time</code>
     */
    byte[] encodeTime(Time d);

    byte[] encodeTimeCalendar(Time d, Calendar c);

    /**
     * Decode a <code>Time</code> value using a given <code>Calendar</code>.
     *
     * @param d The <code>Time</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in the decoding, may
     *        be <code>null</code>
     * @return The decooded <code>Time</code>
     */
    Time decodeTime(Time d, Calendar cal, boolean invertTimeZone);

    /**
     * Decode a <code>byte</code> array into a <code>Time</code> value.
     *
     * @param int_byte The <code>byte</code> array to be decoded
     * @return The decoded <code>Time</code>
     */
    Time decodeTime(byte[] int_byte);

    Time decodeTimeCalendar(byte[] int_byte, Calendar c);

    /**
     * Encode a given <code>Date</code> value using a given
     * <code>Calendar</code>.
     *
     * @param d The <code>Date</code> to be encoded
     * @param cal The <code>Calendar</code> to be used in the encoding,
     *        may be <code>null</code>
     * @return The encoded <code>Date</code>
     */
    Date encodeDate(Date d, Calendar cal);

    /**
     * Encode a <code>Date</code> value into a <code>byte</code> array.
     *
     * @param d The <code>Date</code> to be encoded
     * @return The array of <code>byte</code>s representing the given
     *         <code>Date</code>
     */
    byte[] encodeDate(Date d);

    byte[] encodeDateCalendar(Date d, Calendar c);

    /**
     * Decode a <code>Date</code> value using a given <code>Calendar</code>.
     *
     * @param d The <code>Date</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in the decoding, may
     *        be <code>null</code>
     * @return The decoded <code>Date</code>
     */
    Date decodeDate(Date d, Calendar cal);

    /**
     * Decode a <code>byte</code> array into a <code>Date</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The decoded <code>Date</code>
     */
    Date decodeDate(byte[] byte_int);

    Date decodeDateCalendar(byte[] byte_int, Calendar c);

    /**
     * Decode boolean from supplied data.
     *
     * @param data (expected) 1 bytes
     * @return <code>false</code> when 0, <code>true</code> for all other values
     */
    boolean decodeBoolean(byte[] data);

    /**
     * Encodes boolean to 1 byte data.
     *
     * @param value Boolean value to encode
     * @return <code>true</code> as 1, <code>false</code> as 0.
     */
    byte[] encodeBoolean(boolean value);

    /**
     * Encodes a java.time.LocalTime equivalent to time bytes.
     *
     * @param hour Number of hours (is assumed to be 0..23)
     * @param minute Number of minutes (is assumed to be 0..59)
     * @param second Number of seconds (is assumed to be 0..59)
     * @param nanos Sub-second nanoseconds (actual resolution is 100 microseconds, is assumed to be 0 .. 10^9 - 1 ns)
     * @return Byte array for time
     */
    byte[] encodeLocalTime(int hour, int minute, int second, int nanos);

    /**
     * Encodes a java.time.LocalDate equivalent to date bytes.
     *
     * @param year Year
     * @param month Month (is assumed to be 1..12)
     * @param day Day (is assumed to be valid for year and month)
     * @return Byte array for date
     */
    byte[] encodeLocalDate(int year, int month, int day);

    /**
     * Encodes a java.time.LocalDateTime equivalent to timestamp bytes.
     *
     * @param year Year
     * @param month Month (is assumed to be 1..12)
     * @param day Day (is assumed to be valid for year and month)
     * @param hour Number of hours (is assumed to be 0..23)
     * @param minute Number of minutes (is assumed to be 0..59)
     * @param second Number of seconds (is assumed to be 0..59)
     * @param nanos Sub-second nanoseconds (actual resolution is 100 microseconds, is assumed to be 0 .. 10^9 - 1 ns)
     * @return Byte array for timestamp
     */
    byte[] encodeLocalDateTime(int year, int month, int day, int hour, int minute, int second, int nanos);

    /**
     * @return The encoding factory.
     */
    IEncodingFactory getEncodingFactory();
}
