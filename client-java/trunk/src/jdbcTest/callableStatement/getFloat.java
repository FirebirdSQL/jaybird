// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getFloat.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getFloat extends TestModule
{

    public getFloat()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getFloat");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(7) == null)
            {
                result("Does not support REAL SQL type");
                passed();
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_FLOAT(?)}");
                callablestatement.registerOutParameter(1, 7);
                callablestatement.execute();
                test(callablestatement, "getFloat()");
                float f = callablestatement.getFloat(1);
                result(f);
                assert((double)f > 1.3903000000000001D && (double)f < 1.3904000000000001D, "Value must be close to 1.39036");
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
