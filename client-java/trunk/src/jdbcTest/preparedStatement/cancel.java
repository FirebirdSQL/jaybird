// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   cancel.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class cancel extends TestModule
{

    public cancel()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.cancel");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            preparedstatement = connection.prepareStatement("SELECT * FROM GTEST");
            preparedstatement.execute();
            test(preparedstatement, "cancel");
            preparedstatement.cancel();
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
