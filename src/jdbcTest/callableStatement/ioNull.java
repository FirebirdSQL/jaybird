// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioNull.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioNull extends TestModule
{

    public ioNull()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setNull");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_NULL(?)}");
            test(callablestatement, "setNull(1,java.sql.Types.CHAR)");
            callablestatement.setNull(1, 1);
            callablestatement.registerOutParameter(1, 1);
            callablestatement.executeUpdate();
            String s = callablestatement.getString(1);
            result(s);
            assert(s == null && callablestatement.wasNull(), "The output value must be null");
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
