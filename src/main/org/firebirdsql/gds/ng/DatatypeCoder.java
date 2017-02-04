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
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public interface DatatypeCoder {

    // A fraction is 100 microseconds
    int NANOSECONDS_PER_FRACTION = 100 * 1000;
    int FRACTIONS_PER_MILLISECOND = 10;
    int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
    int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
    int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;


    /**
     * Encode a {@code short} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     * @see #encodeShort(int)
     */
    byte[] encodeShort(short value);

    /**
     * Encode a {@code short} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeShort(int value);

    /**
     * Decode a {@code byte} array into a {@code short} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code short} value of the decoded {@code byte} array
     */
    short decodeShort(byte[] byte_int);

    /**
     * Encode an {@code int} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeInt(int value);

    /**
     * Decode a {@code byte} array into an {@code int} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code int} value of the decoded {@code byte} array
     */
    int decodeInt(byte[] byte_int);

    /**
     * Encode a {@code long} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeLong(long value);

    /**
     * Decode a {@code byte} array into a {@code long} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code long} value of the decoded {@code byte} array
     */
    long decodeLong(byte[] byte_int);

    /**
     * Encode a {@code float} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeFloat(float value);

    /**
     * Decode a {@code byte} array into a {@code float} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code float} value of the decoded {@code byte} array
     */
    float decodeFloat(byte[] byte_int);

    /**
     * Encode a {@code double} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeDouble(double value);

    /**
     * Decode a {@code byte} array into a {@code double} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code double} value of the decoded {@code byte} array
     */
    double decodeDouble(byte[] byte_int);

// TODO String encoding/decoding might need to be done differently

    /**
     * Encode a {@code String} value into a {@code byte} array using a given encoding.
     *
     * @param value The {@code String} to be encoded
     * @param javaEncoding The java encoding to use in the encoding process
     * @param mappingPath The character mapping path to be used in the encoding
     * @return The value of {@code value} as a {@code byte} array
     * @throws java.sql.SQLException if the given encoding cannot be found, or an error
     *         occurs during the encoding
     */
    byte[] encodeString(String value, String javaEncoding, String mappingPath) throws SQLException;

    byte[] encodeString(String value, Encoding encoding, String mappingPath) throws SQLException;

    /**
     * Decode an encoded {@code byte} array into a {@code String} using a given encoding.
     *
     * @param value The value to be decoded
     * @param javaEncoding The java encoding to be used in the decoding process
     * @param mappingPath The character mapping path to be used in the decoding
     * @return The decoded {@code String}
     * @throws java.sql.SQLException if the given encoding cannot be found, or an
     *         error occurs during the decoding
     */
    String decodeString(byte[] value, String javaEncoding, String mappingPath) throws SQLException;

    String decodeString(byte[] value, Encoding encoding, String mappingPath) throws SQLException;

    /**
     * Encode a {@code Timestamp} using a given {@code Calendar}.
     *
     * @param value The {@code Timestamp} to be encoded
     * @param cal The {@code Calendar} to be used for encoding, may be {@code null}
     */
    Timestamp encodeTimestamp(Timestamp value, Calendar cal);

    /**
     * Encode a {@code Timestamp} using a given {@code Calendar}.
     *
     * @param value The {@code Timestamp} to be encoded
     * @param cal The {@code Calendar} to be used for encoding,
     *        may be {@code null}
     * @param invertTimeZone If {@code true}, the timezone offset value
     *        will be subtracted from the encoded value, otherwise it will
     *        be added
     * @return The encoded {@code Timestamp}
     */
    Timestamp encodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone);

    /**
     * Encode a {@code Timestamp} as a {@code byte} array.
     *
     * @param value The {@code Timestamp} to be encoded
     * @return The array of {@code byte}s that represents the given {@code Timestamp} value
     */
    byte[] encodeTimestamp(Timestamp value);

    /**
     * Encode the date and time portions of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the date and time of the given {@code RawDateTimeStruct}
     */
    byte[] encodeTimestampRaw(RawDateTimeStruct raw);

    byte[] encodeTimestampCalendar(Timestamp value, Calendar c);

    /**
     * Decode a {@code Timestamp} value using a given {@code Calendar}.
     *
     * @param value The {@code Timestamp} to be decoded
     * @param cal The {@code Calendar} to be used in decoding,
     *        may be {@code null}
     * @return The decoded {@code Timestamp}
     */
    Timestamp decodeTimestamp(Timestamp value, Calendar cal);

    /**
     * Decode a {@code Timestamp} value using a given {@code Calendar}.
     *
     * @param value The {@code Timestamp} to be decoded
     * @param cal The {@code Calendar} to be used in decoding,
     *        may be {@code null}
     * @param invertTimeZone If {@code true}, the timezone offset value
     *        will be subtracted from the decoded value, otherwise it will
     *        be added
     * @return The encoded {@code Timestamp}
     */
    Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone);

    /**
     * Decode a 8-byte {@code byte} array into a {@code Timestamp}.
     *
     * @param byte_long The {@code byte} array to be decoded
     * @return A {@code Timestamp} value from the decoded bytes
     */
    Timestamp decodeTimestamp(byte[] byte_long);

    /**
     * Decode a 8-byte {@code byte} array into a raw date time struct.
     *
     * @param byte_long The {@code byte} array to be decoded
     * @return A {@link RawDateTimeStruct}.
     */
    RawDateTimeStruct decodeTimestampRaw(byte[] byte_long);

    Timestamp decodeTimestampCalendar(byte[] byte_long, Calendar c);

    /**
     * Encode a given {@code Time} value using a given {@code Calendar}.
     *
     * @param d The {@code Time} to be encoded
     * @param cal The {@code Calendar} to be used in the encoding, may be {@code null}
     * @return The encoded {@code Time}
     */
    java.sql.Time encodeTime(Time d, Calendar cal, boolean invertTimeZone);

    /**
     * Encode a {@code Time} value into a {@code byte} array.
     *
     * @param d The {@code Time} to be encoded
     * @return The array of {@code byte}s representing the given {@code Time}
     */
    byte[] encodeTime(Time d);

    /**
     * Encode the time portion of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the time of the given {@code RawDateTimeStruct}
     */
    byte[] encodeTimeRaw(RawDateTimeStruct raw);

    byte[] encodeTimeCalendar(Time d, Calendar c);

    /**
     * Decode a {@code Time} value using a given {@code Calendar}.
     *
     * @param d The {@code Time} to be decoded
     * @param cal The {@code Calendar} to be used in the decoding, may be {@code null}
     * @return The decooded {@code Time}
     */
    Time decodeTime(Time d, Calendar cal, boolean invertTimeZone);

    /**
     * Decode a {@code byte} array into a {@code Time} value.
     *
     * @param int_byte The {@code byte} array to be decoded
     * @return The decoded {@code Time}
     */
    Time decodeTime(byte[] int_byte);

    /**
     * Decode a {@code byte} array into a raw date time struct.
     *
     * @param int_byte The {@code byte} array to be decoded
     * @return The {@link RawDateTimeStruct}
     */
    RawDateTimeStruct decodeTimeRaw(byte[] int_byte);

    Time decodeTimeCalendar(byte[] int_byte, Calendar c);

    /**
     * Encode a given {@code Date} value using a given {@code Calendar}.
     *
     * @param d The {@code Date} to be encoded
     * @param cal The {@code Calendar} to be used in the encoding, may be {@code null}
     * @return The encoded {@code Date}
     */
    Date encodeDate(Date d, Calendar cal);

    /**
     * Encode a {@code Date} value into a {@code byte} array.
     *
     * @param d The {@code Date} to be encoded
     * @return The array of {@code byte}s representing the given {@code Date}
     */
    byte[] encodeDate(Date d);

    /**
     * Encode the date portion of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the date of the given {@code RawDateTimeStruct}
     */
    byte[] encodeDateRaw(RawDateTimeStruct raw);

    byte[] encodeDateCalendar(Date d, Calendar c);

    /**
     * Decode a {@code Date} value using a given {@code Calendar}.
     *
     * @param d The {@code Date} to be decoded
     * @param cal The {@code Calendar} to be used in the decoding, may be {@code null}
     * @return The decoded {@code Date}
     */
    Date decodeDate(Date d, Calendar cal);

    /**
     * Decode a {@code byte} array into a {@code Date} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The decoded {@code Date}
     */
    Date decodeDate(byte[] byte_int);

    /**
     * Decode a {@code byte} array into a raw date time struct.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@link RawDateTimeStruct}
     */
    RawDateTimeStruct decodeDateRaw(byte[] byte_int);

    Date decodeDateCalendar(byte[] byte_int, Calendar c);

    /**
     * Decode boolean from supplied data.
     *
     * @param data (expected) 1 bytes
     * @return {@code false} when 0, {@code true} for all other values
     */
    boolean decodeBoolean(byte[] data);

    /**
     * Encodes boolean to 1 byte data.
     *
     * @param value Boolean value to encode
     * @return {@code true} as 1, {@code false} as 0.
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

    /**
     * Raw date/time value.
     * <p>
     * Fractions are sub-second precision in 100 microseconds.
     * </p>
     */
    final class RawDateTimeStruct {
        public int year;
        public int month;
        public int day;
        public int hour;
        public int minute;
        public int second;
        public int fractions; // Sub-second precision in 100 microseconds

        public RawDateTimeStruct() {
        }

        public RawDateTimeStruct(RawDateTimeStruct raw) {
            this.year = raw.year;
            this.month = raw.month;
            this.day = raw.day;
            this.hour = raw.hour;
            this.minute = raw.minute;
            this.second = raw.second;
            this.fractions = raw.fractions;
        }

        public int getFractionsAsNanos() {
            return fractions * NANOSECONDS_PER_FRACTION;
        }
    }
}
