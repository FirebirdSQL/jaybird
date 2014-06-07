/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * The Original Code is the Firebird Java GDS implementation.
 *
 * The Initial Developer of the Original Code is Alejandro Alberola.
 * Portions created by Alejandro Alberola are Copyright (C) 2001
 * Boix i Oltra, S.L. All Rights Reserved.
 *
 */
package org.firebirdsql.gds;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.firebirdsql.encodings.Encoding;
import org.firebirdsql.encodings.EncodingFactory;

/**
 * The class <code>XSQLDA</code> is a java mapping of the XSQLVAR server
 * data structure used to represent one column for input and output.
 *
 * @author <a href="mailto:alberola@users.sourceforge.net">Alejandro Alberola</a>
 * @version 1.0
 */
public class XSQLVAR {
    
    public int sqltype;
    public int sqlscale;
    public int sqlsubtype;
    public int sqllen;
    public byte[] sqldata;
    public Object cachedobject;
//    public int sqlind;
    public String sqlname;
    public String relname;
    public String relaliasname;
    public String ownname;
    public String aliasname;

    protected Encoding coder;

    public XSQLVAR() {
    }

    /**
     * Constructor for metadata XSQLVAR.
     *
     * @param sqltype Column SQL type
     * @param sqllen Column length
     * @param sqlname Column name
     * @param relname Column table
     */
    public XSQLVAR(int sqltype, int sqllen, String sqlname, String relname) {
        this.sqltype = sqltype;
        this.sqllen = sqllen;
        this.sqlname = sqlname;
        this.relname = relname;
    }
    
    /**
     * Get a deep copy of this object.
     *  
     * @return deep copy of this object.
     */
    public XSQLVAR deepCopy() {
        XSQLVAR result = new XSQLVAR();
        result.copyFrom(this);
        return result;
    }
    
    /**
     * Copy constructor. Initialize this instance of <code>XSQLVAR</code> with
     * values from another instance.
     *
     * @param original The other instance of <code>XSQLVAR</code> to be used
     *        as the base for initializing this instance
     */
    public void copyFrom(XSQLVAR original) {
        copyFrom(original, true);
    }
    
    /**
     * Copy constructor. Initialize this instance of <code>XSQLVAR</code> with
     * values from another instance.
     *
     * @param original The other instance of <code>XSQLVAR</code> to be used
     *        as the base for initializing this instance
     */
    public void copyFrom(XSQLVAR original, boolean copyData) {
        this.sqltype = original.sqltype;
        this.sqlscale = original.sqlscale;
        this.sqlsubtype = original.sqlsubtype;
        this.sqllen = original.sqllen;
        
        if (original.sqldata != null && copyData) {
            this.sqldata = new byte[original.sqldata.length];
            System.arraycopy(original.sqldata, 0, this.sqldata, 0, original.sqldata.length);
        } else
            this.sqldata = null;
        
        this.sqlname = original.sqlname;
        this.relname = original.relname;
        this.relaliasname = original.relaliasname;
        this.ownname = original.ownname;
        this.aliasname = original.aliasname;
    }

    // numbers

    /**
     * Encode a <code>short</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a 
     *         <code>byte</code> array
     */
    public byte[] encodeShort(short value){
        return encodeInt(value);
    }

    /**
     * Encode a <code>short</code> value as a <code>byte</code> array in network-order(big-endian) representation.
     * <p>
     * NOTE: Implementation is identical to {@link #intToBytes(int)}
     * </p>
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    public static byte[] shortToBytes(short value) {
        return intToBytes(value);
    }

    /**
     * Decode a <code>byte</code> array into a <code>short</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>short</code> value of the decoded 
     *         <code>byte</code> array
     */
    public short decodeShort(byte[] byte_int){
        return (short) decodeInt(byte_int);		 
    }

    /**
     * Encode an <code>int</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a 
     *         <code>byte</code> array
     */
    public byte[] encodeInt(int value){
        return intToBytes(value);
    }

