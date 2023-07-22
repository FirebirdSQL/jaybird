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
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Calendar;
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
        return decodeShort(buf, 0);
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
        return decodeInt(buf, 0);
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
        return Float.intBitsToFloat(decodeInt(buf));
    }

    @Override
    public byte[] encodeDouble(double val) {
        return encodeLong(Double.doubleToLongBits(val));
    }

    @Override
    public double decodeDouble(byte[] buf) {
        return Double.longBitsToDouble(decodeLong(buf));
    }

    @Override
    public final byte[] encodeString(String val) {
        return encoding.encodeToCharset(val);
    }

    @Override
    public final Writer createWriter(OutputStream out) {
        return encoding.createWriter(out);
    }

    @Override
    public final String decodeString(byte[] buf) {
        return encoding.decodeFromCharset(buf);
    }

    @Override
    public final Reader createReader(InputStream in) {
        return encoding.createReader(in);
    }

    // times,dates...

    @Override
    public Timestamp encodeTimestamp(Timestamp val, Calendar c) {
        if (c == null) {
            return val;
        }
        return new Timestamp(val.getTime() + calculateOffset(c));
    }

    private int calculateOffset(Calendar cal) {
        return cal.getTimeZone().getRawOffset() - Calendar.getInstance().getTimeZone().getRawOffset();
    }

    @Override
    public byte[] encodeTimestampRaw(RawDateTimeStruct val) {
        return new datetime(val).toTimestampBytes();
    }

    @Override
    public byte[] encodeTimestampCalendar(Timestamp val, Calendar c) {

        /* note, we cannot simply pass millis to the database, because
         * Firebird stores timestamp in format (citing Ann W. Harrison):
         *
         * "[timestamp is] stored a two long words, one representing
         * the number of days since 17 Nov 1858 and one representing number
         * of 100 nano-seconds since midnight" (NOTE: It is actually 100 microseconds!)
         */
        return new datetime(val, c).toTimestampBytes();
    }

    @Override
    public Timestamp decodeTimestamp(Timestamp val, Calendar c) {
        if (c == null) {
            return val;
        }
        return new Timestamp(val.getTime() - calculateOffset(c));
    }

    @Override
    public RawDateTimeStruct decodeTimestampRaw(byte[] buf) {
        datetime d = fromLongBytes(buf);
        return d.getRaw();
    }

    @Override
    public Timestamp decodeTimestampCalendar(byte[] buf, Calendar c) {
        datetime d = fromLongBytes(buf);
        return d.toTimestamp(c);
    }

    @Override
    public Time encodeTime(Time val, Calendar c) {
        if (c == null) {
            return val;
        }
        return new Time(val.getTime() + calculateOffset(c));
    }

    @Override
    public byte[] encodeTimeRaw(RawDateTimeStruct val) {
        return new datetime(val).toTimeBytes();
    }

    @Override
    public byte[] encodeTimeCalendar(Time val, Calendar c) {
        return new datetime(val, c).toTimeBytes();
    }

    @Override
    public Time decodeTime(Time val, Calendar c) {
        if (c == null) {
            return val;
        }
        return new Time(val.getTime() - calculateOffset(c));
    }

    @Override
    public RawDateTimeStruct decodeTimeRaw(byte[] buf) {
        return new datetime(buf, -1, 0).getRaw();
    }

    @Override
    public Time decodeTimeCalendar(byte[] buf, Calendar c) {
        return new datetime(buf, -1, 0).toTime(c);
    }

    @Override
    public Date encodeDate(Date val, Calendar c) {
        if (c == null) {
            return (val);
        } else {
            c.setTime(val);
            return new Date(c.getTime().getTime());
        }
    }

    @Override
    public byte[] encodeDateRaw(RawDateTimeStruct val) {
        return new datetime(val).toDateBytes();
    }

    @Override
    public byte[] encodeDateCalendar(Date val, Calendar c) {
        return new datetime(val, c).toDateBytes();
    }

    @Override
    public Date decodeDate(Date val, Calendar c) {
        if (c == null || val == null) {
            return val;
        } else {
            c.setTime(val);
            return new Date(c.getTime().getTime());
        }
    }

    @Override
    public RawDateTimeStruct decodeDateRaw(byte[] buf) {
        return new datetime(buf, 0, -1).getRaw();
    }

    @Override
    public Date decodeDateCalendar(byte[] buf, Calendar c) {
        return new datetime(buf, 0, -1).toDate(c);
    }

    @Override
    public boolean decodeBoolean(byte[] buf) {
        return buf[0] != 0;
    }

    @Override
    public byte[] encodeBoolean(boolean val) {
        return new byte[] { (byte) (val ? 1 : 0) };
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf) {
        return decodeLocalTime(buf, 0);
    }

    @Override
    public LocalTime decodeLocalTime(byte[] buf, int off) {
        return FbDatetimeConversion.fromFbTimeUnits(decodeInt(buf, off));
    }

    @Override
    public byte[] encodeLocalTime(LocalTime val) {
        return encodeInt(FbDatetimeConversion.toFbTimeUnits(val));
    }

    @Override
    public void encodeLocalTime(LocalTime val, byte[] buf, int off) {
        encodeInt(FbDatetimeConversion.toFbTimeUnits(val), buf, off);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf) {
        return decodeLocalDate(buf, 0);
    }

    @Override
    public LocalDate decodeLocalDate(byte[] buf, int off) {
        return FbDatetimeConversion.fromModifiedJulianDate(decodeInt(buf, off));
    }

    @Override
    public byte[] encodeLocalDate(LocalDate val) {
        return encodeInt(FbDatetimeConversion.toModifiedJulianDate(val));
    }

    @Override
    public void encodeLocalDate(LocalDate val, byte[] buf, int off) {
        encodeInt(FbDatetimeConversion.toModifiedJulianDate(val), buf, off);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf) {
        return decodeLocalDateTime(buf, 0);
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] buf, int off) {
        return LocalDateTime.of(decodeLocalDate(buf, off), decodeLocalTime(buf, off + 4));
    }

    @Override
    public byte[] encodeLocalDateTime(LocalDateTime val) {
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
        return Decimal64.parseBytes(networkOrder(buf));
    }

    @Override
    public byte[] encodeDecimal64(Decimal64 val) {
        return networkOrder(val.toBytes());
    }

    @Override
    public Decimal128 decodeDecimal128(byte[] buf) {
        return Decimal128.parseBytes(networkOrder(buf));
    }

    @Override
    public byte[] encodeDecimal128(Decimal128 val) {
        return networkOrder(val.toBytes());
    }

    @Override
    public BigInteger decodeInt128(byte[] buf) {
        return new BigInteger(networkOrder(buf));
    }

    @Override
    public byte[] encodeInt128(BigInteger val) {
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

    private datetime fromLongBytes(byte[] buf) {
        if (buf.length < 8) {
            throw new IllegalArgumentException("Bad parameter to decode, require byte array of at least length 8");
        }

        // we have to extract time and date correctly see encodeTimestamp(...) for explanations
        return new datetime(buf, 0 , 4);
    }

    /**
     * Helper Class to encode/decode times/dates
     */
    private class datetime {

        private final RawDateTimeStruct raw;

        datetime(Timestamp value, Calendar cOrig) {
            this(new RawDateTimeStruct());
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw.updateDate(c);
            raw.updateTime(c, value.getNanos());
        }

        datetime(Date value, Calendar cOrig) {
            this(new RawDateTimeStruct());
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw.updateDate(c);
        }

        datetime(Time value, Calendar cOrig) {
            this(new RawDateTimeStruct());
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw.updateTime(c, -1);
        }

        datetime(byte[] buf, int offDate, int offTime) {
            final boolean hasDate = offDate != -1;
            final boolean hasTime = offTime != -1;
            raw = new RawDateTimeStruct(
                    hasDate ? decodeInt(buf, offDate) : 0, hasDate,
                    hasTime ? decodeInt(buf, offTime) : 0, hasTime);
        }

        datetime(RawDateTimeStruct raw) {
            this.raw = new RawDateTimeStruct(raw);
        }

        /**
         * @return copy of the raw data time struct contained in this datetime.
         */
        RawDateTimeStruct getRaw() {
            return new RawDateTimeStruct(raw);
        }

        byte[] toTimeBytes() {
            return encodeInt(raw.getEncodedTime());
        }

        byte[] toDateBytes() {
            return encodeInt(raw.getEncodedDate());
        }

        byte[] toTimestampBytes() {
            byte[] result = new byte[8];
            encodeInt(raw.getEncodedDate(), result, 0);
            encodeInt(raw.getEncodedTime(), result, 4);
            return result;
        }

        Time toTime(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, 1970);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, raw.hour);
            c.set(Calendar.MINUTE, raw.minute);
            c.set(Calendar.SECOND, raw.second);
            c.set(Calendar.MILLISECOND, raw.fractions / FRACTIONS_PER_MILLISECOND);
            return new Time(c.getTimeInMillis());
        }

        Timestamp toTimestamp(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, raw.year);
            c.set(Calendar.MONTH, raw.month - 1);
            c.set(Calendar.DAY_OF_MONTH, raw.day);
            c.set(Calendar.HOUR_OF_DAY, raw.hour);
            c.set(Calendar.MINUTE, raw.minute);
            c.set(Calendar.SECOND, raw.second);
            Timestamp timestamp = new Timestamp(c.getTimeInMillis());
            timestamp.setNanos(raw.getFractionsAsNanos());
            return timestamp;
        }

        Date toDate(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, raw.year);
            c.set(Calendar.MONTH, raw.month - 1);
            c.set(Calendar.DAY_OF_MONTH, raw.day);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return new Date(c.getTimeInMillis());
        }

    }
}
