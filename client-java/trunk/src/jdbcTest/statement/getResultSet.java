// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getResultSet.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getResultSet extends TestModule
{

    public getResultSet()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.getResultSet");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "SELECT * FROM JDBCTEST";
            statement = connection.createStatement();
            statement.execute(s);
            test(statement, "getResultSet()");
            resultset = statement.getResultSet();
            int i;
            for(i = 0; resultset.next(); i++);
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
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
