// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setQueryTimeout.java

package jdbcTest.preparedStatement;

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
        PreparedStatement preparedstatement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setQueryTimeout");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            try
            {
                preparedstatement = connection.prepareStatement("SELECT * FROM JDBCTEST");
                test(preparedstatement, "setQueryTimeout()");
                preparedstatement.setQueryTimeout(1);
                preparedstatement.execute();
                int i = preparedstatement.getQueryTimeout();
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
                if(preparedstatement != null)
                    preparedstatement.close();
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
