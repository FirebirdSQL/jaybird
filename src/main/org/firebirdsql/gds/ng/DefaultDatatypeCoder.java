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
import java.util.GregorianCalendar;

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
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 3.0
 */
public class DefaultDatatypeCoder implements DatatypeCoder {

    private final IEncodingFactory encodingFactory;

    public DefaultDatatypeCoder(IEncodingFactory encodingFactory) {
        if (encodingFactory == null) {
            throw new NullPointerException("encodingFactory should not be null");
        }
        this.encodingFactory = encodingFactory;
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
    public short decodeShort(byte[] byte_int) {
        return (short) decodeInt(byte_int);
    }

    @Override
    public byte[] encodeInt(int value) {
        return intToBytes(value);
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
        byte ret[] = new byte[4];
        ret[0] = (byte) ((value >>> 24) & 0xff);
        ret[1] = (byte) ((value >>> 16) & 0xff);
        ret[2] = (byte) ((value >>> 8) & 0xff);
        ret[3] = (byte) ((value) & 0xff);
        return ret;
    }

    @Override
    public int decodeInt(byte[] byte_int) {
        int b1 = byte_int[0] & 0xFF;
        int b2 = byte_int[1] & 0xFF;
        int b3 = byte_int[2] & 0xFF;
        int b4 = byte_int[3] & 0xFF;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
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
        long b1 = byte_int[0] & 0xFF;
        long b2 = byte_int[1] & 0xFF;
        long b3 = byte_int[2] & 0xFF;
        long b4 = byte_int[3] & 0xFF;
        long b5 = byte_int[4] & 0xFF;
        long b6 = byte_int[5] & 0xFF;
        long b7 = byte_int[6] & 0xFF;
        long b8 = byte_int[7] & 0xFF;
        return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32)
                + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8);
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
    public byte[] encodeString(String value, String javaEncoding, String mappingPath) throws SQLException {
        // TODO mappingPath (or translator) might need to be property of DefaultDataTypeCoder itself, and not handed over at each invocation
        return encodingFactory
                .getEncodingForCharsetAlias(javaEncoding)
                .withTranslation(encodingFactory.getCharacterTranslator(mappingPath))
                .encodeToCharset(value);
    }

    @Override
    public byte[] encodeString(String value, Encoding encoding, String mappingPath) throws SQLException {
        // TODO mappingPath (or translator) might need to be property of DefaultDataTypeCoder itself, and not handed over at each invocation
        return encoding
                .withTranslation(encodingFactory.getCharacterTranslator(mappingPath))
                .encodeToCharset(value);
    }

    @Override
    public String decodeString(byte[] value, String javaEncoding, String mappingPath) throws SQLException {
        // TODO mappingPath (or translator) might need to be property of DefaultDataTypeCoder itself, and not handed over at each invocation
        return decodeString(value, encodingFactory.getEncodingForCharsetAlias(javaEncoding), mappingPath);
    }

    @Override
    public String decodeString(byte[] value, Encoding encoding, String mappingPath) throws SQLException {
        // TODO mappingPath (or translator) might need to be property of DefaultDataTypeCoder itself, and not handed over at each invocation
        return encoding
                .withTranslation(encodingFactory.getCharacterTranslator(mappingPath))
                .decodeFromCharset(value);
    }

    // times,dates...

