// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioLong.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioLong extends TestModule
{

    public ioLong()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setLong");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(-5) == null)
            {
                result("Does not support BIGINT SQL type");
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_LONG(?)}");
                test(callablestatement, "setLong(1,10000000000L)");
                callablestatement.setLong(1, 0x2540be400L);
                callablestatement.registerOutParameter(1, -5);
                callablestatement.executeUpdate();
                long l = callablestatement.getLong(1);
                result(l);
                assert(l == 0x4a817c800L, "The output value must be 20000000000");
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
