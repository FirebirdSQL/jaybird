// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getBytes.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getBytes extends TestModule
{

    public getBytes()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getBytes");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                if(getSupportedSQLType(-2) == null)
                {
                    result("Does not support BINARY SQL type");
                } else
                {
                    CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_BYTES(?)}");
                    callablestatement.registerOutParameter(1, -2);
                    callablestatement.execute();
                    test(callablestatement, "getBytes()");
                    byte abyte0[] = callablestatement.getBytes(1);
                    result(abyte0.length);
                    verify(abyte0.length == 10, "The BINARY length must match (Some databases do not provide the actual length of BINARY out params; in this case a failure is expected)");
                    result(Integer.toString(abyte0[0], 16));
                    result(Integer.toString(abyte0[1], 16));
                    verify(abyte0[0] == 98 && abyte0[1] == 99, "The BINARY value must match");
                }
                if(getSupportedSQLType(-3) == null)
                {
                    result("Does not support VARBINARY SQL type");
                } else
                {
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_GET_VBYTES(?)}");
                    callablestatement1.registerOutParameter(1, -3);
                    callablestatement1.execute();
                    test(callablestatement1, "getBytes()");
                    byte abyte1[] = callablestatement1.getBytes(1);
                    result(abyte1.length);
                    verify(abyte1.length == 2, "The VARBINARY length must match");
                    result(Integer.toString(abyte1[0], 16));
                    result(Integer.toString(abyte1[1], 16));
                    verify(abyte1[0] == 98 && abyte1[1] == 99, "The VARBINARY value must match");
                }
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
