// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getString.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getString extends TestModule
{

    public getString()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        Object obj1 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getString");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_STRING(?)}");
                callablestatement.registerOutParameter(1, 1);
                callablestatement.executeUpdate();
                test(callablestatement, "getString()");
                String s = callablestatement.getString(1);
                result(s.length());
                result(s);
                verify(s.equals("bc        "), "The CHAR value must be 'bc        ' (Some databases do not provide the actual length of CHAR out params; in this case a failure is expected)");
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    callablestatement = connection.prepareCall("{call JDBC_GET_VSTRING(?)}");
                    callablestatement.registerOutParameter(1, 12);
                    callablestatement.executeUpdate();
                    test(callablestatement, "getString()");
                    String s1 = callablestatement.getString(1);
                    result(s1.length());
                    result(s1);
                    verify(s1.equals("bc"), "The VARCHAR value must be 'bc'");
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
                //wtf??if(obj != null)
                    //wtf??obj.close();
                if(callablestatement != null)
                    callablestatement.close();
                if(connection != null)
                    connection.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
