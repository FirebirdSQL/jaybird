// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setQueryTimeout.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setQueryTimeout extends TestModule
{

    public setQueryTimeout()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.setQueryTimeout");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            try
            {
                String s = "SELECT * FROM JDBCTEST";
                statement = connection.createStatement();
                test(statement, "setQueryTimeout()");
                statement.setQueryTimeout(1);
                test(statement, "getQueryTimeout()");
                int i = statement.getQueryTimeout();
                result(i);
                if(i == 1)
                    passed();
                else
                    failed();
            }
            catch(SQLException _ex)
            {
                result("setQueryTimeout is not supported");
            }
        }
        catch(Exception exception1)
        {
            exception(exception1);
        }
        finally
        {
            try
            {
                if(statement != null)
                    statement.close();
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
