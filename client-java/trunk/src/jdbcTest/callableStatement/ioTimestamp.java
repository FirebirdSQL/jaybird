// Decompiled by Jad v1.5.8a. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ioTimestamp.java

package jdbcTest.callableStatement;

import java.sql.*;
import jdbcTest.harness.TestModule;

public class ioTimestamp extends TestModule
{

    public ioTimestamp()
    {
    }

    public void run()
    {
        Connection connection = null;
        CallableStatement callablestatement = null;
        try
        {
            jdbcTest.harness.TestCase testcase = createTestCase("Test CallableStatement.setTimestamp");
            execTestCase(testcase);
            connection = DriverManager.getConnection(getUrl(), getSignon(), getPassword());
            DatabaseMetaData databasemetadata = connection.getMetaData();
            if(!databasemetadata.supportsStoredProcedures())
                result("Does not support Stored Procedures");
            else
            if(getSupportedSQLType(93) == null)
            {
                result("Does not support TIMESTAMP SQL type");
            } else
            {
                callablestatement = connection.prepareCall("{call JDBC_IO_TIMESTAMP(?)}");
                test(callablestatement, "setTimestamp()");
                callablestatement.setTimestamp(1, Timestamp.valueOf("1982-03-03 02:02:02"));
                callablestatement.registerOutParameter(1, 93);
                callablestatement.executeUpdate();
                Timestamp timestamp = callablestatement.getTimestamp(1);
                result(timestamp);
                assert(timestamp.equals(Timestamp.valueOf("1982-05-05 04:04:04")), "Timestamp output value must be 1982-05-05 04:04:04");
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
            }
            catch(SQLException _ex) { }
            stop();
        }
    }
}
