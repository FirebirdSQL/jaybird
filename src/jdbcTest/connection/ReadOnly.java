// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ReadOnly.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ReadOnly extends TestModule
{

    public ReadOnly()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.setReadOnly");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            assert(connection.isReadOnly() ^ true, "The connection should default to writable");
            try
            {
                test(connection, "setReadOnly(true)");
                connection.setReadOnly(true);
                assert(connection.isReadOnly(), "The connection should be read only");
            }
            catch(SQLException _ex)
            {
                result("setReadOnly is not supported");
            }
            passed();
        }
        catch(Exception exception1)
        {
            exception(exception1);
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
