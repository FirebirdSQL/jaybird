// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getBigDecimal.java

package jdbcTest.callableStatement;

import java.math.BigDecimal;
import java.sql.*;
import jdbcTest.harness.TestModule;

public class getBigDecimal extends TestModule
{

    public getBigDecimal()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getBigDecimal");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(2) == null && getSupportedSQLType(3) == null)
            {
                result("Does not support NUMERIC or DECIMAL SQL type");
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_BIGDECIMAL(?)}");
                callablestatement.registerOutParameter(1, 2, 5);
                callablestatement.execute();
                test(callablestatement, "getBigDecimal()");
                BigDecimal bigdecimal = callablestatement.getBigDecimal(1, 5);
                result(bigdecimal);
                assert(bigdecimal.equals(new BigDecimal("234567.90100")), "BigDecimal must have value 234567.90100");
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
