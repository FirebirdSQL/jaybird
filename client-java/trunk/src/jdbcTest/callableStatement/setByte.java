// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   setByte.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class setByte extends TestModule
{

    public setByte()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setShort");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(-6) == null)
            {
                result("Does not support TINYINT SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_SET_BYTE(?,?)}");
                test(callablestatement, "setByte(1,1)");
                callablestatement.setByte(1, (byte)1);
                callablestatement.registerOutParameter(2, 4);
                callablestatement.execute();
                int i = callablestatement.getInt(2);
                result(i);
                assert(i == 1, "Row 1 must be found");
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
                    //wtf??obj.close();
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
