// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   GetObjectNamed.java

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

public class GetObjectNamed extends TestModule
{

    public GetObjectNamed()
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
            for(int l = 1; l <= testtable.getColumnCount(); l++)
            {
                test(resultset, "findColumn(" + testtable.getColName(l) + ")");
                int j = resultset.findColumn(testtable.getColName(l));
                result(j);
                if(j != l)
                    verify(false, "findcolumn must return the column's number");
            }

            try
            {
                int k = resultset.findColumn("xxxxxxx");
                verify(false, "findColumn must throw a SQLException for a bad column name");
            }
            catch(SQLException _ex) { }
            while(resultset.next())
            {
                Object aobj[] = new Object[testtable.getColumnCount()];
                for(int i1 = 1; i1 <= testtable.getColumnCount(); i1++)
                {
                    test(resultset, "getObject(" + testtable.getColName(i1) + ")");
                    aobj[i1 - 1] = resultset.getObject(testtable.getColName(i1));
                    result(aobj[i1 - 1]);
                    int i = testtable.getJavaType(i1);
                    switch(i)
                    {
                    case -7:
                        verify(aobj[i1 - 1] instanceof Boolean, "BIT must be read as a Boolean");
                        verify((new Boolean("1".equals("1"))).equals((Boolean)aobj[i1 - 1]), "The BIT value must match the value used to set it");
                        break;

                    case -6:
                        verify(aobj[i1 - 1] instanceof Integer, "TINYINT must be read as a Integer");
                        verify(Integer.valueOf("127").equals((Integer)aobj[i1 - 1]), "The TINYINT value must match the value used to set it");
                        break;

                    case 5: // '\005'
                        verify(aobj[i1 - 1] instanceof Integer, "SMALLINT must be read as a Integer");
                        verify(Integer.valueOf("-9999").equals((Integer)aobj[i1 - 1]), "The SMALLINT value must match the value used to set it");
                        break;

                    case 4: // '\004'
                        verify(aobj[i1 - 1] instanceof Integer, "INTEGER must be read as a Integer");
                        verify(Integer.valueOf("-99999999").equals((Integer)aobj[i1 - 1]), "The INTEGER value must match the value used to set it");
                        break;

                    case -5:
                        verify(aobj[i1 - 1] instanceof Long, "BIGINT must be read as a Long");
                        verify(Long.valueOf("-9999999999999999").equals((Long)aobj[i1 - 1]), "The BIGINT value must match the value used to set it");
                        break;

                    case 6: // '\006'
                    case 8: // '\b'
                        verify(aobj[i1 - 1] instanceof Double, "DOUBLE must be read as a Double");
                        verify(Double.valueOf("1.234567891234").doubleValue() < ((Double)aobj[i1 - 1]).doubleValue() && Double.valueOf("1.234567891235").doubleValue() > ((Double)aobj[i1 - 1]).doubleValue(), "The FLOAT value must match the value used to set it");
                        break;

                    case 7: // '\007'
                        verify(aobj[i1 - 1] instanceof Float, "REAL must be read as a Float");
                        verify(Float.valueOf("1.23456").floatValue() < ((Float)aobj[i1 - 1]).floatValue() && Float.valueOf("1.23457").floatValue() > ((Float)aobj[i1 - 1]).floatValue(), "The REAL value must match the value used to set it");
                        break;

                    case 2: // '\002'
                    case 3: // '\003'
                        verify(aobj[i1 - 1] instanceof BigDecimal, "NUMERIC must be read as a BigDecimal");
                        verify(((BigDecimal)aobj[i1 - 1]).equals(new BigDecimal("12.1234561234567890")), "The BigDecimal value must match the value used to set it");
                        break;

                    case 1: // '\001'
                        verify(aobj[i1 - 1] instanceof String, "CHAR must be read as a String");
                        verify(((String)aobj[i1 - 1]).length() == testtable.getMaxPrecision(i1), "A CHAR value's length must match it's column's precision");
                        verify(((String)aobj[i1 - 1]).trim().compareTo("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ") == 0, "The CHAR value must match the value used to set it");
                        break;

                    case -1:
                    case 12: // '\f'
                        verify(aobj[i1 - 1] instanceof String, "VARCHAR must be read as a String");
                        verify(((String)aobj[i1 - 1]).length() == "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".length(), "A VARCHAR/LONGVARCHAR value's length must match its's original length");
                        verify(((String)aobj[i1 - 1]).equals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"), "The VARCHAR value must match the value used to set it");
                        break;

                    case 91: // '['
                        verify(aobj[i1 - 1] instanceof Date, "DATE must be read as a java.sql.Date");
                        verify(((Date)aobj[i1 - 1]).equals(Date.valueOf("1996-01-01")), "The DATE value must match the value used to set it");
                        break;

                    case 92: // '\\'
                        verify(aobj[i1 - 1] instanceof Time, "TIME must be read as a Time");
                        verify(((Time)aobj[i1 - 1]).equals(Time.valueOf("04:59:59")), "The TIME value must match the value used to set it");
                        break;

                    case 93: // ']'
                        verify(aobj[i1 - 1] instanceof Timestamp, "TIMESTAMP must be read as a Timestamp");
                        verify(((Timestamp)aobj[i1 - 1]).equals(Timestamp.valueOf("1996-02-28 04:59:59.000000")), "The TIMESTAMP value must match the value used to set it");
                        break;

                    case -2:
                        verify(aobj[i1 - 1] instanceof byte[], "BINARY must be read as a byte[]");
                        verify(((byte[])aobj[i1 - 1]).length == testtable.getMaxPrecision(i1), "A BINARY value's length must match it's column's precision");
                        for(int j1 = "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; j1 < ((byte[])aobj[i1 - 1]).length; j1++)
                            if(((byte[])aobj[i1 - 1])[j1] != 0)
                                verify(false, "The BINARY padding must be zero");

                        for(int k1 = 0; k1 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; k1++)
                            if(((byte[])aobj[i1 - 1])[k1] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(k1 * 2, k1 * 2 + 2), 16))
                                verify(false, "The BINARY value must match the value used to set it");

                        break;

                    case -4:
                    case -3:
                        verify(aobj[i1 - 1] instanceof byte[], "VARBINARY must be read as a byte[]");
                        verify("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() == ((byte[])aobj[i1 - 1]).length * 2, "The length is wrong");
                        for(int l1 = 0; l1 < "000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".length() / 2; l1++)
                            if(((byte[])aobj[i1 - 1])[l1] != Integer.parseInt("000102030405060708090a0b0c0d0e0f101112131415161718191A1B1C1D1E1F".substring(l1 * 2, l1 * 2 + 2), 16))
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
