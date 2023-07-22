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
import org.firebirdsql.jaybird.util.FbDatetimeConversion;

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
 * @author Mark Rotteveel
 * @since 3
 */
public interface DatatypeCoder {

    // A fraction is 100 microseconds
    int NANOSECONDS_PER_FRACTION = (int) FbDatetimeConversion.NANOS_PER_UNIT;
    int FRACTIONS_PER_MILLISECOND = 10;
    int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
    int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
    @SuppressWarnings("unused")
    int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;

    /**
     * The size of an encoded short in this data type coder.
     *
     * @return size of an encoded short (either {@code 2} or {@code 4} bytes)
     * @since 4
     */
    int sizeOfShort();

    /**
     * Encode a short value as a byte array of length {@link #sizeOfShort()}.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeShort(short val);

    /**
     * Encode a short value as a byte array of length {@link #sizeOfShort()}.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeShort(int val);

    /**
     * Encode a short value into {@code buf} starting at offset {@code off} for {@link #sizeOfShort()} bytes.
     * <p>
     * NOTE: Implementations using 4 bytes to encode a short may choose to encode {@code val} (an int) as-is (which
     * means the most significant two bytes can have a value other than 0x0000 or 0xFFFF, and a value of 0xFFFF (65_535)
     * may be encoded as 0x0000_FFFF, and not as 0xFFFF_FFFF (-1). This behaviour may change at any time. For
     * consistent behaviour, explicitly cast to short when calling this method.
     * </p>
     *
     * @param val
     *         value to be encoded
     * @param buf
     *         byte array of sufficient size (warning: this is datatype coder specific, see {@link #sizeOfShort()})
     * @param off
     *         offset to start encoding
     * @since 4
     */
    void encodeShort(int val, byte[] buf, int off);

    /**
     * Decode a short value from {@code buf} from the first {@link #sizeOfShort()} bytes.
     *
     * @param buf
     *         byte array of sufficient size (warning: this is datatype coder specific, see {@link #sizeOfShort()})
     * @return short value from {@code buf}
     */
    short decodeShort(byte[] buf);

    /**
     * Decode a short value from {@code buf} starting at offset {@code off} for {@link #sizeOfShort()} bytes.
     *
     * @param buf
     *         byte array of sufficient size (warning: this is datatype coder specific, see {@link #sizeOfShort()})
     * @param off
     *         offset to start decoding
     * @return short value from {@code buf}
     * @since 4
     */
    short decodeShort(byte[] buf, int off);

    /**
     * Encode an int value as a byte array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeInt(int val);

    /**
     * Encode an int value into {@code buf} starting at index {@code off} for 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @param buf
     *         byte array of sufficient size
     * @param off
     *         offset to start encoding
     * @since 4
     */
    void encodeInt(int val, byte[] buf, int off);

    /**
     * Decode an int value from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return int value decoded from {@code buf}
     */
    int decodeInt(byte[] buf);

    /**
     * Decode an int value from {@code buf} starting at offset {@code off} for 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @param off
     *         offset to start decoding
     * @return int value decoded from {@code buf}
     * @since 4
     */
    int decodeInt(byte[] buf, int off);

    /**
     * Encode a long value as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeLong(long val);

    /**
     * Decode a long value from {@code buf} from the first 8 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return long value decoded from {@code buf}
     */
    long decodeLong(byte[] buf);

    /**
     * Encode a float value as a byte array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeFloat(float val);

    /**
     * Decode a float value from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return float value decoded from {@code buf}
     */
    float decodeFloat(byte[] buf);

    /**
     * Encode a double value as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeDouble(double val);

    /**
     * Decode a double value from {@code buf} from the first 8 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return double value decoded from {@code buf}
     */
    double decodeDouble(byte[] buf);

    /**
     * Encode a {@code String} value as a byte array using the encoding of this datatype coder.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     * @since 4
     */
    byte[] encodeString(String val);

    /**
     * Creates a writer wrapping an input stream.
     *
     * @param out
     *         output stream
     * @return writer applying the encoding of this datatype when writing
     * @since 4
     */
    Writer createWriter(OutputStream out);

    /**
     * Decode a {@code String} from {@code buf} using the encoding of this datatype coder.
     *
     * @param buf
     *         byte array to be decoded
     * @return {@code String} decoded from {@code buf}
     * @since 4
     */
    String decodeString(byte[] buf);

    /**
     * Creates a reader wrapping an input stream.
     *
     * @param in
     *         input stream
     * @return reader applying the encoding of this datatype coder when reading
     * @since 4
     */
    Reader createReader(InputStream in);

