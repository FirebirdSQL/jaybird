/*
 * Firebird Open Source J2ee connector - jdbc driver
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
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
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

import java.util.Calendar;
import java.util.GregorianCalendar;
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
//    public int sqlind;
    public String sqlname;
    public String relname;
    public String ownname;
    public String aliasname;

    public XSQLVAR() {
    }

    public XSQLVAR(byte[] sqldata) {
        this.sqldata = sqldata;
    }
    //
    // numbers
    //
    public final static byte[] encodeShort(short value){
        return encodeInt(value);
    }

    public final static short decodeShort(byte[] byte_int){
        return (short) decodeInt(byte_int);		 
    }

    public final static byte[] encodeInt(int value){
        byte ret[] = new byte[4];
        ret[0] = (byte) ((value >>> 24) & 0xff);
        ret[1] = (byte) ((value >>> 16) & 0xff);
        ret[2] = (byte) ((value >>> 8) & 0xff);
        ret[3] = (byte) ((value >>> 0) & 0xff);
        return ret;
    }

    public final static int decodeInt(byte[] byte_int){
        int b1 = byte_int[0]&0xFF;
        int b2 = byte_int[1]&0xFF;
        int b3 = byte_int[2]&0xFF;
        int b4 = byte_int[3]&0xFF;
        return ((b1 << 24) + (b2 << 16) + (b3 << 8) + (b4 << 0));
    }

    public final static byte[] encodeLong(long value){
        byte[] ret = new byte[8];
        ret[0] = (byte) (value >>> 56 & 0xFF);
        ret[1] = (byte) (value >>> 48 & 0xFF);
        ret[2] = (byte) (value >>> 40 & 0xFF);
        ret[3] = (byte) (value >>> 32 & 0xFF);
        ret[4] = (byte) (value >>> 24 & 0xFF);
        ret[5] = (byte) (value >>> 16 & 0xFF);
        ret[6] = (byte) (value >>>  8 & 0xFF);
        ret[7] = (byte) (value >>>  0 & 0xFF);
        return ret;
    }

    public final static long decodeLong(byte[] byte_int){
        long b1 = byte_int[0]&0xFF;
        long b2 = byte_int[1]&0xFF;
        long b3 = byte_int[2]&0xFF;
        long b4 = byte_int[3]&0xFF;
        long b5 = byte_int[4]&0xFF;
        long b6 = byte_int[5]&0xFF;
        long b7 = byte_int[6]&0xFF;
        long b8 = byte_int[7]&0xFF;
        return ((b1 << 56) + (b2 << 48) + (b3 << 40) + (b4 << 32) 
        + (b5 << 24) + (b6 << 16) + (b7 << 8) + (b8 << 0));
    }

    public final static byte[] encodeFloat(float value){
        return encodeInt(Float.floatToIntBits(value));
    }

    public final static float decodeFloat(byte[] byte_int){
        return Float.intBitsToFloat(decodeInt(byte_int));
    }

    public final static byte[] encodeDouble(double value){
        return encodeLong(Double.doubleToLongBits(value));
    }

    public final static double decodeDouble(byte[] byte_int){
        return Double.longBitsToDouble(decodeLong(byte_int));
    }
    //
    // Strings
    //
    public final static byte[] encodeString(String value, String encoding){
        if (encoding==null){
            return value.getBytes();
        }
        else {
            try {
                return value.getBytes(encoding);
            } catch(java.io.UnsupportedEncodingException ex) {
                return value.getBytes();
            }
        }
	 }
    public final static byte[] encodeString(byte[] value, String encoding){
        if (encoding == null)
            return value;
        else {
            try {
                return (new String(value, encoding)).getBytes();
            } catch(java.io.UnsupportedEncodingException ex) {
                return value;
            }
		  }
	 }
    public final static String decodeString(byte[] value, String encoding){
        if (encoding == null)
            return new String(value);
        else {
            try {
                return new String(value, encoding);
            } catch(java.io.UnsupportedEncodingException ex) {
                return new String(value);
            }
        }
	 }
    // 
    // times,dates...
    // 
    public final static java.sql.Timestamp encodeTimestamp(java.sql.Timestamp value, Calendar cal){
        if (cal == null) {
            return value;
        }
        else {
            long time = value.getTime() - cal.getTimeZone().getRawOffset();				
            return new java.sql.Timestamp(time);
        }
	 }
	 
    public final static byte[] encodeTimestamp(java.sql.Timestamp value){

        // note, we cannot simply pass millis to the database, because
        // Firebird stores timestamp in format (citing Ann W. Harrison):
        //
        // "[timestamp is] stored a two long words, one representing 
        // the number of days since 17 Nov 1858 and one representing number 
        // of 100 nano-seconds since midnight"
        datetime d = new datetime(value);

        byte[] date = d.toDateBytes();
        byte[] time = d.toTimeBytes();

        byte[] result = new byte[8];
        System.arraycopy(date, 0, result, 0, 4);
        System.arraycopy(time, 0, result, 4, 4);

        return result;
    }

    public final static java.sql.Timestamp decodeTimestamp(java.sql.Timestamp value, Calendar cal){
        if (cal == null) {
            return value;
        }
        else {
            long time = value.getTime() + cal.getTimeZone().getRawOffset();				
            return new java.sql.Timestamp(time);
        }
	 }

    public final static java.sql.Timestamp decodeTimestamp(byte[] byte_int){
        
        if (byte_int.length != 8)
            throw new IllegalArgumentException("Bad parameter to decode");

        // we have to extract time and date correctly
        // see encodeTimestamp(...) for explanations

        byte[] date = new byte[4];
        byte[] time = new byte[4];
        
        System.arraycopy(byte_int, 0, date, 0, 4);
        System.arraycopy(byte_int, 4, time, 0, 4);

        datetime d = new datetime(date,time);
        return d.toTimestamp();
    }

    public final static java.sql.Time encodeTime(java.sql.Time d, Calendar cal) {
        if (cal == null) {
            return d;
        }
        else {
            cal.setTime(d);
            return new java.sql.Time(cal.getTime().getTime());
        }
    }
	 
    public final static byte[] encodeTime(java.sql.Time d) {
        datetime dt = new datetime(d);
        return dt.toTimeBytes();
    }

    public final static java.sql.Time decodeTime(java.sql.Time d, Calendar cal) {
        if (cal == null) {
            return d;
        }
        else {
            cal.setTime(d);
            return new java.sql.Time(cal.getTime().getTime());    
        }
    }
	 
    public final static java.sql.Time decodeTime(byte[] int_byte) {
        datetime dt = new datetime(null,int_byte);
        return dt.toTime();
    }

    public final static java.sql.Date encodeDate(java.sql.Date d, Calendar cal) {
        if (cal == null) {
            return (d);
        }
        else {
            cal.setTime(d);            
            return new java.sql.Date(cal.getTime().getTime());
        }
    }
	 
    public final static byte[] encodeDate(java.sql.Date d) {
        datetime dt = new datetime(d);
        return dt.toDateBytes();
    }

    public final static java.sql.Date decodeDate(java.sql.Date d, Calendar cal) {
        if (cal == null || d == null) {
            return d;
        } 
        else {
            cal.setTime(d);
            return new java.sql.Date(cal.getTime().getTime());    
        }
    }
	 
    public final static java.sql.Date decodeDate(byte[] byte_int) {
        datetime dt = new datetime(byte_int, null);
        return dt.toDate();
    }

	 //
	 // Helper Class to encode/decode times/dates
	 //
	 private static class datetime{
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;
        int millisecond;

        datetime(java.sql.Timestamp value){
            Calendar c = new GregorianCalendar();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH)+1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            millisecond = value.getNanos()/1000000;
        }

        datetime(java.util.Date value){
            Calendar c = new GregorianCalendar();
            c.setTime(value);
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH)+1;
            day = c.get(Calendar.DAY_OF_MONTH);
            hour = 0;
            minute = 0;
            second = 0;
            millisecond = 0;
        }

        datetime(java.sql.Time value){
            Calendar c = new GregorianCalendar();
            c.setTime(value);
            year = 0;
            month = 0;
            day = 0;
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
            second = c.get(Calendar.SECOND);
            millisecond = c.get(Calendar.MILLISECOND);
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
                int millisInDay = decodeInt(time)/10;
                hour = millisInDay / 3600000;
                minute = (millisInDay - hour*3600000) / 60000;
                second = (millisInDay - hour*3600000 - minute * 60000) / 1000;
                millisecond = millisInDay - hour*3600000 - minute * 60000 - second * 1000;
            }
        }

        byte[] toTimeBytes(){
            int millisInDay = (hour * 3600000 + minute * 60000 + second * 1000 + millisecond)*10; 
            return encodeInt(millisInDay);
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

        java.sql.Time toTime(){
            Calendar c = new GregorianCalendar();
            c.set(Calendar.HOUR_OF_DAY,hour);
            c.set(Calendar.MINUTE,minute);
            c.set(Calendar.SECOND,second);
            c.set(Calendar.MILLISECOND,millisecond);			 
            return new java.sql.Time(c.getTime().getTime());
        }

        java.sql.Timestamp toTimestamp(){
            Calendar c = new GregorianCalendar();
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month-1);
            c.set(Calendar.DAY_OF_MONTH,day);
            c.set(Calendar.HOUR_OF_DAY,hour);
            c.set(Calendar.MINUTE,minute);
            c.set(Calendar.SECOND,second);
            c.set(Calendar.MILLISECOND,millisecond);
            return new java.sql.Timestamp(c.getTime().getTime());
        }

        java.sql.Date toDate(){
            Calendar c = new GregorianCalendar();
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month-1);
            c.set(Calendar.DAY_OF_MONTH,day);
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);															
            return new java.sql.Date(c.getTime().getTime());
        }
    }
}