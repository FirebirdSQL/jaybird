// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Next.java

package jdbcTest.resultSet;

import java.sql.*;
import jdbcTest.harness.*;

public class Next extends TestModule
{

    public Next()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test ResultSet.next");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "getMetaData()");
            java.sql.DatabaseMetaData databasemetadata = connection.getMetaData();
            TestTable testtable = new TestTable("ResultSetNext", 5, connection);
            test(connection, "createStatement()");
            Statement statement = connection.createStatement();
            result(statement);
            test(statement, "executeQuery(\"select * from ResultSetNext\")");
            ResultSet resultset = statement.executeQuery("select * from ResultSetNext");
            result(resultset);
            try
            {
                test(resultset, "getObject(1)");
                Object obj = resultset.getObject(1);
                assert(false, "Prior to calling next, getting a column value should throw a SQLException");
            }
            catch(SQLException _ex) { }
            test(resultset, "next()");
            int i;
            for(i = 0; resultset.next(); i++);
            assert(i == 5, "There should be five rows");
            try
            {
                assert(resultset.next() ^ true, "next() should be false");
            }
            catch(SQLException _ex) { }
            try
            {
                test(resultset, "getObject(1)");
                Object obj1 = resultset.getObject(1);
                assert(false, "When next() is false, getting a column value should throw a SQLException");
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
                trySQL(connection, "drop table ResultSetNext");
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
