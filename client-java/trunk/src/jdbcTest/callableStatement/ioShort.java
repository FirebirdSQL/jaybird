// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioShort.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioShort extends TestModule
{

    public ioShort()
    {
    }

    public void run()
    {
        Connection connection = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setShort");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(5) == null)
            {
                result("Does not support SMALLINT SQL type");
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_IO_SHORT(?)}");
                test(callablestatement, "setShort()");
                callablestatement.setShort(1, (short)-2);
                callablestatement.registerOutParameter(1, 5);
                callablestatement.executeUpdate();
                short word0 = callablestatement.getShort(1);
                result(word0);
                assert(word0 == -4, "The output value must be -4");
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
