// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getWarnings.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getWarnings extends TestModule
{

    public getWarnings()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.getWarnings");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "SELECT * FROM JDBCTEST";
            statement = connection.createStatement();
            statement.execute(s);
            test(statement, "getWarnings");
            java.sql.SQLWarning sqlwarning = statement.getWarnings();
            if(sqlwarning != null)
            {
                result(sqlwarning);
                failed();
            } else
            {
                passed();
            }
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
            failed();
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
