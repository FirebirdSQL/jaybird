// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   RSMeta.java

package jdbcTest.rsmetadata;

import java.io.PrintStream;
import java.sql.*;
import jdbcTest.harness.*;

public class RSMeta extends TestModule
{

    public RSMeta()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        String s = "select * from jdbcrsm";
        Object obj2 = null;
        Object obj3 = null;
        Object obj4 = null;
        boolean flag = true;
        jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSetMetaData");
        execTestCase(testcase);
        try
        {
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            TestTable testtable = new TestTable("jdbcrsm", 1, connection);
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery");
            ResultSet resultset = statement.executeQuery(s);
            logResultSet(resultset);
            test(statement, "executeQuery");
            resultset = statement.executeQuery(s);
            ResultSetMetaData resultsetmetadata = resultset.getMetaData();
            test(resultsetmetadata, "getColumnCount()");
            int j = resultsetmetadata.getColumnCount();
            result(j);
            verify(j == testtable.getColumnCount(), "Column count must match create");
            for(int i = 1; i <= j; i++)
            {
                Log.out.println("Column number: " + i);
                test(resultsetmetadata, "isAutoIncrement(col)");
                result(resultsetmetadata.isAutoIncrement(i));
                verify(resultsetmetadata.isAutoIncrement(i) == testtable.isAutoincrement(i), "isAutoIncrement should match create");
                test(resultsetmetadata, "isCaseSensitive(col)");
                result(resultsetmetadata.isCaseSensitive(i));
                verify(resultsetmetadata.isCaseSensitive(i) == testtable.isCasesensitive(i), "isCaseSensitive should match create");
                test(resultsetmetadata, "isSearchable(col)");
                result(resultsetmetadata.isSearchable(i));
                verify(resultsetmetadata.isSearchable(i) == (testtable.isSearchable(i) != 0), "isSearchable meta data should match create");
                test(resultsetmetadata, "isCurrency(col)");
                result(resultsetmetadata.isCurrency(i));
                verify(testtable.isCurrency(i) == resultsetmetadata.isCurrency(i), "result set meta data isCurrency should match create");
                test(resultsetmetadata, "isNullable(col)");
                result(resultsetmetadata.isNullable(i));
                verify(testtable.isNullable(i) == resultsetmetadata.isNullable(i), "result set meta data isNullable should match create");
                test(resultsetmetadata, "isSigned(col)");
                result(resultsetmetadata.isSigned(i));
                verify(resultsetmetadata.isSigned(i) == testtable.isSigned(i), "sign metadata should match create");
                test(resultsetmetadata, "getColumnDisplaySize(col)");
                result(resultsetmetadata.getColumnDisplaySize(i));
                test(resultsetmetadata, "getColumnLabel(col)");
                result(resultsetmetadata.getColumnLabel(i));
                test(resultsetmetadata, "getColumnName(col)");
                result(resultsetmetadata.getColumnName(i));
                verify(resultsetmetadata.getColumnName(i).equalsIgnoreCase(testtable.getColName(i)), "column name:" + resultsetmetadata.getColumnName(i) + " should match create name:" + testtable.getColName(i));
                test(resultsetmetadata, "getSchemaName(col)");
                result(resultsetmetadata.getSchemaName(i));
                test(resultsetmetadata, "getPrecision(col)");
                result(resultsetmetadata.getPrecision(i));
                switch(testtable.getJavaType(i))
                {
                case -3:
                case -2:
                case 1: // '\001'
                case 12: // '\f'
                    verify(resultsetmetadata.getPrecision(i) == Math.min(256, testtable.getMaxPrecision(i)), "reported precision of " + resultsetmetadata.getPrecision(i) + " should match create precision of " + Math.min(256, testtable.getMaxPrecision(i)));
                    break;

                default:
                    verify(resultsetmetadata.getPrecision(i) == testtable.getMaxPrecision(i), "reported precision of " + resultsetmetadata.getPrecision(i) + " should match create precision of " + testtable.getMaxPrecision(i));
                    break;
                }
                test(resultsetmetadata, "getScale(col)");
                result(resultsetmetadata.getScale(i));
                if(testtable.getMaxScale(i) > 0)
                    verify(resultsetmetadata.getScale(i) == 16, "reported scale should match create scale");
                test(resultsetmetadata, "getTableName(col)");
                result(resultsetmetadata.getTableName(i));
                test(resultsetmetadata, "getCatalogName(col)");
                result(resultsetmetadata.getCatalogName(i));
                test(resultsetmetadata, "getColumnType(col)");
                result(resultsetmetadata.getColumnType(i));
                verify(resultsetmetadata.getColumnType(i) == testtable.getJavaType(i), "reported SQL type should match created type");
                test(resultsetmetadata, "getColumnTypeName(col)");
                result(resultsetmetadata.getColumnTypeName(i));
                verify(resultsetmetadata.getColumnTypeName(i).equalsIgnoreCase(testtable.getTypeName(i)), "reported type name should match created type name");
                test(resultsetmetadata, "isReadOnly(col)");
                result(resultsetmetadata.isReadOnly(i));
                test(resultsetmetadata, "isWritable(col)");
                result(resultsetmetadata.isWritable(i));
                test(resultsetmetadata, "isDefinitelyWritable(col)");
                result(resultsetmetadata.isDefinitelyWritable(i));
            }

            passed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception(exception1);
            failed();
        }
        finally
        {
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
