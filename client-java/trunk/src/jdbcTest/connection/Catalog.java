// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Catalog.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class Catalog extends TestModule
{

    public Catalog()
    {
    }

    public void run()
    {
        Connection connection = null;
        String s = "";
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.setCatalog");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            try
            {
                test(connection, "getCatalog()");
                s = connection.getCatalog();
                result(s);
            }
            catch(SQLException _ex)
            {
                result("getCatalog is not supported");
            }
            try
            {
                test(connection, "setCatalog(\"" + s + "\")");
                connection.setCatalog(s);
            }
            catch(SQLException _ex)
            {
                result("setCatalog is not supported");
            }
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
