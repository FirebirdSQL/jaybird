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
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

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
 * @since 3.0
 */
public class DefaultDatatypeCoder implements DatatypeCoder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDatatypeCoder.class);
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
     * @param encodingFactory Encoding factory
     * @return Datatype coder, this might be a cached instance
     */
    public static DefaultDatatypeCoder forEncodingFactory(IEncodingFactory encodingFactory) {
        return encodingFactory.getOrCreateDatatypeCoder(DefaultDatatypeCoder.class);
    }

    /**
     * Creates a default datatype coder for the wire protocol.
     * <p>
     * In almost all cases, it is better to use {@link #forEncodingFactory(IEncodingFactory)}.
     * </p>
     *
     * @param encodingFactory Encoding factory
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
    public byte[] encodeShort(short value) {
        return intToBytes(value);
    }

    @Override
    public byte[] encodeShort(int value) {
        return encodeShort((short) value);
    }

    @Override
    public void encodeShort(int value, byte[] target, int fromIndex) {
        encodeInt(value, target, fromIndex);
    }

    @Override
    public short decodeShort(byte[] byte_int) {
        return (short) decodeInt(byte_int);
    }

    @Override
    public short decodeShort(byte[] bytes, int fromIndex) {
        return (short) decodeInt(bytes, fromIndex);
    }

    @Override
    public byte[] encodeInt(int value) {
        return intToBytes(value);
    }

    @Override
    public void encodeInt(int value, byte[] target, int fromIndex) {
        target[fromIndex] = (byte) ((value >>> 24) & 0xff);
        target[fromIndex + 1] = (byte) ((value >>> 16) & 0xff);
        target[fromIndex + 2] = (byte) ((value >>> 8) & 0xff);
        target[fromIndex + 3] = (byte) ((value) & 0xff);
    }

    /**
     * Encode an <code>int</code> value as a <code>byte</code> array in network-order(big-endian) representation.
     *
     * @param value
     *         The value to be encoded
     * @return The value of <code>value</code> encoded as a
     * <code>byte</code> array
     */
    protected byte[] intToBytes(int value) {
        byte[] ret = new byte[4];
        ret[0] = (byte) ((value >>> 24) & 0xff);
        ret[1] = (byte) ((value >>> 16) & 0xff);
        ret[2] = (byte) ((value >>> 8) & 0xff);
        ret[3] = (byte) ((value) & 0xff);
        return ret;
    }

    @Override
    public int decodeInt(byte[] byte_int) {
        return (((byte_int[0] & 0xFF) << 24) +
                ((byte_int[1] & 0xFF) << 16) +
                ((byte_int[2] & 0xFF) << 8) +
                (byte_int[3] & 0xFF));
    }

    @Override
    public int decodeInt(byte[] bytes, int fromIndex) {
        return (((bytes[fromIndex] & 0xFF) << 24) +
                ((bytes[fromIndex + 1] & 0xFF) << 16) +
                ((bytes[fromIndex + 2] & 0xFF) << 8) +
                (bytes[fromIndex + 3] & 0xFF));
    }

    @Override
    public byte[] encodeLong(long value) {
        byte[] ret = new byte[8];
        ret[0] = (byte) (value >>> 56 & 0xFF);
        ret[1] = (byte) (value >>> 48 & 0xFF);
        ret[2] = (byte) (value >>> 40 & 0xFF);
        ret[3] = (byte) (value >>> 32 & 0xFF);
        ret[4] = (byte) (value >>> 24 & 0xFF);
        ret[5] = (byte) (value >>> 16 & 0xFF);
        ret[6] = (byte) (value >>> 8 & 0xFF);
        ret[7] = (byte) (value & 0xFF);
        return ret;
    }

    @Override
    public long decodeLong(byte[] byte_int) {
        return ((((long) (byte_int[0] & 0xFF)) << 56) +
                (((long) (byte_int[1] & 0xFF)) << 48) +
                (((long) (byte_int[2] & 0xFF)) << 40) +
                (((long) (byte_int[3] & 0xFF)) << 32) +
                (((long) (byte_int[4] & 0xFF)) << 24) +
                (((long) (byte_int[5] & 0xFF)) << 16) +
                (((long) (byte_int[6] & 0xFF)) << 8) +
                ((long) (byte_int[7] & 0xFF)));
    }

    @Override
    public byte[] encodeFloat(float value) {
        return encodeInt(Float.floatToIntBits(value));
    }

    @Override
    public float decodeFloat(byte[] byte_int) {
        return Float.intBitsToFloat(decodeInt(byte_int));
    }

    @Override
    public byte[] encodeDouble(double value) {
        return encodeLong(Double.doubleToLongBits(value));
    }

    @Override
    public double decodeDouble(byte[] byte_int) {
        return Double.longBitsToDouble(decodeLong(byte_int));
    }

    @Override
    public final byte[] encodeString(String value) {
        return encoding.encodeToCharset(value);
    }

    @Override
    public final Writer createWriter(OutputStream outputStream) {
        return encoding.createWriter(outputStream);
    }

    @Override
    public final String decodeString(byte[] value) {
        return encoding.decodeFromCharset(value);
    }

    @Override
    public final Reader createReader(InputStream inputStream) {
        return encoding.createReader(inputStream);
    }

    // times,dates...

    @Override
    public Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return value;
        } else {
            long time = value.getTime() + calculateOffset(cal, invertTimeZone);

            return new Timestamp(time);
        }
    }

    private int calculateOffset(Calendar cal, boolean invertTimeZone) {
        return (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() -
                Calendar.getInstance().getTimeZone().getRawOffset());
    }

    @Override
    public byte[] encodeTimestampRaw(RawDateTimeStruct raw) {
        return new datetime(raw).toTimestampBytes();
    }

    @Override
    public byte[] encodeTimestampCalendar(Timestamp value, Calendar c) {

        /* note, we cannot simply pass millis to the database, because
         * Firebird stores timestamp in format (citing Ann W. Harrison):
         *
         * "[timestamp is] stored a two long words, one representing
         * the number of days since 17 Nov 1858 and one representing number
         * of 100 nano-seconds since midnight" (NOTE: It is actually 100 microseconds!)
         */
        datetime d = new datetime(value, c);
        return d.toTimestampBytes();
    }

    @Override
    public java.sql.Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return value;
        } else {
            long time = value.getTime() - calculateOffset(cal, invertTimeZone);

            return new Timestamp(time);
        }
    }

    @Override
    public RawDateTimeStruct decodeTimestampRaw(byte[] byte_long) {
        datetime d = fromLongBytes(byte_long);
        return d.getRaw();
    }

    @Override
    public Timestamp decodeTimestampCalendar(byte[] byte_long, Calendar c) {
        datetime d = fromLongBytes(byte_long);
        return d.toTimestamp(c);
    }

    @Override
    public java.sql.Time encodeTime(Time d, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return d;
        } else {
            long time = d.getTime() + calculateOffset(cal, invertTimeZone);

            return new Time(time);
        }
    }

    @Override
    public byte[] encodeTimeRaw(RawDateTimeStruct raw) {
        return new datetime(raw).toTimeBytes();
    }

    @Override
    public byte[] encodeTimeCalendar(Time d, Calendar c) {
        datetime dt = new datetime(d, c);
        return dt.toTimeBytes();
    }

    @Override
    public java.sql.Time decodeTime(java.sql.Time d, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return d;
        } else {
            long time = d.getTime() - calculateOffset(cal, invertTimeZone);

            return new Time(time);
        }
    }

    @Override
    public RawDateTimeStruct decodeTimeRaw(byte[] int_byte) {
        datetime d = new datetime(null, int_byte);
        return d.getRaw();
    }

    @Override
    public Time decodeTimeCalendar(byte[] int_byte, Calendar c) {
        datetime dt = new datetime(null, int_byte);
        return dt.toTime(c);
    }

    @Override
    public Date encodeDate(java.sql.Date d, Calendar cal) {
        if (cal == null) {
            return (d);
        } else {
            cal.setTime(d);
            return new Date(cal.getTime().getTime());
        }
    }

    @Override
    public byte[] encodeDateRaw(RawDateTimeStruct raw) {
        return new datetime(raw).toDateBytes();
    }

    @Override
    public byte[] encodeDateCalendar(Date d, Calendar c) {
        datetime dt = new datetime(d, c);
        return dt.toDateBytes();
    }

    @Override
    public java.sql.Date decodeDate(Date d, Calendar cal) {
        if (cal == null || d == null) {
            return d;
        } else {
            cal.setTime(d);
            return new Date(cal.getTime().getTime());
        }
    }

    @Override
    public RawDateTimeStruct decodeDateRaw(byte[] byte_int) {
        datetime d = new datetime(byte_int, null);
        return d.getRaw();
    }

    @Override
    public Date decodeDateCalendar(byte[] byte_int, Calendar c) {
        datetime dt = new datetime(byte_int, null);
        return dt.toDate(c);
    }

    @Override
    public boolean decodeBoolean(byte[] data) {
        return data[0] != 0;
    }

    @Override
    public byte[] encodeBoolean(boolean value) {
        return new byte[] { (byte) (value ? 1 : 0) };
    }

    @Override
    public LocalTime decodeLocalTime(byte[] data) {
        datetime dt = new datetime(null, data);
        return dt.toLocalTime();
    }

    @Override
    public byte[] encodeLocalTime(LocalTime value) {
        datetime dt = new datetime(null, value);
        return dt.toTimeBytes();
    }

    @Override
    public LocalDate decodeLocalDate(byte[] data) {
        datetime dt = new datetime(data, null);
        return dt.toLocalDate();
    }

    @Override
    public byte[] encodeLocalDate(LocalDate value) {
        datetime dt = new datetime(value, null);
        return dt.toDateBytes();
    }

    @Override
    public LocalDateTime decodeLocalDateTime(byte[] data) {
        datetime dt = fromLongBytes(data);
        return dt.toLocalDateTime();
    }

    @Override
    public byte[] encodeLocalDateTime(LocalDateTime value) {
        datetime dt = new datetime(value);
        return dt.toTimestampBytes();
    }

    @Override
    public Decimal64 decodeDecimal64(byte[] data) {
        return Decimal64.parseBytes(data);
    }

    @Override
    public byte[] encodeDecimal64(Decimal64 decimal64) {
        return decimal64.toBytes();
    }

    @Override
    public Decimal128 decodeDecimal128(byte[] data) {
        return Decimal128.parseBytes(data);
    }

    @Override
    public byte[] encodeDecimal128(Decimal128 decimal128) {
        return decimal128.toBytes();
    }

    @Override
    public BigInteger decodeInt128(byte[] data) {
        return new BigInteger(data);
    }

    @Override
    public byte[] encodeInt128(BigInteger bigInteger) {
        if (bigInteger.bitLength() > 127) {
            throw new IllegalArgumentException("Received BigInteger value requires more than 16 bytes storage");
        }
        byte[] minimumBytes = bigInteger.toByteArray();
        if (minimumBytes.length == 16) {
            return minimumBytes;
        }
        byte[] int128Bytes = new byte[16];
        int startOfMinimum = 16 - minimumBytes.length;
        if (bigInteger.signum() == -1) {
            // extend sign
            Arrays.fill(int128Bytes, 0, startOfMinimum, (byte) -1);
        }
        System.arraycopy(minimumBytes, 0, int128Bytes, startOfMinimum, minimumBytes.length);
        return int128Bytes;
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

            if (cacheMaintenanceCount % LOG_CACHE_MAINTENANCE_WARNING == 1 && logger.isWarnEnabled()) {
                logger.warn("Cleared encoding specific datatype coder cache (current reset count: "
                        + cacheMaintenanceCount + "). Consider setting system property "
                        + JaybirdSystemProperties.DATATYPE_CODER_CACHE_SIZE
                        + " to a value higher than the current maximum size of " + DATATYPE_CODER_CACHE_SIZE);
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof DatatypeCoder)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        DatatypeCoder other = (DatatypeCoder) o;
        if (o instanceof EncodingSpecificDatatypeCoder) {
            // TODO This isn't correct, maybe should be unwrap().getClass() == other.unwrap().getClass()?
            return getEncodingDefinition().equals(other.getEncodingDefinition())
                    && getClass() == other.unwrap().getClass();
        } else {
            return getEncodingDefinition().equals(other.getEncodingDefinition())
                    && getClass() == other.getClass();
        }
    }

    @Override
    public final int hashCode() {
        // TODO See note in equals
        return hash(getClass(), getEncodingDefinition());
    }

    private datetime fromLongBytes(byte[] byte_long) {
        if (byte_long.length < 8) {
            throw new IllegalArgumentException("Bad parameter to decode, require byte array of at least length 8");
        }

        // we have to extract time and date correctly see encodeTimestamp(...) for explanations
        int encodedDate = decodeInt(byte_long);
        int encodedTime = decodeInt(byte_long, 4);
        return new datetime(encodedDate, encodedTime);
    }

    /**
     * Helper Class to encode/decode times/dates
     */
    private class datetime {

        private final RawDateTimeStruct raw;

        datetime(LocalDateTime localDateTime) {
            this(localDateTime.toLocalDate(), localDateTime.toLocalTime());
        }

        datetime(LocalDate localDate, LocalTime localTime) {
            raw = new RawDateTimeStruct();
            if (localDate != null) {
                raw.year = localDate.getYear();
                raw.month = localDate.getMonthValue();
                raw.day = localDate.getDayOfMonth();
            }
            if (localTime != null) {
                raw.hour = localTime.getHour();
                raw.minute = localTime.getMinute();
                raw.second = localTime.getSecond();
                raw.setFractionsFromNanos(localTime.getNano());
            }
        }

        datetime(Timestamp value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw = new RawDateTimeStruct();
            raw.year = c.get(Calendar.YEAR);
            raw.month = c.get(Calendar.MONTH) + 1;
            raw.day = c.get(Calendar.DAY_OF_MONTH);
            raw.hour = c.get(Calendar.HOUR_OF_DAY);
            raw.minute = c.get(Calendar.MINUTE);
            raw.second = c.get(Calendar.SECOND);
            raw.setFractionsFromNanos(value.getNanos());
        }

        datetime(Date value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw = new RawDateTimeStruct();
            raw.year = c.get(Calendar.YEAR);
            raw.month = c.get(Calendar.MONTH) + 1;
            raw.day = c.get(Calendar.DAY_OF_MONTH);
        }

        datetime(Time value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            raw = new RawDateTimeStruct();
            raw.hour = c.get(Calendar.HOUR_OF_DAY);
            raw.minute = c.get(Calendar.MINUTE);
            raw.second = c.get(Calendar.SECOND);
            raw.fractions = c.get(Calendar.MILLISECOND) * FRACTIONS_PER_MILLISECOND;
        }

        datetime(int encodedDate, int encodedTime) {
            raw = new RawDateTimeStruct(encodedDate, true, encodedTime, true);
        }

        datetime(byte[] date, byte[] time) {
            int encodedDate = date != null ? decodeInt(date) : 0;
            int encodedTime = time != null ? decodeInt(time) : 0;
            raw = new RawDateTimeStruct(encodedDate, date != null, encodedTime, time != null);
        }

        datetime(RawDateTimeStruct raw) {
            this.raw = new RawDateTimeStruct(raw);
        }

        /**
         * @return A copy of the raw data time struct contained in this datetime.
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

        LocalDateTime toLocalDateTime() {
            return LocalDateTime.of(toLocalDate(), toLocalTime());
        }

        LocalDate toLocalDate() {
            return LocalDate.of(raw.year, raw.month, raw.day);
        }

        LocalTime toLocalTime() {
            return LocalTime.of(raw.hour, raw.minute, raw.second, raw.getFractionsAsNanos());
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