    /**
     * Encode a {@code Timestamp} using a given {@code Calendar}.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use for encoding, may be {@code null}
     * @return encoded {@code Timestamp}
     */
    Timestamp encodeTimestamp(Timestamp val, Calendar c);

    /**
     * Encode the date and time portions of a raw date time struct as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeTimestampRaw(RawDateTimeStruct val);

    /**
     * Encode a {@code Timestamp} as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeTimestampCalendar(Timestamp val, Calendar c);

    /**
     * Decode a {@code Timestamp} value using a given {@code Calendar}.
     *
     * @param val
     *         value to be decoded
     * @param c
     *         calendar to use in decoding, may be {@code null}
     * @return encoded {@code Timestamp}
     */
    Timestamp decodeTimestamp(Timestamp val, Calendar c);

    /**
     * Decode the date and time portions of a raw date time struct from {@code buf} from the first 8 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return {@code RawDateTimeStruct} decoded from {@code buf}
     */
    RawDateTimeStruct decodeTimestampRaw(byte[] buf);

    /**
     * Decode a {@code Timestamp} from {@code buf} from the first 8 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code Timestamp} decoded from {@code buf}
     */
    Timestamp decodeTimestampCalendar(byte[] buf, Calendar c);

    /**
     * Encode a given {@code Time} value using a given {@code Calendar}.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use in the encoding, may be {@code null}
     * @return encoded {@code Time}
     */
    Time encodeTime(Time val, Calendar c);

    /**
     * Encode the time portion of a raw date time struct as a byte array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeTimeRaw(RawDateTimeStruct val);

    /**
     * Encode a {@code Time} value as a byte array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeTimeCalendar(Time val, Calendar c);

    /**
     * Decode a {@code Time} value using a given {@code Calendar}.
     *
     * @param val
     *         value to be decoded
     * @param c
     *         calendar to used in the decoding, may be {@code null}
     * @return decoded {@code Time}
     */
    Time decodeTime(Time val, Calendar c);

    /**
     * Decode the time portion of a raw date time struct from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return {@code RawDateTimeStruct} decoded from {@code buf}
     */
    RawDateTimeStruct decodeTimeRaw(byte[] buf);

    /**
     * Decode a {@code Time} value from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code Time} decoded from {@code buf}
     */
    Time decodeTimeCalendar(byte[] buf, Calendar c);

    /**
     * Encode a given {@code Date} value using a given {@code Calendar}.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use in the encoding, may be {@code null}
     * @return encoded {@code Date}
     */
    Date encodeDate(Date val, Calendar c);

    /**
     * Encode the date portion of a raw date time struct as a byte array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeDateRaw(RawDateTimeStruct val);

    /**
     * Encode a {@code Date} value as a {@code byte} array of 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeDateCalendar(Date val, Calendar c);

    /**
     * Decode a {@code Date} value using a given {@code Calendar}.
     *
     * @param val
     *         value to be decoded
     * @param c
     *         calendar to use in the decoding, may be {@code null}
     * @return decoded {@code Date}
     */
    Date decodeDate(Date val, Calendar c);

    /**
     * Decode the date portion of a raw date time struct from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @return {@code RawDateTimeStruct} decoded from {@code buf}
     */
    RawDateTimeStruct decodeDateRaw(byte[] buf);

    /**
     * Decode a {@code Date} value from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size
     * @param c
     *         calendar to use for time zone calculation
     * @return {@code Date} decoded from {@code buf}
     */
    Date decodeDateCalendar(byte[] buf, Calendar c);

    /**
     * Decode a boolean from {@code buf} from the first byte.
     *
     * @param buf
     *         (expected) 1 bytes
     * @return {@code false} when 0, {@code true} for all other values
     */
    boolean decodeBoolean(byte[] buf);

    /**
     * Encodes boolean as a byte array of 1 byte.
     *
     * @param val
     *         value to encode
     * @return {@code true} as 1, {@code false} as 0.
     */
    byte[] encodeBoolean(boolean val);

    /**
     * Decode {@code java.time.LocalTime} from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         (expected) at least 4 bytes
     * @return {@code LocalTime} decoded from {@code buf}
     * @since 5
     */
    LocalTime decodeLocalTime(byte[] buf);

    /**
     * Decode {@code java.time.LocalTime} from {@code buf} from the 4 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 4 bytes from {@code off}
     * @param off
     *         offset of the time value in {@code buf}
     * @return {@code LocalTime} decoded from {@code buf}
     * @since 6
     */
    LocalTime decodeLocalTime(byte[] buf, int off);

