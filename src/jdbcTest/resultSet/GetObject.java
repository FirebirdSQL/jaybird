// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetObject.java

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

public class GetObject extends TestModule
{

    public GetObject()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.getObject");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "getMetaData()");
            TestTable testtable = new TestTable("ResultSetGet", 2, connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetGet\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetGet");
            result(resultset);
            while(resultset.next())
            {
                Object aobj[] = new Object[testtable.getColumnCount()];
                for(int j = 1; j <= testtable.getColumnCount(); j++)
                {
                    test(resultset, "getObject(" + j + ")");
                    aobj[j - 1] = resultset.getObject(j);
                    result(aobj[j - 1]);
                    int i = testtable.getJavaType(j);
                    switch(i)
                    {
                    case -7:
                        verify(aobj[j - 1] instanceof Boolean, "BIT must be read as a Boolean");
                        verify((new Boolean("1".equals("1"))).equals((Boolean)aobj[j - 1]), "The BIT value must match the value used to set it");
                        break;

                    case -6:
                        verify(aobj[j - 1] instanceof Integer, "TINYINT must be read as a Integer");
                        verify(Integer.valueOf("127").equals((Integer)aobj[j - 1]), "The TINYINT value must match the value used to set it");
                        break;

                    case 5: // '\005'
                        verify(aobj[j - 1] instanceof Integer, "SMALLINT must be read as a Integer");
                        verify(Integer.valueOf("-9999").equals((Integer)aobj[j - 1]), "The SMALLINT value must match the value used to set it");
                        break;

                    case 4: // '\004'
                        verify(aobj[j - 1] instanceof Integer, "INTEGER must be read as a Integer");
                        verify(Integer.valueOf("-99999999").equals((Integer)aobj[j - 1]), "The INTEGER value must match the value used to set it");
                        break;

                    case -5:
                        verify(aobj[j - 1] instanceof Long, "BIGINT must be read as a Long");
                        test(resultset, "getLong(" + j + ")");
                        verify(Long.valueOf("-9999999999999999").equals((Long)aobj[j - 1]), "The BIGINT value must match the value used to set it");
                        break;

                    case 6: // '\006'
                    case 8: // '\b'
                        verify(aobj[j - 1] instanceof Double, "DOUBLE must be read as a Double");
                        verify(Double.valueOf("1.234567891234").doubleValue() < ((Double)aobj[j - 1]).doubleValue() && Double.valueOf("1.234567891235").doubleValue() > ((Double)aobj[j - 1]).doubleValue(), "The FLOAT value must match the value used to set it");
                        break;

                    case 7: // '\007'
                        verify(aobj[j - 1] instanceof Float, "REAL must be read as a Float");
                        verify(Float.valueOf("1.23456").floatValue() < ((Float)aobj[j - 1]).floatValue() && Float.valueOf("1.23457").floatValue() > ((Float)aobj[j - 1]).floatValue(), "The REAL value must match the value used to set it");
                        break;

                    case 2: // '\002'
                    case 3: // '\003'
                        verify(aobj[j - 1] instanceof BigDecimal, "BigDecimal must be read as a BigDecimal");
                        verify(((BigDecimal)aobj[j - 1]).equals(new BigDecimal("12.1234561234567890")), "The BigDecimal value must match the value used to set it");
                        break;

                    case 1: // '\001'
                        verify(aobj[j - 1] instanceof String, "CHAR must be read as a String");
                        verify(((String)aobj[j - 1]).length() == testtable.getMaxPrecision(j), "A CHAR value's length must match it's column's precision");
                        verify(((String)aobj[j - 1]).trim().compareTo("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") == 0, "The CHAR value must match the value used to set it");
                        break;

                    case -1:
                    case 12: // '\f'
                        verify(aobj[j - 1] instanceof String, "VARCHAR must be read as a String");
                        verify(((String)aobj[j - 1]).length() == "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length(), "A VARCHAR/LONGVARCHAR value's length must match its's original length");
                        verify(((String)aobj[j - 1]).equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The VARCHAR value must match the value used to set it");
                        break;

                    case 91: // '['
                        verify(aobj[j - 1] instanceof Date, "DATE must be read as a java.sql.Date");
                        verify(((Date)aobj[j - 1]).equals(Date.valueOf("1996-01-01")), "The DATE value must match the value used to set it");
                        break;

                    case 92: // '\\'
                        verify(aobj[j - 1] instanceof Time, "TIME must be read as a Time");
                        verify(((Time)aobj[j - 1]).equals(Time.valueOf("04:59:59")), "The TIME value must match the value used to set it");
                        break;

                    case 93: // ']'
                        verify(aobj[j - 1] instanceof Timestamp, "TIMESTAMP must be read as a Timestamp");
                        verify(((Timestamp)aobj[j - 1]).equals(Timestamp.valueOf("1996-02-28 04:59:59.000000")), "The TIMESTAMP value must match the value used to set it");
                        break;

                    case -2:
                        verify(aobj[j - 1] instanceof byte[], "BINARY must be read as a byte[]");
                        verify(((byte[])aobj[j - 1]).length == testtable.getMaxPrecision(j), "A BINARY value's length must match it's column's precision");
                        for(int k = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; k < ((byte[])aobj[j - 1]).length; k++)
                            if(((byte[])aobj[j - 1])[k] != 0)
                                verify(false, "The BINARY padding must be zero");

                        for(int l = 0; l < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; l++)
                            if(((byte[])aobj[j - 1])[l] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(l * 2, l * 2 + 2), 16))
                                verify(false, "The BINARY value must match the value used to set it");

                        break;

                    case -4:
                    case -3:
                        verify(aobj[j - 1] instanceof byte[], "VARBINARY must be read as a byte[]");
                        verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() == ((byte[])aobj[j - 1]).length * 2, "The length is wrong");
                        for(int i1 = 0; i1 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; i1++)
                            if(((byte[])aobj[j - 1])[i1] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(i1 * 2, i1 * 2 + 2), 16))
                                verify(false, "The VARBINARY value must match the value used to set it");

                        break;

                    default:
                        verify(false, "This is an unexpected SQL Type");
                        break;
                    }
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
