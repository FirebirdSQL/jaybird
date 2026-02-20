// SPDX-FileCopyrightText: Copyright 2017-2026 Mark Rotteveel
// SPDX-License-Identifier: LGPL-2.1-or-later
package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingDefinition;
import org.firebirdsql.encodings.IEncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.jspecify.annotations.Nullable;

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
    public byte @Nullable [] encodeString(@Nullable String val) {
        return val != null ? encoding.encodeToCharset(val) : null;
    }

    @Override
    public Writer createWriter(OutputStream out) {
        return encoding.createWriter(out);
    }

    @Override
    public @Nullable String decodeString(byte @Nullable [] buf) {
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
    public boolean equals(@Nullable Object o) {
        if (o == this) return true;
        if (!(o instanceof DatatypeCoder other)) return false;
        if (other instanceof EncodingSpecificDatatypeCoder otherSpecific) {
            return encodingDefinition.equals(otherSpecific.encodingDefinition)
                   && parentCoder.getClass() == otherSpecific.parentCoder.getClass();
        } else {
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
    public short decodeShort(byte @Nullable [] buf) {
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
    public int decodeInt(byte @Nullable [] buf) {
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
    public long decodeLong(byte @Nullable [] buf) {
        return parentCoder.decodeLong(buf);
    }

    @Override
    public byte[] encodeFloat(float val) {
        return parentCoder.encodeFloat(val);
    }

    @Override
    public float decodeFloat(byte @Nullable [] buf) {
        return parentCoder.decodeFloat(buf);
    }

    @Override
    public byte[] encodeDouble(double val) {
        return parentCoder.encodeDouble(val);
    }

    @Override
    public double decodeDouble(byte @Nullable [] buf) {
        return parentCoder.decodeDouble(buf);
    }

    @Override
    public boolean decodeBoolean(byte @Nullable [] buf) {
        return parentCoder.decodeBoolean(buf);
    }

    @Override
    public byte[] encodeBoolean(boolean val) {
        return parentCoder.encodeBoolean(val);
    }

    @Override
    public @Nullable LocalTime decodeLocalTime(byte @Nullable [] buf) {
        return parentCoder.decodeLocalTime(buf);
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf, int off) {
        return parentCoder.decodeLocalTime(buf, off);
    }

    @Override
    public byte @Nullable [] encodeLocalTime(@Nullable LocalTime val) {
        return parentCoder.encodeLocalTime(val);
    }

    @Override
    public void encodeLocalTime(LocalTime val, byte[] buf, int off) {
        parentCoder.encodeLocalTime(val, buf, off);
    }

    @Override
    public @Nullable LocalDate decodeLocalDate(byte @Nullable [] buf) {
        return parentCoder.decodeLocalDate(buf);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf, int off) {
        return parentCoder.decodeLocalDate(buf, off);
    }

    @Override
    public byte @Nullable [] encodeLocalDate(@Nullable LocalDate val) {
        return parentCoder.encodeLocalDate(val);
    }

    @Override
    public void encodeLocalDate(LocalDate val, byte[] buf, int off) {
        parentCoder.encodeLocalDate(val, buf, off);
    }

    @Override
    public @Nullable LocalDateTime decodeLocalDateTime(byte @Nullable [] buf) {
        return parentCoder.decodeLocalDateTime(buf);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf, int off) {
        return parentCoder.decodeLocalDateTime(buf, off);
    }

    @Override
    public byte @Nullable [] encodeLocalDateTime(@Nullable LocalDateTime val) {
        return parentCoder.encodeLocalDateTime(val);
    }

    @Override
    public void encodeLocalDateTime(LocalDateTime val, byte[] buf, int off) {
        parentCoder.encodeLocalDateTime(val, buf, off);
    }

    @Override
    public @Nullable Decimal64 decodeDecimal64(byte @Nullable [] buf) {
        return parentCoder.decodeDecimal64(buf);
    }

    @Override
    public byte @Nullable [] encodeDecimal64(@Nullable Decimal64 val) {
        return parentCoder.encodeDecimal64(val);
    }

    @Override
    public @Nullable Decimal128 decodeDecimal128(byte @Nullable [] buf) {
        return parentCoder.decodeDecimal128(buf);
    }

    @Override
    public byte @Nullable [] encodeDecimal128(@Nullable Decimal128 val) {
        return parentCoder.encodeDecimal128(val);
    }

    @Override
    public @Nullable BigInteger decodeInt128(byte @Nullable [] buf) {
        return parentCoder.decodeInt128(buf);
    }

    @Override
    public byte @Nullable [] encodeInt128(@Nullable BigInteger val) {
        return parentCoder.encodeInt128(val);
    }

    @Override
    public IEncodingFactory getEncodingFactory() {
        return parentCoder.getEncodingFactory();
    }

}
