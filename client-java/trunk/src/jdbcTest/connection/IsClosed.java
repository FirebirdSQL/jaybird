// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   IsClosed.java

package jdbcTest.connection;

import java.sql.*;
import jdbcTest.harness.Log;
import jdbcTest.harness.TestModule;

public class IsClosed extends TestModule
{

    public IsClosed()
    {
    }

    public void run()
    {
        Connection connection = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Connection.isClosed");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            assert(connection.isClosed() ^ true, "The connection should be open");
            test(connection, "close()");
            connection.close();
            try
            {
                test(connection, "rollback()");
                connection.rollback();
                assert(false, "Rollbock on a closed Connection should throw an exception");
            }
            catch(SQLException _ex) { }
            assert(connection.isClosed(), "The connection should be closed");
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
