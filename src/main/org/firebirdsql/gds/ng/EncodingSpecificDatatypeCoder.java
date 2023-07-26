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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * Datatype coder wrapping another datatype coder and applying a specific encoding definition, while delegating other
 * methods to the wrapped datatype coder.
 *
 * @author Mark Rotteveel
 * @since 4
 */
public final class EncodingSpecificDatatypeCoder implements DatatypeCoder {

    private final DatatypeCoder parentCoder;
    private final EncodingDefinition encodingDefinition;
    private final Encoding encoding;

    /**
     * Creates an encoding datatype coder.
     *
     * @param parentCoder
     *         parent datatype coder
     * @param encodingDefinition
     *         encoding definition to apply for string conversions
     */
    EncodingSpecificDatatypeCoder(DatatypeCoder parentCoder, EncodingDefinition encodingDefinition) {
        this.parentCoder = requireNonNull(parentCoder, "parentCoder");
        this.encodingDefinition = requireNonNull(encodingDefinition, "encodingDefinition");
        encoding = requireNonNull(encodingDefinition.getEncoding(), "encodingDefinition.encoding");
    }

    @Override
    public byte[] encodeString(String val) {
        return val != null ? encoding.encodeToCharset(val) : null;
    }

    @Override
    public Writer createWriter(OutputStream out) {
        return encoding.createWriter(out);
    }

    @Override
    public String decodeString(byte[] buf) {
        return buf != null ? encoding.decodeFromCharset(buf) : null;
    }

    @Override
    public Reader createReader(InputStream in) {
        return encoding.createReader(in);
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

    @Override
    public DatatypeCoder unwrap() {
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
    public int sizeOfShort() {
        return parentCoder.sizeOfShort();
    }

    @Override
    public byte[] encodeShort(short val) {
        return parentCoder.encodeShort(val);
    }

    @Override
    public byte[] encodeShort(int val) {
        return parentCoder.encodeShort(val);
    }

    @Override
    public void encodeShort(int val, byte[] buf, int off) {
        parentCoder.encodeShort(val, buf, off);
    }

    @Override
    public short decodeShort(byte[] buf) {
        return parentCoder.decodeShort(buf);
    }

    @Override
    public short decodeShort(byte[] buf, int off) {
        return parentCoder.decodeShort(buf, off);
    }

    @Override
    public byte[] encodeInt(int val) {
        return parentCoder.encodeInt(val);
    }

    @Override
    public void encodeInt(int val, byte[] buf, int off) {
        parentCoder.encodeInt(val, buf, off);
    }

    @Override
    public int decodeInt(byte[] buf) {
        return parentCoder.decodeInt(buf);
    }

    @Override
    public int decodeInt(byte[] buf, int off) {
        return parentCoder.decodeInt(buf, off);
    }

    @Override
    public byte[] encodeLong(long val) {
        return parentCoder.encodeLong(val);
    }

    @Override
    public long decodeLong(byte[] buf) {
        return parentCoder.decodeLong(buf);
    }

    @Override
    public byte[] encodeFloat(float val) {
        return parentCoder.encodeFloat(val);
    }

    @Override
    public float decodeFloat(byte[] buf) {
        return parentCoder.decodeFloat(buf);
    }

    @Override
    public byte[] encodeDouble(double val) {
        return parentCoder.encodeDouble(val);
    }

    @Override
    public double decodeDouble(byte[] buf) {
        return parentCoder.decodeDouble(buf);
    }

    @Override
    public boolean decodeBoolean(byte[] buf) {
        return parentCoder.decodeBoolean(buf);
    }

    @Override
    public byte[] encodeBoolean(boolean val) {
        return parentCoder.encodeBoolean(val);
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf) {
        return parentCoder.decodeLocalTime(buf);
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf, int off) {
        return parentCoder.decodeLocalTime(buf, off);
    }

    @Override
    public byte[] encodeLocalTime(LocalTime val) {
        return parentCoder.encodeLocalTime(val);
    }

    @Override
    public void encodeLocalTime(LocalTime val, byte[] buf, int off) {
        parentCoder.encodeLocalTime(val, buf, off);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf) {
        return parentCoder.decodeLocalDate(buf);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf, int off) {
        return parentCoder.decodeLocalDate(buf, off);
    }

    @Override
    public byte[] encodeLocalDate(LocalDate val) {
        return parentCoder.encodeLocalDate(val);
    }

    @Override
    public void encodeLocalDate(LocalDate val, byte[] buf, int off) {
        parentCoder.encodeLocalDate(val, buf, off);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf) {
        return parentCoder.decodeLocalDateTime(buf);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf, int off) {
        return parentCoder.decodeLocalDateTime(buf, off);
    }

    @Override
    public byte[] encodeLocalDateTime(LocalDateTime val) {
        return parentCoder.encodeLocalDateTime(val);
    }

    @Override
    public void encodeLocalDateTime(LocalDateTime val, byte[] buf, int off) {
        parentCoder.encodeLocalDateTime(val, buf, off);
    }

    @Override
    public Decimal64 decodeDecimal64(byte[] buf) {
        return parentCoder.decodeDecimal64(buf);
    }

    @Override
    public byte[] encodeDecimal64(Decimal64 val) {
        return parentCoder.encodeDecimal64(val);
    }

    @Override
    public Decimal128 decodeDecimal128(byte[] buf) {
        return parentCoder.decodeDecimal128(buf);
    }

    @Override
    public byte[] encodeDecimal128(Decimal128 val) {
        return parentCoder.encodeDecimal128(val);
    }

    @Override
    public BigInteger decodeInt128(byte[] buf) {
        return parentCoder.decodeInt128(buf);
    }

    @Override
    public byte[] encodeInt128(BigInteger val) {
        return parentCoder.encodeInt128(val);
    }

    @Override
    public IEncodingFactory getEncodingFactory() {
        return parentCoder.getEncodingFactory();
    }

}
