// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setBigDecimal.java

package jdbcTest.callableStatement;

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
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setBigDecimal");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            PreparedStatement preparedstatement = connection.prepareStatement("SELECT NUMERICCOL FROM JDBCTEST WHERE INTEGERCOL=5");
            ResultSet resultset = preparedstatement.executeQuery();
            resultset.next();
            java.math.BigDecimal bigdecimal = resultset.getBigDecimal(1, 5);
            result(bigdecimal);
            preparedstatement.close();
            CallableStatement callablestatement = connection.prepareCall("{call JDBC_SET_BIGDECIMAL(?,?)}");
            test(callablestatement, "setBigDecimal()");
            callablestatement.setBigDecimal(1, bigdecimal);
            callablestatement.registerOutParameter(2, 4);
            callablestatement.execute();
            int i = callablestatement.getInt(2);
            result(i);
            assert(i == 5, "Row 5 must be found");
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
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