    /**
     * Encode a {@code java.time.LocalTime} as a byte array of 4 bytes.
     *
     * @param val
     *         value to encode
     * @return {@code val} encoded as a byte array
     * @since 5
     */
    byte[] encodeLocalTime(LocalTime val);

    /**
     * Encode a {@code java.time.LocalTime} to a byte array, requiring 4 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 4 bytes starting at {@code off}
     * @param off
     *         offset of the time value in {@code buf}
     * @since 6
     */
    void encodeLocalTime(LocalTime val, byte[] buf, int off);

    /**
     * Decode {@code java.time.LocalDate} from {@code buf} from the first 4 bytes.
     *
     * @param buf
     *         (expected) at least 4 bytes
     * @return {@code LocalDate} decoded from {@code buf}
     * @since 5
     */
    LocalDate decodeLocalDate(byte[] buf);

    /**
     * Decode {@code java.time.LocalDate} from {@code buf} from the 4 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 4 bytes from {@code off}
     * @param off
     *         offset of the time value in {@code buf}
     * @return {@code LocalDate} decoded from {@code buf}
     * @since 6
     */
    LocalDate decodeLocalDate(byte[] buf, int off);

    /**
     * Encode a {@code java.time.LocalDate} as a byte array of 4 bytes.
     *
     * @param val
     *         value to encode
     * @return {@code val} encoded as a byte array
     * @since 5
     */
    byte[] encodeLocalDate(LocalDate val);

    /**
     * Encode a {@code java.time.LocalDate} to a byte array, requiring 4 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 4 bytes starting at {@code off}
     * @param off
     *         offset of the date value in {@code buf}
     * @since 6
     */
    void encodeLocalDate(LocalDate val, byte[] buf, int off);

    /**
     * Decode {@code java.time.LocalDateTime} from {@code buf} from the first 8 bytes.
     *
     * @param buf
     *         (expected) at least 8 bytes
     * @return {@code LocalDateTime} decoded from {@code buf}
     * @since 5
     */
    LocalDateTime decodeLocalDateTime(byte[] buf);

    /**
     * Decode {@code java.time.LocalDateTime} from {@code buf} from the 8 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 8 bytes from {@code off}
     * @param off
     *         offset of the datetime value in {@code buf}
     * @return {@code LocalDateTime} decoded from {@code buf}
     * @since 6
     */
    LocalDateTime decodeLocalDateTime(byte[] buf, int off);

    /**
     * Encode a {@code java.time.LocalDateTime} as a byte array of 8 bytes.
     *
     * @param val
     *         value to encode
     * @return {@code val} encoded as a byte array
     * @since 5
     */
    byte[] encodeLocalDateTime(LocalDateTime val);

    /**
     * Encode a {@code java.time.LocalDateTime} to a byte array, requiring 8 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 8 bytes starting at {@code off}
     * @param off
     *         offset of the datetime value in {@code buf}
     * @since 6
     */
    void encodeLocalDateTime(LocalDateTime val, byte[] buf, int off);

    /**
     * Decodes a decimal64 from a byte array of 8 bytes.
     *
     * @param buf
     *         data to decode (expects exactly 8 bytes)
     * @return Decimal64 decoded from {@code buf}
     */
    Decimal64 decodeDecimal64(byte[] buf);

    /**
     * Encodes a decimal64 as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeDecimal64(Decimal64 val);

    /**
     * Decodes a decimal128 from a byte array of 16 bytes.
     *
     * @param buf
     *         data to decode (expects exactly 16 bytes)
     * @return Decimal128 decoded from {@code buf}
     */
    Decimal128 decodeDecimal128(byte[] buf);

    /**
     * Encodes a decimal128 as a byte array of 16 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeDecimal128(Decimal128 val);

    /**
     * Decodes a BigInteger from a byte array of 16 bytes (int128 format).
     *
     * @param buf
     *         data to decode (expects exactly 16 bytes)
     * @return BigInteger decoded from {@code buf}
     */
    BigInteger decodeInt128(byte[] buf);

    /**
     * Encodes a BigInteger as a byte array of 16 bytes (int128 format).
     * <p>
     * The implementation expects to be passed a value that fits in 16 bytes. If a larger value is passed, an
     * {@code IllegalArgumentException} is thrown.
     * </p>
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array
     */
    byte[] encodeInt128(BigInteger val);

