// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Close.java

package jdbcTest.resultSet;

import java.sql.*;
import jdbcTest.harness.*;

public class Close extends TestModule
{

    public Close()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.close");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "getMetaData()");
            java.sql.DatabaseMetaData databasemetadata = connection.getMetaData();
            TestTable testtable = new TestTable("ResultSetClose", 5, connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetClose\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetClose");
            result(resultset);
            if(next(resultset))
            {
                test(resultset, "close()");
                resultset.close();
            }
            try
            {
                resultset.next();
                assert(false, "The ResultsSet should be closed");
            }
            catch(SQLException _ex) { }
            try
            {
                String s = resultset.getString(1);
                assert(false, "The ResultsSet should be closed");
            }
            catch(SQLException _ex) { }
            try
            {
                Object obj = resultset.getObject(1);
                assert(false, "The ResultsSet should be closed");
            }
            catch(SQLException _ex) { }
            try
            {
                java.sql.ResultSetMetaData resultsetmetadata = resultset.getMetaData();
                assert(false, "The ResultsSet should be closed");
            }
            catch(SQLException _ex) { }
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
                trySQL(connection, "drop table ResultSetClose");
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
