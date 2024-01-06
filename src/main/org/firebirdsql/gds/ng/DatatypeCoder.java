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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
     *         byte array of sufficient size (warning: this is datatype coder specific, see {@link #sizeOfShort()}),
     *         never {@code null}
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
     * @return short value from {@code buf}, or {@code 0} when {@code buf} is {@code null}
     */
    short decodeShort(byte[] buf);

    /**
     * Decode a short value from {@code buf} starting at offset {@code off} for {@link #sizeOfShort()} bytes.
     *
     * @param buf
     *         byte array of sufficient size (warning: this is datatype coder specific, see {@link #sizeOfShort()}),
     *         never {@code null}
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
     * @return {@code val} encoded as a byte array, or {@code 0} when {@code buf} is {@code null}
     */
    byte[] encodeInt(int val);

    /**
     * Encode an int value into {@code buf} starting at index {@code off} for 4 bytes.
     *
     * @param val
     *         value to be encoded
     * @param buf
     *         byte array of sufficient size, never {@code null}
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
     * @return int value decoded from {@code buf}, or {@code 0} when {@code buf} is {@code null}
     */
    int decodeInt(byte[] buf);

    /**
     * Decode an int value from {@code buf} starting at offset {@code off} for 4 bytes.
     *
     * @param buf
     *         byte array of sufficient size, never {@code null}
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
     *         byte array of sufficient size, or {@code 0} when {@code buf} is {@code null}
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
     *         byte array of sufficient size, or {@code 0} when {@code buf} is {@code null}
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
     *         byte array of sufficient size, or {@code 0} when {@code buf} is {@code null}
     * @return double value decoded from {@code buf}
     */
    double decodeDouble(byte[] buf);

    /**
     * Encode a {@code String} value as a byte array using the encoding of this datatype coder.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
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
     * @return {@code String} decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
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
     * Decode a boolean from {@code buf} from the first byte.
     *
     * @param buf
     *         (expected) 1 bytes
     * @return {@code false} when 0, {@code true} for all other values, or {@code false} if {@code buf} is {@code null}
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
     * @return {@code LocalTime} decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
     * @since 5
     */
    LocalTime decodeLocalTime(byte[] buf);

    /**
     * Decode {@code java.time.LocalTime} from {@code buf} from the 4 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 4 bytes from {@code off}, never {@code null}
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
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
     * @since 5
     */
    byte[] encodeLocalTime(LocalTime val);

    /**
     * Encode a {@code java.time.LocalTime} to a byte array, requiring 4 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 4 bytes starting at {@code off}, never {@code null}
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
     * @return {@code LocalDate} decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
     * @since 5
     */
    LocalDate decodeLocalDate(byte[] buf);

    /**
     * Decode {@code java.time.LocalDate} from {@code buf} from the 4 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 4 bytes from {@code off}, never {@code null}
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
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
     * @since 5
     */
    byte[] encodeLocalDate(LocalDate val);

    /**
     * Encode a {@code java.time.LocalDate} to a byte array, requiring 4 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 4 bytes starting at {@code off}, never {@code null}
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
     * @return {@code LocalDateTime} decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
     * @since 5
     */
    LocalDateTime decodeLocalDateTime(byte[] buf);

    /**
     * Decode {@code java.time.LocalDateTime} from {@code buf} from the 8 bytes starting at {@code off}.
     *
     * @param buf
     *         (expected) at least 8 bytes from {@code off}, never {@code null}
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
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
     * @since 5
     */
    byte[] encodeLocalDateTime(LocalDateTime val);

    /**
     * Encode a {@code java.time.LocalDateTime} to a byte array, requiring 8 bytes.
     *
     * @param val
     *         value to encode
     * @param buf
     *         byte array with at least 8 bytes starting at {@code off}, never {@code null}
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
     * @return Decimal64 decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
     */
    Decimal64 decodeDecimal64(byte[] buf);

    /**
     * Encodes a decimal64 as a byte array of 8 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
     */
    byte[] encodeDecimal64(Decimal64 val);

    /**
     * Decodes a decimal128 from a byte array of 16 bytes.
     *
     * @param buf
     *         data to decode (expects exactly 16 bytes)
     * @return Decimal128 decoded from {@code buf}, or {@code null} if {@code buf} is {@code null}
     */
    Decimal128 decodeDecimal128(byte[] buf);

    /**
     * Encodes a decimal128 as a byte array of 16 bytes.
     *
     * @param val
     *         value to be encoded
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
     */
    byte[] encodeDecimal128(Decimal128 val);

    /**
     * Decodes a BigInteger from a byte array of 16 bytes (int128 format).
     *
     * @param buf
     *         data to decode (expects exactly 16 bytes)
     * @return BigInteger decoded from {@code buf}, or {@code null} if {@code val} is {@code null}
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
     * @return {@code val} encoded as a byte array, or {@code null} if {@code val} is {@code null}
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

}
