// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Warning.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class Warning extends TestModule
{

    public Warning()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection Warnings");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            test(connection, "clearWarnings()");
            connection.clearWarnings();
            test(connection, "getWarnings()");
            java.sql.SQLWarning sqlwarning = connection.getWarnings();
            assert(sqlwarning == null, "There should be no warnings");
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
