// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setTimestamp.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setTimestamp extends TestModule
{

    public setTimestamp()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setTimestamp");
            execTestCase(testcase);
            if(getSupportedSQLType(93) == null)
            {
                result("Does not support TIMESTAMP SQL type");
            } else
            {
                connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
                preparedstatement = connection.prepareStatement("SELECT TSCOL FROM JDBCTEST WHERE INTEGERCOL=5");
                ResultSet resultset = preparedstatement.executeQuery();
                resultset.next();
                java.sql.Timestamp timestamp = resultset.getTimestamp(1);
                result(timestamp);
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE TSCOL=?");
                test(preparedstatement, "setTimestamp()");
                preparedstatement.setTimestamp(1, timestamp);
                resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int i = resultset.getInt(1);
                    result(i);
                    assert(i == 5, "The correct row is found");
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
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
