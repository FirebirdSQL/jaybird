// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getDouble.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getDouble extends TestModule
{

    public getDouble()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        Object obj2 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getDouble");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(8) == null)
            {
                result("Does not support DOUBLE SQL type");
                passed();
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_DOUBLE(?)}");
                callablestatement.registerOutParameter(1, 8);
                callablestatement.execute();
                test(callablestatement, "getDouble()");
                double d = callablestatement.getDouble(1);
                result(d);
                assert(d > 6.2831000000000001D && d < 6.2831999999999999D, "Value must be close to 6.28318");
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
