// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getTime.java

package jdbcTest.callableStatement;

import java.sql.*;
import java.util.Date;
import jdbcTest.harness.TestModule;

public class getTime extends TestModule
{

    public getTime()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getTime");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(92) == null)
            {
                result("Does not support TIME SQL type");
                passed();
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_TIME(?)}");
                callablestatement.registerOutParameter(1, 92);
                callablestatement.execute();
                test(callablestatement, "getTime()");
                Time time = callablestatement.getTime(1);
                Time time1 = Time.valueOf("01:01:01");
                if(time != null)
                {
                    result(time);
                    if(time.equals(time1))
                        passed();
                    else
                        failed();
                } else
                {
                    failed();
                }
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
