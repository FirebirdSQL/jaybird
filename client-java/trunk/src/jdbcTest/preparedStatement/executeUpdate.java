// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   executeUpdate.java

package jdbcTest.preparedStatement;

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
        PreparedStatement preparedstatement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test Prepared Statement.executeUpdate");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            String s = "INSERT INTO GTEST (CHARCOL) SELECT CHARCOL FROM JDBCTEST";
            preparedstatement = connection.prepareStatement(s);
            int i = preparedstatement.executeUpdate();
            test(preparedstatement, "ExecuteUpdate()");
            result(i);
            assert(i == 6, "Six records must have been inserted");
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
