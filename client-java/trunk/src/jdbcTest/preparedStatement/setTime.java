// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setTime.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setTime extends TestModule
{

    public setTime()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setTime");
            execTestCase(testcase);
            if(getSupportedSQLType(92) == null)
            {
                result("Does not support TIME SQL type");
            } else
            {
                connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE TIMECOL=?");
                Time time = Time.valueOf("01:01:01");
                test(preparedstatement, "setTime()");
                preparedstatement.setTime(1, time);
                resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int i = resultset.getInt(1);
                    result(i);
                    assert(i == 2, "The correct row is found");
                }
            }
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
                if(resultset != null)
                    resultset.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
