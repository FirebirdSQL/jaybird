// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   executeQuery.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class executeQuery extends TestModule
{

    public executeQuery()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.executeQuery");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "SELECT * FROM JDBCTEST";
            statement = connection.createStatement();
            test(statement, "ExecuteQuery");
            resultset = statement.executeQuery(s);
            int i = 0;
            if(resultset != null)
                while(resultset.next())
                    i++;
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
                if(resultset != null)
                    resultset.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
