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
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.jaybird.util.FbDatetimeConversion;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;

/**
 * The default datatype coder.
 * <p>
 * Implements the encoding and decoding for the wire protocol.
 * </p>
 * <p>
 * As a lot of the implementation also applies to the big endian and little endian decoders for the JNA implementation,
 * this class is not placed in package {@link org.firebirdsql.gds.ng.wire}
 * </p>
 *
 * @author Mark Rotteveel
 * @since 3
 */
public class DefaultDatatypeCoder implements DatatypeCoder {

    private static final System.Logger logger = System.getLogger(DefaultDatatypeCoder.class.getName());
    private static final int DEFAULT_DATATYPE_CODER_CACHE_SIZE = 10;
    private static final int DATATYPE_CODER_CACHE_SIZE = Math.max(1,
            JaybirdSystemProperties.getDatatypeCoderCacheSize(DEFAULT_DATATYPE_CODER_CACHE_SIZE));
    private static final int LOG_CACHE_MAINTENANCE_WARNING = 10;

    private final IEncodingFactory encodingFactory;
    private final Encoding encoding;

    private final ConcurrentMap<EncodingDefinition, DatatypeCoder> encodingSpecificDatatypeCoders =
            new ConcurrentHashMap<>(DATATYPE_CODER_CACHE_SIZE);
    private final Lock cacheMaintenanceLock = new ReentrantLock();
    private int cacheMaintenanceCount = 0;

