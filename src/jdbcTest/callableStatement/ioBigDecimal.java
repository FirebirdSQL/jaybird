// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioBigDecimal.java

package jdbcTest.callableStatement;

import java.math.BigDecimal;
import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioBigDecimal extends TestModule
{

    public ioBigDecimal()
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
            BigDecimal bigdecimal = resultset.getBigDecimal(1, 5);
            result(bigdecimal);
            preparedstatement.close();
            CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_BIGDECIMAL(?)}");
            test(callablestatement, "setBigDecimal()");
            callablestatement.setBigDecimal(1, bigdecimal);
            callablestatement.registerOutParameter(1, 2);
            callablestatement.executeUpdate();
            BigDecimal bigdecimal1 = callablestatement.getBigDecimal(1, 5);
            result(bigdecimal1);
            assert(bigdecimal1.equals(new BigDecimal("12340.12340")), "BigDecimal value must be 12340.1234");
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
