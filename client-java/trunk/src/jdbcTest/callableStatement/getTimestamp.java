// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getTimestamp.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getTimestamp extends TestModule
{

    public getTimestamp()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getTimestamp");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(93) == null)
            {
                result("Does not support TIMESTAMP SQL type");
                passed();
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_TIMESTAMP(?)}");
                callablestatement.registerOutParameter(1, 93);
                callablestatement.execute();
                test(callablestatement, "getTimestamp()");
                Timestamp timestamp = callablestatement.getTimestamp(1);
                Timestamp timestamp1 = Timestamp.valueOf("1981-02-02 01:01:01");
                if(timestamp != null)
                {
                    result(timestamp.toString());
                    if(timestamp.equals(timestamp1))
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
