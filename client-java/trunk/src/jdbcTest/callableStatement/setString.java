// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setString.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setString extends TestModule
{

    public setString()
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
            callablestatement = connection.prepareCall("{call JDBC_SET_STRING(?,?)}");
            test(callablestatement, "setString()");
            callablestatement.setString(1, "cde       ");
            callablestatement.registerOutParameter(2, 4);
            callablestatement.execute();
            int i = callablestatement.getInt(2);
            result(i);
            assert(i == 2, "The correct row is found");
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(12) == null)
            {
                result("Does not support VARCHAR SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_SET_VSTRING(?,?)}");
                test(callablestatement, "setString()");
                callablestatement.setString(1, "cde");
                callablestatement.registerOutParameter(2, 4);
                callablestatement.execute();
                int j = callablestatement.getInt(2);
                result(j);
                assert(j == 2, "The correct row is found");
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
