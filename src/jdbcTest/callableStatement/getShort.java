// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   getShort.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class getShort extends TestModule
{

    public getShort()
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
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.getShort");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
            {
                result("Does not support Stored Procedures");
                passed();
            } else
            if(getSupportedSQLType(5) == null)
            {
                result("Does not support SMALLINT SQL type");
                passed();
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_GET_SHORT(?)}");
                callablestatement.registerOutParameter(1, 5);
                callablestatement.execute();
                test(callablestatement, "getShort()");
                short word0 = callablestatement.getShort(1);
                result(word0);
                assert(word0 == -2, "The short must be -2");
                passed();
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
                   //wtf?? obj.close();
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
