// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetStream.java

package jdbcTest.resultSet;

import java.io.InputStream;
import java.sql.*;
import jdbcTest.harness.TestModule;
import jdbcTest.harness.TestTable;

public class GetStream extends TestModule
{

    public GetStream()
    {
    }

    public void run()
    {
        Connection connection = null;
        byte abyte2[] = new byte[10];
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.getXXX");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            TestTable testtable = new TestTable("ResultSetGet", 3, connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetGet\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetGet");
            result(resultset);
            next(resultset);
            for(int l3 = 1; l3 <= testtable.getColumnCount(); l3++)
            {
                int i3 = testtable.getJavaType(l3);
                switch(i3)
                {
                case -7:
                case -6:
                case -5:
                case 2: // '\002'
                case 3: // '\003'
                case 4: // '\004'
                case 5: // '\005'
                case 6: // '\006'
                case 7: // '\007'
                case 8: // '\b'
                case 91: // '['
                case 92: // '\\'
                case 93: // ']'
                    break;

                case 1: // '\001'
                    test(resultset, "getUnicodeStream(" + l3 + ")");
                    InputStream inputstream4 = resultset.getUnicodeStream(l3);
                    result(inputstream4);
                    StringBuffer stringbuffer = new StringBuffer();
                    do
                    {
                        int i = inputstream4.read(abyte2);
                        if(i < 0)
                            break;
                        for(int i4 = 0; i4 < i; i4 += 2)
                        {
                            char c = (char)(abyte2[i4] * 256 + abyte2[i4 + 1]);
                            stringbuffer.append(c);
                        }

                    } while(true);
                    String s = stringbuffer.toString();
                    verify(s.length() == testtable.getMaxPrecision(l3), "The CHAR value's length:" + s.length() + " must match it's column's precision:" + testtable.getMaxPrecision(l3));
                    verify(s.trim().equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The CHAR value must match the value used to set it");
                    break;

                case -1:
                case 12: // '\f'
                    test(resultset, "getUnicodeStream(" + l3 + ")");
                    InputStream inputstream5 = resultset.getUnicodeStream(l3);
                    result(inputstream5);
                    StringBuffer stringbuffer1 = new StringBuffer();
                    do
                    {
                        int j = inputstream5.read(abyte2);
                        if(j < 0)
                            break;
                        for(int j4 = 0; j4 < j; j4 += 2)
                        {
                            char c1 = (char)(abyte2[j4] * 256 + abyte2[j4 + 1]);
                            stringbuffer1.append(c1);
                        }

                    } while(true);
                    String s1 = stringbuffer1.toString();
                    verify(s1.length() == "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length(), "The VARCHAR value's length:" + s1.length() + " must match it's original length:" + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
                    verify(s1.equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The VARCHAR value must match the value used to set it");
                    break;

                case -2:
                    test(resultset, "getUnicodeStream(" + l3 + ")");
                    InputStream inputstream6 = resultset.getUnicodeStream(l3);
                    result(inputstream6);
                    StringBuffer stringbuffer2 = new StringBuffer();
                    do
                    {
                        int k = inputstream6.read(abyte2);
                        if(k < 0)
                            break;
                        for(int k4 = 0; k4 < k; k4 += 2)
                        {
                            char c2 = (char)(abyte2[k4] * 256 + abyte2[k4 + 1]);
                            stringbuffer2.append(c2);
                        }

                    } while(true);
                    String s2 = stringbuffer2.toString();
                    verify(s2.length() == testtable.getMaxPrecision(l3) * 2, "The BINARY value's length:" + s2.length() + " must match it's column's precision*2:" + testtable.getMaxPrecision(l3) * 2);
                    verify(s2.substring(0, "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length()).equalsIgnoreCase("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F"), "The BINARY value must match the value used to set it");
                    break;

                case -4:
                case -3:
                    test(resultset, "getUnicodeStream(" + l3 + ")");
                    InputStream inputstream7 = resultset.getUnicodeStream(l3);
                    result(inputstream7);
                    StringBuffer stringbuffer3 = new StringBuffer();
                    do
                    {
                        int l = inputstream7.read(abyte2);
                        if(l < 0)
                            break;
                        for(int l4 = 0; l4 < l; l4 += 2)
                        {
                            char c3 = (char)(abyte2[l4] * 256 + abyte2[l4 + 1]);
                            stringbuffer3.append(c3);
                        }

                    } while(true);
                    String s3 = stringbuffer3.toString();
                    verify(s3.length() == "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length(), "The VARBINARY value's length:" + s3.length() + " must match it's original length:" + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length());
                    verify(s3.equalsIgnoreCase("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F"), "The VARBINARY value must match the value used to set it");
                    break;

                default:
                    verify(false, "This is an unexpected SQL Type");
                    break;
                }
            }

            next(resultset);
            for(int i5 = 1; i5 <= testtable.getColumnCount(); i5++)
            {
                int j3 = testtable.getJavaType(i5);
                switch(j3)
                {
                case -7:
                case -6:
                case -5:
                case 2: // '\002'
                case 3: // '\003'
                case 4: // '\004'
                case 5: // '\005'
                case 6: // '\006'
                case 7: // '\007'
                case 8: // '\b'
                case 91: // '['
                case 92: // '\\'
                case 93: // ']'
                    break;

                case 1: // '\001'
                    test(resultset, "getAsciiStream(" + i5 + ")");
                    InputStream inputstream = resultset.getAsciiStream(i5);
                    result(inputstream);
                    StringBuffer stringbuffer4 = new StringBuffer();
                    do
                    {
                        int i1 = inputstream.read(abyte2);
                        if(i1 < 0)
                            break;
                        for(int j5 = 0; j5 < i1; j5++)
                        {
                            char c4 = (char)abyte2[j5];
                            stringbuffer4.append(c4);
                        }

                    } while(true);
                    String s4 = stringbuffer4.toString();
                    verify(s4.length() == testtable.getMaxPrecision(i5), "The CHAR value's length:" + s4.length() + " must match it's column's precision:" + testtable.getMaxPrecision(i5));
                    verify(s4.trim().equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The CHAR value must match the value used to set it");
                    break;

                case -1:
                case 12: // '\f'
                    test(resultset, "getAsciiStream(" + i5 + ")");
                    InputStream inputstream1 = resultset.getAsciiStream(i5);
                    result(inputstream1);
                    StringBuffer stringbuffer5 = new StringBuffer();
                    do
                    {
                        int j1 = inputstream1.read(abyte2);
                        if(j1 < 0)
                            break;
                        for(int k5 = 0; k5 < j1; k5++)
                        {
                            char c5 = (char)abyte2[k5];
                            stringbuffer5.append(c5);
                        }

                    } while(true);
                    String s5 = stringbuffer5.toString();
                    verify(s5.length() == "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length(), "The VARCHAR value's length:" + s5.length() + " must match it's original length:" + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length());
                    verify(s5.equalsIgnoreCase("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The VARCHAR value must match the value used to set it");
                    break;

                case -2:
                    test(resultset, "getAsciiStream(" + i5 + ")");
                    InputStream inputstream2 = resultset.getAsciiStream(i5);
                    result(inputstream2);
                    StringBuffer stringbuffer6 = new StringBuffer();
                    do
                    {
                        int k1 = inputstream2.read(abyte2);
                        if(k1 < 0)
                            break;
                        for(int l5 = 0; l5 < k1; l5++)
                        {
                            char c6 = (char)abyte2[l5];
                            stringbuffer6.append(c6);
                        }

                    } while(true);
                    String s6 = stringbuffer6.toString();
                    verify(s6.length() == testtable.getMaxPrecision(i5) * 2, "The BINARY value's length:" + s6.length() + " must match it's column's precision*2:" + testtable.getMaxPrecision(i5) * 2);
                    verify(s6.substring(0, "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length()).equalsIgnoreCase("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F"), "The BINARY value must match the value used to set it");
                    break;

                case -4:
                case -3:
                    test(resultset, "getAsciiStream(" + i5 + ")");
                    InputStream inputstream3 = resultset.getAsciiStream(i5);
                    result(inputstream3);
                    StringBuffer stringbuffer7 = new StringBuffer();
                    do
                    {
                        int l1 = inputstream3.read(abyte2);
                        if(l1 < 0)
                            break;
                        for(int i6 = 0; i6 < l1; i6++)
                        {
                            char c7 = (char)abyte2[i6];
                            stringbuffer7.append(c7);
                        }

                    } while(true);
                    String s7 = stringbuffer7.toString();
                    verify(s7.length() == "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length(), "The VARBINARY value's length:" + s7.length() + " must match it's original length:" + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length());
                    verify(s7.equalsIgnoreCase("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F"), "The VARBINARY value must match the value used to set it");
                    break;

                default:
                    verify(false, "This is an unexpected SQL Type");
                    break;
                }
            }

            next(resultset);
            for(int j6 = 1; j6 <= testtable.getColumnCount(); j6++)
            {
                int k3 = testtable.getJavaType(j6);
                switch(k3)
                {
                case -7:
                case -6:
                case -5:
                case -1:
                case 1: // '\001'
                case 2: // '\002'
                case 3: // '\003'
                case 4: // '\004'
                case 5: // '\005'
                case 6: // '\006'
                case 7: // '\007'
                case 8: // '\b'
                case 12: // '\f'
                case 91: // '['
                case 92: // '\\'
                case 93: // ']'
                    break;

                case -2:
                    test(resultset, "getBinaryStream(" + j6 + ")");
                    InputStream inputstream8 = resultset.getBinaryStream(j6);
                    result(inputstream8);
                    byte abyte0[] = new byte[testtable.getMaxPrecision(j6)];
                    int k2 = 0;
                    do
                    {
                        int i2 = inputstream8.read(abyte2);
                        if(i2 < 0)
                            break;
                        for(int k6 = 0; k6 < i2; k6++)
                        {
                            if(k2 > abyte0.length)
                                verify(false, "A BINARY value's length can't be larger that it's column's precision");
                            abyte0[k2++] = abyte2[k6];
                        }

                    } while(true);
                    verify(abyte0.length == testtable.getMaxPrecision(j6), "The BINARY value's length:" + abyte0.length + " must match it's column's precision:" + testtable.getMaxPrecision(j6));
                    for(int l6 = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; l6 < abyte0.length; l6++)
                        if(abyte0[l6] != 0)
                            verify(false, "The BINARY padding must be zero");

                    for(int i7 = 0; i7 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; i7++)
                        if(abyte0[i7] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(i7 * 2, i7 * 2 + 2), 16))
                            verify(false, "The BINARY value must match the value used to set it");

                    break;

                case -4:
                case -3:
                    test(resultset, "getBinaryStream(" + j6 + ")");
                    InputStream inputstream9 = resultset.getBinaryStream(j6);
                    result(inputstream9);
                    byte abyte1[] = new byte["000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2];
                    int l2 = 0;
                    do
                    {
                        int j2 = inputstream9.read(abyte2);
                        if(j2 < 0)
                            break;
                        for(int j7 = 0; j7 < j2; j7++)
                        {
                            if(l2 > abyte1.length)
                                verify(false, "A VARBINARY value's length can't be larger that it's column's precision");
                            abyte1[l2++] = abyte2[j7];
                        }

                    } while(true);
                    verify(abyte1.length == "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2, "The VARBINARY value's length:" + abyte1.length + " must match it's original length:" + "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2);
                    for(int k7 = 0; k7 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; k7++)
                        if(abyte1[k7] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(k7 * 2, k7 * 2 + 2), 16))
                            verify(false, "The VARBINARY value must match the value used to set it");

                    break;

                default:
                    verify(false, "This is an unexpected SQL Type");
                    break;
                }
            }

            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
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
