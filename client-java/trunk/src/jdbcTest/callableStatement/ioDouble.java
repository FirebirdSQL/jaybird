// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioDouble.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioDouble extends TestModule
{

    public ioDouble()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setDouble");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(8) == null)
            {
                result("Does not support DOUBLE SQL type");
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_DOUBLE(?)}");
                test(callablestatement, "setDouble(1,9.4247123456789)");
                callablestatement.setDouble(1, 9.4247123456789001D);
                callablestatement.registerOutParameter(1, 8);
                callablestatement.executeUpdate();
                double d = callablestatement.getDouble(1);
                result(d);
                assert(d > 6.4247123456787998D && d < 6.4247123456789996D, "The output value must be ~6.4247123456789");
            }
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