    @Override
    public Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal) {
        return encodeTimestamp(value, cal, false);
    }

    @Override
    public Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return value;
        } else {
            long time = value.getTime() +
                    (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() -
                            Calendar.getInstance().getTimeZone().getRawOffset());

            return new Timestamp(time);
        }
    }

    @Override
    public byte[] encodeTimestamp(Timestamp value) {
        return encodeTimestampCalendar(value, new GregorianCalendar());
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

        byte[] date = d.toDateBytes();
        byte[] time = d.toTimeBytes();

        byte[] result = new byte[8];
        System.arraycopy(date, 0, result, 0, 4);
        System.arraycopy(time, 0, result, 4, 4);

        return result;
    }

    @Override
    public java.sql.Timestamp decodeTimestamp(Timestamp value, Calendar cal) {
        return decodeTimestamp(value, cal, false);
    }

    @Override
    public java.sql.Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return value;
        } else {
            long time = value.getTime() -
                    (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() -
                            Calendar.getInstance().getTimeZone().getRawOffset());

            return new Timestamp(time);
        }
    }

    @Override
    public Timestamp decodeTimestamp(byte[] byte_int) {
        return decodeTimestampCalendar(byte_int, new GregorianCalendar());
    }

    @Override
    public Timestamp decodeTimestampCalendar(byte[] byte_int, Calendar c) {
        if (byte_int.length != 8)
            throw new IllegalArgumentException("Bad parameter to decode");

        /* we have to extract time and date correctly
         * see encodeTimestamp(...) for explanations
         */

        byte[] date = new byte[4];
        byte[] time = new byte[4];

        System.arraycopy(byte_int, 0, date, 0, 4);
        System.arraycopy(byte_int, 4, time, 0, 4);

        datetime d = new datetime(date, time);
        return d.toTimestamp(c);
    }

    @Override
    public java.sql.Time encodeTime(Time d, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return d;
        } else {
            long time = d.getTime() +
                    (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() -
                            Calendar.getInstance().getTimeZone().getRawOffset());

            return new Time(time);
        }
    }

    @Override
    public byte[] encodeTime(Time d) {
        return encodeTimeCalendar(d, new GregorianCalendar());
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
            long time = d.getTime() -
                    (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() -
                            Calendar.getInstance().getTimeZone().getRawOffset());

            return new Time(time);
        }
    }

    @Override
    public Time decodeTime(byte[] int_byte) {
        return decodeTimeCalendar(int_byte, new GregorianCalendar());
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
    public byte[] encodeDate(Date d) {
        return encodeDateCalendar(d, new GregorianCalendar());
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
    public Date decodeDate(byte[] byte_int) {
        return decodeDateCalendar(byte_int, new GregorianCalendar());
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
    public byte[] encodeLocalTime(int hour, int minute, int second, int nanos) {
        datetime dt = new datetime(0, 0, 0, hour, minute, second, nanos);
        return dt.toTimeBytes();
    }

    @Override
    public byte[] encodeLocalDate(int year, int month, int day) {
        datetime dt = new datetime(year, month, day, 0, 0, 0, 0);
        return dt.toDateBytes();
    }

    @Override
    public byte[] encodeLocalDateTime(int year, int month, int day, int hour, int minute, int second, int nanos) {
        datetime dt = new datetime(year, month, day, hour, minute, second, nanos);
        byte[] date = dt.toDateBytes();
        byte[] time = dt.toTimeBytes();

        byte[] result = new byte[8];
        System.arraycopy(date, 0, result, 0, 4);
        System.arraycopy(time, 0, result, 4, 4);

        return result;
    }

    @Override
    public IEncodingFactory getEncodingFactory() {
        return encodingFactory;
    }

    /**
     * Helper Class to encode/decode times/dates
     */
    private class datetime {

        private static final int NANOSECONDS_PER_FRACTION = 100 * 1000;
        private static final int FRACTIONS_PER_MILLISECOND = 10;
        private static final int FRACTIONS_PER_SECOND = 1000 * FRACTIONS_PER_MILLISECOND;
        private static final int FRACTIONS_PER_MINUTE = 60 * FRACTIONS_PER_SECOND;
        private static final int FRACTIONS_PER_HOUR = 60 * FRACTIONS_PER_MINUTE;

        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int fractions; // Sub-second precision in 100 microseconds

        datetime(int year, int month, int day, int hour, int minute, int second, int nanos) {
            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            fractions = (nanos / NANOSECONDS_PER_FRACTION) % FRACTIONS_PER_SECOND;
        }

        datetime(Timestamp value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            fractions = value.getNanos() / NANOSECONDS_PER_FRACTION;
        }

        datetime(Date value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = 0;
            minute = 0;
            second = 0;
            fractions = 0;
        }

        datetime(Time value, Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.setTime(value);
            year = 0;
            month = 0;
            day = 0;
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            fractions = c.get(Calendar.MILLISECOND) * FRACTIONS_PER_MILLISECOND;
        }

        datetime(byte[] date, byte[] time) {

            if (date != null) {
                int sql_date = decodeInt(date);
                int century;
                sql_date -= 1721119 - 2400001;
                century = (4 * sql_date - 1) / 146097;
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
            if (time != null) {
                int fractionsInDay = decodeInt(time);
                hour = fractionsInDay / FRACTIONS_PER_HOUR;
                fractionsInDay -= hour * FRACTIONS_PER_HOUR;
                minute = fractionsInDay / FRACTIONS_PER_MINUTE;
                fractionsInDay -= minute * FRACTIONS_PER_MINUTE;
                second = fractionsInDay / FRACTIONS_PER_SECOND;
                fractions = fractionsInDay - second * FRACTIONS_PER_SECOND;
            }
        }

        byte[] toTimeBytes() {
            int fractionsInDay =
                    hour * FRACTIONS_PER_HOUR
                            + minute * FRACTIONS_PER_MINUTE
                            + second * FRACTIONS_PER_SECOND
                            + fractions;
            return encodeInt(fractionsInDay);
        }

        byte[] toDateBytes() {
            int cpMonth = month;
            int cpYear = year;
            int c, ya;

            if (cpMonth > 2) {
                cpMonth -= 3;
            } else {
                cpMonth += 9;
                cpYear -= 1;
            }

            c = cpYear / 100;
            ya = cpYear - 100 * c;

            int value = ((146097 * c) / 4 +
                    (1461 * ya) / 4 +
                    (153 * cpMonth + 2) / 5 +
                    day + 1721119 - 2400001);
            return encodeInt(value);
        }

        Time toTime(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, 1970);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            c.set(Calendar.MILLISECOND, fractions / FRACTIONS_PER_MILLISECOND);
            return new Time(c.getTime().getTime());
        }

        Timestamp toTimestamp(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.DAY_OF_MONTH, day);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            Timestamp timestamp = new Timestamp(c.getTime().getTime());
            timestamp.setNanos(fractions * NANOSECONDS_PER_FRACTION);
            return timestamp;
        }

        Date toDate(Calendar cOrig) {
            Calendar c = (Calendar) cOrig.clone();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month - 1);
            c.set(Calendar.DAY_OF_MONTH, day);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return new Date(c.getTime().getTime());
        }
    }
}