    /**
     * @return encoding factory.
     */
    IEncodingFactory getEncodingFactory();

    /**
     * @return encoding definition used by this datatype coder for string conversions.
     */
    EncodingDefinition getEncodingDefinition();

    /**
     * @return encoding used by this datatype coder for string conversions.
     */
    Encoding getEncoding();

    /**
     * Return a derived datatype coder that applies the supplied encoding definition for string conversions.
     *
     * @param encodingDefinition
     *         encoding definition
     * @return derived datatype coder (this instance, if encoding definition is the same)
     * @since 4
     */
    DatatypeCoder forEncodingDefinition(EncodingDefinition encodingDefinition);

    /**
     * Unwrap this datatype coder to its parent (or itself).
     *
     * @return parent of this datatype code, or itself if it has no parent.
     * @since 4.0
     */
    DatatypeCoder unwrap();

    /**
     * {@inheritDoc}
     * <p>
     * Equality: same basic type (i.e.: wire protocol/JNA type + endianness) and same encoding definition.
     * </p>
     * <p>
     * This does not need to take into account the encoding factory, as usage should be limited to datatype coders
     * derived from the same connection.
     * </p>
     *
     * @param other
     *         object to compare to
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
         * @param encodedDate
         *         encoded date (Modified Julian Date)
         * @param hasDate
         *         if date should be decoded (set {@code false} for a time-only value)
         * @param encodedTime
         *         encoded time (fractions in day)
         * @param hasTime
         *         if time should be decoded (set {@code false} for a date-only value)
         * @since 4
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
         * @param nanos
         *         Sub-second nanoseconds
         * @since 4
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
            return FbDatetimeConversion.toModifiedJulianDate(toLocalDate());
        }

        private void decodeDate(int encodedDate) {
            updateDate(FbDatetimeConversion.fromModifiedJulianDate(encodedDate));
        }

        /**
         * Encodes the time as used by Firebird (fractions (100 milliseconds) in a day).
         *
         * @return Encoded time
         * @since 4.0
         */
        public int getEncodedTime() {
            return FbDatetimeConversion.toFbTimeUnits(toLocalTime());
        }

        private void decodeTime(int encodedTime) {
            updateTime(FbDatetimeConversion.fromFbTimeUnits(encodedTime));
        }

        /**
         * Update the date fields from a calendar.
         *
         * @param c
         *         calendar
         */
        void updateDate(Calendar c) {
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
        }

        /**
         * Updates the date fields from a local date.
         *
         * @param localDate
         *         local date
         */
        public void updateDate(LocalDate localDate) {
            year = localDate.getYear();
            month = localDate.getMonthValue();
            day = localDate.getDayOfMonth();
        }

        /**
         * Updates the time field from a calendar and (optional) nanoseconds component.
         * <p>
         * When a non-negative {@code nanos} is provided, the {@code MILLISECOND} component of {@code c} is ignored.
         * </p>
         *
         * @param c
         *         calendar
         * @param nanos
         *         nanosecond component (ignored if {@code < 0})
         */
        void updateTime(Calendar c, long nanos) {
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            if (nanos < 0) {
                fractions = c.get(Calendar.MILLISECOND) * FRACTIONS_PER_MILLISECOND;
            } else {
                setFractionsFromNanos(nanos);
            }
        }

        /**
         * Updates the time fields from a local time.
         *
         * @param localTime
         *         local time
         */
        public void updateTime(LocalTime localTime) {
            hour = localTime.getHour();
            minute = localTime.getMinute();
            second = localTime.getSecond();
            setFractionsFromNanos(localTime.getNano());
        }

        /**
         * Updates the date and time fields from a local datetime.
         *
         * @param localDateTime
         *         local datetime
         */
        public void updateDateTime(LocalDateTime localDateTime) {
            updateDate(localDateTime.toLocalDate());
            updateTime(localDateTime.toLocalTime());
        }

        /**
         * Converts the current date field values to a local date.
         *
         * @return local date
         */
        public LocalDate toLocalDate() {
            return LocalDate.of(year, month, day);
        }

        /**
         * Converts the current time field values to a local time.
         *
         * @return local time
         */
        public LocalTime toLocalTime() {
            return LocalTime.of(hour, minute, second, getFractionsAsNanos());
        }

        /**
         * Converts the current date and time field values to a local datetime
         *
         * @return local datetime
         */
        public LocalDateTime toLocalDateTime() {
            return LocalDateTime.of(toLocalDate(), toLocalTime());
        }

    }
}
