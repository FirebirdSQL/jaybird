// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getLong.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getLong extends TestModule
{

    public getLong()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getLong");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(-5) == null)
            {
                result("Does not support BIGINT SQL type");
                passed();
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_LONG(?)}");
                callablestatement.registerOutParameter(1, -5);
                callablestatement.execute();
                test(callablestatement, "getLong()");
                long l = callablestatement.getLong(1);
                result(l);
                if(l == 2L)
                    passed();
                else
                    failed();
            }
        }
        catch(SQLException sqlexception)
        {
            exception(sqlexception);
        }
        catch(Exception exception1)
        {
            exception1.printStackTrace();
            failed();
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