    /**
     * Encode an <code>int</code> value as a <code>byte</code> array in network-order(big-endian) representation.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    public static byte[] intToBytes(int value) {
        byte ret[] = new byte[4];
        ret[0] = (byte) ((value >>> 24) & 0xff);
        ret[1] = (byte) ((value >>> 16) & 0xff);
        ret[2] = (byte) ((value >>> 8) & 0xff);
        ret[3] = (byte) ((value) & 0xff);
        return ret;
    }

    /**
     * Decode a <code>byte</code> array into an <code>int</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>int</code> value of the decoded 
     *         <code>byte</code> array
     */
    public int decodeInt(byte[] byte_int){
        int b1 = byte_int[0]&0xFF;
        int b2 = byte_int[1]&0xFF;
        int b3 = byte_int[2]&0xFF;
        int b4 = byte_int[3]&0xFF;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + b4);
    }

    /**
     * Encode a <code>long</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a 
     *         <code>byte</code> array
     */
    public byte[] encodeLong(long value){
        return longToBytes(value);
    }

    /**
     * Encode a <code>long</code> value as a <code>byte</code> array in network-order(big-endian) representation.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a
     *         <code>byte</code> array
     */
    public static byte[] longToBytes(long value) {
        byte[] ret = new byte[8];
        ret[0] = (byte) (value >>> 56 & 0xFF);
        ret[1] = (byte) (value >>> 48 & 0xFF);
        ret[2] = (byte) (value >>> 40 & 0xFF);
        ret[3] = (byte) (value >>> 32 & 0xFF);
        ret[4] = (byte) (value >>> 24 & 0xFF);
        ret[5] = (byte) (value >>> 16 & 0xFF);
        ret[6] = (byte) (value >>>  8 & 0xFF);
        ret[7] = (byte) (value & 0xFF);
        return ret;
    }

    /**
     * Decode a <code>byte</code> array into a <code>long</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>long</code> value of the decoded 
     *         <code>byte</code> array
     */
    public long decodeLong(byte[] byte_int){
        long b1 = byte_int[0]&0xFF;
        long b2 = byte_int[1]&0xFF;
        long b3 = byte_int[2]&0xFF;
        long b4 = byte_int[3]&0xFF;
        long b5 = byte_int[4]&0xFF;
        long b6 = byte_int[5]&0xFF;
        long b7 = byte_int[6]&0xFF;
        long b8 = byte_int[7]&0xFF;
        return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32) 
        + (b5 << 24) + (b6 << 16) + (b7 << 8) + b8);
    }

    /**
     * Encode a <code>float</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a 
     *         <code>byte</code> array
     */
    public byte[] encodeFloat(float value){
        return encodeInt(Float.floatToIntBits(value));
    }

    /**
     * Decode a <code>byte</code> array into a <code>float</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>float</code> value of the decoded 
     *         <code>byte</code> array
     */
    public float decodeFloat(byte[] byte_int){
        return Float.intBitsToFloat(decodeInt(byte_int));
    }

    /**
     * Encode a <code>double</code> value as a <code>byte</code> array.
     *
     * @param value The value to be encoded
     * @return The value of <code>value</code> encoded as a 
     *         <code>byte</code> array
     */
    public byte[] encodeDouble(double value){
        return encodeLong(Double.doubleToLongBits(value));
    }

    /**
     * Decode a <code>byte</code> array into a <code>double</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The <code>double</code> value of the decoded 
     *         <code>byte</code> array
     */
    public double decodeDouble(byte[] byte_int){
        return Double.longBitsToDouble(decodeLong(byte_int));
    }

    // Strings with mapping

    /**
     * Encode a <code>String</code> value into a <code>byte</code> array using
     * a given encoding.
     *
     * @param value The <code>String</code> to be encoded
     * @param encoding The encoding to use in the encoding process
     * @param mappingPath The character mapping path to be used in the encoding
     * @return The value of <code>value</code> as a <code>byte</code> array
     * @throws SQLException if the given encoding cannot be found, or an error 
     *         occurs during the encoding
     */
    public byte[] encodeString(String value, String encoding, String mappingPath) throws SQLException {
        if (coder == null)
            coder = EncodingFactory.getEncoding(encoding, mappingPath);
        return coder.encodeToCharset(value);
    }

    /**
     * Encode a <code>byte</code> array using a given encoding.
     *
     * @param value The <code>byte</code> array to be encoded
     * @param encoding The encoding to use in the encoding process
     * @param mappingPath The character mapping path to be used in the encoding
     * @return The value of <code>value</code> encoded using the given encoding
     * @throws SQLException if the given encoding cannot be found, or an error 
     *         occurs during the encoding
     */
    public byte[] encodeString(byte[] value, String encoding, String mappingPath)throws SQLException {
        if (encoding == null)
            return value;
        else {
            if (coder == null)
                coder = EncodingFactory.getEncoding(encoding, mappingPath);
            return coder.encodeToCharset(coder.decodeFromCharset(value));
        }
    }

    /**
     * Decode an encoded <code>byte</code> array into a <code>String</code> 
     * using a given encoding.
     *
     * @param value The value to be decoded
     * @param encoding The encoding to be used in the decoding process
     * @param mappingPath The character mapping path to be used in the decoding
     * @return The decoded <code>String</code>
     * @throws SQLException if the given encoding cannot be found, or an
     *         error occurs during the decoding
     */
    public String decodeString(byte[] value, String encoding, String mappingPath) throws SQLException{
        if (coder == null)
            coder = EncodingFactory.getEncoding(encoding, mappingPath);
        return coder.decodeFromCharset(value);
    }
    
    // times,dates...
   
    /**
     * Encode a <code>Timestamp</code> using a given <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @param cal The <code>Calendar</code> to be used for encoding,
     *        may be <code>null</code>
     */
    public Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal){
        return encodeTimestamp(value, cal, false);
    }
    
    /**
     * Encode a <code>Timestamp</code> using a given <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @param cal The <code>Calendar</code> to be used for encoding, 
     *        may be <code>null</code>
     * @param invertTimeZone If <code>true</code>, the timezone offset value 
     *        will be subtracted from the encoded value, otherwise it will
     *        be added
     * @return The encoded <code>Timestamp</code>
     */
    public Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal, boolean invertTimeZone){
        if (cal == null) {
            return value;
        }
        else {
            long time = value.getTime() + 
                (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() - 
                Calendar.getInstance().getTimeZone().getRawOffset());
            
            return new Timestamp(time);
        }
    }

    /**
     * Encode a <code>Timestamp</code> as a <code>byte</code> array.
     *
     * @param value The <code>Timestamp</code> to be encoded
     * @return The array of <code>byte</code>s that represents the given
     *         <code>Timestamp</code> value
     */
    public byte[] encodeTimestamp(Timestamp value){
    	return encodeTimestampCalendar(value, new GregorianCalendar());
    }
    	
	public byte[] encodeTimestampCalendar(Timestamp value, Calendar c){

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

    /**
     * Decode a <code>Timestamp</code> value using a given 
     * <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in decoding, 
     *        may be <code>null</code>
     * @return The decoded <code>Timestamp</code>
     */
    public java.sql.Timestamp decodeTimestamp(Timestamp value, Calendar cal) {
        return decodeTimestamp(value, cal, false);
    }

    /**
     * Decode a <code>Timestamp</code> value using a given 
     * <code>Calendar</code>.
     *
     * @param value The <code>Timestamp</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in decoding, 
     *        may be <code>null</code>
     * @param invertTimeZone If <code>true</code>, the timezone offset value 
     *        will be subtracted from the decoded value, otherwise it will
     *        be added
     * @return The encoded <code>Timestamp</code>
     */
    public java.sql.Timestamp decodeTimestamp(Timestamp value, Calendar cal, boolean invertTimeZone){
        if (cal == null) {
            return value;
        }
        else {
            long time = value.getTime() - 
                (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() - 
                 Calendar.getInstance().getTimeZone().getRawOffset());
            
            return new Timestamp(time);
        }
    }

    /**
     * Decode a <code>byte</code> array into a <code>Timestamp</code>.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return A <code>Timestamp</code> value from the decoded 
     *         <code>byte</code>s
     */
    public Timestamp decodeTimestamp(byte[] byte_int){
    	return decodeTimestampCalendar(byte_int, new GregorianCalendar());
    }
    
	public Timestamp decodeTimestampCalendar(byte[] byte_int, Calendar c){

        
        if (byte_int.length != 8)
            throw new IllegalArgumentException("Bad parameter to decode");

        /* we have to extract time and date correctly
         * see encodeTimestamp(...) for explanations
         */

        byte[] date = new byte[4];
        byte[] time = new byte[4];
        
        System.arraycopy(byte_int, 0, date, 0, 4);
        System.arraycopy(byte_int, 4, time, 0, 4);

        datetime d = new datetime(date,time);
        return d.toTimestamp(c);
    }

    /**
     * Encode a given <code>Time</code> value using a given 
     * <code>Calendar</code>.
     *
     * @param d The <code>Time</code> to be encoded
     * @param cal The <code>Calendar</code> to be used in the encoding,
     *        may be <code>null</code>
     * @return The encoded <code>Time</code>
     */
    public java.sql.Time encodeTime(Time d, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return d;
        }
        else {
            long time = d.getTime() + 
            (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() - 
            Calendar.getInstance().getTimeZone().getRawOffset());
        
            return new Time(time);
        }
    }

    /**
     * Encode a <code>Time</code> value into a <code>byte</code> array.
     *
     * @param d The <code>Time</code> to be encoded
     * @return The array of <code>byte</code>s representing the given 
     *         <code>Time</code>
     */
    public byte[] encodeTime(Time d) {
    	return encodeTimeCalendar(d, new GregorianCalendar());
    }
    	
	public byte[] encodeTimeCalendar(Time d, Calendar c) {

        datetime dt = new datetime(d, c);
        return dt.toTimeBytes();
    }

    /**
     * Decode a <code>Time</code> value using a given <code>Calendar</code>.
     *
     * @param d The <code>Time</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in the decoding, may
     *        be <code>null</code>
     * @return The decooded <code>Time</code>
     */
    public java.sql.Time decodeTime(java.sql.Time d, Calendar cal, boolean invertTimeZone) {
        if (cal == null) {
            return d;
        }
        else {
            long time = d.getTime() - 
            (invertTimeZone ? -1 : 1) * (cal.getTimeZone().getRawOffset() - 
             Calendar.getInstance().getTimeZone().getRawOffset());
        
            return new Time(time);
        }
    }

    /**
     * Decode a <code>byte</code> array into a <code>Time</code> value.
     *
     * @param int_byte The <code>byte</code> array to be decoded
     * @return The decoded <code>Time</code>
     */
    public Time decodeTime(byte[] int_byte) {
    	return decodeTimeCalendar(int_byte, new GregorianCalendar());
    }
    
	public Time decodeTimeCalendar(byte[] int_byte, Calendar c) {
        datetime dt = new datetime(null,int_byte);
        return dt.toTime(c);
    }

    /**
     * Encode a given <code>Date</code> value using a given 
     * <code>Calendar</code>.
     *
     * @param d The <code>Date</code> to be encoded
     * @param cal The <code>Calendar</code> to be used in the encoding,
     *        may be <code>null</code>
     * @return The encoded <code>Date</code>
     */
    public Date encodeDate(java.sql.Date d, Calendar cal) {
        if (cal == null) {
            return (d);
        }
        else {
            cal.setTime(d);
            return new Date(cal.getTime().getTime());
        }
    }

    /**
     * Encode a <code>Date</code> value into a <code>byte</code> array.
     *
     * @param d The <code>Date</code> to be encoded
     * @return The array of <code>byte</code>s representing the given 
     *         <code>Date</code>
     */
    public byte[] encodeDate(Date d) {
    	return encodeDateCalendar(d, new GregorianCalendar());
    }
    
	public byte[] encodeDateCalendar(Date d, Calendar c) {
        datetime dt = new datetime(d, c);
        return dt.toDateBytes();
    }


    /**
     * Decode a <code>Date</code> value using a given <code>Calendar</code>.
     *
     * @param d The <code>Date</code> to be decoded
     * @param cal The <code>Calendar</code> to be used in the decoding, may
     *        be <code>null</code>
     * @return The decoded <code>Date</code>
     */
    public java.sql.Date decodeDate(Date d, Calendar cal) {
        if (cal == null || d == null) {
            return d;
        } 
        else {
            cal.setTime(d);
            return new Date(cal.getTime().getTime());
        }
    }

    /**
     * Decode a <code>byte</code> array into a <code>Date</code> value.
     *
     * @param byte_int The <code>byte</code> array to be decoded
     * @return The decoded <code>Date</code>
     */
    public Date decodeDate(byte[] byte_int) {
    	return decodeDateCalendar(byte_int, new GregorianCalendar());
    }
    
	public Date decodeDateCalendar(byte[] byte_int, Calendar c) {
       datetime dt = new datetime(byte_int, null);
        return dt.toDate(c);
    }

    /**
     * Decode boolean from supplied data.
     *
     * @param data (expected) 1 bytes
     * @return <code>false</code> when 0, <code>true</code> for all other values
     */
    public boolean decodeBoolean(byte[] data) {
        return data[0] != 0;
    }

    /**
     * Encodes boolean to 1 byte data.
     *
     * @param value Boolean value to encode
     * @return <code>true</code> as 1, <code>false</code> as 0.
     */
    public byte[] encodeBoolean(boolean value) {
        return new byte[] { (byte) (value ? 1 : 0) };
    }

    /**
     * Encodes a java.time.LocalTime equivalent to time bytes.
     *
     * @param hour Number of hours (is assumed to be 0..23)
     * @param minute Number of minutes (is assumed to be 0..59)
     * @param second Number of seconds (is assumed to be 0..59)
     * @param nanos Sub-second nanoseconds (actual resolution is 100 microseconds, is assumed to be 0 .. 10^9 - 1 ns)
     * @return Byte array for time
     */
    public byte[] encodeLocalTime(int hour, int minute, int second, int nanos) {
        datetime dt = new datetime(0, 0, 0, hour, minute, second, nanos);
        return dt.toTimeBytes();
    }

    /**
     * Encodes a java.time.LocalDate equivalent to date bytes.
     *
     * @param year Year
     * @param month Month (is assumed to be 1..12)
     * @param day Day (is assumed to be valid for year and month)
     * @return Byte array for date
     */
    public byte[] encodeLocalDate(int year, int month, int day) {
        datetime dt = new datetime(year, month, day, 0, 0, 0, 0);
        return dt.toDateBytes();
    }

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
    public byte[] encodeLocalDateTime(int year, int month, int day, int hour, int minute, int second, int nanos) {
        datetime dt = new datetime(year, month, day, hour, minute, second, nanos);
        byte[] date = dt.toDateBytes();
        byte[] time = dt.toTimeBytes();

        byte[] result = new byte[8];
        System.arraycopy(date, 0, result, 0, 4);
        System.arraycopy(time, 0, result, 4, 4);

        return result;
    }

    /**
     * Helper Class to encode/decode times/dates
     */
    private class datetime{

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

        datetime(Timestamp value, Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            fractions = value.getNanos() / NANOSECONDS_PER_FRACTION;
        }

        datetime(Date value, Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH) + 1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = 0;
            minute = 0;
            second = 0;
            fractions = 0;
        }

        datetime(Time value, Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
            c.setTime(value);
            year = 0;
            month = 0;
            day = 0;
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            fractions = c.get(Calendar.MILLISECOND) * FRACTIONS_PER_MILLISECOND;
        }

        datetime(byte[] date, byte[] time){

            if (date != null){
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
            if (time != null){
                int fractionsInDay = decodeInt(time);
                hour = fractionsInDay / FRACTIONS_PER_HOUR;
                fractionsInDay -= hour * FRACTIONS_PER_HOUR;
                minute = fractionsInDay / FRACTIONS_PER_MINUTE;
                fractionsInDay -= minute * FRACTIONS_PER_MINUTE;
                second = fractionsInDay / FRACTIONS_PER_SECOND;
                fractions = fractionsInDay - second * FRACTIONS_PER_SECOND;
            }
        }

        byte[] toTimeBytes(){
            int fractionsInDay =
                    hour * FRACTIONS_PER_HOUR
                            + minute * FRACTIONS_PER_MINUTE
                            + second * FRACTIONS_PER_SECOND
                            + fractions;
            return encodeInt(fractionsInDay);
        }

        byte[] toDateBytes(){
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

        Time toTime(Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
            c.set(Calendar.YEAR, 1970);
            c.set(Calendar.MONTH, Calendar.JANUARY);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            c.set(Calendar.SECOND, second);
            c.set(Calendar.MILLISECOND, fractions / FRACTIONS_PER_MILLISECOND);
            return new Time(c.getTime().getTime());
        }

        Timestamp toTimestamp(Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
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

        Date toDate(Calendar cOrig){
        	Calendar c = (Calendar)cOrig.clone();
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
