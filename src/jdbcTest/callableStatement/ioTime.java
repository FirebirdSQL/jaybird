// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioTime.java

package jdbcTest.callableStatement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import jdbcTest.harness.TestModule;

public class ioTime extends TestModule
{

    public ioTime()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        Object obj = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setTime");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(92) == null)
            {
                result("Does not support TIME SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_SET_TIME(?)}");
                Time time = Time.valueOf("02:02:02");
                test(callablestatement, "setTime()");
                callablestatement.setTime(1, time);
                callablestatement.registerOutParameter(1, 92);
                callablestatement.executeUpdate();
                Time time1 = callablestatement.getTime(1);
                result(time1);
                assert(time1.equals(Date.valueOf("04:04:04")), "Time output value must be 04:04:04");
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
