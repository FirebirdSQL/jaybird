// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setShort.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setShort extends TestModule
{

    public setShort()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setShort");
            execTestCase(testcase);
            if(getSupportedSQLType(5) == null)
            {
                result("Does not support SMALLINT SQL type");
            } else
            {
                connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE SMALLCOL=?");
                test(preparedstatement, "setShort()");
                preparedstatement.setShort(1, (short)-6);
                ResultSet resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int i = resultset.getInt(1);
                    result(i);
                    assert(i == 3, "The correct row is found");
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
