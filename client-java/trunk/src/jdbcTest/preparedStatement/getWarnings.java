// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getWarnings.java

package jdbcTest.preparedStatement;

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
        PreparedStatement preparedstatement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.getWarnings");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            preparedstatement = connection.prepareStatement("SELECT * FROM JDBCTEST");
            preparedstatement.execute();
            test(preparedstatement, "getWarnings()");
            java.sql.SQLWarning sqlwarning = preparedstatement.getWarnings();
            if(sqlwarning != null)
                result(sqlwarning);
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
