// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBigDecimal.java

package jdbcTest.preparedStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setBigDecimal extends TestModule
{

    public setBigDecimal()
    {
    }

    public void run()
    {
        Connection connection = null;
        PreparedStatement preparedstatement = null;
        ResultSet resultset = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test PreparedStatement.setBigDecimal");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            preparedstatement = connection.prepareStatement("SELECT NUMERICCOL FROM JDBCTEST WHERE INTEGERCOL=5");
            resultset = preparedstatement.executeQuery();
            resultset.next();
            java.math.BigDecimal bigdecimal = resultset.getBigDecimal(1, 5);
            result(bigdecimal);
            preparedstatement = connection.prepareStatement("SELECT INTEGERCOL FROM JDBCTEST WHERE NUMERICCOL = ?");
            test(preparedstatement, "setBigDecimal()");
            preparedstatement.setBigDecimal(1, bigdecimal);
            resultset = preparedstatement.executeQuery();
            if(next(resultset))
            {
                int i = resultset.getInt(1);
                result(i);
                assert(i == 5, "The correct row is found");
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
