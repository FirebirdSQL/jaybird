// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setString.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setString extends TestModule
{

    public setString()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setString");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE CHARCOL=?");
            test(preparedstatement, "setString()");
            preparedstatement.setString(1, "cde       ");
            resultset = preparedstatement.executeQuery();
            if(next(resultset))
            {
                int i = resultset.getInt(1);
                result(i);
                assert(i == 2, "Row 2 is found");
            }
            preparedstatement.close();
            if(getSupportedSQLType(12) == null)
            {
                result("Does not support VARCHAR SQL type");
            } else
            {
                preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE VCHARCOL=?");
                test(preparedstatement, "setString()");
                preparedstatement.setString(1, "cde");
                resultset = preparedstatement.executeQuery();
                if(next(resultset))
                {
                    int j = resultset.getInt(1);
                    result(j);
                    assert(j == 2, "Row 2 is found");
                }
                preparedstatement.close();
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
