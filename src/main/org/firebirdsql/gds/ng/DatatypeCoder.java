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
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * The size of an encoded short in this data type coder.
     *
     * @return The size of an encoded short (either {@code 2} or {@code 4} bytes)
     * @since 4.0
     */
    int sizeOfShort();

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
     * Encode a {@code short} value into the {@code target} byte array starting at index {@code fromIndex}.
     *
     * @param value
     *         The value to be encoded
     * @param target
     *         Target byte array of sufficient size (warning: this may be datatype coder specific)
     * @param fromIndex
     *         Index to start writing
     * @since 4.0
     */
    void encodeShort(int value, byte[] target, int fromIndex);

    /**
     * Decode a {@code byte} array into a {@code short} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code short} value of the decoded {@code byte} array
     */
    short decodeShort(byte[] byte_int);

    /**
     * Decode from a {@code byte} array to a {@code short} value.
     *
     * @param bytes
     *         The {@code byte} array to be decoded
     * @param fromIndex
     *         The index to start reading
     * @return The {@code short} value of the decoded {@code byte} array
     * @since 4.0
     */
    short decodeShort(byte[] bytes, int fromIndex);

    /**
     * Encode an {@code int} value as a {@code byte} array.
     *
     * @param value The value to be encoded
     * @return The value of {@code value} encoded as a {@code byte} array
     */
    byte[] encodeInt(int value);

    /**
     * Encode an {@code int} value into the {@code target} byte array starting at index {@code fromIndex}.
     *
     * @param value
     *         The value to be encoded
     * @param target
     *         Target byte array of sufficient size
     * @param fromIndex
     *         Index to start writing
     * @since 4.0
     */
    void encodeInt(int value, byte[] target, int fromIndex);

    /**
     * Decode a {@code byte} array into an {@code int} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@code int} value of the decoded {@code byte} array
     */
    int decodeInt(byte[] byte_int);

    /**
     * Decode a {@code byte} array to an {@code int} value.
     *
     * @param bytes
     *         The {@code byte} array to be decoded
     * @param fromIndex
     *         The index to start reading
     * @return The {@code int} value of the decoded {@code byte} array
     * @since 4.0
     */
    int decodeInt(byte[] bytes, int fromIndex);

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

    /**
     * Encode a {@code String} value into a {@code byte} array using the encoding of this datatype coder.
     *
     * @param value The {@code String} to be encoded
     * @return The value of {@code value} as a {@code byte} array
     * @since 4.0
     */
    byte[] encodeString(String value);

    /**
     * Creates a writer wrapping an input stream.
     *
     * @param outputStream
     *         Input stream
     * @return Writer applying the encoding of this datatype when writing
     * @since 4.0
     */
    Writer createWriter(OutputStream outputStream);

    /**
     * Decode an encoded {@code byte} array into a {@code String} using the encoding of this datatype coder.
     *
     * @param value The value to be decoded
     * @return The decoded {@code String}
     * @since 4.0
     */
    String decodeString(byte[] value);

    /**
     * Creates a reader wrapping an input stream.
     *
     * @param inputStream
     *         Input stream
     * @return Reader applying the encoding of this datatype coder when reading
     * @since 4.0
     */
    Reader createReader(InputStream inputStream);

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
     * Encode the date and time portions of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the date and time of the given {@code RawDateTimeStruct}
     */
    byte[] encodeTimestampRaw(RawDateTimeStruct raw);

    /**
     * Encode a {@code Timestamp} as a {@code byte} array.
     *
     * @param value The {@code Timestamp} to be encoded
     * @param c Calendar to use for time zone calculation
     * @return The array of {@code byte}s that represents the given {@code Timestamp} value
     */
    byte[] encodeTimestampCalendar(Timestamp value, Calendar c);

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
     * Decode an 8-byte {@code byte} array into a raw date time struct.
     *
     * @param byte_long The {@code byte} array to be decoded
     * @return A {@link RawDateTimeStruct}.
     */
    RawDateTimeStruct decodeTimestampRaw(byte[] byte_long);

    /**
     * Decode an 8-byte {@code byte} array into a {@code Timestamp}.
     *
     * @param byte_long The {@code byte} array to be decoded
     * @param c Calendar to use for time zone calculation
     * @return A {@code Timestamp} value from the decoded bytes
     */
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
     * Encode the time portion of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the time of the given {@code RawDateTimeStruct}
     */
    byte[] encodeTimeRaw(RawDateTimeStruct raw);

    /**
     * Encode a {@code Time} value into a {@code byte} array.
     *
     * @param d The {@code Time} to be encoded
     * @param c Calendar to use for time zone calculation
     * @return The array of {@code byte}s representing the given {@code Time}
     */
    byte[] encodeTimeCalendar(Time d, Calendar c);

    /**
     * Decode a {@code Time} value using a given {@code Calendar}.
     *
     * @param d The {@code Time} to be decoded
     * @param cal The {@code Calendar} to be used in the decoding, may be {@code null}
     * @return The decoded {@code Time}
     */
    Time decodeTime(Time d, Calendar cal, boolean invertTimeZone);

    /**
     * Decode a {@code byte} array into a raw date time struct.
     *
     * @param int_byte The {@code byte} array to be decoded
     * @return The {@link RawDateTimeStruct}
     */
    RawDateTimeStruct decodeTimeRaw(byte[] int_byte);

    /**
     * Decode a {@code byte} array into a {@code Time} value.
     *
     * @param int_byte The {@code byte} array to be decoded
     * @param c Calendar to use for time zone calculation
     * @return The decoded {@code Time}
     */
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
     * Encode the date portion of a raw date time struct into a {@code byte} array.
     *
     * @param raw The {@code RawDateTimeStruct} to be encoded
     * @return The array of {@code byte}s representing the date of the given {@code RawDateTimeStruct}
     */
    byte[] encodeDateRaw(RawDateTimeStruct raw);

    /**
     * Encode a {@code Date} value into a {@code byte} array.
     *
     * @param d The {@code Date} to be encoded
     * @param c Calendar to use for time zone calculation
     * @return The array of {@code byte}s representing the given {@code Date}
     */
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
     * Decode a {@code byte} array into a raw date time struct.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @return The {@link RawDateTimeStruct}
     */
    RawDateTimeStruct decodeDateRaw(byte[] byte_int);

    /**
     * Decode a {@code byte} array into a {@code Date} value.
     *
     * @param byte_int The {@code byte} array to be decoded
     * @param c Calendar to use for time zone calculation
     * @return The decoded {@code Date}
     */
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
     * Decode LocalTime from supplied data.
     *
     * @param data (expected) 4 bytes
     * @return LocalTime value
     * @since 5
     */
    LocalTime decodeLocalTime(byte[] data);

    /**
     * Encodes a java.time.LocalTime to time bytes.
     *
     * @param value LocalTime value to encode
     * @return Byte array for time
     * @since 5
     */
    byte[] encodeLocalTime(LocalTime value);

    /**
     * Decode LocalDate from supplied data.
     *
     * @param data (expected) 4 bytes
     * @return LocalDate value
     * @since 5
     */
    LocalDate decodeLocalDate(byte[] data);

    /**
     * Encodes a java.time.LocalDate to date bytes.
     *
     * @param value LocalDate to encode
     * @return Byte array for date
     * @since 5
     */
    byte[] encodeLocalDate(LocalDate value);

    /**
     * Decode LocalDateTime from supplied data.
     *
     * @param data (expected) 8 bytes
     * @return LocalDateTime value
     * @since 5
     */
    LocalDateTime decodeLocalDateTime(byte[] data);

    /**
     * Encodes a java.time.LocalDateTime to timestamp bytes.
     *
     * @param value LocalDateTime to encode
     * @return Byte array for date
     * @since 5
     */
    byte[] encodeLocalDateTime(LocalDateTime value);

    /**
     * Decodes a decimal64 from byte array.
     *
     * @param data Data to decode (expected 8 bytes)
     * @return Decimal64 value
     */
    Decimal64 decodeDecimal64(byte[] data);

    /**
     * Encodes a decimal64 to a byte array.
     *
     * @param decimal64 The decimal64 value to be encoded
     * @return Byte array for decimal64 value
     */
    byte[] encodeDecimal64(Decimal64 decimal64);

    /**
     * Decodes a decimal128 from byte array.
     *
     * @param data Data to decode (expected 16 bytes)
     * @return Decimal128 value
     */
    Decimal128 decodeDecimal128(byte[] data);

    /**
     * Encodes a decimal128 to a byte array.
     *
     * @param decimal128 The decimal128 value to be encoded
     * @return Byte array for decimal128 value
     */
    byte[] encodeDecimal128(Decimal128 decimal128);

    /**
     * Decodes a BigInteger from byte array.
     *
     * @param data Data to decode (expected 16 bytes)
     * @return BigInteger value
     */
    BigInteger decodeInt128(byte[] data);

    /**
     * Encodes a BigInteger to a 16-byte byte array.
     * <p>
     * The implementation expects to be passed a value that fits in 16 bytes. If a larger value is passed, and
     * {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param bigInteger The BigInteger value to be encoded
     * @return Byte array for bigInteger value
     */
    byte[] encodeInt128(BigInteger bigInteger);

    /**
     * @return The encoding factory.
     */
    IEncodingFactory getEncodingFactory();

    /**
     * @return The encoding definition used by this datatype coder for string conversions.
     */
    EncodingDefinition getEncodingDefinition();

    /**
     * @return The encoding used by this datatype coder for string conversions.
     */
    Encoding getEncoding();

    /**
     * Return a derived datatype coder that applies the supplied encoding definition for string conversions.
     *
     * @param encodingDefinition Encoding definition
     * @return Derived datatype coder (this instance, if encoding definition is the same)
     * @since 4.0
     */
    DatatypeCoder forEncodingDefinition(EncodingDefinition encodingDefinition);

    /**
     * Unwrap this datatype coder to its parent (or itself).
     *
     * @return Return the parent of this datatype code, or itself if it has no parent.
     * @since 4.0
     */
    DatatypeCoder unwrap();

    /**
     * {@inheritDoc}
     * <p>
     * Equality: same basic type (ie: wire protocol/JNA type + endianness) and same encoding definition.
     * </p>
     * <p>
     * This does not need to take into account the encoding factory, as usage should be limited to datatype coders
     * derived from the same connection.
     * </p>
     *
     * @param other Object to compare to
     * @return {@code true} if other is an equivalent datatype coder.
     */
    @Override
    boolean equals(Object other);

    @Override
    int hashCode();

    /**
     * Raw date/time value.
     * <p>
     * Fractions are sub-second precision in 100 microseconds.
     * </p>
     * <p>
     * We cannot simply pass millis to the database, because Firebird stores timestamp in format (citing Ann W.
     * Harrison):
     * </p>
     * <p>
     * "[timestamp is] stored a two long words, one representing the number of days since 17 Nov 1858 and one
     * representing number of 100 nano-seconds since midnight" (NOTE: It is actually 100 microseconds!)
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

        /**
         * Initializes a raw date/time value from encoded time and/or date integers.
         *
         * @param encodedDate Encoded date (Modified Julian Date)
         * @param hasDate If date should be decoded (set {@code false} for a time-only value)
         * @param encodedTime Encoded time (fractions in day)
         * @param hasTime If time should be decoded (set {@code false} for a date-only value)
         * @since 4.0
         */
        public RawDateTimeStruct(int encodedDate, boolean hasDate, int encodedTime, boolean hasTime) {
            if (hasDate) {
                decodeDate(encodedDate);
            }
            if (hasTime) {
                decodeTime(encodedTime);
            }
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

        /**
         * Sets the sub-second fraction (100 microseconds) from a nanosecond value.
         *
         * @param nanos Sub-second nanoseconds
         * @since 4.0
         */
        public void setFractionsFromNanos(long nanos) {
            fractions = (int) ((nanos / NANOSECONDS_PER_FRACTION) % FRACTIONS_PER_SECOND);
        }

        /**
         * Encodes the date as used by Firebird (Modified Julian Date, or number of days since 17 November 1858).
         *
         * @return Encoded date
         * @since 4.0
         */
        public int getEncodedDate() {
            int cpMonth = month;
            int cpYear = year;

            if (cpMonth > 2) {
                cpMonth -= 3;
            } else {
                cpMonth += 9;
                cpYear -= 1;
            }

            int c = cpYear / 100;
            int ya = cpYear - 100 * c;

            return ((146097 * c) / 4 +
                    (1461 * ya) / 4 +
                    (153 * cpMonth + 2) / 5 +
                    day + 1721119 - 2400001);
        }

        private void decodeDate(int encodedDate) {
            int sql_date = encodedDate - 1721119 + 2400001;
            int century = (4 * sql_date - 1) / 146097;
            sql_date = 4 * sql_date - 1 - 146097 * century;
            day = sql_date / 4;

            sql_date = (4 * day + 3) / 1461;
            day = 4 * day + 3 - 1461 * sql_date;
            day = (day + 4) / 4;

            month = (5 * day - 3) / 153;
            day = 5 * day - 3 - 153 * month;
            day = (day + 5) / 5;

            year = 100 * century + sql_date;

            if (month < 10) {
                month += 3;
            } else {
                month -= 9;
                year += 1;
            }
        }

        /**
         * Encodes the time as used by Firebird (fractions (100 milliseconds) in a day).
         *
         * @return Encoded time
         * @since 4.0
         */
        public int getEncodedTime() {
            return hour * FRACTIONS_PER_HOUR
                    + minute * FRACTIONS_PER_MINUTE
                    + second * FRACTIONS_PER_SECOND
                    + fractions;
        }

        private void decodeTime(int encodedTime) {
            int fractionsInDay = encodedTime;
            hour = fractionsInDay / FRACTIONS_PER_HOUR;
            fractionsInDay -= hour * FRACTIONS_PER_HOUR;
            minute = fractionsInDay / FRACTIONS_PER_MINUTE;
            fractionsInDay -= minute * FRACTIONS_PER_MINUTE;
            second = fractionsInDay / FRACTIONS_PER_SECOND;
            fractions = fractionsInDay - second * FRACTIONS_PER_SECOND;
        }

    }
}
