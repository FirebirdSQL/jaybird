// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioByte.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioByte extends TestModule
{

    public ioByte()
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
                callablestatement = connection.prepareCall("{call JDBC_IO_BYTE(?)}");
                test(callablestatement, "setByte(1,1)");
                callablestatement.setByte(1, (byte)1);
                callablestatement.registerOutParameter(1, -6);
                callablestatement.executeUpdate();
                int i = callablestatement.getInt(1);
                result(i);
                assert(i == 2, "A byte value of 2 is expected.");
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
