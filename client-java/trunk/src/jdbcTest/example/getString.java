// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getString.java

package jdbcTest.example;

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
        Object obj = null;
        Object obj1 = null;
        Object obj2 = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test example.getString");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
            } else
            {
                CallableStatement callablestatement = connection.prepareCall("{call JDBC_GET_STRING(?)}");
                callablestatement.registerOutParameter(1, 1);
                callablestatement.executeUpdate();
                test(callablestatement, "getString()");
                String s = callablestatement.getString(1);
                result(s.length());
                result(s);
                assert(s.equals("bc        "), "The CHAR value must be 'bc        '");
                if(getSupportedSQLType(12) == null)
                {
                    result("Does not support VARCHAR SQL type");
                } else
                {
                    CallableStatement callablestatement1 = connection.prepareCall("{call JDBC_GET_VSTRING(?)}");
                    callablestatement1.registerOutParameter(1, 12);
                    callablestatement1.executeUpdate();
                    test(callablestatement1, "getString()");
                    String s1 = callablestatement1.getString(1);
                    result(s1.length());
                    result(s1);
                    assert(s1.equals("bc"), "The VARCHAR value must be 'bc'");
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
