// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetXXXNamed.java

package jdbcTest.resultSet;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;
import jdbcTest.harness.TestTable;

public class GetXXXNamed extends TestModule
{

    public GetXXXNamed()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.getXXX");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            TestTable testtable = new TestTable("ResultSetGet", 2, connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetGet\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetGet");
            result(resultset);
            next(resultset);
            for(int i1 = 1; i1 <= testtable.getColumnCount(); i1++)
            {
                int j = testtable.getJavaType(i1);
                switch(j)
                {
                case -7:
                    test(resultset, "getBoolean(" + testtable.getColName(i1) + ")");
                    boolean flag = resultset.getBoolean(testtable.getColName(i1));
                    result(flag);
                    verify(flag == ("1" == "1"), "The BIT value must match the value used to set it");
                    break;

                case -6:
                    test(resultset, "getByte(" + testtable.getColName(i1) + ")");
                    byte byte0 = resultset.getByte(testtable.getColName(i1));
                    result(byte0);
                    verify(Integer.parseInt("127") == byte0, "The TINYINT value must match the value used to set it");
                    break;

                case 5: // '\005'
                    test(resultset, "getShort(" + testtable.getColName(i1) + ")");
                    short word0 = resultset.getShort(testtable.getColName(i1));
                    result(word0);
                    verify(Integer.parseInt("-9999") == word0, "The SMALLINT value must match the value used to set it");
                    break;

                case 4: // '\004'
                    test(resultset, "getInt(" + testtable.getColName(i1) + ")");
                    int i = resultset.getInt(testtable.getColName(i1));
                    result(i);
                    verify(Integer.parseInt("-99999999") == i, "The INTEGER value must match the value used to set it");
                    break;

                case -5:
                    test(resultset, "getLong(" + testtable.getColName(i1) + ")");
                    long l = resultset.getLong(testtable.getColName(i1));
                    result(l);
                    verify(Long.parseLong("-9999999999999999") == l, "The BIGINT value must match the value used to set it");
                    break;

                case 6: // '\006'
                case 8: // '\b'
                    test(resultset, "getDouble(" + testtable.getColName(i1) + ")");
                    double d = resultset.getDouble(testtable.getColName(i1));
                    result(d);
                    verify(Double.valueOf("1.234567891234").doubleValue() < d && Double.valueOf("1.234567891235").doubleValue() > d, "The FLOAT value must match the value used to set it");
                    break;

                case 7: // '\007'
                    test(resultset, "getFloat(" + testtable.getColName(i1) + ")");
                    float f = resultset.getFloat(testtable.getColName(i1));
                    result(f);
                    verify(Float.valueOf("1.23456").floatValue() < f && Float.valueOf("1.23457").floatValue() > f, "The REAL value must match the value used to set it");
                    break;

                case 2: // '\002'
                case 3: // '\003'
                    test(resultset, "getBigDecimal(" + testtable.getColName(i1) + ", " + 16 + ")");
                    BigDecimal bigdecimal = resultset.getBigDecimal(testtable.getColName(i1), 16);
                    result(bigdecimal);
                    verify(bigdecimal.equals(new BigDecimal("12.1234561234567890")), "The NUMERIC value must match the value used to set it");
                    break;

                case 1: // '\001'
                    test(resultset, "getString(" + testtable.getColName(i1) + ")");
                    String s = resultset.getString(testtable.getColName(i1));
                    result(s);
                    verify(s.length() == testtable.getMaxPrecision(i1), "A CHAR value's length must match it's column's precision");
                    verify(s.trim().equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The CHAR value must match the value used to set it");
                    break;

                case -1:
                case 12: // '\f'
                    test(resultset, "getString(" + testtable.getColName(i1) + ")");
                    String s1 = resultset.getString(testtable.getColName(i1));
                    result(s1);
                    verify(s1.length() == "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length(), "A VARCHAR/LONGVARCHAR value's length must match its's original length");
                    verify(s1.equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The VARCHAR value must match the value used to set it");
                    break;

                case 91: // '['
                    test(resultset, "getDate(" + testtable.getColName(i1) + ")");
                    Date date = resultset.getDate(testtable.getColName(i1));
                    result(date);
                    verify(date.equals(Date.valueOf("1996-01-01")), "The DATE value must match the value used to set it");
                    break;

                case 92: // '\\'
                    test(resultset, "getTime(" + testtable.getColName(i1) + ")");
                    Time time = resultset.getTime(testtable.getColName(i1));
                    result(time);
                    verify(time.equals(Time.valueOf("04:59:59")), "The TIME value must match the value used to set it");
                    break;

                case 93: // ']'
                    test(resultset, "getTimestamp(" + testtable.getColName(i1) + ")");
                    Timestamp timestamp = resultset.getTimestamp(testtable.getColName(i1));
                    result(timestamp);
                    verify(timestamp.equals(Timestamp.valueOf("1996-02-28 04:59:59.000000")), "The TIMESTAMP value must match the value used to set it");
                    break;

                case -2:
                    test(resultset, "getBytes(" + testtable.getColName(i1) + ")");
                    byte abyte0[] = resultset.getBytes(testtable.getColName(i1));
                    result(abyte0);
                    verify(abyte0.length == testtable.getMaxPrecision(i1), "A BINARY value's length must match it's column's precision");
                    for(int j1 = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; j1 < abyte0.length; j1++)
                        if(abyte0[j1] != 0)
                            verify(false, "The BINARY padding must be zero");

                    for(int k1 = 0; k1 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; k1++)
                        if(abyte0[k1] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(k1 * 2, k1 * 2 + 2), 16))
                            verify(false, "The BINARY value must match the value used to set it");

                    break;

                case -4:
                case -3:
                    test(resultset, "getBytes(" + testtable.getColName(i1) + ")");
                    byte abyte1[] = resultset.getBytes(testtable.getColName(i1));
                    result(abyte1);
                    verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() == abyte1.length * 2, "The length of a VARBINARY value must match the value used to set it");
                    for(int l1 = 0; l1 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; l1++)
                        if(abyte1[l1] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(l1 * 2, l1 * 2 + 2), 16))
                            verify(false, "The VARBINARY value must match the value used to set it");

                    break;

                default:
                    verify(false, "This is an unexpected SQL Type");
                    break;
                }
            }

            next(resultset);
            for(int i2 = 1; i2 <= testtable.getColumnCount(); i2++)
            {
                int k = testtable.getJavaType(i2);
                switch(k)
                {
                case -7:
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s2 = resultset.getString(testtable.getColName(i2));
                    result(s2);
                    verify("1".equals(s2), "The BIT value must match the value used to set it");
                    break;

                case -1:
                case 1: // '\001'
                case 12: // '\f'
                    break;

                case -6:
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s3 = resultset.getString(testtable.getColName(i2));
                    result(s3);
                    verify("127".equals(s3), "The TINYINT value must match the value used to set it");
                    break;

                case 5: // '\005'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s4 = resultset.getString(testtable.getColName(i2));
                    result(s4);
                    verify("-9999".equals(s4), "The SMALLINT value must match the value used to set it");
                    break;

                case 4: // '\004'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s5 = resultset.getString(testtable.getColName(i2));
                    result(s5);
                    verify("-99999999".equals(s5), "The INTEGER value must match the value used to set it");
                    break;

                case -5:
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s6 = resultset.getString(testtable.getColName(i2));
                    result(s6);
                    verify("-9999999999999999".equals(s6), "The BIGINT value must match the value used to set it");
                    break;

                case 6: // '\006'
                case 8: // '\b'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s8 = resultset.getString(testtable.getColName(i2));
                    result(s8);
                    verify("1.234567891234".compareTo(s8) < 0 && "1.234567891235".compareTo(s8) > 0, "The FLOAT value must match the value used to set it");
                    break;

                case 7: // '\007'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s7 = resultset.getString(testtable.getColName(i2));
                    result(s7);
                    verify("1.23456".compareTo(s7) < 0 && "1.23457".compareTo(s7) > 0, "The REAL value must match the value used to set it");
                    break;

                case 2: // '\002'
                case 3: // '\003'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s9 = resultset.getString(testtable.getColName(i2));
                    result(s9);
                    verify("12.1234561234567890".equals(s9.substring(0, "12.1234561234567890".length())), "The NUMERIC value must match the value used to set it");
                    for(int j2 = "12.1234561234567890".length(); j2 < s9.length(); j2++)
                        if(s9.charAt(j2) != '0')
                            verify(false, "A NUMERIC value must be padded with '0's");

                    break;

                case 91: // '['
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s10 = resultset.getString(testtable.getColName(i2));
                    result(s10);
                    verify(Date.valueOf("1996-01-01").equals(Date.valueOf(s10)), "The DATE value must match the value used to set it");
                    break;

                case 92: // '\\'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s11 = resultset.getString(testtable.getColName(i2));
                    result(s11);
                    verify(Time.valueOf("04:59:59").equals(Time.valueOf(s11)), "The TIME value must match the value used to set it");
                    break;

                case 93: // ']'
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s12 = resultset.getString(testtable.getColName(i2));
                    result(s12);
                    verify(Timestamp.valueOf("1996-02-28 04:59:59.000000").equals(Timestamp.valueOf(s12)), "The TIMESTAMP value must match the value used to set it");
                    break;

                case -2:
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s13 = resultset.getString(testtable.getColName(i2));
                    result(s13);
                    verify(s13.length() / 2 == testtable.getMaxPrecision(i2), "A BINARY value's length must match it's column's precision");
                    verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".equalsIgnoreCase(s13.substring(0, "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length())), "The BINARY value must match the value used to set it");
                    for(int k2 = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length(); k2 < s13.length(); k2++)
                        if(s13.charAt(k2) != '0')
                            verify(false, "A BINARY string value must be padded with '0's");

                    break;

                case -4:
                case -3:
                    test(resultset, "getString(" + testtable.getColName(i2) + ")");
                    String s14 = resultset.getString(testtable.getColName(i2));
                    result(s14);
                    verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() == s14.length(), "A VARBINARY value's length must match its original length");
                    verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".equalsIgnoreCase(s14), "The VARBINARY value must match the value used to set it");
                    break;

                default:
                    verify(false, "This is an unexpected SQL Type");
                    break;
                }
            }

            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace(Log.out);
            failed();
        }
        finally
        {
            if(connection != null)
                trySQL(connection, "drop table ResultSetGet");
            try
            {
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
