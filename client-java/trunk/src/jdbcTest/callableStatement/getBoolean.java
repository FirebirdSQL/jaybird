// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getBoolean.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getBoolean extends TestModule
{

    public getBoolean()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getBoolean");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(-7) == null)
            {
                result("Does not support BIT SQL type");
                passed();
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_BOOLEAN(?)}");
                callablestatement.registerOutParameter(1, -7);
                callablestatement.execute();
                test(callablestatement, "getBoolean()");
                boolean flag = callablestatement.getBoolean(1);
                result(flag);
                if(flag)
                    passed();
                else
                    failed();
            }
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
