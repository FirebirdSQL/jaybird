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
    // Methods to encode/decode
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

    public final static byte[] encodeTimestamp(java.sql.Timestamp value){
        return encodeLong(value.getTime());
    }

    public final static java.sql.Timestamp decodeTimestamp(byte[] byte_int){
        return new java.sql.Timestamp(decodeLong(byte_int));
    }
/*
    public final static byte[] encodeTime(java.sql.Time value) {
        long millis = value.getTime();
        // only take time data, not date
        millis = millis % 3600000;
        System.out.println("Time entrada    "+value);
        System.out.println("Time entrada ms "+millis);
        return encodeInt((int) millis*10);
    }
*/
    public final static byte[] encodeTime(java.sql.Time d) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
//        System.out.println("Time entrada    "+d);
        long millisInDay =
          c.get(Calendar.HOUR_OF_DAY)*60*60*1000
          +
          c.get(Calendar.MINUTE)*60*1000
          +
          c.get(Calendar.SECOND)*1000
          +
          c.get(Calendar.MILLISECOND)
          ;
        int saving_offset = c.get(Calendar.DST_OFFSET);
        int zone_offset = c.get(Calendar.ZONE_OFFSET);
//        System.out.println("Time entrada ms "+millisInDay+" dst="+saving_offset
//		  +" zone="+zone_offset);
        int iTime = (int) (millisInDay * 10);
        return encodeInt(iTime);
    }
/*	 	 
    public final static java.sql.Time decodeTime(byte[] int_byte) {
        int millis = decodeInt(int_byte);
        System.out.println("Time entrada ms "+millis);
        return new java.sql.Time(millis/10);
    }
*/
    public final static java.sql.Time decodeTime(byte[] int_byte) {
        GregorianCalendar c = new GregorianCalendar();
        int millisInDay = decodeInt(int_byte)/10;
//        System.out.println("Time entrada ms "+millisInDay+" offset="+c.get(Calendar.ZONE_OFFSET));
        java.sql.Time time = new java.sql.Time(millisInDay-c.get(Calendar.ZONE_OFFSET));
//        System.out.println("Time ="+time);
        return time;		  
    }
	 
    public final static byte[] encodeDate(java.util.Date d) {
        int day, month, year;
        int c, ya;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH) + 1;
        year = calendar.get(Calendar.YEAR);

        if (month > 2) {
            month -= 3;
        } else {
            month += 9;
            year -= 1;
        }

        c = year / 100;
        ya = year - 100 * c;

        int value = ((146097 * c) / 4 +
                 (1461 * ya) / 4 +
                 (153 * month + 2) / 5 +
                 day + 1721119 - 2400001);
//        System.out.println("encodeDate "+value);
        return encodeInt(value);
    }

    public final static java.sql.Date decodeDate(byte[] byte_int) {        		 
        int sql_date = decodeInt(byte_int);
//        System.out.println("decodeDate "+sql_date);

        int year, month, day, century;

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

        Calendar calendar = new GregorianCalendar(year, month - 1, day);
        return new java.sql.Date(calendar.getTime().getTime());
    }
}
