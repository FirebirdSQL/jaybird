// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   clearWarnings.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class clearWarnings extends TestModule
{

    public clearWarnings()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.clearWarnings");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "SELECT * FROM GTEST";
            statement = connection.createStatement();
            statement.execute(s);
            test(statement, "clearWarnings()");
            statement.clearWarnings();
            passed();
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
