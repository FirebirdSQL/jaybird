// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   executeUpdate.java

package jdbcTest.statement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class executeUpdate extends TestModule
{

    public executeUpdate()
    {
    }

    public void run()
    {
        Connection connection = null;
        Statement statement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Statement.executeUpdate");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "INSERT INTO GTEST (CHARCOL) SELECT CHARCOL FROM JDBCTEST";
            statement = connection.createStatement();
            test(statement, "executeUpdate()");
            int i = statement.executeUpdate(s);
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
                //wtf??if(obj != null)
                    //wtf??obj.close();
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