    /**
     * Returns an instance of {@code DefaultDatatypeCoder} for an encoding factory.
     *
     * @param encodingFactory
     *         encoding factory
     * @return datatype coder, this might be a cached instance
     */
    public static DefaultDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory.getOrCreateDatatypeCoder(DefaultDatatypeCoder.class, DefaultDatatypeCoder::new);
    }

    /**
     * Creates a default datatype coder for the wire protocol.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory
     *         encoding factory
     */
    public DefaultDatatypeCoder(IEncodingFactory encodingFactory) {
        this.encodingFactory = requireNonNull(encodingFactory, "encodingFactory");
        encoding = encodingFactory.getDefaultEncoding();
    }

    @Override
    public int sizeOfShort() {
        return 4;
    }

    @Override
    public byte[] encodeShort(short val) {
        final byte[] buf = new byte[sizeOfShort()];
        encodeShort(val, buf, 0);
        return buf;
    }

    @Override
    public byte[] encodeShort(int val) {
        return encodeShort((short) val);
    }

    @Override
    public void encodeShort(int val, byte[] buf, int off) {
        encodeInt(val, buf, off);
    }

    @Override
    public short decodeShort(byte[] buf) {
        return buf != null ? decodeShort(buf, 0) : 0;
    }

    @Override
    public short decodeShort(byte[] buf, int off) {
        return (short) decodeInt(buf, off);
    }

    @Override
    public byte[] encodeInt(int val) {
        byte[] buf = new byte[4];
        encodeInt(val, buf, 0);
        return buf;
    }

    @Override
    public void encodeInt(int val, byte[] buf, int off) {
        buf[off] = (byte) (val >>> 24);
        buf[off + 1] = (byte) (val >>> 16);
        buf[off + 2] = (byte) (val >>> 8);
        buf[off + 3] = (byte) val;
    }

    @Override
    public int decodeInt(byte[] buf) {
        return buf != null ? decodeInt(buf, 0) : 0;
    }

    @Override
    public int decodeInt(byte[] buf, int off) {
        return (buf[off] << 24) +
               ((buf[off + 1] & 0xFF) << 16) +
               ((buf[off + 2] & 0xFF) << 8) +
               (buf[off + 3] & 0xFF);
    }

    @Override
    public byte[] encodeLong(long val) {
        final byte[] buf = new byte[8];
        buf[0] = (byte) (val >>> 56);
        buf[1] = (byte) (val >>> 48);
        buf[2] = (byte) (val >>> 40);
        buf[3] = (byte) (val >>> 32);
        buf[4] = (byte) (val >>> 24);
        buf[5] = (byte) (val >>> 16);
        buf[6] = (byte) (val >>> 8);
        buf[7] = (byte) val;
        return buf;
    }

    @Override
    public long decodeLong(byte[] buf) {
        if (buf == null) return 0;
        return ((long) (buf[0]) << 56) +
               ((buf[1] & 0xFFL) << 48) +
               ((buf[2] & 0xFFL) << 40) +
               ((buf[3] & 0xFFL) << 32) +
               ((buf[4] & 0xFFL) << 24) +
               ((buf[5] & 0xFFL) << 16) +
               ((buf[6] & 0xFFL) << 8) +
               (buf[7] & 0xFFL);
    }

    @Override
    public byte[] encodeFloat(float val) {
        return encodeInt(Float.floatToIntBits(val));
    }

    @Override
    public float decodeFloat(byte[] buf) {
        return buf != null ? Float.intBitsToFloat(decodeInt(buf)) : 0;
    }

    @Override
    public byte[] encodeDouble(double val) {
        return encodeLong(Double.doubleToLongBits(val));
    }

    @Override
    public double decodeDouble(byte[] buf) {
        return buf != null ? Double.longBitsToDouble(decodeLong(buf)) : 0;
    }

    @Override
    public final byte[] encodeString(String val) {
        return val != null ? encoding.encodeToCharset(val) : null;
    }

    @Override
    public final Writer createWriter(OutputStream out) {
        return encoding.createWriter(out);
    }

    @Override
    public final String decodeString(byte[] buf) {
        return buf != null ? encoding.decodeFromCharset(buf) : null;
    }

    @Override
    public final Reader createReader(InputStream in) {
        return encoding.createReader(in);
    }

    @Override
    public boolean decodeBoolean(byte[] buf) {
        return buf != null && buf[0] != 0;
    }

    @Override
    public byte[] encodeBoolean(boolean val) {
        return new byte[] { (byte) (val ? 1 : 0) };
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf) {
        return buf != null ? decodeLocalTime(buf, 0) : null;
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf, int off) {
        return FbDatetimeConversion.fromFbTimeUnits(decodeInt(buf, off));
    }

    @Override
    public byte[] encodeLocalTime(LocalTime val) {
        return val != null ? encodeInt(FbDatetimeConversion.toFbTimeUnits(val)) : null;
    }

    @Override
    public void encodeLocalTime(LocalTime val, byte[] buf, int off) {
        encodeInt(FbDatetimeConversion.toFbTimeUnits(val), buf, off);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf) {
        return buf != null ? decodeLocalDate(buf, 0) : null;
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf, int off) {
        return FbDatetimeConversion.fromModifiedJulianDate(decodeInt(buf, off));
    }

    @Override
    public byte[] encodeLocalDate(LocalDate val) {
        return val != null ? encodeInt(FbDatetimeConversion.toModifiedJulianDate(val)) : null;
    }

    @Override
    public void encodeLocalDate(LocalDate val, byte[] buf, int off) {
        encodeInt(FbDatetimeConversion.toModifiedJulianDate(val), buf, off);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf) {
        return buf != null ? decodeLocalDateTime(buf, 0) : null;
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf, int off) {
        return LocalDateTime.of(decodeLocalDate(buf, off), decodeLocalTime(buf, off + 4));
    }

    @Override
    public byte[] encodeLocalDateTime(LocalDateTime val) {
        if (val == null) return null;
        byte[] buf = new byte[8];
        encodeLocalDateTime(val, buf, 0);
        return buf;
    }

    @Override
    public void encodeLocalDateTime(LocalDateTime val, byte[] buf, int off) {
        encodeLocalDate(val.toLocalDate(), buf, off);
        encodeLocalTime(val.toLocalTime(), buf, off + 4);
    }

    /**
     * Returns {@code buf} as an array in network byte order.
     * <p>
     * If this is a big-endian coder, {@code buf} should be returned as-is. Otherwise, a new array <em>must</em> be
     * returned with the bytes reversed, as the operation must be repeatable on the same original byte array.
     * </p>
     *
     * @param buf
     *         byte array
     * @return new byte array in network byte order (or {@code buf} if this a big-endian coder, so the array is already
     * network byte order)
     */
    protected byte[] networkOrder(final byte[] buf) {
        return buf;
    }

    @Override
    public Decimal64 decodeDecimal64(byte[] buf) {
        return buf != null ? Decimal64.parseBytes(networkOrder(buf)) : null;
    }

    @Override
    public byte[] encodeDecimal64(Decimal64 val) {
        return val != null ? networkOrder(val.toBytes()) : null;
    }

    @Override
    public Decimal128 decodeDecimal128(byte[] buf) {
        return buf != null ? Decimal128.parseBytes(networkOrder(buf)) : null;
    }

    @Override
    public byte[] encodeDecimal128(Decimal128 val) {
        return val != null ? networkOrder(val.toBytes()) : null;
    }

    @Override
    public BigInteger decodeInt128(byte[] buf) {
        return buf != null ? new BigInteger(networkOrder(buf)) : null;
    }

    @Override
    public byte[] encodeInt128(BigInteger val) {
        if (val == null) return null;
        if (val.bitLength() > 127) {
            throw new IllegalArgumentException("Received BigInteger value requires more than 16 bytes storage");
        }
        byte[] minimumBytes = val.toByteArray();
        if (minimumBytes.length == 16) {
            return networkOrder(minimumBytes);
        }
        byte[] int128Bytes = new byte[16];
        int startOfMinimum = 16 - minimumBytes.length;
        if (val.signum() == -1) {
            // extend sign
            Arrays.fill(int128Bytes, 0, startOfMinimum, (byte) -1);
        }
        System.arraycopy(minimumBytes, 0, int128Bytes, startOfMinimum, minimumBytes.length);
        return networkOrder(int128Bytes);
    }

    @Override
    public final IEncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    @Override
    public final EncodingDefinition getEncodingDefinition() {
        return encodingFactory.getDefaultEncodingDefinition();
    }

    @Override
    public final Encoding getEncoding() {
        return encoding;
    }

    @Override
    public final DatatypeCoder forEncodingDefinition(final EncodingDefinition encodingDefinition) {
        if (getEncodingDefinition().equals(encodingDefinition)) {
            return this;
        }
        return getOrCreateForEncodingDefinition(encodingDefinition);
    }

    @Override
    public DatatypeCoder unwrap() {
        return this;
    }

    private DatatypeCoder getOrCreateForEncodingDefinition(final EncodingDefinition encodingDefinition) {
        final DatatypeCoder coder = encodingSpecificDatatypeCoders.get(encodingDefinition);
        if (coder != null) {
            // existing instance in cache
            return coder;
        }
        return createForEncodingDefinition(encodingDefinition);
    }

    private DatatypeCoder createForEncodingDefinition(final EncodingDefinition encodingDefinition) {
        final DatatypeCoder newCoder = new EncodingSpecificDatatypeCoder(this, encodingDefinition);
        final DatatypeCoder coder = encodingSpecificDatatypeCoders.putIfAbsent(encodingDefinition, newCoder);
        if (coder != null) {
            // Other thread already created and added an instance; return that
            return coder;
        }
        try {
            return newCoder;
        } finally {
            if (encodingSpecificDatatypeCoders.size() > DATATYPE_CODER_CACHE_SIZE) {
                performCacheMaintenance();
            }
        }
    }

    private void performCacheMaintenance() {
        if (cacheMaintenanceLock.tryLock()) {
            try {
                // Simple but brute force maintenance: clear entire cache
                encodingSpecificDatatypeCoders.clear();
                cacheMaintenanceCount++;
            } finally {
                cacheMaintenanceLock.unlock();
            }

            if (cacheMaintenanceCount % LOG_CACHE_MAINTENANCE_WARNING == 1) {
                logger.log(System.Logger.Level.WARNING,
                        "Cleared encoding specific datatype coder cache (current reset count: {0}). "
                        + "Consider setting system property " + JaybirdSystemProperties.DATATYPE_CODER_CACHE_SIZE
                        + " to a value higher than the current maximum size of {1}",
                        cacheMaintenanceCount, DATATYPE_CODER_CACHE_SIZE);
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DatatypeCoder other)) return false;
        return getEncodingDefinition().equals(other.getEncodingDefinition())
               && getClass() == other.unwrap().getClass();
    }

    @Override
    public final int hashCode() {
        return hash(getClass(), getEncodingDefinition());
    }

}
