// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getUpdateCount.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getUpdateCount extends TestModule
{

    public getUpdateCount()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.getUpdateCount");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "INSERT INTO GTEST SELECT CHARCOL FROM JDBCTEST";
            statement = connection.createStatement();
            statement.execute(s);
            test(statement, "getUpdateCount()");
            int i = statement.getUpdateCount();
            result(i);
            if(i == 6)
                passed();
            else
                failed();
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
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
