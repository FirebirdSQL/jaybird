// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioString.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioString extends TestModule
{

    public ioString()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setString");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_IO_STRING(?)}");
                test(callablestatement, "setString(1, \"cde   \")");
                callablestatement.setString(1, "cde   ");
                callablestatement.registerOutParameter(1, 1);
                callablestatement.executeUpdate();
                String s = callablestatement.getString(1);
                result(s);
                assert(s.equals("fgh   "), "The output value must be 'fgh   '");
                callablestatement.close();
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    callablestatement = connection.prepareCall("{call JDBC_IO_VSTRING(?)}");
                    test(callablestatement, "setString(1, \"cde\")");
                    callablestatement.setString(1, "cde");
                    callablestatement.registerOutParameter(1, 1);
                    callablestatement.executeUpdate();
                    String s1 = callablestatement.getString(1);
                    result(s1);
                    assert(s1.equals("fgh"), "The output value must be 'fgh'");
                    callablestatement.close();
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
                if(callablestatement != null)
                    callablestatement.close();
                if(connection != null)
                    connection.close();
                //wtf??if(obj != null)
                   //wtf?? obj.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
